package com.example.pothole.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
public class Report {
    @Id
    @Column(length = 10, nullable = false, updatable = false)
    private String id;

    private static final String REPORT_ID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int REPORT_ID_LENGTH = 10;
    private static final SecureRandom REPORT_ID_RANDOM = new SecureRandom();

    private String photoPath;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_photos", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "photo_path", nullable = false)
    private List<String> photoPaths = new ArrayList<>();

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    private String reporterContact;

    private Long assignedAdminId;
    private String assignedBy;

    private Long assignedContractorId;

    private String repairPhotoPath;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_repair_photos", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "photo_path", nullable = false)
    private List<String> repairPhotoPaths = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_repair_before_photos", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "photo_path", nullable = false)
    private List<String> repairBeforePhotoPaths = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Zone zone;

    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Enumerated(EnumType.STRING)
    private Priority predictedPriority;
    private Double predictedPriorityScore;
    private String priorityModelVersion;

    private Double estimatedCost;
    private Double actualCost;
    private String materialsUsed;
    private Double timeSpentHours;

    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime repairedAt;
    private LocalDateTime approvedAt;
    private String approvedBy;

    public Report() {
        this.createdAt = LocalDateTime.now();
        this.status = ReportStatus.NEW;
        this.priority = Priority.MEDIUM;
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.trim().isEmpty()) {
            this.id = generateReportId();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ReportStatus.NEW;
        }
        if (this.priority == null) {
            this.priority = Priority.MEDIUM;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public List<String> getPhotoPaths() {
        return photoPaths;
    }

    public void setPhotoPaths(List<String> photoPaths) {
        this.photoPaths = photoPaths;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public String getReporterContact() {
        return reporterContact;
    }

    public void setReporterContact(String reporterContact) {
        this.reporterContact = reporterContact;
    }

    public Long getAssignedAdminId() {
        return assignedAdminId;
    }

    public void setAssignedAdminId(Long assignedAdminId) {
        this.assignedAdminId = assignedAdminId;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Long getAssignedContractorId() {
        return assignedContractorId;
    }

    public void setAssignedContractorId(Long assignedContractorId) {
        this.assignedContractorId = assignedContractorId;
    }

    public String getRepairPhotoPath() {
        return repairPhotoPath;
    }

    public void setRepairPhotoPath(String repairPhotoPath) {
        this.repairPhotoPath = repairPhotoPath;
    }

    public List<String> getRepairPhotoPaths() {
        return repairPhotoPaths;
    }

    public void setRepairPhotoPaths(List<String> repairPhotoPaths) {
        this.repairPhotoPaths = repairPhotoPaths;
    }

    public List<String> getRepairBeforePhotoPaths() {
        return repairBeforePhotoPaths;
    }

    public void setRepairBeforePhotoPaths(List<String> repairBeforePhotoPaths) {
        this.repairBeforePhotoPaths = repairBeforePhotoPaths;
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

    public Priority getPredictedPriority() {
        return predictedPriority;
    }

    public void setPredictedPriority(Priority predictedPriority) {
        this.predictedPriority = predictedPriority;
    }

    public Double getPredictedPriorityScore() {
        return predictedPriorityScore;
    }

    public void setPredictedPriorityScore(Double predictedPriorityScore) {
        this.predictedPriorityScore = predictedPriorityScore;
    }

    public String getPriorityModelVersion() {
        return priorityModelVersion;
    }

    public void setPriorityModelVersion(String priorityModelVersion) {
        this.priorityModelVersion = priorityModelVersion;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public Double getActualCost() {
        return actualCost;
    }

    public void setActualCost(Double actualCost) {
        this.actualCost = actualCost;
    }

    public String getMaterialsUsed() {
        return materialsUsed;
    }

    public void setMaterialsUsed(String materialsUsed) {
        this.materialsUsed = materialsUsed;
    }

    public Double getTimeSpentHours() {
        return timeSpentHours;
    }

    public void setTimeSpentHours(Double timeSpentHours) {
        this.timeSpentHours = timeSpentHours;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
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

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    private static String generateReportId() {
        char[] buf = new char[REPORT_ID_LENGTH];
        for (int i = 0; i < REPORT_ID_LENGTH; i++) {
            buf[i] = REPORT_ID_CHARS.charAt(REPORT_ID_RANDOM.nextInt(REPORT_ID_CHARS.length()));
        }
        return new String(buf);
    }
}
