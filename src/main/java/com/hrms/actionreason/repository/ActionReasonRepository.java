package com.hrms.actionreason.repository;

import com.hrms.actionreason.entity.ActionReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ActionReasonRepository
        extends JpaRepository<ActionReason, Long>,
        JpaSpecificationExecutor<ActionReason> {

    Optional<ActionReason> findByActionReasonName(String name);

    Optional<ActionReason> findByActionReasonCode(String code);

}