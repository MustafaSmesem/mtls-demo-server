package com.joumer.mtlsserver.utils;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAUtil {
    private static final String CIPHER_RSA = "RSA";

    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        byte[] encryptedData = encrypt(data.getBytes(), publicKey);
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    public static String decrypt(String data, PrivateKey privateKey) throws Exception {
        var encryptedBytes = Base64.getDecoder().decode(data);
        byte[] decryptedData = decrypt(encryptedBytes, privateKey);
        return new String(decryptedData);
    }

    private static byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
        var cipher = Cipher.getInstance(CIPHER_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        var cipher = Cipher.getInstance(CIPHER_RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }
}
