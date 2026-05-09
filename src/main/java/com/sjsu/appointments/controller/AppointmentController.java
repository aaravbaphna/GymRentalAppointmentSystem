package com.sjsu.appointments.controller;

import com.sjsu.appointments.exception.NotFoundException;
import com.sjsu.appointments.exception.SlotAlreadyBookedException;
import com.sjsu.appointments.model.Appointment;
import com.sjsu.appointments.model.AvailabilitySlot;
import com.sjsu.appointments.repository.UserRepository;
import com.sjsu.appointments.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AppointmentController {

    private final AppointmentService service;
    private final UserRepository users;

    public AppointmentController(AppointmentService service, UserRepository users) {
        this.service = service;
        this.users = users;
    }

    @GetMapping("/slots")
    public String openSlots(Model model) {
        model.addAttribute("slots", service.listOpenSlots());
        return "slots";
    }

    @GetMapping("/book")
    public String bookForm(@RequestParam("slotId") long slotId,
                           Model model,
                           RedirectAttributes redirect) {
        Optional<AvailabilitySlot> slotOpt = service.findSlot(slotId);
        if (slotOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Slot " + slotId + " was not found.");
            return "redirect:/slots";
        }
        AvailabilitySlot slot = slotOpt.get();
        if (!"OPEN".equals(slot.getStatus())) {
            redirect.addFlashAttribute("error", "That slot is no longer available.");
            return "redirect:/slots";
        }
        model.addAttribute("slot", slot);
        model.addAttribute("customers", users.findAll().stream()
                .filter(u -> "CUSTOMER".equals(u.getRole())).toList());
        return "book";
    }

    @PostMapping("/book")
    public String submitBooking(@RequestParam("slotId") long slotId,
                                @RequestParam("customerId") long customerId,
                                @RequestParam(value = "notes", required = false) String notes,
                                RedirectAttributes redirect) {
        if (notes != null && notes.length() > 500) {
            notes = notes.substring(0, 500);
        }
        try {
            Appointment appt = service.book(customerId, slotId, notes);
            redirect.addAttribute("apptId", appt.getId());
            return "redirect:/confirmation";
        } catch (SlotAlreadyBookedException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/slots";
        } catch (NotFoundException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/slots";
        }
    }

    @GetMapping("/confirmation")
    public String confirmation(@RequestParam("apptId") long apptId,
                               Model model,
                               RedirectAttributes redirect) {
        Optional<Appointment> apptOpt = service.findAppointment(apptId);
        if (apptOpt.isEmpty()) {
            redirect.addFlashAttribute("error", "Appointment " + apptId + " not found.");
            return "redirect:/appointments";
        }
        model.addAttribute("appointment", apptOpt.get());
        return "confirmation";
    }

    @GetMapping("/appointments")
    public String myAppointments(@RequestParam(value = "customerId", required = false) Long customerId,
                                 Model model) {
        List<Appointment> list = (customerId == null)
                ? service.listAll()
                : service.listForCustomer(customerId);
        model.addAttribute("appointments", list);
        model.addAttribute("customers", users.findAll().stream()
                .filter(u -> "CUSTOMER".equals(u.getRole())).toList());
        model.addAttribute("selectedCustomerId", customerId);
        return "appointments";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancel(@PathVariable("id") long id,
                         @RequestParam(value = "customerId", required = false) Long customerId,
                         RedirectAttributes redirect) {
        service.cancel(id);
        redirect.addFlashAttribute("info", "Appointment " + id + " canceled.");
        if (customerId != null) {
            redirect.addAttribute("customerId", customerId);
        }
        return "redirect:/appointments";
    }

    @PostMapping("/api/appointments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiBook(@Valid @RequestBody BookRequest req) {
        try {
            Appointment a = service.book(req.customerId(), req.slotId(), req.notes());
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("id", a.getId());
            resp.put("customerId", a.getCustomerId());
            resp.put("slotId", a.getSlotId());
            resp.put("status", a.getStatus());
            resp.put("provider", a.getProviderName());
            resp.put("service", a.getServiceName());
            resp.put("startTime", a.getStartTime());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (SlotAlreadyBookedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/appointments")
    @ResponseBody
    public List<Appointment> apiList(@RequestParam(value = "customerId", required = false) Long customerId) {
        return (customerId == null) ? service.listAll() : service.listForCustomer(customerId);
    }

    @PostMapping("/api/appointments/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> apiCancel(@PathVariable("id") long id) {
        service.cancel(id);
        return ResponseEntity.ok(Map.of("id", id, "status", "CANCELED"));
    }
}
