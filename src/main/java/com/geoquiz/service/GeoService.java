package com.geoquiz.service;

import org.springframework.stereotype.Service;

@Service
public class GeoService {

    private static final double EARTH_RADIUS = 6371; // km

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    public double calculateBearingDegrees(double lat1, double lon1, double lat2, double lon2) {
        double y = Math.sin(Math.toRadians(lon2 - lon1)) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) -
                   Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(lon2 - lon1));
        
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    public String calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double bearing = calculateBearingDegrees(lat1, lon1, lat2, lon2);
        
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        return directions[(int) Math.round(bearing / 45) % 8];
    }

    public String getProximityColor(double distance) {
        // De Verde (0km) até Vermelho (20.000km)
        double maxDistance = 20000;
        double ratio = Math.min(distance / maxDistance, 1.0);
        
        // HSL: 120 (Verde) até 0 (Vermelho)
        int hue = (int) (120 * (1.0 - ratio));
        return "hsl(" + hue + ", 80%, 50%)";
    }
}
