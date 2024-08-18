package com.joumer.mtlsserver.config;

import com.joumer.mtlsserver.utils.KeyStoreUtil;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.security.KeyStoreException;


@Configuration
public class TomcatConfig {

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();

        // Second SSL Connector
        tomcat.addAdditionalTomcatConnectors(createSslConnector(
                9091, "myserver_keystore.jks", "12345678"));

        return tomcat;
    }

    private Connector createSslConnector(int port, String keystore, String keystorePassword) {
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setPort(port);
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setProperty("sslProtocol", "mTLS");


        var protocol = (Http11NioProtocol)connector.getProtocolHandler();
        protocol.setSSLEnabled(true);
        protocol.setSecure(true);
        protocol.setProperty("clientAuth", "need");

        try {
            var sslHostConfig = getSslHostConfig(keystore, keystorePassword);
            connector.addSslHostConfig(sslHostConfig);
        }
        catch (IOException | KeyStoreException ex) {
            //... handle exception here
        }
        return connector;
    }

    private SSLHostConfig getSslHostConfig(String keystore, String keystorePassword) throws IOException, KeyStoreException {
        var keystoreResource = new ClassPathResource(keystore);
        var keystoreUrl = keystoreResource.getURL();
        String keystoreLocation = keystoreUrl.toString();

        var sslHostConfig = new SSLHostConfig();
        var sslHostConfigCertificate = new SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.UNDEFINED);

        sslHostConfigCertificate.setCertificateKeystoreFile(keystoreLocation);
        sslHostConfigCertificate.setCertificateKeystorePassword(keystorePassword);
        sslHostConfigCertificate.setCertificateKeystoreType("PKCS12");
        sslHostConfigCertificate.setCertificateKeyAlias("api-server.joumer.com");
        sslHostConfig.addCertificate(sslHostConfigCertificate);
        sslHostConfig.setCertificateVerification("required");

        var tPath = resourceLoader.getResource("classpath:myclient_truststore.jks").getURI().getPath();
        var trustStore = KeyStoreUtil.getKeyStoreInstance(tPath, keystorePassword);

        sslHostConfig.setTrustStore(trustStore);

        return sslHostConfig;
    }
}