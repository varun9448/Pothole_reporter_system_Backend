package com.example.pothole.repository;

import com.example.pothole.Entity.Priority;
import com.example.pothole.Entity.Report;
import com.example.pothole.Entity.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Test
    void findOpenReportsWithinRadiusFiltersStatusesAndOrdersByDistance() {
        reportRepository.save(buildReport("OPENNEAR1", ReportStatus.NEW, "12.9718000", "77.5946000"));
        reportRepository.save(buildReport("REPAIR0001", ReportStatus.REPAIRED, "12.9718500", "77.5946000"));
        reportRepository.save(buildReport("OPENFAR01", ReportStatus.IN_PROGRESS, "12.9720000", "77.5946000"));
        reportRepository.save(buildReport("OPENOUT01", ReportStatus.VERIFIED, "12.9724000", "77.5946000"));

        List<Report> results = reportRepository.findOpenReportsWithinRadius(12.9716, 77.5946, 0.06);

        assertThat(results).extracting(Report::getId).containsExactly("OPENNEAR1", "OPENFAR01");
        assertThat(results).extracting(Report::getStatus)
                .containsExactly(ReportStatus.NEW, ReportStatus.IN_PROGRESS);
    }

    private Report buildReport(String id, ReportStatus status, String lat, String lng) {
        Report report = new Report();
        report.setId(id);
        report.setStatus(status);
        report.setPriority(Priority.MEDIUM);
        report.setLatitude(new BigDecimal(lat));
        report.setLongitude(new BigDecimal(lng));
        report.setDescription("Test report " + id);
        report.setCreatedAt(LocalDateTime.of(2026, 3, 6, 8, 0));
        return report;
    }
}
