package com.example.pothole.dto;

import com.example.pothole.Entity.Priority;
import com.example.pothole.Entity.ReportStatus;
import com.example.pothole.Entity.Zone;

import java.time.LocalDateTime;

public class ReportTrackingResponse {
    private String reportId;
    private Zone zone;
    private Priority priority;
    private ReportStatus status;
    private Long assignedAdminId;
    private String assignedAdminName;
    private Long assignedContractorId;
    private String assignedContractorName;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime repairedAt;
    private LocalDateTime approvedAt;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public Long getAssignedAdminId() {
        return assignedAdminId;
    }

    public void setAssignedAdminId(Long assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    public String getAssignedAdminName() {
        return assignedAdminName;
    }

    public void setAssignedAdminName(String assignedAdminName) {
        this.assignedAdminName = assignedAdminName;
    }

    public Long getAssignedContractorId() {
        return assignedContractorId;
    }

    public void setAssignedContractorId(Long assignedContractorId) {
        this.assignedContractorId = assignedContractorId;
    }

    public String getAssignedContractorName() {
        return assignedContractorName;
    }

    public void setAssignedContractorName(String assignedContractorName) {
        this.assignedContractorName = assignedContractorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getRepairedAt() {
        return repairedAt;
    }

    public void setRepairedAt(LocalDateTime repairedAt) {
        this.repairedAt = repairedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }
}
