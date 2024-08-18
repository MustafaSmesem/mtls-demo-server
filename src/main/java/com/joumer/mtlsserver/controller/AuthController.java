package com.joumer.mtlsserver.controller;

import com.joumer.mtlsserver.model.CertificateSignRequest;
import com.joumer.mtlsserver.service.CertificateManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {

    private final CertificateManagementService certificateManagementService;

    public AuthController(CertificateManagementService certificateManagementService) {
        this.certificateManagementService = certificateManagementService;
    }

    @PostMapping("certificate-sign")
    public ResponseEntity<String> certificateSign(@RequestBody CertificateSignRequest request) throws Exception {
        var crt = certificateManagementService.signCertificate(request);
        return ResponseEntity.ok(crt);
    }
}
