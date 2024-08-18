package com.joumer.mtlsserver.utils;

import org.springframework.core.io.ResourceLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class KeyStoreUtil {

    public static PrivateKey loadCAPrivateKey(String keystorePath, String keystorePassword, String alias) throws Exception {
        var keystore = getKeyStoreInstance(keystorePath, keystorePassword);
        return (PrivateKey) keystore.getKey(alias, keystorePassword.toCharArray());
    }

    public static X509Certificate loadCACertificate(String keystorePath, String keystorePassword, String alias) throws Exception {
        var keystore = getKeyStoreInstance(keystorePath, keystorePassword);
        return (X509Certificate) keystore.getCertificate(alias);
    }

    public static KeyStore getKeyStoreInstance(String keystorePath, String keystorePassword) throws KeyStoreException {
        var keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        } catch (NoSuchAlgorithmException | IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
        return keyStore;
    }
}