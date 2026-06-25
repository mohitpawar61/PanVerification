package com.verify.panverification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Enumeration;

@Service
@RequiredArgsConstructor
public class SignatureService {

    @Value("${opv.pfx-file}")
    private String pfxFile;

    @Value("${opv.pfx-password}")
    private String pfxPassword;

    private KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore =
        KeyStore.getInstance("PKCS12");

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
                return alias;
            }
        }

        throw new RuntimeException(
                "Alias not found");
    }

    public String testCertificate() {

        System.out.println("Inside testCertificate Service");
        try {

            KeyStore ks = loadKeyStore();

            String alias = getAlias(ks);

            System.out.println("Alias = " + alias);

            return "Certificate Loaded : " + alias;

        } catch (Exception e) {

            e.printStackTrace();

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

        KeyStore keyStore = loadKeyStore();

        String alias = getAlias(keyStore);

        return (X509Certificate)
                keyStore.getCertificate(alias);
    }

    public String generateSignature(String json) throws Exception {

        PrivateKey privateKey = getPrivateKey();

        Signature sign = Signature.getInstance("SHA256withRSA");

        sign.initSign(privateKey);

        sign.update(json.getBytes());

        byte[] signedBytes = sign.sign();

        return Base64.getEncoder()
                .encodeToString(signedBytes);

    }

    public String testSignature() {

        try {

            String data =
                    "[{\"pan\":\"ABCDE1234F\",\"name\":\"TEST USER\",\"fathername\":\"\",\"dob\":\"01/01/1990\"}]";

            String signature =
                    generateSignature(data);

            System.out.println(signature);

            return signature;

        } catch (Exception e) {

            e.printStackTrace();

            return e.getMessage();
        }
    }
}
