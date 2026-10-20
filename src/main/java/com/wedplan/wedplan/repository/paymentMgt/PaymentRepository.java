package com.wedplan.wedplan.repository.paymentMgt;

import com.wedplan.wedplan.model.paymentMgt.PaymentMethod;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentRepository extends JpaRepository<PaymentModel, Long>, JpaSpecificationExecutor<PaymentModel> {
    boolean existsByBookingReferenceIgnoreCase(String bookingReference);

    boolean existsByBookingReferenceIgnoreCaseAndIdNot(String bookingReference, Long id);

    boolean existsByTransactionReferenceIgnoreCase(String transactionReference);

    boolean existsByTransactionReferenceIgnoreCaseAndIdNot(String transactionReference, Long id);

    long countByPaymentStatus(PaymentStatus paymentStatus);
}
