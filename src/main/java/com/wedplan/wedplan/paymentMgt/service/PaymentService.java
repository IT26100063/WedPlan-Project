package com.wedplan.wedplan.paymentMgt.service;

import com.wedplan.wedplan.paymentMgt.model.PaymentCalculationResult;
import com.wedplan.wedplan.paymentMgt.model.PaymentMethod;
import com.wedplan.wedplan.paymentMgt.model.PaymentModel;
import com.wedplan.wedplan.paymentMgt.model.PaymentStatus;
import com.wedplan.wedplan.paymentMgt.model.UserCheckoutRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    Page<PaymentModel> findPayments(String search, PaymentStatus paymentStatus, PaymentMethod paymentMethod, Pageable pageable);

    PaymentModel getPayment(Long id);

    PaymentSummary getSummary();

    PaymentModel createPayment(PaymentModel payment);

    PaymentModel updatePayment(Long id, PaymentModel payment);

    void deletePayment(Long id);

    // New standard features
    PaymentModel verifyPayment(Long id, String verificationNotes);

    PaymentModel refundPayment(Long id, String refundReason);

    PaymentModel updateStatus(Long id, PaymentStatus newStatus, String notes);

    PaymentModel sendReminder(Long id);

    PaymentModel processUserCheckout(UserCheckoutRequest request);

    PaymentCalculationResult calculateCheckout(String bookingReference, String paymentType, PaymentMethod method, boolean isPlusMember, BigDecimal baseAmount);

    List<PaymentModel> getPaymentsByBooking(String bookingReference);

    List<PaymentModel> getPaymentsByCustomer(String customerEmail);

    byte[] exportPaymentsCsv();
}
