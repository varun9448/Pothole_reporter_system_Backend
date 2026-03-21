package com.example.pothole.service;

import com.example.pothole.dto.AssignRequest;
import com.example.pothole.dto.PublicNearbyReportResponse;
import com.example.pothole.dto.ReportTrackingResponse;
import com.example.pothole.Entity.AdminUser;
import com.example.pothole.Entity.Priority;
import com.example.pothole.Entity.Report;
import com.example.pothole.Entity.ReportStatus;
import com.example.pothole.Entity.RepairLog;
import com.example.pothole.Entity.Zone;
import com.example.pothole.Entity.StatusHistory;
import com.example.pothole.Entity.Contractor;
import com.example.pothole.exception.ResourceNotFoundException;
import com.example.pothole.repository.AdminUserRepository;
import com.example.pothole.repository.ContractorRepository;
import com.example.pothole.repository.RepairLogRepository;
import com.example.pothole.repository.ReportRepository;
import com.example.pothole.repository.StatusHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private static final int DEFAULT_PUBLIC_RADIUS_METERS = 60;
    private static final int MAX_PUBLIC_NEARBY_RESULTS = 20;
    private static final int DESCRIPTION_PREVIEW_LENGTH = 120;
    private static final int MIN_REPORT_PHOTOS = 4;
    private static final int MAX_REPORT_PHOTOS = 5;
    private static final Set<ReportStatus> OPEN_PUBLIC_STATUSES = EnumSet.of(
            ReportStatus.NEW,
            ReportStatus.VERIFIED,
            ReportStatus.ASSIGNED,
            ReportStatus.IN_PROGRESS
    );

    private final ReportRepository reportRepository;
    private final ContractorRepository contractorRepository;
    private final RepairLogRepository repairLogRepository;
    private final AdminUserRepository adminUserRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final ZoneDetectionService zoneDetectionService;

    /**
     * Preferred: set app.upload.dir in application.properties to an absolute path:
     * app.upload.dir=C:/Users/varun/Downloads/pothole/uploads
     *
     * If not set, it falls back to ${user.dir}/uploads (project working dir)/uploads
     */
    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    public ReportService(ReportRepository reportRepository,
                         ContractorRepository contractorRepository,
                         RepairLogRepository repairLogRepository,
                         AdminUserRepository adminUserRepository,
                         StatusHistoryRepository statusHistoryRepository,
                         ZoneDetectionService zoneDetectionService) {
        this.reportRepository = reportRepository;
        this.contractorRepository = contractorRepository;
        this.repairLogRepository = repairLogRepository;
        this.adminUserRepository = adminUserRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.zoneDetectionService = zoneDetectionService;
    }

    /**
     * Create a report with uploaded photo + metadata.
     * Returns saved Report entity (201 from controller).
     */
    public Report createReport(MultipartFile[] photos,
                               BigDecimal lat,
                               BigDecimal lng,
                               String description,
                               String reporterContact) throws IOException {

        if (photos == null || photos.length == 0) {
            throw new IllegalArgumentException("Please upload 4 to 5 report photos");
        }
        if (lat == null || lng == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        List<MultipartFile> validPhotos = new java.util.ArrayList<>();
        for (MultipartFile photo : photos) {
            if (photo != null && !photo.isEmpty()) {
                validPhotos.add(photo);
            }
        }

        if (validPhotos.size() < MIN_REPORT_PHOTOS || validPhotos.size() > MAX_REPORT_PHOTOS) {
            throw new IllegalArgumentException("Please upload 4 to 5 report photos");
        }

        List<String> photoPaths = new java.util.ArrayList<>(validPhotos.size());
        for (MultipartFile photo : validPhotos) {
            String filename = saveFile(photo);
            photoPaths.add("/uploads/" + filename);
        }

        Report r = new Report();
        r.setPhotoPaths(photoPaths);
        r.setPhotoPath(photoPaths.get(0));
        r.setLatitude(lat);
        r.setLongitude(lng);
        r.setDescription(description);
        r.setReporterContact(reporterContact);
        r.setCreatedAt(LocalDateTime.now());
        r.setStatus(ReportStatus.NEW);
        r.setPriority(Priority.MEDIUM);

        Zone detectedZone = zoneDetectionService.detectZone(lat, lng);
        r.setZone(detectedZone);

        PriorityPrediction prediction = predictPriority(description, lat, lng);
        r.setPredictedPriority(prediction.priority);
        r.setPredictedPriorityScore(prediction.score);
        r.setPriorityModelVersion(prediction.modelVersion);

        adminUserRepository.findFirstByZoneOrderByIdAsc(detectedZone)
                .map(AdminUser::getId)
                .ifPresent(r::setAssignedAdminId);

        Report saved = reportRepository.save(r);
        statusHistoryRepository.save(createStatusHistory(saved.getId(), saved.getStatus().name(), resolveActor(reporterContact)));
        return saved;
    }

    /**
     * Save uploaded file safely to disk and return the stored filename (not full path).
     * Uses InputStream copy to avoid relying on container-specific Part.write() behavior.
     */
    private String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Photo is required");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String original = StringUtils.cleanPath(Optional.ofNullable(file.getOriginalFilename()).orElse("image.jpg"));
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".jpg";
        String filename = UUID.randomUUID().toString() + ext;

        Path target = uploadPath.resolve(filename);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IOException("Upload failed: " + ex.getMessage(), ex);
        }

        return filename;
    }

    public List<Report> listAll() {
        return reportRepository.findAll();
    }

    public List<Report> listReportsForAdmin(String adminUsername) {
        AdminUser admin = adminUserRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        if (admin.getZone() == null) {
            throw new IllegalStateException("Admin zone not set");
        }
        return reportRepository.findByZone(admin.getZone());
    }

    public Report getById(String id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }

    public ReportTrackingResponse getTrackingById(String id) {
        Report r = getById(id);

        String adminName = null;
        if (r.getAssignedAdminId() != null) {
            adminName = adminUserRepository.findById(r.getAssignedAdminId())
                    .map(a -> StringUtils.hasText(a.getFullName()) ? a.getFullName() : a.getUsername())
                    .orElse(null);
        }

        String contractorName = null;
        if (r.getAssignedContractorId() != null) {
            contractorName = contractorRepository.findById(r.getAssignedContractorId())
                    .map(c -> c.getName())
                    .orElse(null);
        }

        ReportTrackingResponse resp = new ReportTrackingResponse();
        resp.setReportId(r.getId());
        resp.setZone(r.getZone());
        resp.setPriority(r.getPriority() != null ? r.getPriority() : Priority.MEDIUM);
        resp.setStatus(r.getStatus());
        resp.setAssignedAdminId(r.getAssignedAdminId());
        resp.setAssignedAdminName(adminName);
        resp.setAssignedContractorId(r.getAssignedContractorId());
        resp.setAssignedContractorName(contractorName);
        resp.setCreatedAt(r.getCreatedAt());
        resp.setVerifiedAt(r.getVerifiedAt());
        resp.setAssignedAt(r.getAssignedAt());
        resp.setRepairedAt(r.getRepairedAt());
        resp.setApprovedAt(r.getApprovedAt());
        return resp;
    }

    public List<StatusHistory> getStatusHistory(String id) {
        getById(id);
        return statusHistoryRepository.findByReportIdOrderByTimestampAsc(id);
    }

    public Report verifyReport(String id, String verifiedBy) {
        Report r = getById(id);
        r.setStatus(ReportStatus.VERIFIED);
        r.setVerifiedBy(verifiedBy);
        r.setVerifiedAt(LocalDateTime.now());
        repairLogRepository.save(createLog(r.getId(), "Verified", verifiedBy, null));
        statusHistoryRepository.save(createStatusHistory(r.getId(), r.getStatus().name(), verifiedBy));
        return reportRepository.save(r);
    }

    public List<Report> listReportsForContractor(Long contractorId) {
        return reportRepository.findByAssignedContractorId(contractorId);
    }

    public Report contractorConfirm(String id, Long contractorId, String byUser) {
        Report r = getById(id);
        ensureContractorAssigned(r, contractorId);
        if (r.getStatus() != ReportStatus.ASSIGNED) {
            throw new IllegalStateException("Report is not in ASSIGNED state");
        }
        r.setStatus(ReportStatus.IN_PROGRESS);
        repairLogRepository.save(createLog(r.getId(), "Contractor confirmed work", byUser, null));
        statusHistoryRepository.save(createStatusHistory(r.getId(), r.getStatus().name(), byUser));
        return reportRepository.save(r);
    }

    public Report contractorSubmitRepair(String id,
                                         Long contractorId,
                                         MultipartFile[] photos,
                                         MultipartFile[] beforePhotos,
                                         Double actualCost,
                                         String materialsUsed,
                                         Double timeSpentHours,
                                         String notes,
                                         String byUser) throws IOException {
        Report r = getById(id);
        ensureContractorAssigned(r, contractorId);
        if (r.getStatus() != ReportStatus.IN_PROGRESS) {
            throw new IllegalStateException("Report is not in IN_PROGRESS state");
        }
        if (photos == null || photos.length == 0) {
            throw new IOException("Repair photos are required");
        }
        List<String> repairPaths = new java.util.ArrayList<>();
        for (MultipartFile photo : photos) {
            if (photo == null || photo.isEmpty()) {
                continue;
            }
            String filename = saveFile(photo);
            repairPaths.add("/uploads/" + filename);
        }
        if (repairPaths.size() < 3 || repairPaths.size() > 4) {
            throw new IOException("Please upload 3 to 4 repair photos");
        }
        r.setRepairPhotoPaths(repairPaths);
        r.setRepairPhotoPath(repairPaths.get(0));

        if (beforePhotos != null && beforePhotos.length > 0) {
            List<String> beforePaths = new java.util.ArrayList<>();
            for (MultipartFile photo : beforePhotos) {
                if (photo == null || photo.isEmpty()) {
                    continue;
                }
                String filename = saveFile(photo);
                beforePaths.add("/uploads/" + filename);
            }
            r.setRepairBeforePhotoPaths(beforePaths);
        }

        if (actualCost != null) {
            r.setActualCost(actualCost);
        }
        if (StringUtils.hasText(materialsUsed)) {
            r.setMaterialsUsed(materialsUsed);
        }
        if (timeSpentHours != null) {
            r.setTimeSpentHours(timeSpentHours);
        }
        repairLogRepository.save(createLog(r.getId(), "Repair submitted", byUser, notes));
        return reportRepository.save(r);
    }

    public Report assignReport(String id, AssignRequest req, String byUser) {
        Report r = getById(id);
        AdminUser admin = adminUserRepository.findByUsername(byUser)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        if (admin.getZone() == null) {
            throw new IllegalStateException("Admin zone not set");
        }
        if (r.getZone() == null || !admin.getZone().equals(r.getZone())) {
            throw new IllegalStateException("Report not in admin zone");
        }
        Contractor contractor = contractorRepository.findById(req.getContractorId())
                .orElseThrow(() -> new ResourceNotFoundException("Contractor not found"));
        if (contractor.getZone() == null) {
            throw new IllegalStateException("Contractor zone not set");
        }
        if (!contractor.getZone().equals(r.getZone())) {
            throw new IllegalStateException("Contractor not in report zone");
        }
        r.setAssignedContractorId(req.getContractorId());
        r.setEstimatedCost(req.getEstimatedCost());
        r.setStatus(ReportStatus.ASSIGNED);
        r.setAssignedAt(LocalDateTime.now());
        r.setAssignedBy(byUser);
        contractor.setAvailable(false);
        contractorRepository.save(contractor);
        repairLogRepository.save(createLog(r.getId(), "Assigned to contractor " + req.getContractorId(), byUser, null));
        statusHistoryRepository.save(createStatusHistory(r.getId(), r.getStatus().name(), byUser));
        return reportRepository.save(r);
    }

    public Report updateStatus(String id, ReportStatus status, Double actualCost, String byUser, String notes) {
        Report r = getById(id);
        r.setStatus(status);
        if (actualCost != null) r.setActualCost(actualCost);
        if (status == ReportStatus.REPAIRED) {
            r.setRepairedAt(LocalDateTime.now());
            if (r.getAssignedContractorId() != null) {
                contractorRepository.findById(r.getAssignedContractorId()).ifPresent(c -> {
                    c.setAvailable(true);
                    contractorRepository.save(c);
                });
            }
        }
        repairLogRepository.save(createLog(r.getId(), "Status: " + status.name(), byUser, notes));
        statusHistoryRepository.save(createStatusHistory(r.getId(), r.getStatus().name(), byUser));
        return reportRepository.save(r);
    }

    public Report updatePriority(String id, Priority priority, String byUser) {
        Report r = getById(id);
        r.setPriority(priority != null ? priority : Priority.MEDIUM);
        repairLogRepository.save(createLog(r.getId(), "Priority: " + r.getPriority().name(), byUser, null));
        return reportRepository.save(r);
    }

    public Report approveReport(String id, String byUser) {
        Report r = getById(id);
        r.setStatus(ReportStatus.REPAIRED);
        if (r.getRepairedAt() == null) {
            r.setRepairedAt(LocalDateTime.now());
        }
        r.setApprovedAt(LocalDateTime.now());
        r.setApprovedBy(byUser);
        if (r.getAssignedContractorId() != null) {
            contractorRepository.findById(r.getAssignedContractorId()).ifPresent(c -> {
                c.setAvailable(true);
                contractorRepository.save(c);
            });
        }
        repairLogRepository.save(createLog(r.getId(), "Approved", byUser, null));
        statusHistoryRepository.save(createStatusHistory(r.getId(), r.getStatus().name(), byUser));
        return reportRepository.save(r);
    }

    @Transactional
    public void deleteReportForAdmin(String reportId, String adminUsername) {
        AdminUser admin = adminUserRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        Report r = getById(reportId);
        if (admin.getZone() != null && r.getZone() != null) {
            boolean sameZone = admin.getZone().equals(r.getZone());
            boolean assignedToAdmin = r.getAssignedAdminId() != null && r.getAssignedAdminId().equals(admin.getId());
            if (!sameZone && !assignedToAdmin) {
                throw new IllegalStateException("Report not in admin zone");
            }
        }

        if (r.getAssignedContractorId() != null) {
            contractorRepository.findById(r.getAssignedContractorId()).ifPresent(c -> {
                c.setAvailable(true);
                contractorRepository.save(c);
            });
        }

        deleteFileIfExists(r.getPhotoPath());
        if (r.getPhotoPaths() != null) {
            for (String path : r.getPhotoPaths()) {
                if (path != null && !path.equals(r.getPhotoPath())) {
                    deleteFileIfExists(path);
                }
            }
        }
        deleteFileIfExists(r.getRepairPhotoPath());
        if (r.getRepairPhotoPaths() != null) {
            for (String path : r.getRepairPhotoPaths()) {
                if (path != null && !path.equals(r.getRepairPhotoPath())) {
                    deleteFileIfExists(path);
                }
            }
        }
        if (r.getRepairBeforePhotoPaths() != null) {
            for (String path : r.getRepairBeforePhotoPaths()) {
                deleteFileIfExists(path);
            }
        }

        repairLogRepository.deleteByReportId(reportId);
        statusHistoryRepository.deleteByReportId(reportId);
        reportRepository.deleteById(reportId);
    }

    private RepairLog createLog(String reportId, String action, String byUser, String notes) {
        RepairLog log = new RepairLog();
        log.setReportId(reportId);
        log.setAction(action);
        log.setByUser(byUser);
        log.setNotes(notes);
        return log;
    }

    private StatusHistory createStatusHistory(String reportId, String status, String updatedBy) {
        StatusHistory history = new StatusHistory();
        history.setReportId(reportId);
        history.setStatus(status);
        history.setUpdatedBy(updatedBy);
        return history;
    }

    private void deleteFileIfExists(String webPath) {
        if (!StringUtils.hasText(webPath)) {
            return;
        }
        if (!webPath.startsWith("/uploads/")) {
            return;
        }
        String filename = webPath.substring("/uploads/".length());
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path target = uploadPath.resolve(filename);
            Files.deleteIfExists(target);
        } catch (IOException ex) {
            // Best-effort cleanup; ignore failures
        }
    }

    private void ensureContractorAssigned(Report r, Long contractorId) {
        if (contractorId == null || r.getAssignedContractorId() == null || !contractorId.equals(r.getAssignedContractorId())) {
            throw new IllegalStateException("Report not assigned to this contractor");
        }
    }

    private String resolveActor(String reporterContact) {
        if (StringUtils.hasText(reporterContact)) {
            return reporterContact;
        }
        return "citizen";
    }

    public List<Report> findWithinRadius(double lat, double lng, double radiusKm) {
        return reportRepository.findReportsWithinRadius(lat, lng, radiusKm);
    }

    public List<PublicNearbyReportResponse> findPublicNearbyReports(BigDecimal lat, BigDecimal lng, Integer radiusMeters) {
        if (lat == null || lng == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }

        int effectiveRadiusMeters = radiusMeters != null ? radiusMeters : DEFAULT_PUBLIC_RADIUS_METERS;
        double radiusKm = effectiveRadiusMeters / 1000.0;

        return reportRepository.findOpenReportsWithinRadius(lat.doubleValue(), lng.doubleValue(), radiusKm)
                .stream()
                .filter(report -> OPEN_PUBLIC_STATUSES.contains(report.getStatus()))
                .filter(report -> report.getLatitude() != null && report.getLongitude() != null)
                .map(report -> toPublicNearbyResponse(report, lat, lng))
                .sorted(Comparator.comparingLong(PublicNearbyReportResponse::getDistanceMeters))
                .limit(MAX_PUBLIC_NEARBY_RESULTS)
                .collect(Collectors.toList());
    }

    private PriorityPrediction predictPriority(String description, BigDecimal lat, BigDecimal lng) {
        int nearbyCount = 0;
        if (lat != null && lng != null) {
            try {
                List<Report> nearby = reportRepository.findReportsWithinRadius(
                        lat.doubleValue(),
                        lng.doubleValue(),
                        0.3
                );
                nearbyCount = nearby == null ? 0 : nearby.size();
            } catch (Exception ex) {
                // ignore lookup failures
            }
        }

        return ruleBasedPrediction(description, nearbyCount);
    }

    private PriorityPrediction ruleBasedPrediction(String description, int nearbyCount) {
        double score = 0.0;
        String text = description == null ? "" : description.toLowerCase();

        String[] severeKeywords = {
                "deep", "huge", "dangerous", "accident", "sinkhole", "crater",
                "severe", "collapsed", "broken", "very big", "major", "flood"
        };
        for (String keyword : severeKeywords) {
            if (text.contains(keyword)) {
                score += 2.0;
                break;
            }
        }

        String[] roadKeywords = {
                "highway", "main road", "arterial", "ring road", "flyover",
                "bridge", "junction", "intersection", "crossroad", "nh", "state highway"
        };
        for (String keyword : roadKeywords) {
            if (text.contains(keyword)) {
                score += 1.0;
                break;
            }
        }

        if (text.length() > 120) {
            score += 0.5;
        }

        if (nearbyCount >= 3) {
            score += 2.0;
        } else if (nearbyCount >= 1) {
            score += 1.0;
        }

        Priority predicted;
        if (score >= 4.5) {
            predicted = Priority.HIGH;
        } else if (score >= 2.5) {
            predicted = Priority.MEDIUM;
        } else {
            predicted = Priority.LOW;
        }

        return new PriorityPrediction(predicted, score, "rule-v1");
    }

    private PublicNearbyReportResponse toPublicNearbyResponse(Report report, BigDecimal sourceLat, BigDecimal sourceLng) {
        PublicNearbyReportResponse response = new PublicNearbyReportResponse();
        response.setReportId(report.getId());
        response.setLatitude(report.getLatitude());
        response.setLongitude(report.getLongitude());
        response.setStatus(report.getStatus());
        response.setPriority(report.getPriority() != null ? report.getPriority() : Priority.MEDIUM);
        response.setCreatedAt(report.getCreatedAt());
        response.setDescriptionPreview(buildDescriptionPreview(report.getDescription()));
        response.setDistanceMeters(Math.round(distanceKm(
                sourceLat.doubleValue(),
                sourceLng.doubleValue(),
                report.getLatitude().doubleValue(),
                report.getLongitude().doubleValue()
        ) * 1000));
        return response;
    }

    private String buildDescriptionPreview(String description) {
        if (!StringUtils.hasText(description)) {
            return "";
        }
        String normalized = description.trim();
        if (normalized.length() <= DESCRIPTION_PREVIEW_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, DESCRIPTION_PREVIEW_LENGTH - 3) + "...";
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    private static class PriorityPrediction {
        private final Priority priority;
        private final double score;
        private final String modelVersion;

        private PriorityPrediction(Priority priority, double score, String modelVersion) {
            this.priority = priority;
            this.score = score;
            this.modelVersion = modelVersion;
        }
    }

}
