package com.example.pothole.service;

import com.example.pothole.Entity.AdminUser;
import com.example.pothole.Entity.Zone;
import com.example.pothole.repository.AdminUserRepository;
import com.example.pothole.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {
    private final AdminUserRepository adminUserRepository;

    public AdminService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    public Optional<AdminUser> findByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    public AdminUser updateZone(Long id, Zone zone) {
        AdminUser admin = adminUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        admin.setZone(zone);
        return adminUserRepository.save(admin);
    }
}
