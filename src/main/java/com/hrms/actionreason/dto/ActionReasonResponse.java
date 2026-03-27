package com.hrms.actionreason.dto;

import java.time.LocalDate;
import java.util.List;

import com.hrms.actionreason.enums.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionReasonResponse {

    private Long id;
    private Long actionReasonRefId;
    private String actionReasonName;
    private String actionReasonCode;
    private String description;
    private String module;
    private String moduleMaster;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private List<String> linkedActions;
    private Status status;
    private Integer version;
    private String createdBy;
    private String modifiedBy;
    private String checkedBy;
    private String currentAssignee;

}
