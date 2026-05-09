package com.sjsu.appointments.controller;

import com.sjsu.appointments.service.MetricsRegistry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbc;
    private final MetricsRegistry metrics;

    public HealthController(JdbcTemplate jdbc, MetricsRegistry metrics) {
        this.jdbc = jdbc;
        this.metrics = metrics;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("status", "UP");
        resp.put("timestamp", Instant.now().toString());

        Map<String, Object> components = new LinkedHashMap<>();
        components.put("application", Map.of("status", "UP"));

        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            Integer slotCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM availability_slots", Integer.class);
            components.put("database", Map.of(
                    "status", one != null && one == 1 ? "UP" : "DOWN",
                    "slotCount", slotCount == null ? 0 : slotCount
            ));
        } catch (Exception e) {
            resp.put("status", "DOWN");
            components.put("database", Map.of("status", "DOWN", "error", e.getMessage()));
        }
        resp.put("components", components);
        return resp;
    }

    @GetMapping("/metrics-summary")
    public Map<String, Object> metricsSummary() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("bookingsTotal", metrics.bookings());
        r.put("bookingsFailed", metrics.failedBookings());
        r.put("bookingAvgLatencyMs", metrics.averageBookingLatencyMs());
        return r;
    }
}
