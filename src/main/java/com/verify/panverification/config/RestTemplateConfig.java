package com.verify.panverification.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Slf4j
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() throws Exception {

        log.info("Configuring RestTemplate with SSL bypass for UAT");

        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(null, (cert, authType) -> true)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setSSLSocketFactory(
                                        SSLConnectionSocketFactoryBuilder.create()
                                                .setSslContext(sslContext)
                                                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                                .build()
                                )
                                .build()
                )
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);

        log.info("RestTemplate configured successfully with SSL bypass");

        return new RestTemplate(factory);
    }
}