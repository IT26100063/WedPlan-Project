package com.wedplan.wedplan.bookingMgt.service;

import com.wedplan.wedplan.bookingMgt.model.BookingModel;
import com.wedplan.wedplan.bookingMgt.model.BookingStatus;
import com.wedplan.wedplan.bookingMgt.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    private BookingRepository repository;
    private BookingService service;

    @BeforeEach
    void setUp() {
        repository = (BookingRepository) Proxy.newProxyInstance(
                BookingRepository.class.getClassLoader(),
                new Class<?>[]{BookingRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        return args[0];
                    }
                    return null;
                }
        );
        service = new BookingService(repository);
    }

    @Test
    void testCreateBookingCalculatesDiscountAndDepositForPlusMember() {
        BookingModel booking = new BookingModel();
        booking.setCustomerName("Amara & Kasun");
        booking.setCustomerEmail("amara@example.com");
        booking.setWeddingDate(LocalDate.of(2026, 12, 15));
        booking.setTotalAmount(new BigDecimal("100000.00"));
        booking.setIsPlusMember(true);

        BookingModel saved = service.createBooking(booking);

        assertNotNull(saved);
        // 10% discount: 100,000 * 0.90 = 90,000.00
        assertEquals(new BigDecimal("90000.00"), saved.getTotalAmount());
        // 30% deposit of 90,000.00 = 27,000.00
        assertEquals(new BigDecimal("27000.00"), saved.getDepositAmount());
        assertNotNull(saved.getBookingReference());
        assertTrue(saved.getBookingReference().startsWith("WP-"));
    }

    @Test
    void testCreateBookingWithoutPlusDiscount() {
        BookingModel booking = new BookingModel();
        booking.setCustomerName("Nimal & Dilki");
        booking.setCustomerEmail("nimal@example.com");
        booking.setWeddingDate(LocalDate.of(2027, 2, 20));
        booking.setTotalAmount(new BigDecimal("50000.00"));
        booking.setIsPlusMember(false);

        BookingModel saved = service.createBooking(booking);

        assertNotNull(saved);
        assertEquals(new BigDecimal("50000.00"), saved.getTotalAmount());
        assertEquals(new BigDecimal("15000.00"), saved.getDepositAmount());
    }
}
