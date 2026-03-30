package com.hrms.actionreason.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionReasonVersionId {

    private Long actionReasonRefId;
    private String actionReasonCode;
    private Integer versionNumber;

}
