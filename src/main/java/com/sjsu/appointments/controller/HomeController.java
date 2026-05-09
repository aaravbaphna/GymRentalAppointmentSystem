package com.sjsu.appointments.controller;

import com.sjsu.appointments.repository.UserRepository;
import com.sjsu.appointments.service.AppointmentService;
import com.sjsu.appointments.service.MetricsRegistry;
import com.sjsu.appointments.service.ProviderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository users;
    private final ProviderService providerService;
    private final AppointmentService appointmentService;
    private final MetricsRegistry metrics;

    public HomeController(UserRepository users,
                          ProviderService providerService,
                          AppointmentService appointmentService,
                          MetricsRegistry metrics) {
        this.users = users;
        this.providerService = providerService;
        this.appointmentService = appointmentService;
        this.metrics = metrics;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("openSlotCount", appointmentService.listOpenSlots().size());
        model.addAttribute("providerCount", providerService.listProviders().size());
        model.addAttribute("userCount", users.findAll().size());
        model.addAttribute("bookings", metrics.bookings());
        model.addAttribute("failed", metrics.failedBookings());
        model.addAttribute("avgLatencyMs", String.format("%.2f", metrics.averageBookingLatencyMs()));
        return "home";
    }
}
