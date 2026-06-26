package com.verify.panverification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignatureService {

    @Value("${opv.pfx-file}")
    private String pfxFile;

    @Value("${opv.pfx-password}")
    private String pfxPassword;

    private KeyStore loadKeyStore() throws Exception {

        log.debug("Loading Keystore from file: {}",pfxFile);
        KeyStore keyStore =
        KeyStore.getInstance("JKS");

        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                                pfxFile
                        );

        keyStore.load(
                inputStream,
                pfxPassword.toCharArray()
        );
        log.debug("KeyStore loaded successfully");
        return keyStore;
    }

    private String getAlias(
            KeyStore keyStore)
            throws Exception {

        Enumeration<String> aliases =
                keyStore.aliases();

        while (aliases.hasMoreElements()) {

            String alias =
                    aliases.nextElement();

            if (keyStore.isKeyEntry(alias)) {
                log.debug("Found key alies: {}",alias);
                return alias;
            }
        }


        throw new RuntimeException(
                "Alias not found");
    }

    public String testCertificate() {

        log.info("Testing certificate load");
        try {

            KeyStore ks = loadKeyStore();

            String alias = getAlias(ks);

           log.info("Certificate loaded successfully. Alies={}",alias);

            return "Certificate Loaded : " + alias;

        } catch (Exception e) {

            log.error("Certificate test failed: {}",e.getMessage(),e);

            return "ERROR : " + e.getMessage();
        }
    }

    private PrivateKey getPrivateKey() throws Exception {

        KeyStore keyStore = loadKeyStore();

        String alias = getAlias(keyStore);

        return (PrivateKey)
                keyStore.getKey(
                        alias,
                        pfxPassword.toCharArray()
                );
    }

    private X509Certificate getCertificate()
            throws Exception {

        log.debug("Fetching certificate from KeyStore");
        KeyStore keyStore = loadKeyStore();

        String alias = getAlias(keyStore);

        return (X509Certificate)
                keyStore.getCertificate(alias);
    }

    public String generateSignature(String json) throws Exception {

        log.info("Generating digital signature");
        PrivateKey privateKey = getPrivateKey();

        Signature sign = Signature.getInstance("SHA256withRSA");

        sign.initSign(privateKey);

        sign.update(json.getBytes());

        byte[] signedBytes = sign.sign();

        String encoded = Base64.getEncoder().encodeToString(signedBytes);

        log.info("Signature generated successfully");

        return Base64.getEncoder()
                .encodeToString(signedBytes);

    }

    public String testSignature() {

        log.info("Testing signature generation");

        try {

            String data =
                    "[{\"pan\":\"ABCDE1234F\",\"name\":\"TEST USER\",\"fathername\":\"\",\"dob\":\"01/01/1990\"}]";

            String signature =
                    generateSignature(data);

            log.info("Test signature generated successfully");

            log.debug("Test Signature: {}",signature);
            return signature;

        } catch (Exception e) {

            log.error("Test signature failed: {}",e.getMessage(),e);

            return e.getMessage();
        }
    }
}
