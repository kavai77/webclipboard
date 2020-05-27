package com.himadri.webclipboard;

import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Component
public class CipherEngine {
    private static final String AES = "AES";
    private static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5PADDING";

    public String encrypt(AESKey aesKey, String text) {
        IvParameterSpec iv = new IvParameterSpec(aesKey.getIv());
        SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getKey(), AES);

        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String decrypt(AESKey aesKey, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(aesKey.getIv());
            SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getKey(), AES);

            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
