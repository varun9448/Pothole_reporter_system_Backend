package com.example.pothole.dto;

import com.example.pothole.Entity.Zone;
import jakarta.validation.constraints.NotNull;

public class AdminZoneUpdateRequest {
    @NotNull
    private Zone zone;

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }
}
