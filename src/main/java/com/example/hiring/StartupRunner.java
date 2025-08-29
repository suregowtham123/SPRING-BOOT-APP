package com.example.hiring;

import com.example.hiring.client.GenerateWebhookResponse;
import com.example.hiring.client.WebhookClient;
import com.example.hiring.sql.SqlChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupRunner.class);

    private final WebhookClient client;
    private final SqlChooser sqlChooser;

    @Value("${app.candidate.name}")
    private String name;
    @Value("${app.candidate.regNo}")
    private String regNo;
    @Value("${app.candidate.email}")
    private String email;

    public StartupRunner(WebhookClient client, SqlChooser sqlChooser) {
        this.client = client;
        this.sqlChooser = sqlChooser;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting flow for candidate: {} (regNo: {}, email: {})", name, regNo, email);

        // 1) Generate webhook
        GenerateWebhookResponse resp = client.generateWebhook(name, regNo, email);
        if (resp == null || resp.getWebhook() == null || resp.getAccessToken() == null) {
            log.error("Failed to get webhook or access token. Aborting.");
            return;
        }
        log.info("Received webhook: {}", resp.getWebhook());

        // 2) Prepare SQL based on regNo (EVEN -> Question 2)
        String finalQuery = sqlChooser.chooseForRegNo(regNo);
        log.info("Final SQL prepared ({} chars)", finalQuery.length());

        // 3) Submit to returned webhook URL
        client.submitFinalQuery(resp.getWebhook(), resp.getAccessToken(), finalQuery);

        // 4) Also submit to fixed testWebhook as per spec
        client.submitFinalQuery("https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA",
                resp.getAccessToken(), finalQuery);

        log.info("Done.");
    }
}
