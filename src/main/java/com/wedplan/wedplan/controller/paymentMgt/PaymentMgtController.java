package com.wedplan.wedplan.controller.paymentMgt;

import com.wedplan.wedplan.model.paymentMgt.PaymentMethod;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.model.paymentMgt.PaymentStatus;
import com.wedplan.wedplan.services.paymentMgt.DuplicatePaymentException;
import com.wedplan.wedplan.services.paymentMgt.PaymentServices;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class PaymentMgtController {
    private static final int PAGE_SIZE = 10;

    private final PaymentServices paymentServices;

    public PaymentMgtController(PaymentServices paymentServices) {
        this.paymentServices = paymentServices;
    }

    @GetMapping
    public String listPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            Model model,
            RedirectAttributes redirectAttributes) {
        int safePage = Math.max(page, 0);
        Page<PaymentModel> payments = paymentServices.findPayments(
                q, paymentStatus, paymentMethod,
                PageRequest.of(safePage, PAGE_SIZE, Sort.by(
                        Sort.Order.desc("paymentDate"),
                        Sort.Order.desc("id")
                )));
        if (payments.getTotalPages() > 0 && safePage >= payments.getTotalPages()) {
                    redirectAttributes.addAttribute("page", payments.getTotalPages() - 1);
                    if (q != null && !q.isBlank()) {
                        redirectAttributes.addAttribute("q", q);
                    }
                    if (paymentStatus != null) {
                        redirectAttributes.addAttribute("paymentStatus", paymentStatus);
                    }
                    if (paymentMethod != null) {
                        redirectAttributes.addAttribute("paymentMethod", paymentMethod);
                    }
                    return "redirect:/payments";
        }
        model.addAttribute("payments", payments);
        model.addAttribute("summary", paymentServices.getSummary());
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("selectedStatus", paymentStatus);
        model.addAttribute("selectedMethod", paymentMethod);
        return "paymentMgt/list";
    }

    @GetMapping("/new")
    public String newPayment(Model model) {
        PaymentModel payment = new PaymentModel();
        payment.setPaymentDate(LocalDate.now());
        model.addAttribute("payment", payment);
        model.addAttribute("editing", false);
        addFormOptions(model);
        return "paymentMgt/form";
    }

    @PostMapping
    public String createPayment(
            @Valid @ModelAttribute("payment") PaymentModel payment,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showFormWithErrors(model, false);
        }
        try {
            PaymentModel saved = paymentServices.createPayment(payment);
            redirectAttributes.addFlashAttribute("successMessage", "Payment record created.");
            return "redirect:/payments/" + saved.getId();
        } catch (DuplicatePaymentException exception) {
            addDuplicateError(bindingResult, exception);
            return showFormWithErrors(model, false);
        }
    }

    @GetMapping("/{id}")
    public String paymentDetails(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentServices.getPayment(id));
        return "paymentMgt/details";
    }

    @GetMapping("/{id}/edit")
    public String editPayment(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentServices.getPayment(id));
        model.addAttribute("editing", true);
        addFormOptions(model);
        return "paymentMgt/form";
    }

    @PostMapping("/{id}")
    public String updatePayment(
            @PathVariable Long id,
            @Valid @ModelAttribute("payment") PaymentModel payment,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return showFormWithErrors(model, true);
        }
        try {
            paymentServices.updatePayment(id, payment);
            redirectAttributes.addFlashAttribute("successMessage", "Payment record updated.");
            return "redirect:/payments/" + id;
        } catch (DuplicatePaymentException exception) {
            addDuplicateError(bindingResult, exception);
            return showFormWithErrors(model, true);
        }
    }

    @PostMapping("/{id}/delete")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentServices.deletePayment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Payment record deleted.");
        return "redirect:/payments";
    }

    private String showFormWithErrors(Model model, boolean editing) {
        model.addAttribute("editing", editing);
        addFormOptions(model);
        return "paymentMgt/form";
    }

    private void addFormOptions(Model model) {
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
    }

    private void addDuplicateError(BindingResult bindingResult, DuplicatePaymentException exception) {
        if (exception.getField() == null) {
            bindingResult.reject("duplicateReference", exception.getMessage());
        } else {
            bindingResult.rejectValue(exception.getField(), "duplicate", exception.getMessage());
        }
    }
}
