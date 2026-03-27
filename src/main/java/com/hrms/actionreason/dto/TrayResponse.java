package com.hrms.actionreason.dto;

import java.time.LocalDateTime;

import com.hrms.actionreason.enums.TrayStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrayResponse {

    private Long trayId;
    private Long actionReasonId;
    private String actionReasonName;
    private String actionReasonCode;
    private String checkerId;
    private String module;
    private TrayStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime claimedAt;

}
