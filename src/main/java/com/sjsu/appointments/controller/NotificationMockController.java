package com.sjsu.appointments.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/mock")
public class NotificationMockController {

    private static final Logger log = LoggerFactory.getLogger(NotificationMockController.class);

    @PostMapping("/notify")
    public Map<String, Object> notify(@RequestBody Map<String, Object> payload) {
        String messageId = "msg_" + UUID.randomUUID();
        log.info("[MOCK NOTIFY] -> id={} channel={} to={} appt={}",
                messageId,
                payload.get("channel"),
                payload.get("customerEmail"),
                payload.get("appointmentId"));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("messageId", messageId);
        resp.put("acceptedAt", Instant.now().toString());
        resp.put("status", "ACCEPTED");
        return resp;
    }
}
