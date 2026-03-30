package com.hrms.actionreason.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PushBackRequest {

    @NotNull
    private Long tenantId;

    @NotBlank
    private String actionReasonCode;

    @NotBlank
    private String checkerId;

    @NotBlank
    private String checkerRemarks;

    private String attachmentReference;

}
