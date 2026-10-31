package com.wedplan.wedplan.paymentMgt.service;

import java.math.BigDecimal;

public record PaymentSummary(
        long total,
        long completed,
        long pending,
        long failed,
        long refunded,
        BigDecimal totalRevenue,
        BigDecimal pendingRevenue,
        BigDecimal refundedRevenue
) {
    public PaymentSummary(long total, long completed, long pending, long failed) {
        this(total, completed, pending, failed, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
