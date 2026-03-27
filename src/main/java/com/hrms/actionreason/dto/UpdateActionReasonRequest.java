package com.hrms.actionreason.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateActionReasonRequest {

    @NotNull
    private Long actionReasonId;

    @Size(max = 20)
    @Pattern(regexp = "^[A-Za-z0-9- ]+$",
            message = "Action Reason Name can contain only alphabets, numbers, spaces and hyphen")
    private String actionReasonName;

    @Size(max = 40)
    private String description;

    private LocalDate effectiveStartDate;

    private List<@Size(max = 30) String> linkedActions;

    private Boolean linkedToActivePosition;

    @Size(max = 100)
    private String remarks;

    private String modifiedBy;

}
