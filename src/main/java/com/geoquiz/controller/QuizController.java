package com.geoquiz.controller;

import com.geoquiz.dto.GuessResponse;
import com.geoquiz.model.Country;
import com.geoquiz.repository.CountryRepository;
import com.geoquiz.service.CountryService;
import com.geoquiz.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final CountryRepository countryRepository;
    private final GeoService geoService;
    private final CountryService countryService;

    @GetMapping("/guess")
    public ResponseEntity<GuessResponse> makeGuess(
            @RequestParam Long guessedId,
            @RequestParam Long targetId) {

        Country guessed = countryRepository.findById(guessedId)
                .orElseThrow(() -> new RuntimeException("País palpite não encontrado"));
        
        Country target = countryRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("País objetivo não encontrado"));

        double distance = geoService.calculateDistance(
                guessed.getLatitude(), guessed.getLongitude(),
                target.getLatitude(), target.getLongitude()
        );

        String bearing = geoService.calculateBearing(
                guessed.getLatitude(), guessed.getLongitude(),
                target.getLatitude(), target.getLongitude()
        );

        String color = geoService.getProximityColor(distance);
        boolean correct = guessedId.equals(targetId);

        GuessResponse response = new GuessResponse(
                correct,
                Math.round(distance * 100.0) / 100.0,
                bearing,
                color,
                guessed.getName(),
                countryService.getFlagUrl(guessed.getIsoAlpha2())
        );

        return ResponseEntity.ok(response);
    }
}
