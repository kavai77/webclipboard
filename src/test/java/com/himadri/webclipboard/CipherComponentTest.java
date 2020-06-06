package com.himadri.webclipboard;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CipherComponentTest {
    @Test
    public void encryptAndDecrypt() {
        AESKey aesKey = new AESKey(
            RandomUtils.nextBytes(32),
            RandomUtils.nextBytes(16)
        );
        CipherEngine cipherComponent = new CipherEngine();
        for (int i = 0; i < 1000; i++) {
            String randomStr = RandomStringUtils.random(i);
            byte[] encrypt = cipherComponent.encrypt(aesKey, randomStr);
            String decrypt = cipherComponent.decrypt(aesKey, encrypt);
            assertEquals(randomStr, decrypt, "Iteration: " + i +" original string: " + randomStr);
        }
    }


}