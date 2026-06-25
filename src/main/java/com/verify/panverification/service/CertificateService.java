package com.verify.panverification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;

@Slf4j
@Service
public class CertificateService {

    @Value("${certificate.path}")
    private String certificatePath;

    @Value("${certificate.password}")
    private String password;

    public PrivateKey loadPrivateKey() throws Exception {

        log.info("Loading certificate from {}", certificatePath);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        try (InputStream is =
                     new FileInputStream(certificatePath)) {

            keyStore.load(
                    is,
                    password.toCharArray()
            );
        }

        String alias =
                keyStore.aliases().nextElement();

        log.info("Certificate loaded successfully");
        return (PrivateKey)
                keyStore.getKey(
                        alias,
                        password.toCharArray()
                );
    }
}
