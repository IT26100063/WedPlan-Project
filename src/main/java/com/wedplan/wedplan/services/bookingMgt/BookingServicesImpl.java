package com.wedplan.wedplan.services.bookingMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.model.bookingMgt.BookingStatus;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentStatus;
import com.wedplan.wedplan.repository.bookingMgt.BookingRepository;
import com.wedplan.wedplan.repository.paymentMgt.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class BookingServicesImpl implements BookingServices {
    private static final Set<String> AVAILABLE_VENDORS = Set.of(
            "Golden Hour Studio",
            "The Garden House",
            "Petal & Pearl Beauty",
            "Wildflower Atelier",
            "Table for Two Catering",
            "The Evening Set"
    );

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    public BookingServicesImpl(BookingRepository bookingRepository, PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public BookingModel createBooking(BookingRequest request) {
        if (!AVAILABLE_VENDORS.contains(request.getVendorName())) {
            throw new IllegalArgumentException("Select a vendor from the available listings.");
        }
        BookingModel booking = new BookingModel();
        booking.setCustomerName(request.getCustomerName().trim());
        booking.setCustomerEmail(request.getCustomerEmail().trim().toLowerCase());
        booking.setCustomerPhone(request.getCustomerPhone().trim());
        booking.setCustomerAddress(request.getCustomerAddress().trim());
        booking.setCustomerCity(request.getCustomerCity().trim());
        booking.setCustomerCountry(request.getCustomerCountry() == null || request.getCustomerCountry().isBlank()
                ? "Sri Lanka" : request.getCustomerCountry().trim());
        booking.setVendorName(request.getVendorName());
        booking.setEventDate(request.getEventDate());
        booking.setGuestCount(trimToNull(request.getGuestCount()));
        booking.setNotes(trimToNull(request.getNotes()));
        booking.setStatus(BookingStatus.AWAITING_QUOTE);
        return bookingRepository.save(booking);
    }

    @Override
    public BookingModel getBooking(String reference) {
        return bookingRepository.findByBookingReference(reference).orElseThrow(BookingNotFoundException::new);
    }

    @Override
    public List<BookingModel> getBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<BookingModel> getBookingsNeedingAttention() {
        return bookingRepository.findByStatusInOrderByCreatedAtDesc(List.of(
                BookingStatus.AWAITING_QUOTE,
                BookingStatus.AWAITING_PAYMENT,
                BookingStatus.TRANSFER_REVIEW
        ));
    }

    @Override
    public long countBookingsAwaitingQuote() {
        return bookingRepository.countByStatus(BookingStatus.AWAITING_QUOTE);
    }

    @Override
    @Transactional
    public BookingModel setQuote(String reference, BigDecimal amount, String adminNotes) {
        BookingModel booking = getBooking(reference);
        if (booking.getStatus() == BookingStatus.PAID || booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.TRANSFER_REVIEW) {
            throw new IllegalStateException("This booking cannot be repriced in its current status.");
        }
        BigDecimal quote;
        try {
            quote = amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("The booking quote must have no more than two decimal places.");
        }
        if (quote.signum() <= 0) {
            throw new IllegalArgumentException("The booking quote must be greater than zero.");
        }
        PaymentModel existingPayment = paymentRepository.findByBookingReference(reference).orElse(null);
        if (existingPayment != null && existingPayment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("A completed payment cannot be repriced.");
        }
        if (existingPayment != null && existingPayment.getReceiptStorageKey() != null) {
            throw new IllegalStateException("A booking with a submitted transfer receipt cannot be repriced.");
        }
        booking.setQuotedAmount(quote);
        booking.setStatus(BookingStatus.AWAITING_PAYMENT);
        booking.setAdminNotes(trimToNull(adminNotes));
        if (existingPayment != null) {
            existingPayment.setAmount(quote);
            existingPayment.setCurrency(booking.getCurrency());
            paymentRepository.save(existingPayment);
        }
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public void updateBookingStatus(BookingModel booking, BookingStatus status) {
        booking.setStatus(status);
        bookingRepository.save(booking);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
