package com.wedplan.wedplan.paymentMgt.model;

import java.math.BigDecimal;

public class PaymentCalculationResult {
    private String bookingReference;
    private String paymentType;
    private BigDecimal baseAmount;
    private int installmentPercentage;
    private BigDecimal installmentAmount;
    private BigDecimal plusDiscountRate;
    private BigDecimal plusDiscountAmount;
    private BigDecimal bankDiscountRate;
    private BigDecimal bankDiscountAmount;
    private BigDecimal totalDiscountAmount;
    private BigDecimal finalPayableAmount;
    private String formula;

    public PaymentCalculationResult() {
    }

    public PaymentCalculationResult(
            String bookingReference,
            String paymentType,
            BigDecimal baseAmount,
            int installmentPercentage,
            BigDecimal installmentAmount,
            BigDecimal plusDiscountRate,
            BigDecimal plusDiscountAmount,
            BigDecimal bankDiscountRate,
            BigDecimal bankDiscountAmount,
            BigDecimal totalDiscountAmount,
            BigDecimal finalPayableAmount,
            String formula) {
        this.bookingReference = bookingReference;
        this.paymentType = paymentType;
        this.baseAmount = baseAmount;
        this.installmentPercentage = installmentPercentage;
        this.installmentAmount = installmentAmount;
        this.plusDiscountRate = plusDiscountRate;
        this.plusDiscountAmount = plusDiscountAmount;
        this.bankDiscountRate = bankDiscountRate;
        this.bankDiscountAmount = bankDiscountAmount;
        this.totalDiscountAmount = totalDiscountAmount;
        this.finalPayableAmount = finalPayableAmount;
        this.formula = formula;
    }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public int getInstallmentPercentage() { return installmentPercentage; }
    public void setInstallmentPercentage(int installmentPercentage) { this.installmentPercentage = installmentPercentage; }

    public BigDecimal getInstallmentAmount() { return installmentAmount; }
    public void setInstallmentAmount(BigDecimal installmentAmount) { this.installmentAmount = installmentAmount; }

    public BigDecimal getPlusDiscountRate() { return plusDiscountRate; }
    public void setPlusDiscountRate(BigDecimal plusDiscountRate) { this.plusDiscountRate = plusDiscountRate; }

    public BigDecimal getPlusDiscountAmount() { return plusDiscountAmount; }
    public void setPlusDiscountAmount(BigDecimal plusDiscountAmount) { this.plusDiscountAmount = plusDiscountAmount; }

    public BigDecimal getBankDiscountRate() { return bankDiscountRate; }
    public void setBankDiscountRate(BigDecimal bankDiscountRate) { this.bankDiscountRate = bankDiscountRate; }

    public BigDecimal getBankDiscountAmount() { return bankDiscountAmount; }
    public void setBankDiscountAmount(BigDecimal bankDiscountAmount) { this.bankDiscountAmount = bankDiscountAmount; }

    public BigDecimal getTotalDiscountAmount() { return totalDiscountAmount; }
    public void setTotalDiscountAmount(BigDecimal totalDiscountAmount) { this.totalDiscountAmount = totalDiscountAmount; }

    public BigDecimal getFinalPayableAmount() { return finalPayableAmount; }
    public void setFinalPayableAmount(BigDecimal finalPayableAmount) { this.finalPayableAmount = finalPayableAmount; }

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
}
