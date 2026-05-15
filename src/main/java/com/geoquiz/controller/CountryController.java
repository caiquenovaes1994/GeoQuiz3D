package com.geoquiz.controller;

import com.geoquiz.repository.CountryRepository;
import com.geoquiz.service.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryRepository countryRepository;
    private final CountryService countryService;

    @GetMapping
    public List<CountryDTO> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(c -> new CountryDTO(
                        c.getId(),
                        c.getName(),
                        c.getIsoAlpha2(),
                        countryService.getFlagUrl(c.getIsoAlpha2()),
                        c.getLatitude(),
                        c.getLongitude()
                ))
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .collect(Collectors.toList());
    }

    public record CountryDTO(Long id, String name, String iso2, String flagUrl, Double lat, Double lon) {}
}
