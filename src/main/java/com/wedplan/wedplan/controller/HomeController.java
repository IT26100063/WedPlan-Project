package com.wedplan.wedplan.controller;

import com.wedplan.wedplan.services.bookingMgt.BookingRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(@RequestParam(required = false) String bookingError, Model model) {
        model.addAttribute("bookingRequest", new BookingRequest());
        if (bookingError != null) {
            model.addAttribute("bookingError", bookingError);
        }
        return "index";
    }
}
