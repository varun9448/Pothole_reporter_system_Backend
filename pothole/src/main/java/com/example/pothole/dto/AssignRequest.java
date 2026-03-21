package com.example.pothole.dto;


import jakarta.validation.constraints.NotNull;


public class AssignRequest {
@NotNull
private Long contractorId;
private Double estimatedCost;
public Long getContractorId() {
	return contractorId;
}
public void setContractorId(Long contractorId) {
	this.contractorId = contractorId;
}
public Double getEstimatedCost() {
	return estimatedCost;
}
public void setEstimatedCost(Double estimatedCost) {
	this.estimatedCost = estimatedCost;
}


// getters and setters
}