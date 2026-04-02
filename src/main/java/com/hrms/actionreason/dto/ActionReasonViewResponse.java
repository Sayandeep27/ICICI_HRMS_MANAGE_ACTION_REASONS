package com.hrms.actionreason.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionReasonViewResponse {

    private Long tenantId;

    private String actionReasonName;

    private String actionReasonCode;

    private String description;

    private String module;

    private String moduleMaster;

    private String linkedAction;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Boolean active;

    private String workflowStatus;

    private LocalDate createdDate;

    private String createdBy;

    private Integer versionNumber;

    private List<ActionReasonViewHistoryItem> remarksHistory;

}
