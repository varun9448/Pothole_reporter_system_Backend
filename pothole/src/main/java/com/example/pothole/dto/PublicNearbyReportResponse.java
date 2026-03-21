package com.example.pothole.dto;

import com.example.pothole.Entity.Priority;
import com.example.pothole.Entity.ReportStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PublicNearbyReportResponse {
    private String reportId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private ReportStatus status;
    private Priority priority;
    private LocalDateTime createdAt;
    private String descriptionPreview;
    private Long distanceMeters;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescriptionPreview() {
        return descriptionPreview;
    }

    public void setDescriptionPreview(String descriptionPreview) {
        this.descriptionPreview = descriptionPreview;
    }

    public Long getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Long distanceMeters) {
        this.distanceMeters = distanceMeters;
    }
}
