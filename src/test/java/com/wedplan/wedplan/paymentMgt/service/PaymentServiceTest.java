package com.wedplan.wedplan.paymentMgt.service;

import com.wedplan.wedplan.paymentMgt.model.PaymentCalculationResult;
import com.wedplan.wedplan.paymentMgt.model.PaymentMethod;
import com.wedplan.wedplan.paymentMgt.model.PaymentModel;
import com.wedplan.wedplan.paymentMgt.model.PaymentStatus;
import com.wedplan.wedplan.paymentMgt.model.UserCheckoutRequest;
import com.wedplan.wedplan.paymentMgt.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private PaymentServiceImpl paymentServices;

    @BeforeEach
    void setUp() {
        paymentRepository = (PaymentRepository) Proxy.newProxyInstance(
                PaymentRepository.class.getClassLoader(),
                new Class<?>[]{PaymentRepository.class},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("count".equals(methodName)) {
                        return 10L;
                    }
                    if ("countByPaymentStatus".equals(methodName)) {
                        PaymentStatus status = (PaymentStatus) args[0];
                        if (status == PaymentStatus.COMPLETED) return 6L;
                        if (status == PaymentStatus.PENDING) return 3L;
                        if (status == PaymentStatus.FAILED) return 1L;
                        if (status == PaymentStatus.REFUNDED) return 1L;
                        return 0L;
                    }
                    if ("existsByBookingReferenceIgnoreCase".equals(methodName)) {
                        String ref = (String) args[0];
                        return "WP-DUPLICATE".equalsIgnoreCase(ref);
                    }
                    if ("saveAndFlush".equals(methodName)) {
                        return args[0];
                    }
                    if ("findById".equals(methodName)) {
                        Long id = (Long) args[0];
                        PaymentModel payment = new PaymentModel();
                        payment.setId(id);
                        payment.setBookingReference("WP-2026-001");
                        payment.setCustomerName("Nethmi & Kavin");
                        payment.setCustomerEmail("nethmi@example.com");
                        payment.setAmount(new BigDecimal("120000.00"));
                        payment.setCurrency("LKR");
                        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
                        payment.setPaymentStatus(PaymentStatus.PENDING);
                        payment.setPaymentDate(LocalDate.now());
                        return Optional.of(payment);
                    }
                    if ("findAll".equals(methodName)) {
                        PaymentModel p = new PaymentModel();
                        p.setId(1L);
                        p.setBookingReference("WP-2026-001");
                        p.setCustomerName("Nethmi & Kavin");
                        p.setCustomerEmail("nethmi@example.com");
                        p.setAmount(new BigDecimal("120000.00"));
                        p.setCurrency("LKR");
                        p.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
                        p.setPaymentStatus(PaymentStatus.COMPLETED);
                        p.setPaymentDate(LocalDate.now());
                        return List.of(p);
                    }
                    return null;
                }
        );
        paymentServices = new PaymentServiceImpl(paymentRepository);
    }

    @Test
    void testGetSummaryReturnsAccurateCounts() {
        PaymentSummary summary = paymentServices.getSummary();

        assertNotNull(summary);
        assertEquals(10L, summary.total());
        assertEquals(6L, summary.completed());
        assertEquals(3L, summary.pending());
        assertEquals(1L, summary.failed());
    }

    @Test
    void testCreatePaymentRejectsDuplicateBookingReference() {
        PaymentModel payment = new PaymentModel();
        payment.setBookingReference("WP-DUPLICATE");
        payment.setCustomerName("Test Customer");
        payment.setCustomerEmail("test@example.com");
        payment.setAmount(new BigDecimal("25000.00"));
        payment.setCurrency("LKR");
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDate.now());

        DuplicatePaymentException ex = assertThrows(
                DuplicatePaymentException.class,
                () -> paymentServices.createPayment(payment)
        );
        assertEquals("bookingReference", ex.getField());
    }

    @Test
    void testCreatePaymentNormalizesCurrencyAndFields() {
        PaymentModel payment = new PaymentModel();
        payment.setBookingReference("  WP-NEW-001  ");
        payment.setCustomerName("  Jane Doe  ");
        payment.setCustomerEmail("  jane@example.com  ");
        payment.setAmount(new BigDecimal("15000.00"));
        payment.setCurrency("lkr");
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDate.now());

        PaymentModel saved = paymentServices.createPayment(payment);

        assertNotNull(saved);
        assertEquals("WP-NEW-001", saved.getBookingReference());
        assertEquals("Jane Doe", saved.getCustomerName());
        assertEquals("jane@example.com", saved.getCustomerEmail());
        assertEquals("LKR", saved.getCurrency());
    }

    @Test
    void testCalculateCheckoutWithDiscounts() {
        // Base amount 100,000, 30% deposit = 30,000
        // Plus 10% = 3,000 -> 27,000
        // Bank 5% = 1,350 -> 25,650
        PaymentCalculationResult result = paymentServices.calculateCheckout(
                "WP-TEST-01",
                "DEPOSIT",
                PaymentMethod.BANK_TRANSFER,
                true,
                new BigDecimal("100000.00")
        );

        assertNotNull(result);
        assertEquals(30, result.getInstallmentPercentage());
        assertEquals(new BigDecimal("30000.00"), result.getInstallmentAmount());
        assertEquals(new BigDecimal("3000.00"), result.getPlusDiscountAmount());
        assertEquals(new BigDecimal("1350.00"), result.getBankDiscountAmount());
        assertEquals(new BigDecimal("4350.00"), result.getTotalDiscountAmount());
        assertEquals(new BigDecimal("25650.00"), result.getFinalPayableAmount());
    }

    @Test
    void testVerifyPaymentUpdatesStatus() {
        PaymentModel verified = paymentServices.verifyPayment(1L, "Bank slip verified");

        assertNotNull(verified);
        assertEquals(PaymentStatus.COMPLETED, verified.getPaymentStatus());
        assertTrue(verified.getNotes().contains("Bank slip verified"));
    }

    @Test
    void testRefundPaymentSetsStatusAndReason() {
        PaymentModel refunded = paymentServices.refundPayment(1L, "Couple postponed wedding");

        assertNotNull(refunded);
        assertEquals(PaymentStatus.REFUNDED, refunded.getPaymentStatus());
        assertEquals("Couple postponed wedding", refunded.getRefundReason());
        assertEquals(LocalDate.now(), refunded.getRefundDate());
    }

    @Test
    void testUpdateStatusSetsNewStatusAndNotes() {
        PaymentModel updated = paymentServices.updateStatus(1L, PaymentStatus.FAILED, "Card gateway rejected authorization");

        assertNotNull(updated);
        assertEquals(PaymentStatus.FAILED, updated.getPaymentStatus());
        assertTrue(updated.getNotes().contains("Card gateway rejected authorization"));
    }

    @Test
    void testSendReminderAppendsReminderAuditNote() {
        PaymentModel updated = paymentServices.sendReminder(1L);

        assertNotNull(updated);
        assertTrue(updated.getNotes().contains("Payment reminder notice sent"));
    }

    @Test
    void testExportPaymentsCsvGeneratesContent() {
        byte[] csv = paymentServices.exportPaymentsCsv();

        assertNotNull(csv);
        String csvText = new String(csv, StandardCharsets.UTF_8);
        assertTrue(csvText.contains("Booking Reference"));
        assertTrue(csvText.contains("WP-2026-001"));
        assertTrue(csvText.contains("Nethmi & Kavin"));
    }
}