package com.geoquiz.controller;

import com.geoquiz.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final CountryRepository countryRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("countries", countryRepository.findAll());
        return "index";
    }
}
