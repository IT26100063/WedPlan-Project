package com.wedplan.wedplan.paymentMgt.repository;

import com.wedplan.wedplan.paymentMgt.model.PaymentMethod;
import com.wedplan.wedplan.paymentMgt.model.PaymentModel;
import com.wedplan.wedplan.paymentMgt.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentModel, Long>, JpaSpecificationExecutor<PaymentModel> {
    boolean existsByBookingReferenceIgnoreCase(String bookingReference);

    boolean existsByBookingReferenceIgnoreCaseAndIdNot(String bookingReference, Long id);

    boolean existsByTransactionReferenceIgnoreCase(String transactionReference);

    boolean existsByTransactionReferenceIgnoreCaseAndIdNot(String transactionReference, Long id);

    long countByPaymentStatus(PaymentStatus paymentStatus);

    long countByPaymentMethod(PaymentMethod paymentMethod);

    List<PaymentModel> findByBookingReferenceIgnoreCase(String bookingReference);

    List<PaymentModel> findByCustomerEmailIgnoreCase(String customerEmail);

    Optional<PaymentModel> findByReceiptNumberIgnoreCase(String receiptNumber);

    Optional<PaymentModel> findByTransactionReferenceIgnoreCase(String transactionReference);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentModel p WHERE p.paymentStatus = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentModel p")
    BigDecimal sumTotalAmount();
}
