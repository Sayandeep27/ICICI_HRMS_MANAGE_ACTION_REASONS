package com.hrms.actionreason.entity;

import java.time.LocalDate;

import com.hrms.actionreason.enums.Status;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "action_reason")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;

    @Column(name = "action_reason_ref_id")
    private Long actionReasonRefId;

    @Column(name = "action_reason_name", length = 20)
    private String actionReasonName;

    @Column(name = "action_reason_code", length = 20)
    private String actionReasonCode;

    @Column(length = 40)
    private String description;

    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;

    @ManyToOne
    @JoinColumn(name = "module_master_id")
    private ModuleMaster moduleMaster;

    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    @ElementCollection
    @CollectionTable(
            name = "action_reason_linked_action",
            joinColumns = @JoinColumn(name = "action_reason_id")
    )
    @Column(name = "linked_action", length = 30)
    @OrderColumn(name = "sort_order")
    private java.util.List<String> linkedActions;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(length = 100)
    private String remarks;

    @Column(length = 100)
    private String checkerRemarks;

    @Column(length = 100)
    private String makerReplyRemarks;

    private Integer version;

    private LocalDate creationDate;

    private String createdBy;

    private LocalDate modifiedDate;

    private String modifiedBy;

    private String checkedBy;

    private LocalDate checkedDate;

    @Column(nullable = false)
    private Boolean linkedToActivePosition;

    private String currentAssignee;

    @Column(nullable = false)
    private Boolean makerReplyRequired;

}
