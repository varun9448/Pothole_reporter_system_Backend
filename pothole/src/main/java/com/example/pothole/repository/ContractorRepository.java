package com.example.pothole.repository;


import com.example.pothole.Entity.Contractor;
import com.example.pothole.Entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;


public interface ContractorRepository extends JpaRepository<Contractor, Long> {
List<Contractor> findByAvailableTrue();
List<Contractor> findByZone(Zone zone);
List<Contractor> findByZoneAndAvailableTrue(Zone zone);
Optional<Contractor> findByEmail(String email);
}
