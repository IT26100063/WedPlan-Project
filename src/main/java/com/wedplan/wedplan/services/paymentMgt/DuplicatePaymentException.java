package com.wedplan.wedplan.services.paymentMgt;

public class DuplicatePaymentException extends RuntimeException {
    private final String field;

    public DuplicatePaymentException(String field, String message) {
        super(message);
        this.field = field;
    }

    public DuplicatePaymentException(String message) {
        this(null, message);
    }

    public String getField() {
        return field;
    }
}
