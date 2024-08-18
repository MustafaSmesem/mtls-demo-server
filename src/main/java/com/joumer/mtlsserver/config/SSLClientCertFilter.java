package com.joumer.mtlsserver.config;

import java.io.IOException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;

//@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SSLClientCertFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        var httpRequest = (HttpServletRequest) request;
        var httpResponse = (HttpServletResponse) response;

        var requestUri = httpRequest.getRequestURI();
//        var certs = (X509Certificate[]) httpRequest.getAttribute("jakarta.servlet.request.X509Certificate");
//
//        if (certs == null || certs.length == 0) {
//            handleMissingClientCertificate(httpResponse);
//            return;
//        }

        try {
//            var crt = certs[0];
//            crt.checkValidity();

            if (requestUri.startsWith("/auth") && httpRequest.getServerPort() != 9090) {
                return;
            }
            chain.doFilter(request, response);
        } catch (Exception e) {
            handleInvalidClientCertificate(httpResponse, e);
        }
    }

    private void handleInvalidClientCertificate(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid client certificate: " + e.getMessage());
    }

    private void handleMissingClientCertificate(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Client certificate is required");
    }

    private void validateClientCertificate(X509Certificate cert) throws CertificateNotYetValidException, CertificateExpiredException {

    }

    private void validateDefaultCertificate(X509Certificate cert) {

    }
}