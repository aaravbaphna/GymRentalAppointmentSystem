package com.sjsu.appointments.service;

import com.sjsu.appointments.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient http;

    public NotificationClient(@Value("${app.notification.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.http = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public boolean sendBookingConfirmation(Appointment appt, String customerEmail) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("appointmentId", appt.getId());
        payload.put("customerEmail", customerEmail);
        payload.put("provider", appt.getProviderName());
        payload.put("service", appt.getServiceName());
        payload.put("startTime", appt.getStartTime());
        payload.put("channel", "EMAIL");
        try {
            ResponseEntity<Map<String, Object>> resp = http.post()
                    .uri("/mock/notify")
                    .body(payload)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
            log.info("Notification dispatched: appt={} status={}",
                    appt.getId(), resp.getStatusCode());
            return resp.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            log.warn("Notification failed (non-fatal): appt={} err={}", appt.getId(), e.getMessage());
            return false;
        }
    }
}
