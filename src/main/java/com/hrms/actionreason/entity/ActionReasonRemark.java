package com.hrms.actionreason.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "action_reason_remark")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionReasonRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "action_reason_remark_seq_gen")
    @SequenceGenerator(name = "action_reason_remark_seq_gen", sequenceName = "ACTION_REASON_REMARK_SEQ", allocationSize = 1)
    private Long id;

    private Long tenantId;

    private Long actionReasonId;

    private String actionReasonCode;

    private Integer versionNumber;

    private String remarks;

    private String actorId;

    private String fileName;

    private String fileType;

    private Long fileSize;

    private String filePath;

    private LocalDateTime createdAt;

}
