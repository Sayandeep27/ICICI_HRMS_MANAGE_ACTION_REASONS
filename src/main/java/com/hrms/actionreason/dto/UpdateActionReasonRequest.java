package com.hrms.actionreason.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateActionReasonRequest {

    @NotNull
    private Long tenantId;

    @NotBlank
    @Size(max = 20)
    private String actionReasonCode;

    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9- ]+$",
            message = "Action Reason Name can contain only alphabets, numbers, spaces and hyphen")
    private String actionReasonName;

    @Size(max = 40)
    private String description;

    @Size(max = 40)
    private String module;

    @Size(max = 40)
    private String moduleMaster;

    private LocalDate effectiveStartDate;

    private String linkedAction;

    private Boolean linkedToActivePosition;

    @Size(max = 100)
    private String remarks;

    private Boolean submit;

    private String makerId;

}
