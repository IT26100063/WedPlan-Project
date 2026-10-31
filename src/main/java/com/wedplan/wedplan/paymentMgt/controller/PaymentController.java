package com.wedplan.wedplan.paymentMgt.controller;

import com.wedplan.wedplan.paymentMgt.model.PaymentCalculationResult;
import com.wedplan.wedplan.paymentMgt.model.PaymentMethod;
import com.wedplan.wedplan.paymentMgt.model.PaymentModel;
import com.wedplan.wedplan.paymentMgt.model.PaymentStatus;
import com.wedplan.wedplan.paymentMgt.model.UserCheckoutRequest;
import com.wedplan.wedplan.paymentMgt.service.DuplicatePaymentException;
import com.wedplan.wedplan.paymentMgt.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String listPayments(
            @RequestParam(value = "q", required = false) String search,
            @RequestParam(value = "paymentStatus", required = false) PaymentStatus paymentStatus,
            @RequestParam(value = "paymentMethod", required = false) PaymentMethod paymentMethod,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        int validPage = Math.max(0, page);
        int validSize = (size > 0 && size <= 100) ? size : 10;
        Pageable pageable = PageRequest.of(validPage, validSize, Sort.by(Sort.Direction.DESC, "paymentDate", "id"));

        Page<PaymentModel> payments = paymentService.findPayments(search, paymentStatus, paymentMethod, pageable);

        model.addAttribute("payments", payments);
        model.addAttribute("summary", paymentService.getSummary());
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("q", search);
        model.addAttribute("selectedStatus", paymentStatus);
        model.addAttribute("selectedMethod", paymentMethod);

        return "paymentMgt/list";
    }

    @GetMapping("/new")
    public String newPaymentForm(Model model) {
        PaymentModel payment = new PaymentModel();
        payment.setCurrency("LKR");
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDate.now());

        model.addAttribute("payment", payment);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("editing", false);

        return "paymentMgt/form";
    }

    @PostMapping
    public String savePayment(
            @Valid @ModelAttribute("payment") PaymentModel payment,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("statuses", PaymentStatus.values());
            model.addAttribute("methods", PaymentMethod.values());
            model.addAttribute("editing", false);
            return "paymentMgt/form";
        }

        try {
            PaymentModel saved = paymentService.createPayment(payment);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment of " + saved.getCurrency() + " " + saved.getAmount()
                            + " for booking " + saved.getBookingReference() + " recorded successfully."
            );
            return "redirect:/payments";
        } catch (DuplicatePaymentException ex) {
            if (ex.getField() != null) {
                bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            } else {
                bindingResult.reject("duplicate", ex.getMessage());
            }
            model.addAttribute("statuses", PaymentStatus.values());
            model.addAttribute("methods", PaymentMethod.values());
            model.addAttribute("editing", false);
            return "paymentMgt/form";
        }
    }

    @GetMapping("/{id}")
    public String viewPayment(@PathVariable Long id, Model model) {
        PaymentModel payment = paymentService.getPayment(id);
        model.addAttribute("payment", payment);
        return "paymentMgt/details";
    }

    @GetMapping("/{id}/edit")
    public String editPaymentForm(@PathVariable Long id, Model model) {
        PaymentModel payment = paymentService.getPayment(id);
        model.addAttribute("payment", payment);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("editing", true);
        return "paymentMgt/form";
    }

    @PostMapping("/{id}")
    public String updatePayment(
            @PathVariable Long id,
            @Valid @ModelAttribute("payment") PaymentModel payment,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            payment.setId(id);
            model.addAttribute("statuses", PaymentStatus.values());
            model.addAttribute("methods", PaymentMethod.values());
            model.addAttribute("editing", true);
            return "paymentMgt/form";
        }

        try {
            PaymentModel saved = paymentService.updatePayment(id, payment);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment for booking " + saved.getBookingReference() + " updated successfully."
            );
            return "redirect:/payments/" + id;
        } catch (DuplicatePaymentException ex) {
            payment.setId(id);
            if (ex.getField() != null) {
                bindingResult.rejectValue(ex.getField(), "duplicate", ex.getMessage());
            } else {
                bindingResult.reject("duplicate", ex.getMessage());
            }
            model.addAttribute("statuses", PaymentStatus.values());
            model.addAttribute("methods", PaymentMethod.values());
            model.addAttribute("editing", true);
            return "paymentMgt/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        PaymentModel payment = paymentService.getPayment(id);
        paymentService.deletePayment(id);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Payment record for booking " + payment.getBookingReference() + " has been deleted."
        );
        return "redirect:/payments";
    }

    // --- NEW ACTIONS: VERIFY, REFUND, RECEIPT, EXPORT ---

    @PostMapping("/{id}/verify")
    public String verifyPayment(
            @PathVariable Long id,
            @RequestParam(value = "notes", required = false, defaultValue = "Bank transfer verified by finance admin") String notes,
            RedirectAttributes redirectAttributes) {
        PaymentModel payment = paymentService.verifyPayment(id, notes);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Payment for booking " + payment.getBookingReference() + " verified and marked COMPLETED."
        );
        return "redirect:/payments/" + id;
    }

    @PostMapping("/{id}/refund")
    public String refundPayment(
            @PathVariable Long id,
            @RequestParam(value = "reason", required = false, defaultValue = "Customer requested cancellation") String reason,
            RedirectAttributes redirectAttributes) {
        PaymentModel payment = paymentService.refundPayment(id, reason);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Payment for booking " + payment.getBookingReference() + " has been marked REFUNDED."
        );
        return "redirect:/payments/" + id;
    }

    @GetMapping("/{id}/receipt")
    public String viewReceipt(@PathVariable Long id, Model model) {
        PaymentModel payment = paymentService.getPayment(id);
        model.addAttribute("payment", payment);
        return "paymentMgt/receipt";
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csv = paymentService.exportPaymentsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"wedplan-payments-" + System.currentTimeMillis() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    // --- USER-SIDE PAYMENT PROCESS & HISTORY ---

    @GetMapping("/checkout")
    public String userCheckoutForm(
            @RequestParam(value = "booking", required = false, defaultValue = "BK2041") String bookingRef,
            @RequestParam(value = "type", required = false, defaultValue = "DEPOSIT") String type,
            Model model) {
        UserCheckoutRequest request = new UserCheckoutRequest();
        request.setBookingReference(bookingRef);
        request.setPaymentType(type);
        request.setBaseAmount(new BigDecimal("873000.00"));
        request.setCustomerName("Nethmi & Kavin");
        request.setCustomerEmail("nethmi@example.com");
        request.setVendorName("The Garden House");
        request.setPackageName("Classic Venue Package");
        request.setPlusMember(true);
        request.setPaymentMethod(PaymentMethod.CARD);

        PaymentCalculationResult calc = paymentService.calculateCheckout(
                request.getBookingReference(),
                request.getPaymentType(),
                request.getPaymentMethod(),
                request.isPlusMember(),
                request.getBaseAmount()
        );

        model.addAttribute("checkout", request);
        model.addAttribute("calc", calc);
        model.addAttribute("methods", PaymentMethod.values());

        return "paymentMgt/checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(
            @Valid @ModelAttribute("checkout") UserCheckoutRequest checkoutRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            PaymentCalculationResult calc = paymentService.calculateCheckout(
                    checkoutRequest.getBookingReference(),
                    checkoutRequest.getPaymentType(),
                    checkoutRequest.getPaymentMethod(),
                    checkoutRequest.isPlusMember(),
                    checkoutRequest.getBaseAmount()
            );
            model.addAttribute("calc", calc);
            model.addAttribute("methods", PaymentMethod.values());
            return "paymentMgt/checkout";
        }

        PaymentModel saved = paymentService.processUserCheckout(checkoutRequest);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Your " + saved.getPaymentType() + " payment of " + saved.getCurrency() + " "
                        + saved.getAmount() + " was processed successfully! Receipt: " + saved.getReceiptNumber()
        );

        return "redirect:/payments/" + saved.getId() + "/receipt";
    }

    @GetMapping("/my-payments")
    public String myPayments(
            @RequestParam(value = "email", required = false, defaultValue = "nethmi@example.com") String email,
            Model model) {
        List<PaymentModel> userPayments = paymentService.getPaymentsByCustomer(email);
        model.addAttribute("payments", userPayments);
        model.addAttribute("email", email);
        return "paymentMgt/my-payments";
    }
}
