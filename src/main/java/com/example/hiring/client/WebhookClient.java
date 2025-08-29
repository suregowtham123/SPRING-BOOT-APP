package com.example.hiring.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class WebhookClient {

    private static final Logger log = LoggerFactory.getLogger(WebhookClient.class);
    private final WebClient webClient;
    private final boolean bearerPrefix;

    public WebhookClient(@Value("${app.bearerPrefix:false}") boolean bearerPrefix) {
        this.webClient = WebClient.builder()
                .baseUrl("https://bfhldevapigw.healthrx.co.in")
                .build();
        this.bearerPrefix = bearerPrefix;
    }

    public GenerateWebhookResponse generateWebhook(String name, String regNo, String email) {
        try {
            return webClient.post()
                    .uri("/hiring/generateWebhook/JAVA")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", name, "regNo", regNo, "email", email))
                    .retrieve()
                    .bodyToMono(GenerateWebhookResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .doOnError(e -> log.error("generateWebhook error: {}", e.toString()))
                    .block();
        } catch (Exception e) {
            log.error("generateWebhook failed", e);
            return null;
        }
    }

    public void submitFinalQuery(String url, String accessToken, String finalQuery) {
        String authValue = bearerPrefix ? ("Bearer " + accessToken) : accessToken;
        try {
            String response = WebClient.create()
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", authValue)
                    .bodyValue(Map.of("finalQuery", finalQuery))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .doOnError(e -> log.error("submitFinalQuery error: {}", e.toString()))
                    .block();
            log.info("Submission to {} response: {}", url, response);
        } catch (Exception e) {
            log.error("submitFinalQuery failed for url {}", url, e);
        }
    }
}
