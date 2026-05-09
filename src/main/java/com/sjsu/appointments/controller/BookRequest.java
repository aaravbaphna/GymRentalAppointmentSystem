package com.sjsu.appointments.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotNull @Positive Long customerId,
        @NotNull @Positive Long slotId,
        @Size(max = 500) String notes
) {}
