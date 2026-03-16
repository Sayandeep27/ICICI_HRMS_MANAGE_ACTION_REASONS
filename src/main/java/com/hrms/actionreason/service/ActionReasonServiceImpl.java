package com.hrms.actionreason.service;

import com.hrms.actionreason.dto.ApproveRequest;
import com.hrms.actionreason.dto.CreateActionReasonRequest;
import com.hrms.actionreason.dto.SearchRequest;
import com.hrms.actionreason.dto.UpdateActionReasonRequest;
import com.hrms.actionreason.entity.ActionReason;
import com.hrms.actionreason.entity.ActionReasonHistory;
import com.hrms.actionreason.entity.Module;
import com.hrms.actionreason.entity.ModuleMaster;
import com.hrms.actionreason.enums.Status;
import com.hrms.actionreason.repository.ActionReasonHistoryRepository;
import com.hrms.actionreason.repository.ActionReasonRepository;
import com.hrms.actionreason.repository.ModuleMasterRepository;
import com.hrms.actionreason.repository.ModuleRepository;
import com.hrms.actionreason.specification.ActionReasonSpecification;
import com.hrms.actionreason.util.CodeGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionReasonServiceImpl implements ActionReasonService {

    private final ActionReasonRepository actionReasonRepository;
    private final ActionReasonHistoryRepository historyRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleMasterRepository moduleMasterRepository;

    @Override
    public void create(CreateActionReasonRequest request) {

        actionReasonRepository.findByActionReasonName(request.getActionReasonName())
                .ifPresent(ar -> {
                    throw new RuntimeException("Action Reason Name already exists");
                });

        String generatedCode =
                CodeGeneratorUtil.generateCode(request.getActionReasonName());

        Module module = moduleRepository
                .findById(request.getModuleId())
                .orElse(null);

        ModuleMaster moduleMaster = moduleMasterRepository
                .findById(request.getModuleMasterId())
                .orElse(null);

        ActionReason actionReason = ActionReason.builder()
                .actionReasonName(request.getActionReasonName())
                .actionReasonCode(generatedCode)
                .description(request.getDescription())
                .module(module)
                .moduleMaster(moduleMaster)
                .effectiveStartDate(request.getEffectiveStartDate())
                .effectiveEndDate(request.getEffectiveEndDate())
                .remarks(request.getRemarks())
                .status(Status.DRAFT)
                .version(1)
                .creationDate(LocalDate.now())
                .createdBy(request.getCreatedBy())
                .build();

        actionReasonRepository.save(actionReason);
    }

    @Override
    public void update(Long id, UpdateActionReasonRequest request) {

        ActionReason actionReason = actionReasonRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Action Reason not found"));

        actionReason.setActionReasonName(request.getActionReasonName());
        actionReason.setDescription(request.getDescription());
        actionReason.setEffectiveEndDate(request.getEffectiveEndDate());
        actionReason.setRemarks(request.getRemarks());
        actionReason.setModifiedBy(request.getModifiedBy());
        actionReason.setModifiedDate(LocalDate.now());

        actionReasonRepository.save(actionReason);
    }

    @Override
    public void submit(Long id) {

        ActionReason actionReason = actionReasonRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Action Reason not found"));

        actionReason.setStatus(Status.SUBMITTED);

        actionReasonRepository.save(actionReason);
    }

    @Override
    public void approve(Long id, ApproveRequest request) {

        ActionReason actionReason = actionReasonRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Action Reason not found"));

        actionReason.setStatus(Status.APPROVED);
        actionReason.setCheckedBy(request.getCheckerId());

        actionReasonRepository.save(actionReason);

        saveHistory(actionReason);
    }

    @Override
    public void reject(Long id) {

        ActionReason actionReason = actionReasonRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Action Reason not found"));

        actionReason.setStatus(Status.REJECTED);

        actionReasonRepository.save(actionReason);
    }

    @Override
    public void inactivate(Long id) {

        ActionReason actionReason = actionReasonRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Action Reason not found"));

        actionReason.setStatus(Status.INACTIVE);

        actionReasonRepository.save(actionReason);
    }

    @Override
    public List<ActionReason> search(SearchRequest request) {

        Specification<ActionReason> specification =
                ActionReasonSpecification.search(
                        request.getField(),
                        request.getValue(),
                        request.getOperator()
                );

        return actionReasonRepository.findAll(specification);
    }

    @Override
    public List<ActionReasonHistory> history(Long id) {

        return historyRepository.findByActionReasonId(id);
    }

    private void saveHistory(ActionReason actionReason) {

        ActionReasonHistory history = ActionReasonHistory.builder()
                .actionReasonId(actionReason.getId())
                .actionReasonName(actionReason.getActionReasonName())
                .actionReasonCode(actionReason.getActionReasonCode())
                .description(actionReason.getDescription())
                .module(actionReason.getModule() != null
                        ? actionReason.getModule().getModuleName()
                        : null)
                .moduleMaster(actionReason.getModuleMaster() != null
                        ? actionReason.getModuleMaster().getModuleMasterName()
                        : null)
                .version(actionReason.getVersion())
                .creationDate(actionReason.getCreationDate())
                .createdBy(actionReason.getCreatedBy())
                .checkedBy(actionReason.getCheckedBy())
                .effectiveStartDate(actionReason.getEffectiveStartDate())
                .effectiveEndDate(actionReason.getEffectiveEndDate())
                .build();

        historyRepository.save(history);
    }

}