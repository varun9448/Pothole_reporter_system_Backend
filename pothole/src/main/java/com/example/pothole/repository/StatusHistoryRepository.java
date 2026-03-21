package com.example.pothole.repository;

import com.example.pothole.Entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByReportIdOrderByTimestampAsc(String reportId);
    void deleteByReportId(String reportId);
}
