package com.hrms.actionreason.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HistoryRequest {

    @NotNull
    private Long actionReasonId;

}
