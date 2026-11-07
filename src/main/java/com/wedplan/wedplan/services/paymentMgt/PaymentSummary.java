package com.wedplan.wedplan.services.paymentMgt;

public record PaymentSummary(long total, long completed, long pending, long failed) {
}
