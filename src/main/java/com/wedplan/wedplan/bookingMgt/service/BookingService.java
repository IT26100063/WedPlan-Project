package com.wedplan.wedplan.bookingMgt.service;

import com.wedplan.wedplan.bookingMgt.model.BookingModel;
import com.wedplan.wedplan.bookingMgt.model.BookingStatus;
import com.wedplan.wedplan.bookingMgt.repository.BookingRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Primary
public class BookingService {

    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BookingModel createBooking(BookingModel booking) {
        if (booking.getBookingReference() == null || booking.getBookingReference().isBlank()) {
            booking.setBookingReference("WP-" + (System.currentTimeMillis() % 10000000));
        }

        // Apply 10% Plus discount if the couple is a Plus member
        if (Boolean.TRUE.equals(booking.getIsPlusMember()) && booking.getTotalAmount() != null) {
            BigDecimal discounted = booking.getTotalAmount().multiply(new BigDecimal("0.90"))
                    .setScale(2, RoundingMode.HALF_UP);
            booking.setTotalAmount(discounted);
        }

        if (booking.getDepositAmount() == null && booking.getTotalAmount() != null) {
            // 30% initial deposit
            booking.setDepositAmount(booking.getTotalAmount().multiply(new BigDecimal("0.30"))
                    .setScale(2, RoundingMode.HALF_UP));
        }

        return repository.save(booking);
    }

    public List<BookingModel> getAllBookings() {
        return repository.findAll();
    }

    public BookingModel getBookingById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public BookingModel getBookingByReference(String reference) {
        return repository.findByBookingReferenceIgnoreCase(reference)
                .orElseThrow(() -> new RuntimeException("Booking not found with reference: " + reference));
    }

    public List<BookingModel> getBookingsByCustomer(String email) {
        return repository.findByCustomerEmailIgnoreCase(email);
    }

    public List<BookingModel> getBookingsByVendor(Long vendorId) {
        return repository.findByVendorId(vendorId);
    }

    public List<BookingModel> getBookingsByStatus(BookingStatus status) {
        return repository.findByStatus(status);
    }

    @Transactional
    public BookingModel updateBooking(Long id, BookingModel details) {
        BookingModel existing = getBookingById(id);

        existing.setCustomerName(details.getCustomerName());
        existing.setCustomerEmail(details.getCustomerEmail());
        existing.setCustomerPhone(details.getCustomerPhone());
        existing.setPartnerName(details.getPartnerName());
        existing.setWeddingDate(details.getWeddingDate());
        existing.setLocation(details.getLocation());
        existing.setVendorId(details.getVendorId());
        existing.setVendorName(details.getVendorName());
        existing.setPackageId(details.getPackageId());
        existing.setPackageName(details.getPackageName());
        existing.setGuestCount(details.getGuestCount());
        existing.setTotalAmount(details.getTotalAmount());
        existing.setDepositAmount(details.getDepositAmount());
        if (details.getStatus() != null) {
            existing.setStatus(details.getStatus());
        }
        existing.setSpecialNotes(details.getSpecialNotes());

        return repository.save(existing);
    }

    @Transactional
    public BookingModel updateStatus(Long id, BookingStatus status) {
        BookingModel existing = getBookingById(id);
        existing.setStatus(status);
        return repository.save(existing);
    }

    @Transactional
    public void deleteBooking(Long id) {
        BookingModel existing = getBookingById(id);
        repository.delete(existing);
    }
}
