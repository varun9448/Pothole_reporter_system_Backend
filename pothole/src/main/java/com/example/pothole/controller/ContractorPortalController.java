package com.example.pothole.controller;

import com.example.pothole.Entity.Contractor;
import com.example.pothole.Entity.Report;
import com.example.pothole.dto.ContractorLoginRequest;
import com.example.pothole.service.ContractorService;
import com.example.pothole.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contractor")
@CrossOrigin(origins = "*")
public class ContractorPortalController {

    private final ContractorService contractorService;
    private final ReportService reportService;

    public ContractorPortalController(ContractorService contractorService, ReportService reportService) {
        this.contractorService = contractorService;
        this.reportService = reportService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody ContractorLoginRequest req) {
        return contractorService.authenticate(req.getEmail(), req.getPassword())
                .map(c -> ResponseEntity.ok("OK"))
                .orElse(ResponseEntity.status(401).body("Invalid credentials"));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> myReports(@RequestHeader("X-Contractor-Email") String email,
                                       @RequestHeader("X-Contractor-Password") String password) {
        Contractor c = authenticate(email, password);
        if (c == null) {
            return ResponseEntity.status(401).body("Invalid contractor credentials");
        }
        List<Report> reports = reportService.listReportsForContractor(c.getId());
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/reports/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable String id,
                                     @RequestHeader("X-Contractor-Email") String email,
                                     @RequestHeader("X-Contractor-Password") String password) {
        Contractor c = authenticate(email, password);
        if (c == null) {
            return ResponseEntity.status(401).body("Invalid contractor credentials");
        }
        try {
            Report r = reportService.contractorConfirm(id, c.getId(), c.getEmail());
            return ResponseEntity.ok(r);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping(value = "/reports/{id}/repair", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> submitRepair(@PathVariable String id,
                                          @RequestHeader("X-Contractor-Email") String email,
                                          @RequestHeader("X-Contractor-Password") String password,
                                          @RequestParam(value = "beforePhoto", required = false) MultipartFile[] beforePhotos,
                                          @RequestParam("photo") MultipartFile[] photos,
                                          @RequestParam(value = "actualCost", required = false) Double actualCost,
                                          @RequestParam(value = "materialsUsed", required = false) String materialsUsed,
                                          @RequestParam(value = "timeSpentHours", required = false) Double timeSpentHours,
                                          @RequestParam(value = "notes", required = false) String notes) {
        Contractor c = authenticate(email, password);
        if (c == null) {
            return ResponseEntity.status(401).body("Invalid contractor credentials");
        }
        try {
            Report r = reportService.contractorSubmitRepair(
                    id,
                    c.getId(),
                    photos,
                    beforePhotos,
                    actualCost,
                    materialsUsed,
                    timeSpentHours,
                    notes,
                    c.getEmail()
            );
            return ResponseEntity.ok(r);
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IOException ex) {
            return ResponseEntity.status(500).body("Upload failed: " + ex.getMessage());
        }
    }

    private Contractor authenticate(String email, String password) {
        return contractorService.authenticate(email, password).orElse(null);
    }
}
