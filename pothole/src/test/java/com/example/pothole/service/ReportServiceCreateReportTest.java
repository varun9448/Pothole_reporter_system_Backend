package com.example.pothole.service;

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
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ReportServiceCreateReportTest {

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
    void createReportRejectsTooFewPhotos() {
        assertThatThrownBy(() -> reportService.createReport(
                buildPhotos(3),
                new BigDecimal("12.9716"),
                new BigDecimal("77.5946"),
                "Large pothole",
                "9876543210"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Please upload 4 to 5 report photos");

        verifyNoInteractions(reportRepository, zoneDetectionService);
    }

    @Test
    void createReportRejectsTooManyPhotos() {
        assertThatThrownBy(() -> reportService.createReport(
                buildPhotos(6),
                new BigDecimal("12.9716"),
                new BigDecimal("77.5946"),
                "Large pothole",
                "9876543210"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Please upload 4 to 5 report photos");

        verifyNoInteractions(reportRepository, zoneDetectionService);
    }

    private MockMultipartFile[] buildPhotos(int count) {
        MockMultipartFile[] files = new MockMultipartFile[count];
        for (int i = 0; i < count; i++) {
            files[i] = new MockMultipartFile(
                    "photo",
                    "photo-" + i + ".jpg",
                    "image/jpeg",
                    ("image-" + i).getBytes()
            );
        }
        return files;
    }
}
