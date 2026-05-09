package com.sjsu.appointments.service;

import com.sjsu.appointments.model.AvailabilitySlot;
import com.sjsu.appointments.model.Provider;
import com.sjsu.appointments.model.ServiceOffering;
import com.sjsu.appointments.repository.AvailabilitySlotRepository;
import com.sjsu.appointments.repository.ProviderRepository;
import com.sjsu.appointments.repository.ServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class ProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderService.class);

    private final ProviderRepository providers;
    private final ServiceRepository services;
    private final AvailabilitySlotRepository slots;

    public ProviderService(ProviderRepository providers,
                           ServiceRepository services,
                           AvailabilitySlotRepository slots) {
        this.providers = providers;
        this.services = services;
        this.slots = slots;
    }

    @Transactional(readOnly = true)
    public List<Provider> listProviders() {
        return providers.findAll();
    }

    @Transactional(readOnly = true)
    public List<ServiceOffering> listServices() {
        return services.findAll();
    }

    @Transactional(readOnly = true)
    public List<ServiceOffering> listServicesForProvider(long providerId) {
        return services.findByProviderId(providerId);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlot> listSlotsForProvider(long providerId) {
        return slots.findAllByProvider(providerId);
    }

    @Transactional
    public long addAvailability(long providerId, long serviceId, String startTime, String endTime) {
        LocalDateTime start = parseIso(startTime, "startTime");
        LocalDateTime end   = parseIso(endTime,   "endTime");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("endTime must be strictly after startTime");
        }
        log.info("Provider {} adding slot service={} {} -> {}", providerId, serviceId, startTime, endTime);
        return slots.create(providerId, serviceId, start.toString(), end.toString());
    }

    private static LocalDateTime parseIso(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required (ISO-8601)");
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(field + " must be ISO-8601 (yyyy-MM-ddTHH:mm:ss)");
        }
    }
}
