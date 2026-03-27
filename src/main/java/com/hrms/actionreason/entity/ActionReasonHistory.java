package com.hrms.actionreason.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hrms.actionreason.enums.HistoryActionType;
import com.hrms.actionreason.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "action_reason_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionReasonHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actionReasonId;

    private String actionReasonName;

    private String actionReasonCode;

    private String description;

    private String module;

    private String moduleMaster;

    private String linkedActions;

    private Integer version;

    private LocalDate creationDate;

    private String createdBy;

    private String checkedBy;

    private LocalDate checkedDate;

    private LocalDate modifiedDate;

    private String modifiedBy;

    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private HistoryActionType actionType;

    private String remarks;

    private String checkerRemarks;

    private String makerReplyRemarks;

    private String actorId;

    private LocalDateTime actionedAt;

}
