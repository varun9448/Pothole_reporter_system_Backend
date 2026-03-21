package com.example.pothole.config;

import com.example.pothole.Entity.Zone;

public class ZoneBoundary {
    private final double centerLatitude;
    private final double centerLongitude;
    private final double radiusKm;
    private final Zone zone;
    private final int priority;

    public ZoneBoundary(double centerLatitude, double centerLongitude, double radiusKm, Zone zone, int priority) {
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.radiusKm = radiusKm;
        this.zone = zone;
        this.priority = priority;
    }

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    public double getRadiusKm() {
        return radiusKm;
    }

    public Zone getZone() {
        return zone;
    }

    public int getPriority() {
        return priority;
    }
}
