package com.wedplan.wedplan.paymentMgt.controller;

import com.wedplan.wedplan.paymentMgt.model.PaymentCalculationResult;
import com.wedplan.wedplan.paymentMgt.model.PaymentMethod;
import com.wedplan.wedplan.paymentMgt.model.PaymentModel;
import com.wedplan.wedplan.paymentMgt.model.PaymentStatus;
import com.wedplan.wedplan.paymentMgt.model.UserCheckoutRequest;
import com.wedplan.wedplan.paymentMgt.service.PaymentService;
import com.wedplan.wedplan.paymentMgt.service.PaymentSummary;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentRestController {

    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<Page<PaymentModel>> getAllPayments(
            @RequestParam(value = "q", required = false) String search,
            @RequestParam(value = "status", required = false) PaymentStatus status,
            @RequestParam(value = "method", required = false) PaymentMethod method,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "paymentDate", "id"));
        return ResponseEntity.ok(paymentService.findPayments(search, status, method, pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<PaymentSummary> getPaymentSummary() {
        return ResponseEntity.ok(paymentService.getSummary());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentModel> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PostMapping
    public ResponseEntity<PaymentModel> createPayment(@Valid @RequestBody PaymentModel payment) {
        PaymentModel created = paymentService.createPayment(payment);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentModel> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentModel payment) {
        PaymentModel updated = paymentService.updatePayment(id, payment);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Payment record deleted successfully"));
    }

    // --- NEW STANDARD FEATURES ---

    @PatchMapping("/{id}/verify")
    public ResponseEntity<PaymentModel> verifyPayment(
            @PathVariable Long id,
            @RequestParam(value = "notes", required = false, defaultValue = "Bank transfer verified") String notes) {
        PaymentModel verified = paymentService.verifyPayment(id, notes);
        return ResponseEntity.ok(verified);
    }

    @PatchMapping("/{id}/refund")
    public ResponseEntity<PaymentModel> refundPayment(
            @PathVariable Long id,
            @RequestParam(value = "reason", required = false, defaultValue = "Customer refund requested") String reason) {
        PaymentModel refunded = paymentService.refundPayment(id, reason);
        return ResponseEntity.ok(refunded);
    }

    @PostMapping("/calculate")
    public ResponseEntity<PaymentCalculationResult> calculateAmount(@RequestBody UserCheckoutRequest request) {
        PaymentCalculationResult result = paymentService.calculateCheckout(
                request.getBookingReference(),
                request.getPaymentType(),
                request.getPaymentMethod(),
                request.isPlusMember(),
                request.getBaseAmount()
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/checkout")
    public ResponseEntity<PaymentModel> processUserCheckout(@Valid @RequestBody UserCheckoutRequest request) {
        PaymentModel payment = paymentService.processUserCheckout(request);
        return new ResponseEntity<>(payment, HttpStatus.CREATED);
    }

    @GetMapping("/booking/{bookingReference}")
    public ResponseEntity<List<PaymentModel>> getPaymentsByBooking(@PathVariable String bookingReference) {
        return ResponseEntity.ok(paymentService.getPaymentsByBooking(bookingReference));
    }

    @GetMapping("/customer/{customerEmail}")
    public ResponseEntity<List<PaymentModel>> getPaymentsByCustomer(@PathVariable String customerEmail) {
        return ResponseEntity.ok(paymentService.getPaymentsByCustomer(customerEmail));
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportPaymentsCsv() {
        byte[] csvData = paymentService.exportPaymentsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"wedplan-payments-" + System.currentTimeMillis() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
