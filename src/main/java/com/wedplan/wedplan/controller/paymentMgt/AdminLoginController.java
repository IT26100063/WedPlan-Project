package com.wedplan.wedplan.controller.paymentMgt;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminLoginController {
    private final boolean adminConfigured;

    public AdminLoginController(@Value("${wedplan.admin.username:}") String username,
                                @Value("${wedplan.admin.password:}") String password) {
        this.adminConfigured = !username.isBlank() && !password.isBlank();
    }

    @GetMapping("/admin/login")
    public String login(Model model) {
        model.addAttribute("adminConfigured", adminConfigured);
        return "paymentMgt/admin-login";
    }
}
