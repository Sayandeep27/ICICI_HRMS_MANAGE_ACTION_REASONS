package com.hrms.actionreason.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionReasonViewHistoryItem {

    private LocalDateTime dateTime;

    private String actionBy;

    private String action;

    private String remarks;

    private String attachment;

}
