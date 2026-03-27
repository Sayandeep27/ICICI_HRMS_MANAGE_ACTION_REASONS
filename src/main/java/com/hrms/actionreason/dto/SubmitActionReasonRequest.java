package com.hrms.actionreason.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitActionReasonRequest {

    @NotNull
    private Long actionReasonId;

    @NotBlank
    private String makerId;

    private String remarks;

    @NotEmpty
    private List<@NotBlank String> checkerIds;

}
