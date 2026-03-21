package com.example.pothole.controller;

import com.example.pothole.dto.AdminLoginRequest;
import com.example.pothole.dto.AdminZoneUpdateRequest;
import com.example.pothole.dto.AssignRequest;
import com.example.pothole.Entity.Report;
import com.example.pothole.Entity.Priority;
import com.example.pothole.Entity.ReportStatus;
import com.example.pothole.service.AdminService;
import com.example.pothole.service.ContractorService;
import com.example.pothole.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins="*")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final ContractorService contractorService;

    public AdminController(AdminService adminService, ReportService reportService, ContractorService contractorService) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.contractorService = contractorService;
    }

    // Optional convenience endpoint — the interceptor also validates headers for protected routes.
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AdminLoginRequest req) {
        return adminService.findByUsername(req.getUsername())
                .filter(u -> u.getPassword().equals(req.getPassword()))
                .filter(u -> u.getZone() != null && req.getZone() != null && u.getZone().equals(req.getZone()))
                .map(u -> ResponseEntity.ok("OK"))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    @PutMapping("/admins/{id}/zone")
    public ResponseEntity<?> updateAdminZone(@PathVariable Long id,
                                             @Valid @RequestBody AdminZoneUpdateRequest req) {
        return ResponseEntity.ok(adminService.updateZone(id, req.getZone()));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> listReports(@RequestHeader("X-Admin-Username") String adminUser) {
        try {
            return ResponseEntity.ok(reportService.listReportsForAdmin(adminUser));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/reports/{id}/verify")
    public ResponseEntity<?> verifyReport(@PathVariable String id, @RequestHeader("X-Admin-Username") String adminUser) {
        Report r = reportService.verifyReport(id, adminUser);
        return ResponseEntity.ok(r);
    }

    @PutMapping("/reports/{id}/assign")
    public ResponseEntity<?> assignReport(@PathVariable String id, @Valid @RequestBody AssignRequest req, @RequestHeader("X-Admin-Username") String adminUser) {
        try {
            Report r = reportService.assignReport(id, req, adminUser);
            return ResponseEntity.ok(r);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/reports/{id}/update-status")
    public ResponseEntity<?> updateStatus(@PathVariable String id,
                                          @RequestParam("status") ReportStatus status,
                                          @RequestParam(value = "actualCost", required = false) Double actualCost,
                                          @RequestParam(value = "notes", required = false) String notes,
                                          @RequestHeader("X-Admin-Username") String adminUser) {
        Report r = reportService.updateStatus(id, status, actualCost, adminUser, notes);
        return ResponseEntity.ok(r);
    }

    @PutMapping("/reports/{id}/priority")
    public ResponseEntity<?> updatePriority(@PathVariable String id,
                                            @RequestParam("priority") Priority priority,
                                            @RequestHeader("X-Admin-Username") String adminUser) {
        Report r = reportService.updatePriority(id, priority, adminUser);
        return ResponseEntity.ok(r);
    }

    @PutMapping("/reports/{id}/approve")
    public ResponseEntity<?> approveReport(@PathVariable String id,
                                           @RequestHeader("X-Admin-Username") String adminUser) {
        Report r = reportService.approveReport(id, adminUser);
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/reports/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable String id,
                                          @RequestHeader("X-Admin-Username") String adminUser) {
        try {
            reportService.deleteReportForAdmin(id, adminUser);
            return ResponseEntity.ok("Deleted");
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(ex.getMessage());
        }
    }

    @GetMapping("/reports/nearby")
    public ResponseEntity<List<Report>> nearby(@RequestParam double lat, @RequestParam double lng, @RequestParam double radiusKm) {
        return ResponseEntity.ok(reportService.findWithinRadius(lat, lng, radiusKm));
    }
}
