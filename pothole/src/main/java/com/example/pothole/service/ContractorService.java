package com.example.pothole.service;

import com.example.pothole.Entity.Contractor;
import com.example.pothole.Entity.Zone;
import com.example.pothole.exception.ResourceNotFoundException;
import com.example.pothole.repository.ContractorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContractorService {
    private final ContractorRepository contractorRepository;
    private final AdminService adminService;

    public ContractorService(ContractorRepository contractorRepository, AdminService adminService) {
        this.contractorRepository = contractorRepository;
        this.adminService = adminService;
    }

    public Contractor create(Contractor c, String adminUsername) {
        Zone adminZone = resolveAdminZone(adminUsername);
        if (c.getZone() != null && !c.getZone().equals(adminZone)) {
            throw new IllegalStateException("Contractor zone must match admin zone");
        }
        c.setZone(adminZone);
        return contractorRepository.save(c);
    }

    public List<Contractor> listAll(String adminUsername) {
        Zone adminZone = resolveAdminZone(adminUsername);
        return contractorRepository.findByZone(adminZone);
    }

    public List<Contractor> listAvailable(String adminUsername) {
        Zone adminZone = resolveAdminZone(adminUsername);
        return contractorRepository.findByZoneAndAvailableTrue(adminZone);
    }

    public Optional<Contractor> findByEmail(String email) {
        return contractorRepository.findByEmail(email);
    }

    public Optional<Contractor> authenticate(String email, String password) {
        return contractorRepository.findByEmail(email)
                .filter(c -> password != null && password.equals(c.getPassword()));
    }
    public void  delete(Long id ){
        contractorRepository.deleteById(id);
    }

    private Zone resolveAdminZone(String adminUsername) {
        Zone zone = adminService.findByUsername(adminUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"))
                .getZone();
        if (zone == null) {
            throw new IllegalStateException("Admin zone not set");
        }
        return zone;
    }
}
