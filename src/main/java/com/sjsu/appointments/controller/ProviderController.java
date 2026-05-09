package com.sjsu.appointments.controller;

import com.sjsu.appointments.service.ProviderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/providers")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("providers", providerService.listProviders());
        return "providers";
    }

    @GetMapping("/{id}/availability")
    public String availability(@PathVariable("id") long id, Model model) {
        model.addAttribute("providerId", id);
        model.addAttribute("slots", providerService.listSlotsForProvider(id));
        model.addAttribute("services", providerService.listServicesForProvider(id));
        return "provider_availability";
    }

    @PostMapping("/{id}/availability")
    public String addAvailability(@PathVariable("id") long id,
                                  @RequestParam("serviceId") long serviceId,
                                  @RequestParam("startTime") String startTime,
                                  @RequestParam("endTime") String endTime,
                                  RedirectAttributes redirect) {
        try {
            long slotId = providerService.addAvailability(id, serviceId, startTime, endTime);
            redirect.addFlashAttribute("info", "Slot " + slotId + " added.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/providers/" + id + "/availability";
    }
}
