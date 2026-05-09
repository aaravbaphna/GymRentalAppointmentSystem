package com.sjsu.appointments.service;

import com.sjsu.appointments.exception.NotFoundException;
import com.sjsu.appointments.exception.SlotAlreadyBookedException;
import com.sjsu.appointments.model.Appointment;
import com.sjsu.appointments.model.AvailabilitySlot;
import com.sjsu.appointments.model.User;
import com.sjsu.appointments.repository.AppointmentRepository;
import com.sjsu.appointments.repository.AvailabilitySlotRepository;
import com.sjsu.appointments.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointments;
    private final AvailabilitySlotRepository slots;
    private final UserRepository users;
    private final ApplicationEventPublisher events;
    private final MetricsRegistry metrics;

    public AppointmentService(AppointmentRepository appointments,
                              AvailabilitySlotRepository slots,
                              UserRepository users,
                              ApplicationEventPublisher events,
                              MetricsRegistry metrics) {
        this.appointments = appointments;
        this.slots = slots;
        this.users = users;
        this.events = events;
        this.metrics = metrics;
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlot> listOpenSlots() {
        return slots.findOpenSlots();
    }

    @Transactional(readOnly = true)
    public List<Appointment> listForCustomer(long customerId) {
        return appointments.findByCustomer(customerId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> listAll() {
        return appointments.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> findAppointment(long id) {
        return appointments.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AvailabilitySlot> findSlot(long id) {
        return slots.findById(id);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Appointment book(long customerId, long slotId, String notes) {
        long t0 = System.nanoTime();
        log.info("Booking attempt: customer={} slot={}", customerId, slotId);

        User customer = users.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer " + customerId + " not found"));

        AvailabilitySlot slot = slots.findById(slotId)
                .orElseThrow(() -> new NotFoundException("Slot " + slotId + " not found"));

        if (!"OPEN".equals(slot.getStatus())) {
            metrics.recordFailedBooking();
            log.warn("Booking rejected: slot {} not OPEN (current={})", slotId, slot.getStatus());
            throw new SlotAlreadyBookedException("Slot " + slotId + " is not available");
        }

        int updated = slots.markBookedIfVersionMatches(slot.getId(), slot.getVersion());
        if (updated == 0) {
            metrics.recordFailedBooking();
            log.warn("Booking rejected: optimistic lock conflict on slot {}", slotId);
            throw new SlotAlreadyBookedException("Slot " + slotId + " was just booked by another user");
        }

        long apptId;
        try {
            apptId = appointments.create(customerId, slotId, notes);
        } catch (DataIntegrityViolationException e) {
            metrics.recordFailedBooking();
            log.warn("Booking rejected: unique-index conflict on slot {} (e={})", slotId, e.getMessage());
            throw new SlotAlreadyBookedException("Slot " + slotId + " is already booked");
        }

        Appointment created = appointments.findById(apptId)
                .orElseThrow(() -> new IllegalStateException("Just-created appointment not found"));

        long latency = System.nanoTime() - t0;
        metrics.recordBooking(latency);
        log.info("Booking confirmed: appt={} customer={} slot={} latencyMs={}",
                apptId, customerId, slotId, latency / 1_000_000.0);

        events.publishEvent(new BookingCreatedEvent(created, customer.getEmail()));

        return created;
    }

    @Transactional
    public void cancel(long appointmentId) {
        log.info("Cancel attempt: appt={}", appointmentId);
        Appointment appt = appointments.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment " + appointmentId + " not found"));

        int rows = appointments.cancel(appointmentId);
        if (rows == 0) {
            log.warn("Cancel no-op: appt={} status={}", appointmentId, appt.getStatus());
            return;
        }
        slots.markOpen(appt.getSlotId());
        log.info("Cancel succeeded: appt={} slot reopened={}", appointmentId, appt.getSlotId());
    }
}
