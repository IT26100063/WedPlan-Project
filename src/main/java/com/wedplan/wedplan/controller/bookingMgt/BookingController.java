package com.wedplan.wedplan.controller.bookingMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.model.bookingMgt.BookingStatus;
import com.wedplan.wedplan.services.bookingMgt.BookingServices;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingServices bookingServices;

    public BookingController(BookingServices bookingServices) {
        this.bookingServices = bookingServices;
    }

    @PostMapping
    public ResponseEntity<BookingModel> createBooking(@Valid @RequestBody BookingModel booking) {
        BookingModel created = bookingServices.createBooking(booking);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookingModel>> getAllBookings(
            @RequestParam(required = false) String customerEmail,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) BookingStatus status) {

        if (customerEmail != null && !customerEmail.isBlank()) {
            return ResponseEntity.ok(bookingServices.getBookingsByCustomer(customerEmail));
        }
        if (vendorId != null) {
            return ResponseEntity.ok(bookingServices.getBookingsByVendor(vendorId));
        }
        if (status != null) {
            return ResponseEntity.ok(bookingServices.getBookingsByStatus(status));
        }
        return ResponseEntity.ok(bookingServices.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingModel> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingServices.getBookingById(id));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingModel> getBookingByReference(@PathVariable String reference) {
        return ResponseEntity.ok(bookingServices.getBookingByReference(reference));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingModel> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingModel booking) {
        return ResponseEntity.ok(bookingServices.updateBooking(id, booking));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingModel> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(bookingServices.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        bookingServices.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }
}
