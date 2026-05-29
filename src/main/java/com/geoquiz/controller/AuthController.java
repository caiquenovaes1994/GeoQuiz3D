package com.geoquiz.controller;

import com.geoquiz.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String email, 
                               @RequestParam String password, 
                               Model model) {
        
        // Validação de senha no backend
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) {
            model.addAttribute("error", "A senha não atende aos requisitos mínimos.");
            model.addAttribute("email", email);
            return "register";
        }

        try {
            userService.registerUser(email, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "register";
        }
    }
}
