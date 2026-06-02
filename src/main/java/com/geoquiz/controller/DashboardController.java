package com.geoquiz.controller;

import com.geoquiz.model.User;
import com.geoquiz.repository.ConqueredCountryRepository;
import com.geoquiz.repository.CountryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ConqueredCountryRepository conqueredCountryRepository;
    private final CountryRepository countryRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String gameMode,
            @AuthenticationPrincipal com.geoquiz.security.CustomUserDetails userDetails) {
        // Tratar "all" como null para o filtro do repositório
        String filterMode = (gameMode == null || gameMode.equalsIgnoreCase("all")) ? null : gameMode;

        if (userDetails == null || userDetails.getUser() == null) return ResponseEntity.notFound().build();
        User user = userDetails.getUser();

        long totalCountries = countryRepository.count();
        long conqueredUnique = conqueredCountryRepository.countUniqueCountriesByUserAndGameMode(user, filterMode);
        
        // Dados por Continente
        List<Object[]> continentDataRaw = conqueredCountryRepository.countByContinentForUserAndGameMode(user, filterMode);
        List<Map<String, Object>> continentStats = new ArrayList<>();
        for (Object[] row : continentDataRaw) {
            continentStats.add(Map.of(
                "continent", row[0] != null ? row[0] : "Desconhecido",
                "count", row[1]
            ));
        }

        // Média real de todas as tentativas
        List<com.geoquiz.model.ConqueredCountry> allConquests = conqueredCountryRepository.findByUserAndGameMode(user, filterMode);
        double avgAttempts = allConquests.stream()
            .mapToInt(com.geoquiz.model.ConqueredCountry::getAttempts)
            .average()
            .orElse(0.0);

        // Top 10 Países (por mais acertos)
        List<Map<String, Object>> topCountries = new ArrayList<>();
        List<Object[]> topCountriesRaw = conqueredCountryRepository.findTopCountriesByAcertos(user, filterMode);
        java.util.Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale();
        topCountriesRaw.stream()
            .limit(10)
            .forEach(row -> {
                String iso2 = (String) row[0];
                String defaultName = (String) row[1];
                String localizedName = defaultName;
                if (iso2 != null && !iso2.isEmpty()) {
                    localizedName = new java.util.Locale("", iso2).getDisplayCountry(locale);
                }
                topCountries.add(Map.of(
                    "name", localizedName != null ? localizedName : "Desconhecido",
                    "acertos", row[2]
                ));
            });

        // Códigos ISO para o mapa
        List<String> conqueredIsoCodes = conqueredCountryRepository.findConqueredIsoCodesByUserAndGameMode(user, filterMode);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWorldCountries", totalCountries);
        stats.put("conqueredUnique", conqueredUnique);
        stats.put("conqueredPercentage", totalCountries > 0 ? (double) conqueredUnique / totalCountries * 100 : 0);
        stats.put("averageAttempts", avgAttempts);
        stats.put("continentStats", continentStats);
        stats.put("topCountries", topCountries);
        stats.put("conqueredIsoCodes", conqueredIsoCodes);

        return ResponseEntity.ok(stats);
    }
}
