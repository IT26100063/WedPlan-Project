package com.wedplan.wedplan.controller.paymentMgt;

import com.wedplan.wedplan.model.paymentMgt.PaymentMethod;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentStatus;
import com.wedplan.wedplan.services.paymentMgt.DuplicatePaymentException;
import com.wedplan.wedplan.services.paymentMgt.PaymentServices;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentServices paymentServices;

    public PaymentController(PaymentServices paymentServices) {
        this.paymentServices = paymentServices;
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

        Page<PaymentModel> payments = paymentServices.findPayments(search, paymentStatus, paymentMethod, pageable);

        model.addAttribute("payments", payments);
        model.addAttribute("summary", paymentServices.getSummary());
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
            PaymentModel saved = paymentServices.createPayment(payment);
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
        PaymentModel payment = paymentServices.getPayment(id);
        model.addAttribute("payment", payment);
        return "paymentMgt/details";
    }

    @GetMapping("/{id}/edit")
    public String editPaymentForm(@PathVariable Long id, Model model) {
        PaymentModel payment = paymentServices.getPayment(id);
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
            PaymentModel updated = paymentServices.updatePayment(id, payment);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Payment record for booking " + updated.getBookingReference() + " updated successfully."
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
        paymentServices.deletePayment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Payment record removed successfully.");
        return "redirect:/payments";
    }
}
