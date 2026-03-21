package com.example.pothole.Entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "repair_logs")
public class RepairLog {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;


@Column(length = 10)
private String reportId;
private String action;
public Long getId() {
	return id;
}


public void setId(Long id) {
	this.id = id;
}


public String getReportId() {
	return reportId;
}


public void setReportId(String reportId) {
	this.reportId = reportId;
}


public String getAction() {
	return action;
}


public void setAction(String action) {
	this.action = action;
}


public String getByUser() {
	return byUser;
}


public void setByUser(String byUser) {
	this.byUser = byUser;
}


public LocalDateTime getTimestamp() {
	return timestamp;
}


public void setTimestamp(LocalDateTime timestamp) {
	this.timestamp = timestamp;
}


public String getNotes() {
	return notes;
}


public void setNotes(String notes) {
	this.notes = notes;
}


private String byUser;
private LocalDateTime timestamp;


@Column(columnDefinition = "TEXT")
private String notes;


public RepairLog() {
this.timestamp = LocalDateTime.now();
}


// getters and setters
}
