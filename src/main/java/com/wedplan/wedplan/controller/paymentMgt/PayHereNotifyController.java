package com.wedplan.wedplan.controller.paymentMgt;

import com.wedplan.wedplan.services.paymentMgt.PaymentCheckoutService;
import com.wedplan.wedplan.services.paymentMgt.PaymentWorkflowException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PayHereNotifyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PayHereNotifyController.class);
    private final PaymentCheckoutService checkoutService;

    public PayHereNotifyController(PaymentCheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/payhere/notify")
    public ResponseEntity<Void> notify(
            @RequestParam(name = "merchant_id", required = false) String merchantId,
            @RequestParam(name = "order_id", required = false) String orderId,
            @RequestParam(name = "payhere_amount", required = false) String amount,
            @RequestParam(name = "payhere_currency", required = false) String currency,
            @RequestParam(name = "status_code", required = false) String statusCode,
            @RequestParam(name = "payment_id", required = false) String paymentId,
            @RequestParam(name = "md5sig", required = false) String signature) {
        if (merchantId == null || orderId == null || amount == null || currency == null
                || statusCode == null || signature == null) {
            LOGGER.warn("Rejected incomplete PayHere notification for order {}", orderId);
            return ResponseEntity.badRequest().build();
        }
        try {
            checkoutService.acceptPayHereNotification(
                    merchantId, orderId, amount, currency, statusCode, paymentId, signature);
            return ResponseEntity.ok().build();
        } catch (PaymentWorkflowException exception) {
            LOGGER.warn("Rejected PayHere notification for order {}: {}", orderId, exception.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
