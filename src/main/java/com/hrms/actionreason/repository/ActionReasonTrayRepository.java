package com.hrms.actionreason.repository;

import java.util.List;
import java.util.Optional;

import com.hrms.actionreason.entity.ActionReasonTray;
import com.hrms.actionreason.enums.TrayStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionReasonTrayRepository extends JpaRepository<ActionReasonTray, Long> {

    List<ActionReasonTray> findByCheckerIdOrderByAssignedAtDesc(String checkerId);

    List<ActionReasonTray> findByActionReasonId(Long actionReasonId);

    Optional<ActionReasonTray> findByActionReasonIdAndCheckerId(Long actionReasonId, String checkerId);

    List<ActionReasonTray> findByActionReasonIdAndStatus(Long actionReasonId, TrayStatus status);

}
