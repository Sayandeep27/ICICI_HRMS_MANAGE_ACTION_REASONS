package com.hrms.actionreason.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionReasonRemarkResponse {

    private Long id;

    private Long tenantId;

    private String actionReasonCode;

    private Integer versionNumber;

    private String remarks;

    private String actorId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private LocalDateTime createdAt;

}
