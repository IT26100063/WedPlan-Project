package com.wedplan.wedplan.bookingMgt.controller;

import com.wedplan.wedplan.bookingMgt.model.BookingModel;
import com.wedplan.wedplan.bookingMgt.model.BookingStatus;
import com.wedplan.wedplan.bookingMgt.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingModel> createBooking(@Valid @RequestBody BookingModel booking) {
        BookingModel created = bookingService.createBooking(booking);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookingModel>> getAllBookings(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) BookingStatus status) {

        if (customerEmail != null && !customerEmail.isBlank()) {
            return ResponseEntity.ok(bookingService.getBookingsByCustomer(customerEmail));
        }
        if (vendorId != null) {
            return ResponseEntity.ok(bookingService.getBookingsByVendor(vendorId));
        }
        if (status != null) {
            return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingModel> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingModel> getBookingByReference(@PathVariable String reference) {
        return ResponseEntity.ok(bookingService.getBookingByReference(reference));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingModel> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingModel booking) {
        return ResponseEntity.ok(bookingService.updateBooking(id, booking));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingModel> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }
}
