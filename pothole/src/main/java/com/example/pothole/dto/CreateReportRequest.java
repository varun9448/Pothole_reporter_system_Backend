package com.example.pothole.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public class CreateReportRequest {
@NotNull
private BigDecimal latitude;
@NotNull
private BigDecimal longitude;
@NotBlank
private String description;
private String reporterContact;
public BigDecimal getLatitude() {
	return latitude;
}
public void setLatitude(BigDecimal latitude) {
	this.latitude = latitude;
}
public BigDecimal getLongitude() {
	return longitude;
}
public void setLongitude(BigDecimal longitude) {
	this.longitude = longitude;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public String getReporterContact() {
	return reporterContact;
}
public void setReporterContact(String reporterContact) {
	this.reporterContact = reporterContact;
}


// getters and setters
}