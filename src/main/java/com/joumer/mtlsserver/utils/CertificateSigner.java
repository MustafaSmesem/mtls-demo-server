package com.joumer.mtlsserver.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;

public class CertificateSigner {

    private final String keystorePath;
    private final String keystorePassword;
    private final String trustStorePath;
    private final String trustStorePassword;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CertificateSigner(String keystorePath, String keystorePassword, String rootKeyAlias, String trustStorePath, String trustStorePassword) {
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
    }

    public X509Certificate signCSR(String pemEncodedCsr) throws Exception {
        // Load CA private key and certificate
        String rootKeyAlias = "api-ca.joumer.com";
        PrivateKey caPrivateKey = KeyStoreUtil.loadCAPrivateKey(keystorePath, keystorePassword, rootKeyAlias);
        X509Certificate caCertificate = KeyStoreUtil.loadCACertificate(keystorePath, keystorePassword, rootKeyAlias);

        // Parse the CSR
        PEMParser pemParser = new PEMParser(new StringReader(pemEncodedCsr));
        PKCS10CertificationRequest csr = (PKCS10CertificationRequest) pemParser.readObject();
        pemParser.close();

        JcaPKCS10CertificationRequest jcaCSR = new JcaPKCS10CertificationRequest(csr);

        // Set validity dates
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + (365 * 24 * 60 * 60 * 1000L)); // 1 year validity

        // Build the certificate
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                caCertificate,
                BigInteger.valueOf(System.currentTimeMillis()), // Serial Number
                startDate,
                endDate,
                jcaCSR.getSubject(),
                jcaCSR.getPublicKey()
        );

        // Sign the certificate
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(caPrivateKey);
        X509CertificateHolder certHolder = certBuilder.build(signer);

        // Convert to X509Certificate
        return new JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate(certHolder);
    }

    public static String encodeCertificate(X509Certificate crt) throws Exception {
        var sw = new StringWriter();
        var pemWriter = new JcaPEMWriter(sw);
        pemWriter.writeObject(crt);
        pemWriter.close();

        return sw.toString();
    }

    public void addCertificateToKeystore(X509Certificate certificate, String deviceId) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(trustStorePath)) {
            keyStore.load(fis, trustStorePassword.toCharArray());
        }

        keyStore.setCertificateEntry(deviceId, certificate);

        try (FileOutputStream fos = new FileOutputStream(trustStorePath)) {
            keyStore.store(fos, trustStorePassword.toCharArray());
        }
    }
}