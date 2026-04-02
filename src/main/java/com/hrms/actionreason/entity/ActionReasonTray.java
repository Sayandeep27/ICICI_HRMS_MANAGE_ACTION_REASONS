package com.hrms.actionreason.entity;

import java.time.LocalDateTime;

import com.hrms.actionreason.enums.TrayStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "action_reason_tray")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionReasonTray {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "action_reason_tray_seq_gen")
    @SequenceGenerator(name = "action_reason_tray_seq_gen", sequenceName = "ACTION_REASON_TRAY_SEQ", allocationSize = 1)
    private Long id;

    private Long actionReasonId;

    private String checkerId;

    private String moduleName;

    @Enumerated(EnumType.STRING)
    private TrayStatus status;

    private LocalDateTime assignedAt;

    private LocalDateTime claimedAt;

    private LocalDateTime completedAt;

}
