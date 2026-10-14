package com.wedplan.wedplan.services.paymentMgt;

import com.wedplan.wedplan.model.paymentMgt.PaymentMethod;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentStatus;
import com.wedplan.wedplan.repository.paymentMgt.PaymentRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class PaymentServicesImpl implements PaymentServices {
    private final PaymentRepository paymentRepository;

    public PaymentServicesImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Page<PaymentModel> findPayments(String search, PaymentStatus status, PaymentMethod method, Pageable pageable) {
        String normalizedSearch = search == null || search.isBlank()
                ? null
                : "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

        Specification<PaymentModel> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentStatus"), status));
            }
            if (method != null) {
                predicates.add(criteriaBuilder.equal(root.get("paymentMethod"), method));
            }
            if (normalizedSearch != null) {
                String pattern = normalizedSearch;
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("bookingReference")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("customerEmail")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(
                                root.get("description").as(String.class), "")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(criteriaBuilder.coalesce(
                                root.get("transactionReference").as(String.class), "")), pattern)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
        return paymentRepository.findAll(specification, pageable);
    }

    @Override
    public PaymentModel getPayment(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    @Override
    public PaymentSummary getSummary() {
        return new PaymentSummary(
                paymentRepository.count(),
                paymentRepository.countByPaymentStatus(PaymentStatus.COMPLETED),
                paymentRepository.countByPaymentStatus(PaymentStatus.PENDING),
                paymentRepository.countByPaymentStatus(PaymentStatus.FAILED)
        );
    }

    @Override
    @Transactional
    public PaymentModel createPayment(PaymentModel payment) {
        normalize(payment);
        ensureUniqueReferences(payment, null);
        try {
            return paymentRepository.saveAndFlush(payment);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicatePaymentException("Booking or transaction reference is already in use.");
        }
    }

    @Override
    @Transactional
    public PaymentModel updatePayment(Long id, PaymentModel payment) {
        PaymentModel existing = getPayment(id);
        normalize(payment);
        ensureUniqueReferences(payment, id);

        existing.setBookingReference(payment.getBookingReference());
        existing.setCustomerName(payment.getCustomerName());
        existing.setCustomerEmail(payment.getCustomerEmail());
        existing.setDescription(payment.getDescription());
        existing.setAmount(payment.getAmount());
        existing.setCurrency(payment.getCurrency());
        existing.setPaymentMethod(payment.getPaymentMethod());
        existing.setPaymentStatus(payment.getPaymentStatus());
        existing.setPaymentDate(payment.getPaymentDate());
        existing.setTransactionReference(payment.getTransactionReference());
        existing.setNotes(payment.getNotes());
        try {
            return paymentRepository.saveAndFlush(existing);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicatePaymentException("Booking or transaction reference is already in use.");
        }
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        paymentRepository.delete(getPayment(id));
    }

    private void ensureUniqueReferences(PaymentModel payment, Long existingId) {
        boolean duplicateBooking = existingId == null
                ? paymentRepository.existsByBookingReferenceIgnoreCase(payment.getBookingReference())
                : paymentRepository.existsByBookingReferenceIgnoreCaseAndIdNot(payment.getBookingReference(), existingId);
        if (duplicateBooking) {
            throw new DuplicatePaymentException("bookingReference", "This booking reference is already in use.");
        }
        if (payment.getTransactionReference() != null) {
            boolean duplicateTransaction = existingId == null
                    ? paymentRepository.existsByTransactionReferenceIgnoreCase(payment.getTransactionReference())
                    : paymentRepository.existsByTransactionReferenceIgnoreCaseAndIdNot(payment.getTransactionReference(), existingId);
            if (duplicateTransaction) {
                throw new DuplicatePaymentException("transactionReference", "This transaction reference is already in use.");
            }
        }
    }

    private void normalize(PaymentModel payment) {
        payment.setBookingReference(trimToNull(payment.getBookingReference()));
        payment.setCustomerName(trimToNull(payment.getCustomerName()));
        payment.setCustomerEmail(trimToNull(payment.getCustomerEmail()));
        payment.setDescription(trimToNull(payment.getDescription()));
        payment.setCurrency(payment.getCurrency() == null ? null : payment.getCurrency().trim().toUpperCase(Locale.ROOT));
        payment.setTransactionReference(trimToNull(payment.getTransactionReference()));
        payment.setNotes(trimToNull(payment.getNotes()));
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
