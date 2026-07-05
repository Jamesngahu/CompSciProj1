package com.cbcportal.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Sends email via Brevo's HTTP API rather than raw SMTP - most PaaS hosts
 * (including Railway) block outbound SMTP ports to prevent spam abuse, but
 * a plain HTTPS call works everywhere.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.from-email}")
    private String fromEmail;

    @Value("${brevo.from-name}")
    private String fromName;

    public void sendLoginCode(String toEmail, String code) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("BREVO_API_KEY not configured - login code for {}: {}", toEmail, code);
            return;
        }

        String text = "Your login verification code is: " + code
                + "\n\nThis code expires in 5 minutes. If you did not request it, you can ignore this email.";

        Map<String, Object> body = Map.of(
                "sender", Map.of("email", fromEmail, "name", fromName),
                "to", List.of(Map.of("email", toEmail)),
                "subject", "Your CBC Portal login code",
                "textContent", text
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);
        headers.set("accept", "application/json");

        try {
            restTemplate.postForObject("https://api.brevo.com/v3/smtp/email", new HttpEntity<>(body, headers), Map.class);
        } catch (RestClientException e) {
            // Falls back to logging the code so the app stays usable if Brevo is
            // misconfigured or temporarily unreachable.
            log.warn("Could not email login code to {} ({}). Code: {}", toEmail, e.getMessage(), code);
        }
    }
}
