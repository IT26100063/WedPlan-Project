package com.wedplan.wedplan.services.paymentMgt;

public class PaymentWorkflowException extends RuntimeException {
    public PaymentWorkflowException(String message) {
        super(message);
    }

    public PaymentWorkflowException(String message, Throwable cause) {
        super(message, cause);
    }
}
