package com.geoquiz.controller;

import com.geoquiz.dto.GuessResponse;
import com.geoquiz.model.ConqueredCountry;
import com.geoquiz.model.Country;
import com.geoquiz.model.User;
import com.geoquiz.repository.ConqueredCountryRepository;
import com.geoquiz.repository.CountryRepository;
import com.geoquiz.repository.UserRepository;
import com.geoquiz.service.CountryService;
import com.geoquiz.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final CountryRepository countryRepository;
    private final CountryService countryService;
    private final GeoService geoService;
    private final ConqueredCountryRepository conqueredCountryRepository;
    private final UserRepository userRepository;

    @GetMapping("/guess")
    public ResponseEntity<GuessResponse> handleGuess(
            @RequestParam Long guessedId,
            @RequestParam Long targetId,
            @RequestParam(required = false, defaultValue = "Globle") String gameMode,
            @RequestParam(required = false, defaultValue = "1") int attempts) {
        
        Country guessed = countryRepository.findById(guessedId)
                .orElseThrow(() -> new RuntimeException("País palpite não encontrado"));
        Country target = countryRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("País alvo não encontrado"));

        double distance = geoService.calculateDistance(
                guessed.getLatitude(), guessed.getLongitude(),
                target.getLatitude(), target.getLongitude()
        );

        String bearing = geoService.calculateBearing(
                guessed.getLatitude(), guessed.getLongitude(),
                target.getLatitude(), target.getLongitude()
        );

        double bearingDegrees = geoService.calculateBearingDegrees(
                guessed.getLatitude(), guessed.getLongitude(),
                target.getLatitude(), target.getLongitude()
        );

        boolean correct = guessedId.equals(targetId);
        
        // Salvar conquista se estiver correto
        if (correct) {
            User user = userRepository.findById(1L).orElse(null);
            if (user != null) {
                ConqueredCountry conquest = conqueredCountryRepository
                    .findByUserAndCountry(user, target)
                    .orElse(new ConqueredCountry());
                
                conquest.setUser(user);
                conquest.setCountry(target);
                conquest.setConqueredAt(LocalDateTime.now());
                conquest.setAttempts(attempts);
                conquest.setGameMode(gameMode);
                conqueredCountryRepository.save(conquest);
            }
        }

        return ResponseEntity.ok(new GuessResponse(
                correct,
                distance,
                bearing,
                bearingDegrees,
                geoService.getProximityColor(distance),
                guessed.getName(),
                countryService.getFlagUrl(guessed.getIsoAlpha2())
        ));
    }

    @GetMapping("/target-details")
    public ResponseEntity<Map<String, Object>> getTargetDetails(@RequestParam Long targetId) {
        Country target = countryRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("País objetivo não encontrado"));

        return ResponseEntity.ok(Map.of(
                "countryName", target.getName(),
                "flagUrl", countryService.getFlagUrl(target.getIsoAlpha2()),
                "geometryJson", target.getGeometryJson() != null ? target.getGeometryJson() : "{}"
        ));
    }
}
