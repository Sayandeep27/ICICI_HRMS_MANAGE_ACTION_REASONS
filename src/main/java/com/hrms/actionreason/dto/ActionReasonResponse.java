package com.hrms.actionreason.dto;

import java.time.LocalDate;

import com.hrms.actionreason.enums.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionReasonResponse {

    private Long tenantId;
    private Long pkId;
    private ActionReasonVersionId id;
    private String actionReasonName;
    private String description;
    private String module;
    private String moduleMaster;
    private String linkedAction;
    private boolean status;
    private Status workflowStatus;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String createdBy;
    private LocalDate createdDate;
    private String updatedBy;
    private LocalDate updatedDate;
    private String checkedBy;
    private LocalDate checkedDate;
    private String claimedBy;
    private java.time.LocalDateTime claimedAt;
    private String remarks;
    private String checkerRemarks;

}
