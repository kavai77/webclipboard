package com.himadri.webclipboard;

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

    public byte[] encrypt(AESKey aesKey, String text) {
        IvParameterSpec iv = new IvParameterSpec(aesKey.getIv());
        SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getKey(), AES);

        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            return cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String decrypt(AESKey aesKey, byte[] encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(aesKey.getIv());
            SecretKeySpec skeySpec = new SecretKeySpec(aesKey.getKey(), AES);

            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(encrypted);

            return new String(original, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
