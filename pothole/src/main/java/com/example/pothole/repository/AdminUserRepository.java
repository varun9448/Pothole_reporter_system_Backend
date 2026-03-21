package com.example.pothole.repository;


import com.example.pothole.Entity.AdminUser;
import com.example.pothole.Entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
Optional<AdminUser> findByUsername(String username);
Optional<AdminUser> findFirstByZoneOrderByIdAsc(Zone zone);
}
