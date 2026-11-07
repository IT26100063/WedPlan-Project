package com.wedplan.wedplan.services.bookingMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel.VenueBooking;
import com.wedplan.wedplan.model.bookingMgt.BookingModel.CateringBooking;
import com.wedplan.wedplan.model.bookingMgt.BookingModel.PhotographyBooking;
import com.wedplan.wedplan.model.bookingMgt.*;
import com.wedplan.wedplan.repository.bookingMgt.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class BookingServices {
    private final BookingRepository repo;

    public BookingServices(BookingRepository repo) { this.repo = repo; }

    public List<BookingModel> getAll() { return repo.findAll(); }

    public BookingModel getById(String id) { return repo.findById(id).orElse(null); }

    public List<BookingModel> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return repo.findAll();
        String k = keyword.toLowerCase().trim();
        return repo.findAll().stream()
                .filter(b -> b.getId().toLowerCase().contains(k)
                        || b.getCustomerName().toLowerCase().contains(k)
                        || b.getVendorName().toLowerCase().contains(k)
                        || b.getStatus().toLowerCase().contains(k)
                        || b.getType().toLowerCase().contains(k))
                .toList();
    }
    public List<BookingModel> filterByStatus(String status) {
        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            return repo.findAll();
        }

        return repo.findAll().stream()
                .filter(b -> b.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    public void create(String type, String customer, String vendor, String date,
                       String status, double qty, double rate, double extra) {
        BookingModel b = build(nextId(), LocalDate.now().toString(), type, customer,
                vendor, date, status, qty, rate, extra, true);
        repo.save(b);
    }

    public void update(String id, String type, String customer, String vendor, String date,
                       String status, double qty, double rate, double extra) {
        BookingModel existing = getById(id);
        if (existing == null) throw new IllegalArgumentException("Booking not found.");
        BookingModel b = build(id, existing.getCreatedAt(), type, customer,
                vendor, date, status, qty, rate, extra, false);
        repo.update(b);
    }

    public boolean delete(String id) { return repo.deleteById(id); }

    private String nextId() {
        int max = 0;
        for (BookingModel b : repo.findAll()) {
            try { max = Math.max(max, Integer.parseInt(b.getId().substring(2))); }
            catch (NumberFormatException ignored) { }
        }
        return String.format("BK%03d", max + 1);
    }

    // Factory + validation: returns the right subclass for the type
    private BookingModel build(String id, String createdAt, String type, String customer,
                               String vendor, String date, String status,
                               double qty, double rate, double extra, boolean isNew) {
        customer = customer == null ? "" : customer.trim();
        vendor = vendor == null ? "" : vendor.trim();
        if (customer.isEmpty() || vendor.isEmpty())
            throw new IllegalArgumentException("Customer and vendor names are required.");
        if (customer.contains("|") || vendor.contains("|"))
            throw new IllegalArgumentException("The '|' character is not allowed.");
        try {
            if (isNew && LocalDate.parse(date).isBefore(LocalDate.now()))
                throw new IllegalArgumentException("Event date cannot be in the past.");
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date.");
        }
        if (qty <= 0 || rate <= 0 || extra < 0)
            throw new IllegalArgumentException("Quantity and rate must be positive.");
        if (status == null || status.isBlank()) status = "PENDING";

        return switch (type) {
            case "VENUE"       -> new BookingModel.VenueBooking(id, createdAt, customer, vendor, date, status, qty, rate);
            case "CATERING"    -> new BookingModel.CateringBooking(id, createdAt, customer, vendor, date, status, (int) qty, rate);
            case "PHOTOGRAPHY" -> new PhotographyBooking(id, createdAt, customer, vendor, date, status, qty, rate, extra);
            default -> throw new IllegalArgumentException("Invalid booking type.");
        };
    }
}