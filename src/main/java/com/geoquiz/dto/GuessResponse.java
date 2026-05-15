package com.geoquiz.dto;

public record GuessResponse(
    boolean correct,
    double distanceKm,
    String bearing,
    String color,
    String countryName,
    String flagUrl
) {}
