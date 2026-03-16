package com.hrms.actionreason.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UpdateActionReasonRequest {

    private String actionReasonName;

    private String description;

    private LocalDate effectiveEndDate;

    private String remarks;

    private String modifiedBy;

}