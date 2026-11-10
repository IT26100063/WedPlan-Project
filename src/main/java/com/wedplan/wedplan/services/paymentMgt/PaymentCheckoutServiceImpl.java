package com.wedplan.wedplan.services.paymentMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.model.bookingMgt.BookingStatus;
import com.wedplan.wedplan.model.paymentMgt.PaymentMethod;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentStatus;
import com.wedplan.wedplan.repository.bookingMgt.BookingRepository;
import com.wedplan.wedplan.repository.paymentMgt.PaymentRepository;
import com.wedplan.wedplan.services.bookingMgt.BookingNotFoundException;
import com.wedplan.wedplan.services.bookingMgt.BookingServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentCheckoutServiceImpl implements PaymentCheckoutService {
    private final BookingServices bookingServices;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ReceiptStorage receiptStorage;
    private final String merchantId;
    private final String merchantSecret;
    private final String checkoutUrl;
    private final String publicBaseUrl;
    private final String bankName;
    private final String bankAccountName;
    private final String bankAccountNumber;
    private final String bankBranch;

    public PaymentCheckoutServiceImpl(
            BookingServices bookingServices,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            ReceiptStorage receiptStorage,
            @Value("${wedplan.payhere.merchant-id:}") String merchantId,
            @Value("${wedplan.payhere.merchant-secret:}") String merchantSecret,
            @Value("${wedplan.payhere.checkout-url:https://sandbox.payhere.lk/pay/checkout}") String checkoutUrl,
            @Value("${wedplan.public-base-url:http://localhost:8080}") String publicBaseUrl,
            @Value("${wedplan.bank.name:}") String bankName,
            @Value("${wedplan.bank.account-name:}") String bankAccountName,
            @Value("${wedplan.bank.account-number:}") String bankAccountNumber,
            @Value("${wedplan.bank.branch:}") String bankBranch) {
        this.bookingServices = bookingServices;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.receiptStorage = receiptStorage;
        this.merchantId = merchantId.trim();
        this.merchantSecret = merchantSecret.trim();
        this.checkoutUrl = checkoutUrl.trim();
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
        this.bankName = bankName.trim();
        this.bankAccountName = bankAccountName.trim();
        this.bankAccountNumber = bankAccountNumber.trim();
        this.bankBranch = bankBranch.trim();
    }

    @Override
    public BookingModel getBooking(String reference) {
        return bookingServices.getBooking(reference);
    }

    @Override
    public PaymentModel getPaymentForBooking(String reference) {
        return paymentRepository.findByBookingReference(reference).orElse(null);
    }

    @Override
    public boolean isPayHereConfigured() {
        return !merchantId.isBlank() && !merchantSecret.isBlank() && hasPublicNotifyUrl();
    }

    @Override
    @Transactional
    public PayHereCheckoutData beginCardCheckout(String reference) {
        ensurePayHereConfigured();
        BookingModel booking = bookingServices.getBooking(reference);
        ensurePayable(booking);
        PaymentModel payment = findOrCreatePayment(booking);
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentWorkflowException("This booking has already been paid.");
        }
        if (payment.getReceiptStorageKey() != null) {
            throw new PaymentWorkflowException("A bank-transfer receipt is already awaiting review.");
        }
        payment.setPaymentMethod(PaymentMethod.CARD);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setProviderOrderReference("WP-" + UUID.randomUUID().toString().replace("-", ""));
        payment.setTransactionReference(null);
        payment.setPaymentDate(LocalDate.now());
        try {
            paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException exception) {
            throw new PaymentWorkflowException("The checkout could not be prepared. Please try again.", exception);
        }

        String amount = booking.getQuotedAmount().setScale(2).toPlainString();
        String orderId = payment.getProviderOrderReference();
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant_id", merchantId);
        fields.put("return_url", publicUrl("/checkout/" + booking.getBookingReference() + "/return"));
        fields.put("cancel_url", publicUrl("/checkout/" + booking.getBookingReference() + "/cancel"));
        fields.put("notify_url", publicUrl("/payhere/notify"));
        fields.put("order_id", orderId);
        fields.put("items", "WedPlan booking " + booking.getBookingReference());
        fields.put("currency", booking.getCurrency());
        fields.put("amount", amount);
        fields.put("first_name", firstName(booking.getCustomerName()));
        fields.put("last_name", lastName(booking.getCustomerName()));
        fields.put("email", booking.getCustomerEmail());
        fields.put("phone", booking.getCustomerPhone());
        fields.put("address", booking.getCustomerAddress());
        fields.put("city", booking.getCustomerCity());
        fields.put("country", booking.getCustomerCountry());
        fields.put("custom_1", booking.getBookingReference());
        fields.put("custom_2", "");
        fields.put("hash", PayHereSignature.checkoutHash(
                merchantId, orderId, booking.getQuotedAmount(), booking.getCurrency(), merchantSecret));
        return new PayHereCheckoutData(checkoutUrl, fields);
    }

    @Override
    @Transactional
    public void acceptPayHereNotification(String notifiedMerchantId, String orderId, String amount,
                                          String currency, String statusCode, String paymentId, String signature) {
        if (!isPayHereConfigured() || !merchantId.equals(notifiedMerchantId)
                || !PayHereSignature.verifyNotification(
                merchantId, orderId, amount, currency, statusCode, signature, merchantSecret)) {
            throw new PaymentWorkflowException("Invalid PayHere payment notification signature.");
        }
        PaymentModel payment = paymentRepository.findByProviderOrderReference(orderId)
                .orElseThrow(() -> new PaymentWorkflowException("PayHere notification order was not found."));
        if (payment.getPaymentMethod() != PaymentMethod.CARD || payment.getBooking() == null) {
            throw new PaymentWorkflowException("PayHere notification is not linked to a card checkout.");
        }
        if (!payment.getCurrency().equals(currency) || !matchesAmount(payment.getAmount(), amount)) {
            throw new PaymentWorkflowException("PayHere notification amount or currency does not match the booking.");
        }
        BookingModel booking = payment.getBooking();
        switch (statusCode) {
            case "2" -> {
                if (payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                    payment.setTransactionReference(trimToLength(paymentId, 100));
                    bookingServices.updateBookingStatus(booking, BookingStatus.PAID);
                }
            }
            case "-1", "-2" -> {
                if (payment.getPaymentStatus() == PaymentStatus.PENDING) {
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                    bookingServices.updateBookingStatus(booking, BookingStatus.AWAITING_PAYMENT);
                }
            }
            case "-3" -> {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                bookingServices.updateBookingStatus(booking, BookingStatus.AWAITING_PAYMENT);
            }
            case "0" -> {
                // PayHere reports a pending result; keep the payment pending until a final notification arrives.
            }
            default -> throw new PaymentWorkflowException("Unrecognized PayHere payment status.");
        }
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public BookingModel submitBankTransfer(String reference, String transactionReference, MultipartFile receipt) {
        BookingModel booking = bookingServices.getBooking(reference);
        ensurePayable(booking);
        PaymentModel payment = findOrCreatePayment(booking);
        if (payment.getPaymentStatus() == PaymentStatus.COMPLETED) {
            throw new PaymentWorkflowException("This booking has already been paid.");
        }
        if (payment.getProviderOrderReference() != null) {
            payment.setProviderOrderReference(null);
        }
        ReceiptStorage.StoredReceipt stored = receiptStorage.store(receipt);
        String previousReceipt = payment.getReceiptStorageKey();
        payment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDate.now());
        payment.setTransactionReference(trimToLength(transactionReference, 100));
        payment.setReceiptStorageKey(stored.key());
        payment.setReceiptOriginalName(stored.originalName());
        booking.setStatus(BookingStatus.TRANSFER_REVIEW);
        try {
            paymentRepository.saveAndFlush(payment);
            bookingRepository.save(booking);
        } catch (RuntimeException exception) {
            receiptStorage.delete(stored.key());
            throw exception;
        }
        if (previousReceipt != null) {
            receiptStorage.delete(previousReceipt);
        }
        return booking;
    }

    @Override
    @Transactional
    public void verifyBankTransfer(Long paymentId) {
        PaymentModel payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentWorkflowException("Payment record was not found."));
        if (payment.getPaymentMethod() != PaymentMethod.BANK_TRANSFER
                || payment.getReceiptStorageKey() == null
                || payment.getPaymentStatus() != PaymentStatus.PENDING
                || payment.getBooking() == null) {
            throw new PaymentWorkflowException("Only a pending bank transfer with a receipt can be verified.");
        }
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
        bookingServices.updateBookingStatus(payment.getBooking(), BookingStatus.PAID);
    }

    @Override
    @Transactional
    public void rejectBankTransfer(Long paymentId, String reason) {
        PaymentModel payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentWorkflowException("Payment record was not found."));
        if (payment.getPaymentMethod() != PaymentMethod.BANK_TRANSFER
                || payment.getReceiptStorageKey() == null
                || payment.getPaymentStatus() != PaymentStatus.PENDING
                || payment.getBooking() == null) {
            throw new PaymentWorkflowException("Only a pending bank transfer with a receipt can be rejected.");
        }
        String rejectedReceipt = payment.getReceiptStorageKey();
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setNotes(trimToLength(reason, 2000));
        payment.setReceiptStorageKey(null);
        payment.setReceiptOriginalName(null);
        paymentRepository.save(payment);
        bookingServices.updateBookingStatus(payment.getBooking(), BookingStatus.AWAITING_PAYMENT);
        receiptStorage.delete(rejectedReceipt);
    }

    @Override
    public ReceiptFile getReceipt(Long paymentId) {
        PaymentModel payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentWorkflowException("Payment record was not found."));
        if (payment.getReceiptStorageKey() == null) {
            throw new PaymentWorkflowException("This payment has no uploaded transfer receipt.");
        }
        return receiptStorage.load(payment.getReceiptStorageKey(), payment.getReceiptOriginalName());
    }

    private PaymentModel findOrCreatePayment(BookingModel booking) {
        PaymentModel payment = paymentRepository.findByBookingReference(booking.getBookingReference()).orElseGet(() -> {
            PaymentModel created = new PaymentModel();
            created.setBookingReference(booking.getBookingReference());
            created.setBooking(booking);
            created.setCustomerName(booking.getCustomerName());
            created.setCustomerEmail(booking.getCustomerEmail());
            created.setDescription(booking.getVendorName());
            created.setAmount(booking.getQuotedAmount());
            created.setCurrency(booking.getCurrency());
            created.setPaymentStatus(PaymentStatus.PENDING);
            created.setPaymentDate(LocalDate.now());
            return created;
        });
        payment.setBooking(booking);
        payment.setAmount(booking.getQuotedAmount());
        payment.setCurrency(booking.getCurrency());
        return payment;
    }

    private void ensurePayable(BookingModel booking) {
        if (booking.getStatus() == BookingStatus.PAID) {
            throw new PaymentWorkflowException("This booking has already been paid.");
        }
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT || booking.getQuotedAmount() == null
                || booking.getQuotedAmount().signum() <= 0) {
            throw new PaymentWorkflowException("The booking is not ready for payment. Please wait for the admin quote.");
        }
    }

    private void ensurePayHereConfigured() {
        if (merchantId.isBlank() || merchantSecret.isBlank()) {
            throw new PaymentWorkflowException("Card checkout is not configured. The site administrator must add PayHere sandbox credentials.");
        }
        if (!hasPublicNotifyUrl()) {
            throw new PaymentWorkflowException("Card checkout needs a public HTTPS callback URL. Configure WEDPLAN_PUBLIC_BASE_URL (for local testing use an HTTPS tunnel).");
        }
    }

    private boolean hasPublicNotifyUrl() {
        try {
            java.net.URI uri = java.net.URI.create(publicBaseUrl);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && !uri.getHost().equalsIgnoreCase("localhost")
                    && !uri.getHost().equals("127.0.0.1")
                    && !uri.getHost().endsWith(".local");
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String publicUrl(String path) {
        return UriComponentsBuilder.fromUriString(publicBaseUrl).path(path).build().toUriString();
    }

    private boolean matchesAmount(BigDecimal expected, String actual) {
        try {
            return expected.compareTo(new BigDecimal(actual)) == 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private String firstName(String fullName) {
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts[0];
    }

    private String lastName(String fullName) {
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : parts[0];
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() > maxLength ? trimmed.substring(0, maxLength) : trimmed;
    }
}
