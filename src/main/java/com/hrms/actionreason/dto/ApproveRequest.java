package com.hrms.actionreason.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApproveRequest {

    @NotNull
    private Long tenantId;

    @NotBlank
    private String actionReasonCode;

    @NotBlank
    private String checkerId;

    private String checkerRemarks;

}
