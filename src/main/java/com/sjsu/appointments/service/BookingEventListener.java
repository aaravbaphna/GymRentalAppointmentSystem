package com.sjsu.appointments.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final NotificationClient notifier;

    public BookingEventListener(NotificationClient notifier) {
        this.notifier = notifier;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCreated(BookingCreatedEvent event) {
        try {
            notifier.sendBookingConfirmation(event.appointment(), event.customerEmail());
        } catch (Exception e) {
            log.warn("Post-commit notification failed for appt={}: {}",
                    event.appointment().getId(), e.getMessage());
        }
    }
}
