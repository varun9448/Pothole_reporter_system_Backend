package com.example.pothole.controller;

import com.example.pothole.Entity.Priority;
import com.example.pothole.Entity.ReportStatus;
import com.example.pothole.config.AdminAuthInterceptor;
import com.example.pothole.dto.PublicNearbyReportResponse;
import com.example.pothole.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicReportController.class)
class PublicReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private AdminAuthInterceptor adminAuthInterceptor;

    @Test
    void getNearbyReportsReturnsMarkerSummaries() throws Exception {
        PublicNearbyReportResponse response = new PublicNearbyReportResponse();
        response.setReportId("RPT123");
        response.setLatitude(new BigDecimal("12.9716000"));
        response.setLongitude(new BigDecimal("77.5946000"));
        response.setStatus(ReportStatus.NEW);
        response.setPriority(Priority.MEDIUM);
        response.setCreatedAt(LocalDateTime.of(2026, 3, 6, 10, 0));
        response.setDescriptionPreview("Pothole on main road");
        response.setDistanceMeters(42L);

        when(reportService.findPublicNearbyReports(
                eq(new BigDecimal("12.9716")),
                eq(new BigDecimal("77.5946")),
                eq(60)
        )).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/reports/nearby")
                        .param("lat", "12.9716")
                        .param("lng", "77.5946")
                        .param("radiusMeters", "60")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportId").value("RPT123"))
                .andExpect(jsonPath("$[0].status").value("NEW"))
                .andExpect(jsonPath("$[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$[0].descriptionPreview").value("Pothole on main road"))
                .andExpect(jsonPath("$[0].distanceMeters").value(42));
    }

    @Test
    void getNearbyReportsRejectsMissingCoordinates() throws Exception {
        mockMvc.perform(get("/api/reports/nearby")
                        .param("lng", "77.5946"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    void getNearbyReportsRejectsInvalidCoordinates() throws Exception {
        mockMvc.perform(get("/api/reports/nearby")
                        .param("lat", "abc")
                        .param("lng", "77.5946"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    void getNearbyReportsRejectsOutOfRangeRadius() throws Exception {
        mockMvc.perform(get("/api/reports/nearby")
                        .param("lat", "12.9716")
                        .param("lng", "77.5946")
                        .param("radiusMeters", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    void createReportReturnsBadRequestForInvalidPhotoCount() throws Exception {
        MockMultipartFile photo1 = new MockMultipartFile("photo", "one.jpg", "image/jpeg", "one".getBytes());
        MockMultipartFile photo2 = new MockMultipartFile("photo", "two.jpg", "image/jpeg", "two".getBytes());
        MockMultipartFile photo3 = new MockMultipartFile("photo", "three.jpg", "image/jpeg", "three".getBytes());

        when(reportService.createReport(
                any(),
                eq(new BigDecimal("12.9716")),
                eq(new BigDecimal("77.5946")),
                eq("Large pothole near bus stop"),
                eq("9876543210")
        )).thenThrow(new IllegalArgumentException("Please upload 4 to 5 report photos"));

        mockMvc.perform(multipart("/api/reports")
                        .file(photo1)
                        .file(photo2)
                        .file(photo3)
                        .param("latitude", "12.9716")
                        .param("longitude", "77.5946")
                        .param("description", "Large pothole near bus stop")
                        .param("reporterContact", "9876543210"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please upload 4 to 5 report photos"));
    }
}
