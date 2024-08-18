package com.joumer.mtlsserver.service;

import com.joumer.mtlsserver.model.CertificateSignRequest;
import com.joumer.mtlsserver.utils.CertificateSigner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class CertificateManagementService {

    @Value("${custom.ssl.client.truststore-path}")
    private String trustStoreName;

    @Value("${custom.ssl.client.truststore-password}")
    private String trustStorePassword;

    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    @Value("${custom.ssl.client.keystore-path}")
    private String keystorePath;

    @Value("${custom.ssl.client.keystore-password}")
    private String keystorePassword;

    @Autowired
    private ResourceLoader resourceLoader;

    public String signCertificate(CertificateSignRequest request) throws Exception {
        var ksPath = resourceLoader.getResource(keystorePath).getFile().getPath();
        var tsPath = resourceLoader.getResource(trustStoreName).getFile().getPath();
        var csrSigner = new CertificateSigner(ksPath, keystorePassword, keyAlias, tsPath, trustStorePassword);
        var signedCrt = csrSigner.signCSR(request.pemCsr());
        csrSigner.addCertificateToKeystore(signedCrt, request.deviceId());
        return CertificateSigner.encodeCertificate(signedCrt);
    }
}