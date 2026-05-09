package com.sjsu.appointments.service;

import com.sjsu.appointments.model.Appointment;

public record BookingCreatedEvent(Appointment appointment, String customerEmail) {}
