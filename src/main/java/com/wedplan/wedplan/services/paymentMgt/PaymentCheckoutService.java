package com.wedplan.wedplan.services.paymentMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import org.springframework.web.multipart.MultipartFile;

public interface PaymentCheckoutService {
    BookingModel getBooking(String reference);

    PaymentModel getPaymentForBooking(String reference);

    PayHereCheckoutData beginCardCheckout(String reference);

    void acceptPayHereNotification(String merchantId, String orderId, String amount,
                                   String currency, String statusCode, String paymentId, String signature);

    BookingModel submitBankTransfer(String reference, String transactionReference, MultipartFile receipt);

    void verifyBankTransfer(Long paymentId);

    void rejectBankTransfer(Long paymentId, String reason);

    ReceiptFile getReceipt(Long paymentId);

    boolean isPayHereConfigured();
}
