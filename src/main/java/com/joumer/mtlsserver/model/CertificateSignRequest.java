package com.joumer.mtlsserver.model;

public record CertificateSignRequest(String pemCsr, String deviceId) {
}
