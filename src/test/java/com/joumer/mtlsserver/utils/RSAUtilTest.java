package com.joumer.mtlsserver.utils;

import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.*;

class RSAUtilTest {

    @Test
    void encryptAndDecryptStringData_success() throws Exception {
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        var keyPair = keyPairGenerator.generateKeyPair();

        System.out.println("Public Key: " + keyPair.getPublic());
        System.out.println("Private Key: " + keyPair.getPrivate());

        String testData = "Simple data for test";
        System.out.println("Original data: " + testData);
        var encrypted = RSAUtil.encrypt(testData, keyPair.getPublic());
        System.out.println("Encrypted data: " + encrypted);

        var decrypted = RSAUtil.decrypt(encrypted, keyPair.getPrivate());
        System.out.println("Decrypted data: " + decrypted);

        assertEquals(testData, decrypted);
    }

}