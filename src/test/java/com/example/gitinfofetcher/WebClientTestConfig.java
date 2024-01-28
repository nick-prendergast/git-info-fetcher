package com.example.gitinfofetcher;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
public class WebClientTestConfig {

    @Bean
    @Primary
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    @Primary
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:8089").build();
    }
}
