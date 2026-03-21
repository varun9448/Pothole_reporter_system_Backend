package com.example.pothole.service;

import com.example.pothole.Entity.Zone;
import com.example.pothole.config.ZoneBoundary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class ZoneDetectionService {

    private static final List<ZoneBoundary> ZONE_BOUNDARIES = Arrays.asList(
            new ZoneBoundary(12.9784, 77.5720, 5.0, Zone.CENTRAL, 100),
            new ZoneBoundary(12.9279, 77.5938, 8.0, Zone.SOUTH, 90),
            new ZoneBoundary(13.1020, 77.5965, 10.0, Zone.NORTH, 80),
            new ZoneBoundary(12.9916, 77.5554, 8.0, Zone.WEST, 70),
            new ZoneBoundary(12.9698, 77.7499, 10.0, Zone.EAST, 60)
    );

    public Zone detectZone(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return Zone.OUTSKIRTS;
        }

        ZoneBoundary bestMatch = null;
        for (ZoneBoundary boundary : ZONE_BOUNDARIES) {
            double distanceKm = distanceKm(latitude.doubleValue(), longitude.doubleValue(),
                    boundary.getCenterLatitude(), boundary.getCenterLongitude());
            if (distanceKm <= boundary.getRadiusKm()) {
                if (bestMatch == null || boundary.getPriority() > bestMatch.getPriority()) {
                    bestMatch = boundary;
                }
            }
        }

        return bestMatch != null ? bestMatch.getZone() : Zone.OUTSKIRTS;
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }
}
