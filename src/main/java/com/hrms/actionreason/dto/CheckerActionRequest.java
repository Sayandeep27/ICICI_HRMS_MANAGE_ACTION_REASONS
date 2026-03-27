package com.hrms.actionreason.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckerActionRequest {

    @NotNull
    private Long actionReasonId;

    @NotBlank
    private String checkerId;

    @NotBlank
    private String remarks;

    private String attachmentReference;

}
