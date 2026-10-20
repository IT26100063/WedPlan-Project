package com.wedplan.wedplan.services.paymentMgt;

import com.wedplan.wedplan.model.paymentMgt.PaymentMethod;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentServices {
    Page<PaymentModel> findPayments(String search, PaymentStatus status, PaymentMethod method, Pageable pageable);

    PaymentModel getPayment(Long id);

    PaymentSummary getSummary();

    PaymentModel createPayment(PaymentModel payment);

    PaymentModel updatePayment(Long id, PaymentModel payment);

    void deletePayment(Long id);
}
