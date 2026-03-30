package com.hrms.actionreason.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hrms.actionreason.entity.ActionReasonRemark;

public interface ActionReasonRemarkRepository extends JpaRepository<ActionReasonRemark, Long> {

    List<ActionReasonRemark> findByActionReasonCodeIgnoreCaseOrderByCreatedAtDesc(String actionReasonCode);

}
