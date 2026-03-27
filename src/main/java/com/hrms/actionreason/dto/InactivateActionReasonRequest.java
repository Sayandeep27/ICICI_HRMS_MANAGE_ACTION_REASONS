package com.hrms.actionreason.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InactivateActionReasonRequest {

    @NotNull
    private Long actionReasonId;

    @NotBlank
    private String actorId;

}
