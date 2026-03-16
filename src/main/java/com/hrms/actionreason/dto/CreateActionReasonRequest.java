package com.hrms.actionreason.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateActionReasonRequest {

    @NotBlank
    private String actionReasonName;

    private String description;

    private Long moduleId;

    private Long moduleMasterId;

    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    private String remarks;

    private String createdBy;

}