	package com.example.pothole.repository;


import com.example.pothole.Entity.RepairLog;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;


public interface RepairLogRepository extends JpaRepository<RepairLog, Long> {
List<RepairLog> findByReportIdOrderByTimestampDesc(String reportId);
void deleteByReportId(String reportId);
}
