package com.hrms.actionreason.repository;

import com.hrms.actionreason.entity.ActionReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;

public interface ActionReasonRepository
        extends JpaRepository<ActionReason, Long>,
        JpaSpecificationExecutor<ActionReason> {

    Optional<ActionReason> findByActionReasonName(String name);

    Optional<ActionReason> findByActionReasonNameIgnoreCase(String name);

    Optional<ActionReason> findByActionReasonCode(String code);

    Optional<ActionReason> findByActionReasonCodeIgnoreCase(String code);

    Optional<ActionReason> findByActionReasonCodeIgnoreCaseAndVersion(String code, Integer version);

    List<ActionReason> findByActionReasonCodeIgnoreCaseOrderByVersionDesc(String code);

    Optional<ActionReason> findTopByActionReasonCodeIgnoreCaseAndStatusOrderByVersionDesc(
            String code,
            com.hrms.actionreason.enums.Status status);

    boolean existsByActionReasonNameIgnoreCase(String name);

    boolean existsByActionReasonCodeIgnoreCase(String code);

    List<ActionReason> findByActionReasonRefIdOrderByVersionAsc(Long actionReasonRefId);

    List<ActionReason> findByActionReasonRefId(Long actionReasonRefId);

    Optional<ActionReason> findByActionReasonRefIdAndStatus(Long actionReasonRefId, com.hrms.actionreason.enums.Status status);

}
