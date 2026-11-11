package com.wedplan.wedplan.controller.paymentMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.model.bookingMgt.BookingStatus;
import com.wedplan.wedplan.model.paymentMgt.PaymentModel;
import com.wedplan.wedplan.services.paymentMgt.PayHereCheckoutData;
import com.wedplan.wedplan.services.paymentMgt.PaymentCheckoutService;
import com.wedplan.wedplan.services.paymentMgt.PaymentWorkflowException;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Validated
public class CheckoutController {
    private final PaymentCheckoutService checkoutService;
    private final String bankName;
    private final String bankAccountName;
    private final String bankAccountNumber;
    private final String bankBranch;

    public CheckoutController(
            PaymentCheckoutService checkoutService,
            @Value("${wedplan.bank.name:}") String bankName,
            @Value("${wedplan.bank.account-name:}") String bankAccountName,
            @Value("${wedplan.bank.account-number:}") String bankAccountNumber,
            @Value("${wedplan.bank.branch:}") String bankBranch) {
        this.checkoutService = checkoutService;
        this.bankName = bankName;
        this.bankAccountName = bankAccountName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankBranch = bankBranch;
    }

    @GetMapping("/checkout/{reference}")
    public String checkout(@PathVariable String reference, Model model) {
        BookingModel booking = checkoutService.getBooking(reference);
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT || booking.getQuotedAmount() == null) {
            return "redirect:/reservations/" + reference;
        }
        model.addAttribute("booking", booking);
        model.addAttribute("payment", checkoutService.getPaymentForBooking(reference));
        model.addAttribute("payHereConfigured", checkoutService.isPayHereConfigured());
        return "paymentMgt/checkout";
    }

    @PostMapping("/checkout/{reference}/card")
    public String cardCheckout(
            @PathVariable String reference,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            PayHereCheckoutData checkout = checkoutService.beginCardCheckout(reference);
            model.addAttribute("checkout", checkout);
            return "paymentMgt/payhere-redirect";
        } catch (PaymentWorkflowException exception) {
            redirectAttributes.addFlashAttribute("checkoutError", exception.getMessage());
            return "redirect:/checkout/" + reference;
        }
    }

    @GetMapping("/checkout/{reference}/bank-transfer")
    public String bankTransfer(@PathVariable String reference, Model model) {
        BookingModel booking = checkoutService.getBooking(reference);
        if (booking.getStatus() != BookingStatus.AWAITING_PAYMENT || booking.getQuotedAmount() == null) {
            return "redirect:/reservations/" + reference;
        }
        model.addAttribute("booking", booking);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankAccountName", bankAccountName);
        model.addAttribute("bankAccountNumber", bankAccountNumber);
        model.addAttribute("bankBranch", bankBranch);
        return "paymentMgt/bank-transfer";
    }

    @PostMapping("/checkout/{reference}/bank-transfer")
    public String submitBankTransfer(
            @PathVariable String reference,
            @RequestParam(required = false) @Size(max = 100) String transactionReference,
            @RequestParam("receipt") MultipartFile receipt,
            RedirectAttributes redirectAttributes) {
        try {
            checkoutService.submitBankTransfer(reference, transactionReference, receipt);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Transfer details submitted. Your booking stays pending until an admin verifies the receipt.");
        } catch (PaymentWorkflowException exception) {
            redirectAttributes.addFlashAttribute("checkoutError", exception.getMessage());
            return "redirect:/checkout/" + reference + "/bank-transfer";
        }
        return "redirect:/reservations/" + reference;
    }

    @GetMapping("/checkout/{reference}/return")
    public String cardReturn(@PathVariable String reference, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("checkoutInfo",
                "You returned from PayHere. The booking is marked paid only after the server verifies PayHere's callback.");
        return "redirect:/reservations/" + reference;
    }

    @GetMapping("/checkout/{reference}/cancel")
    public String cardCancel(@PathVariable String reference, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("checkoutInfo", "Checkout was cancelled. You can try again when ready.");
        return "redirect:/reservations/" + reference;
    }

    @GetMapping("/admin/payments/{paymentId}/receipt")
    public ResponseEntity<?> receipt(@PathVariable Long paymentId) {
        var receipt = checkoutService.getReceipt(paymentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(receipt.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + receipt.originalName().replace("\"", "_") + "\"")
                .body(receipt.resource());
    }
}
