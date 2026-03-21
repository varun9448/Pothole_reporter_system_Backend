package com.example.pothole.repository;


import com.example.pothole.Entity.Report;
import com.example.pothole.Entity.ReportStatus;
import com.example.pothole.Entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;


public interface ReportRepository extends JpaRepository<Report, String> {
List<Report> findByStatus(ReportStatus status);
List<Report> findByAssignedContractorId(Long assignedContractorId);
List<Report> findByZone(Zone zone);

@Query(value = "SELECT r.* FROM reports r " +
        "WHERE r.status IN ('NEW', 'VERIFIED', 'ASSIGNED', 'IN_PROGRESS') " +
        "AND r.latitude IS NOT NULL " +
        "AND r.longitude IS NOT NULL " +
        "AND (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * " +
        "cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) <= :radiusKm " +
        "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * " +
        "cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) ASC " +
        "LIMIT 20", nativeQuery = true)
List<Report> findOpenReportsWithinRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);


// Haversine formula for radius search (distance in km)
@Query(value = "SELECT r.*, (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) AS distance_km FROM reports r HAVING distance_km <= :radiusKm ORDER BY distance_km", nativeQuery = true)
List<Report> findReportsWithinRadius(@Param("lat") double lat, @Param("lng") double lng, @Param("radiusKm") double radiusKm);
}
