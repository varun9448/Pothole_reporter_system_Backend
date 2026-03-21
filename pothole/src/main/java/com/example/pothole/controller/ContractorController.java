package com.example.pothole.controller;

import com.example.pothole.Entity.Contractor;
import com.example.pothole.service.ContractorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/contractors")
@CrossOrigin(origins="*")
public class ContractorController {

    private final ContractorService contractorService;

    public ContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Contractor c,
                                    @RequestHeader("X-Admin-Username") String adminUser) {
        try {
            return ResponseEntity.status(201).body(contractorService.create(c, adminUser));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listAll(@RequestHeader("X-Admin-Username") String adminUser) {
        try {
            return ResponseEntity.ok(contractorService.listAll(adminUser));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> available(@RequestHeader("X-Admin-Username") String adminUser) {
        try {
            return ResponseEntity.ok(contractorService.listAvailable(adminUser));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        contractorService.delete(id);

    }

    
}
