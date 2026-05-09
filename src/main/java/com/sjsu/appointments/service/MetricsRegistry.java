package com.sjsu.appointments.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsRegistry {
    private final AtomicLong bookings = new AtomicLong();
    private final AtomicLong failedBookings = new AtomicLong();
    private final AtomicLong totalBookingLatencyNs = new AtomicLong();
    private final AtomicLong bookingSamples = new AtomicLong();

    public void recordBooking(long latencyNs) {
        bookings.incrementAndGet();
        totalBookingLatencyNs.addAndGet(latencyNs);
        bookingSamples.incrementAndGet();
    }

    public void recordFailedBooking() {
        failedBookings.incrementAndGet();
    }

    public long bookings() { return bookings.get(); }
    public long failedBookings() { return failedBookings.get(); }
    public double averageBookingLatencyMs() {
        long s = bookingSamples.get();
        if (s == 0) return 0.0;
        return (totalBookingLatencyNs.get() / (double) s) / 1_000_000.0;
    }
}
