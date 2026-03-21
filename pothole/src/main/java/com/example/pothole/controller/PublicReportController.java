package com.example.pothole.controller;

import com.example.pothole.Entity.Report;
import com.example.pothole.dto.PublicNearbyReportResponse;
import com.example.pothole.service.ReportService;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins="*")
public class PublicReportController {

    private final ReportService reportService;

    public PublicReportController(ReportService reportService) {
        this.reportService = reportService;
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReport(
            @RequestParam("photo") MultipartFile[] photos,
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam("description") String description,
            @RequestParam(value = "reporterContact", required = false) String reporterContact) {
        try {
            Report saved = reportService.createReport(photos, latitude, longitude, description, reporterContact);
            return ResponseEntity.status(201).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + ex.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable String id) {
        return ResponseEntity.ok(reportService.getById(id));
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<?> getTracking(@PathVariable String id) {
        return ResponseEntity.ok(reportService.getTrackingById(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<?> getHistory(@PathVariable String id) {
        return ResponseEntity.ok(reportService.getStatusHistory(id));
    }

    @GetMapping("/nearby")
    public ResponseEntity<?> getNearbyReports(@RequestParam("lat") BigDecimal latitude,
                                              @RequestParam("lng") BigDecimal longitude,
                                              @RequestParam(value = "radiusMeters", required = false) Integer radiusMeters) {
        if (radiusMeters != null && (radiusMeters < 30 || radiusMeters > 100)) {
            return ResponseEntity.badRequest().body("radiusMeters must be between 30 and 100");
        }

        List<PublicNearbyReportResponse> reports = reportService.findPublicNearbyReports(latitude, longitude, radiusMeters);
        return ResponseEntity.ok(reports);
    }
}
