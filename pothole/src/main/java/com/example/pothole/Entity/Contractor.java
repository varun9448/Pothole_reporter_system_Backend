	package com.example.pothole.Entity;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;


@Entity
@Table(name = "contractors")
public class Contractor {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;


private String name;
private String phone;
private String email;
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;
private Boolean available = true;
private LocalDateTime createdTime;

@Enumerated(EnumType.STRING)
private Zone zone;


public Contractor() {
this.createdTime = LocalDateTime.now();
}


public Long getId() {
	return id;
}


public void setId(Long id) {
	this.id = id;
}


public String getName() {
	return name;
}


public void setName(String name) {
	this.name = name;
}


public String getPhone() {
	return phone;
}


public void setPhone(String phone) {
	this.phone = phone;
}


public String getEmail() {
	return email;
}


public void setEmail(String email) {
	this.email = email;
}

public String getPassword() {
	return password;
}

public void setPassword(String password) {
	this.password = password;
}


public Boolean getAvailable() {
	return available;
}


public void setAvailable(Boolean available) {
	this.available = available;
}


public LocalDateTime getCreatedTime() {
	return createdTime;
}


public void setCreatedTime(LocalDateTime createdTime) {
	this.createdTime = createdTime;
}

public Zone getZone() {
	return zone;
}

public void setZone(Zone zone) {
	this.zone = zone;
}


// getters and setters
}
