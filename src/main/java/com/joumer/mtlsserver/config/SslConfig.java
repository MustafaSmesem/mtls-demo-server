package com.joumer.mtlsserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

//@Configuration
public class SslConfig {

    @Value("${custom.ssl.default.keystore-path}")
    private String keystorePathDefault;
    @Value("${custom.ssl.default.keystore-password}")
    private String keystorePasswordDefault;
    @Value("${custom.ssl.default.truststore-path}")
    private String truststorePathDefault;
    @Value("${custom.ssl.default.truststore-password}")
    private String truststorePasswordDefault;

    @Value("${custom.ssl.client.keystore-path}")
    private String keystorePathClient;
    @Value("${custom.ssl.client.keystore-password}")
    private String keystorePasswordClient;
    @Value("${custom.ssl.client.truststore-path}")
    private String truststorePathClient;
    @Value("${custom.ssl.client.truststore-password}")
    private String truststorePasswordClient;

    @Bean
    public SSLContext defaultSSLContext() throws Exception {
        var keyManagerFactory = getKeyManagerFactory(keystorePathDefault, keystorePasswordDefault);
        var trustManagerFactory = getTrustManagerFactory(truststorePathDefault, truststorePasswordDefault);

        // Create the SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    @Bean
    public SSLContext clientSSLContext() throws Exception {
        var keyManagerFactory = getKeyManagerFactory(keystorePathClient, keystorePasswordClient);
        var trustManagerFactory = getTrustManagerFactory(truststorePathClient, truststorePasswordClient);

        // Create the SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return sslContext;
    }

    private KeyManagerFactory getKeyManagerFactory(String keystorePath, String keystorePassword) throws Exception {
        var keyStore = KeyStore.getInstance("JKS");
        try (var fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new Exception("Error loading keystore: " + e.getMessage(), e);
        }

        // Initialize the key manager factory
        var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

        return keyManagerFactory;
    }

    private TrustManagerFactory getTrustManagerFactory(String truststorePath, String truststorePassword) throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(truststorePath)) {
            trustStore.load(fis, truststorePassword.toCharArray());
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new Exception("Error loading truststore: " + e.getMessage(), e);
        }

        // Initialize the trust manager factory
        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        return trustManagerFactory;
    }
}
