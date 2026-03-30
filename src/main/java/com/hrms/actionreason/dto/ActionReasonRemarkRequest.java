package com.hrms.actionreason.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActionReasonRemarkRequest {

    @NotNull
    private Long tenantId;

    @NotBlank
    private String actionReasonCode;

    @NotBlank
    private String actorId;

    @NotBlank
    private String remarks;

}
