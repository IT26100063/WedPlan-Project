
package com.wedplan.wedplan.controller.bookingMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.services.bookingMgt.BookingServices;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingMgtController {

    private final BookingServices service;

    public BookingMgtController(BookingServices service) {
        this.service = service;
    }

    // SERVER-SIDE VALIDATION
    private void validateBookingValues(double qty, double rate, double extra) {

        if (!Double.isFinite(qty) || qty <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0."
            );
        }

        if (!Double.isFinite(rate) || rate <= 0) {
            throw new IllegalArgumentException(
                    "Rate must be greater than 0."
            );
        }

        if (!Double.isFinite(extra) || extra < 0) {
            throw new IllegalArgumentException(
                    "Album Fee cannot be negative."
            );
        }
    }

    // READ: List + Search + Status Filter
    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {

        var allBookings = service.getAll();

        long totalBookings = allBookings.size();

        long confirmedBookings = allBookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .count();

        long pendingBookings = allBookings.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();

        long cancelledBookings = allBookings.stream()
                .filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus()))
                .count();

        var bookings = service.search(keyword);

        if (status != null && !status.isBlank()
                && !status.equalsIgnoreCase("ALL")) {

            bookings = bookings.stream()
                    .filter(b -> b.getStatus() != null
                            && b.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus",
                status == null || status.isBlank() ? "ALL" : status);

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);

        return "booking";
    }

    // OPEN ADD BOOKING FORM
    @GetMapping("/new")
    public String newForm() {
        return "add-booking";
    }

    // CREATE BOOKING
    @PostMapping("/create")
    public String create(
            @RequestParam String type,
            @RequestParam String customerName,
            @RequestParam String vendorName,
            @RequestParam String eventDate,
            @RequestParam double qty,
            @RequestParam double rate,
            @RequestParam(defaultValue = "0") double extra,
            RedirectAttributes ra) {

        try {
            // Validate before saving
            validateBookingValues(qty, rate, extra);

            service.create(
                    type, customerName, vendorName,
                    eventDate, "PENDING", qty, rate, extra
            );

            ra.addFlashAttribute("success",
                    "Booking created successfully.");

            return "redirect:/bookings";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/new";
        }
    }

    // VIEW SINGLE BOOKING
    @GetMapping("/{id}")
    public String view(
            @PathVariable String id,
            Model model,
            RedirectAttributes ra) {

        BookingModel b = service.getById(id);

        if (b == null) {
            ra.addFlashAttribute("error", "Booking not found.");
            return "redirect:/bookings";
        }

        model.addAttribute("b", b);
        return "view-booking";
    }

    // OPEN EDIT BOOKING FORM
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable String id,
            Model model,
            RedirectAttributes ra) {

        BookingModel b = service.getById(id);

        if (b == null) {
            ra.addFlashAttribute("error", "Booking not found.");
            return "redirect:/bookings";
        }

        model.addAttribute("b", b);
        return "edit-booking";
    }

    // UPDATE BOOKING
    @PostMapping("/{id}/update")
    public String update(
            @PathVariable String id,
            @RequestParam String type,
            @RequestParam String customerName,
            @RequestParam String vendorName,
            @RequestParam String eventDate,
            @RequestParam String status,
            @RequestParam double qty,
            @RequestParam double rate,
            @RequestParam(defaultValue = "0") double extra,
            RedirectAttributes ra) {

        try {
            // Validate before updating
            validateBookingValues(qty, rate, extra);

            service.update(
                    id, type, customerName, vendorName,
                    eventDate, status, qty, rate, extra
            );

            ra.addFlashAttribute("success", "Booking updated.");
            return "redirect:/bookings";

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/bookings/" + id + "/edit";
        }
    }

    // DELETE BOOKING
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable String id,
            RedirectAttributes ra) {

        boolean deleted = service.delete(id);

        if (deleted) {
            ra.addFlashAttribute("success", "Booking deleted.");
        } else {
            ra.addFlashAttribute("error", "Booking not found.");
        }

        return "redirect:/bookings";
    }

    // UPDATE BOOKING STATUS
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable String id,
            @RequestParam String status,
            RedirectAttributes ra) {

        try {
            BookingModel booking = service.getById(id);

            if (booking == null) {
                ra.addFlashAttribute("error", "Booking not found.");
                return "redirect:/bookings";
            }

            service.update(
                    id,
                    booking.getType(),
                    booking.getCustomerName(),
                    booking.getVendorName(),
                    booking.getEventDate(),
                    status,
                    booking.getQuantity(),
                    booking.getRate(),
                    booking.getExtra()
            );

            ra.addFlashAttribute("success",
                    "Booking status updated successfully.");

        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/bookings";
    }

    // BOOKING DASHBOARD
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        var bookings = service.getAll();

        long totalBookings = bookings.size();

        long confirmedBookings = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .count();

        long pendingBookings = bookings.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()))
                .count();

        long cancelledBookings = bookings.stream()
                .filter(b -> "CANCELLED".equalsIgnoreCase(b.getStatus()))
                .count();

        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);

        return "booking-dashboard";
    }

    // WEDDING CALENDAR
    @GetMapping("/calendar")
    public String calendar(Model model) {

        model.addAttribute("bookings", service.getAll());

        return "booking-calendar";
    }
}
