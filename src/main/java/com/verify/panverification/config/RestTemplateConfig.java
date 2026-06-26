package com.verify.panverification.config;


import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;


@Slf4j
@Configuration
public class RestTemplateConfig {

    @Value("${opv.pfx-file}")
    private String pfxFile;

    @Value("${opv.pfx-password}")
    private String pfxPassword;



    @Bean
    public RestTemplate restTemplate() {

        return new RestTemplate();}

}