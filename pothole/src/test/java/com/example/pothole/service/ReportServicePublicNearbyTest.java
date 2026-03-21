package com.example.pothole.service;

import com.example.pothole.Entity.AdminUser;
import com.example.pothole.Entity.Priority;
import com.example.pothole.Entity.Report;
import com.example.pothole.Entity.ReportStatus;
import com.example.pothole.dto.PublicNearbyReportResponse;
import com.example.pothole.repository.AdminUserRepository;
import com.example.pothole.repository.ContractorRepository;
import com.example.pothole.repository.RepairLogRepository;
import com.example.pothole.repository.ReportRepository;
import com.example.pothole.repository.StatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServicePublicNearbyTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ContractorRepository contractorRepository;

    @Mock
    private RepairLogRepository repairLogRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private ZoneDetectionService zoneDetectionService;

    @InjectMocks
    private ReportService reportService;

    @Test
    void findPublicNearbyReportsFiltersRepairedAndSortsByDistance() {
        BigDecimal sourceLat = new BigDecimal("12.9716000");
        BigDecimal sourceLng = new BigDecimal("77.5946000");

        Report repaired = buildReport("REPAIRED1", ReportStatus.REPAIRED, "12.9717000", "77.5946000", "Resolved issue");
        Report fartherOpen = buildReport("OPEN2", ReportStatus.ASSIGNED, "12.9720000", "77.5946000", "Pothole near bus stop");
        Report nearestOpen = buildReport("OPEN1", ReportStatus.NEW, "12.9716500", "77.5946000", "Fresh pothole");

        when(reportRepository.findOpenReportsWithinRadius(eq(12.9716), eq(77.5946), eq(0.06)))
                .thenReturn(Arrays.asList(repaired, fartherOpen, nearestOpen));

        List<PublicNearbyReportResponse> results = reportService.findPublicNearbyReports(sourceLat, sourceLng, 60);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getReportId()).isEqualTo("OPEN1");
        assertThat(results.get(1).getReportId()).isEqualTo("OPEN2");
        assertThat(results.get(0).getDistanceMeters()).isLessThan(results.get(1).getDistanceMeters());
        assertThat(results).extracting(PublicNearbyReportResponse::getStatus)
                .containsExactly(ReportStatus.NEW, ReportStatus.ASSIGNED);
    }

    @Test
    void findPublicNearbyReportsTruncatesDescriptionUsesDefaultRadiusAndCapsResults() {
        BigDecimal sourceLat = new BigDecimal("12.9716000");
        BigDecimal sourceLng = new BigDecimal("77.5946000");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            builder.append('A');
        }
        String longDescription = builder.toString();

        List<Report> reports = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            reports.add(buildReport(
                    "OPEN" + i,
                    ReportStatus.VERIFIED,
                    String.format("12.9716%03d", i),
                    "77.5946000",
                    longDescription
            ));
        }

        when(reportRepository.findOpenReportsWithinRadius(eq(12.9716), eq(77.5946), eq(0.06)))
                .thenReturn(reports);

        List<PublicNearbyReportResponse> results = reportService.findPublicNearbyReports(sourceLat, sourceLng, null);

        assertThat(results).hasSize(20);
        assertThat(results.get(0).getDescriptionPreview()).hasSize(120).endsWith("...");
        verify(reportRepository).findOpenReportsWithinRadius(eq(12.9716), eq(77.5946), eq(0.06));
    }

    private Report buildReport(String id, ReportStatus status, String lat, String lng, String description) {
        Report report = new Report();
        report.setId(id);
        report.setStatus(status);
        report.setPriority(Priority.MEDIUM);
        report.setLatitude(new BigDecimal(lat));
        report.setLongitude(new BigDecimal(lng));
        report.setDescription(description);
        report.setCreatedAt(LocalDateTime.of(2026, 3, 6, 9, 0));
        return report;
    }
}
