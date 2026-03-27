package com.hrms.actionreason.repository;

import com.hrms.actionreason.entity.ActionReasonHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionReasonHistoryRepository
        extends JpaRepository<ActionReasonHistory, Long> {

    List<ActionReasonHistory> findByActionReasonIdOrderByVersionDescIdDesc(Long id);

    List<ActionReasonHistory> findByActionReasonIdInOrderByVersionDescIdDesc(List<Long> ids);

}
