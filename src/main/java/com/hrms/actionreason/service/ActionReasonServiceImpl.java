package com.hrms.actionreason.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hrms.actionreason.dto.ActionReasonResponse;
import com.hrms.actionreason.dto.ApproveRequest;
import com.hrms.actionreason.dto.CheckerActionRequest;
import com.hrms.actionreason.dto.ClaimRequest;
import com.hrms.actionreason.dto.CreateActionReasonRequest;
import com.hrms.actionreason.dto.HistoryRequest;
import com.hrms.actionreason.dto.InactivateActionReasonRequest;
import com.hrms.actionreason.dto.SearchRequest;
import com.hrms.actionreason.dto.SubmitActionReasonRequest;
import com.hrms.actionreason.dto.TrayResponse;
import com.hrms.actionreason.dto.UpdateActionReasonRequest;
import com.hrms.actionreason.entity.ActionReason;
import com.hrms.actionreason.entity.ActionReasonHistory;
import com.hrms.actionreason.entity.ActionReasonTray;
import com.hrms.actionreason.entity.Module;
import com.hrms.actionreason.entity.ModuleMaster;
import com.hrms.actionreason.enums.HistoryActionType;
import com.hrms.actionreason.enums.Status;
import com.hrms.actionreason.enums.TrayStatus;
import com.hrms.actionreason.exception.ResourceException;
import com.hrms.actionreason.repository.ActionReasonHistoryRepository;
import com.hrms.actionreason.repository.ActionReasonRepository;
import com.hrms.actionreason.repository.ActionReasonTrayRepository;
import com.hrms.actionreason.repository.ModuleMasterRepository;
import com.hrms.actionreason.repository.ModuleRepository;
import com.hrms.actionreason.specification.ActionReasonSpecification;
import com.hrms.actionreason.util.CodeGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActionReasonServiceImpl implements ActionReasonService {

    private final ActionReasonRepository actionReasonRepository;
    private final ActionReasonHistoryRepository historyRepository;
    private final ActionReasonTrayRepository trayRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleMasterRepository moduleMasterRepository;

    @Override
    public ActionReasonResponse create(CreateActionReasonRequest request) {
        String normalizedName = normalizeName(request.getActionReasonName());
        validateUniqueName(normalizedName, null);

        String generatedCode = CodeGeneratorUtil.generateCode(normalizedName);
        validateUniqueCode(generatedCode, null);

        ActionReason actionReason = ActionReason.builder()
                .actionReasonName(normalizedName)
                .actionReasonCode(generatedCode)
                .description(normalizeDescription(request.getDescription()))
                .module(findModule(request.getModuleId()))
                .moduleMaster(findModuleMaster(request.getModuleMasterId()))
                .effectiveStartDate(request.getEffectiveStartDate())
                .effectiveEndDate(null)
                .linkedActions(normalizeLinkedActions(request.getLinkedActions()))
                .remarks(trimToNull(request.getRemarks()))
                .status(Status.DRAFT)
                .version(1)
                .creationDate(LocalDate.now())
                .createdBy(request.getCreatedBy().trim())
                .linkedToActivePosition(Boolean.TRUE.equals(request.getLinkedToActivePosition()))
                .makerReplyRequired(false)
                .build();

        ActionReason saved = actionReasonRepository.save(actionReason);
        if (saved.getActionReasonRefId() == null) {
            saved.setActionReasonRefId(saved.getId());
            saved = actionReasonRepository.save(saved);
        }
        saveHistory(saved, HistoryActionType.CREATED, saved.getCreatedBy());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse update(UpdateActionReasonRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());

        if (actionReason.getStatus() == Status.ACTIVE) {
            ActionReason draftVersion = buildNextDraftVersion(actionReason, request);
            ActionReason saved = actionReasonRepository.save(draftVersion);
            saveHistory(saved, HistoryActionType.UPDATED, saved.getModifiedBy());
            return toResponse(saved);
        }

        ensureEditable(actionReason);
        applyUpdatableFields(actionReason, request);
        actionReason.setModifiedDate(LocalDate.now());
        actionReason.setStatus(Status.DRAFT);
        actionReason.setCurrentAssignee(null);

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.UPDATED, saved.getModifiedBy());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse submit(SubmitActionReasonRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());
        if (!(actionReason.getStatus() == Status.DRAFT
                || actionReason.getStatus() == Status.SENT_BACK
                || actionReason.getStatus() == Status.REJECTED)) {
            throw new ResourceException("Only draft, sent back or rejected records can be submitted");
        }

        if (actionReason.getMakerReplyRequired() && isBlank(request.getRemarks())) {
            throw new ResourceException("Maker remarks are mandatory after checker sends back remarks");
        }

        actionReason.setStatus(Status.PENDING_APPROVAL);
        actionReason.setCurrentAssignee(null);
        actionReason.setMakerReplyRemarks(trimToNull(request.getRemarks()));
        actionReason.setMakerReplyRequired(false);
        actionReason.setModifiedBy(request.getMakerId().trim());
        actionReason.setModifiedDate(LocalDate.now());

        clearTray(actionReason.getId());
        createTrayEntries(actionReason, request.getCheckerIds());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.SUBMITTED, request.getMakerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse claim(ClaimRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());
        if (actionReason.getStatus() != Status.PENDING_APPROVAL) {
            throw new ResourceException("Only pending approval records can be claimed");
        }

        ActionReasonTray claimantTray = trayRepository.findByActionReasonIdAndCheckerId(actionReason.getId(), request.getCheckerId().trim())
                .orElseThrow(() -> new ResourceException("No tray assignment found for checker"));

        if (claimantTray.getStatus() == TrayStatus.COMPLETED) {
            throw new ResourceException("Completed tray item cannot be claimed");
        }

        List<ActionReasonTray> trayEntries = trayRepository.findByActionReasonId(actionReason.getId());
        for (ActionReasonTray tray : trayEntries) {
            if (Objects.equals(tray.getCheckerId(), request.getCheckerId().trim())) {
                tray.setStatus(TrayStatus.CLAIMED);
                tray.setClaimedAt(LocalDateTime.now());
            } else if (tray.getStatus() != TrayStatus.COMPLETED) {
                tray.setStatus(TrayStatus.HIDDEN);
            }
        }
        trayRepository.saveAll(trayEntries);

        actionReason.setCurrentAssignee(request.getCheckerId().trim());
        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.CLAIMED, request.getCheckerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse approve(ApproveRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());
        ensureCheckerOwnership(actionReason, request.getCheckerId());

        activateApprovedVersion(actionReason, request.getCheckerId().trim(), trimToNull(request.getRemarks()));
        markTrayCompleted(actionReason.getId());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.APPROVED, request.getCheckerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse reject(CheckerActionRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());
        ensureCheckerOwnership(actionReason, request.getCheckerId());

        actionReason.setStatus(Status.REJECTED);
        actionReason.setCheckedBy(request.getCheckerId().trim());
        actionReason.setCheckedDate(LocalDate.now());
        actionReason.setCheckerRemarks(request.getRemarks().trim());
        actionReason.setCurrentAssignee(null);
        actionReason.setMakerReplyRequired(false);

        markTrayCompleted(actionReason.getId());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.REJECTED, request.getCheckerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse sendBack(CheckerActionRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());
        ensureCheckerOwnership(actionReason, request.getCheckerId());

        actionReason.setStatus(Status.SENT_BACK);
        actionReason.setCheckedBy(request.getCheckerId().trim());
        actionReason.setCheckedDate(LocalDate.now());
        actionReason.setCheckerRemarks(request.getRemarks().trim());
        actionReason.setCurrentAssignee(actionReason.getCreatedBy());
        actionReason.setMakerReplyRequired(true);

        markTrayCompleted(actionReason.getId());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.SENT_BACK, request.getCheckerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse inactivate(InactivateActionReasonRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());
        if (actionReason.getStatus() != Status.ACTIVE) {
            throw new ResourceException("Only active action reasons can be inactivated");
        }
        if (Boolean.TRUE.equals(actionReason.getLinkedToActivePosition())) {
            throw new ResourceException("Cannot be inactive");
        }

        actionReason.setStatus(Status.INACTIVE);
        actionReason.setEffectiveEndDate(LocalDate.now());
        actionReason.setModifiedBy(request.getActorId().trim());
        actionReason.setModifiedDate(LocalDate.now());
        actionReason.setCurrentAssignee(null);

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.INACTIVATED, request.getActorId().trim());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionReasonResponse> search(SearchRequest request) {
        Specification<ActionReason> specification = ActionReasonSpecification.search(request);
        return actionReasonRepository.findAll(specification).stream()
                .sorted(Comparator.comparing(ActionReason::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionReasonHistory> history(HistoryRequest request) {
        ActionReason actionReason = getActionReason(request.getActionReasonId());
        List<Long> versionIds = actionReasonRepository.findByActionReasonRefId(actionReason.getActionReasonRefId()).stream()
                .map(ActionReason::getId)
                .toList();
        return historyRepository.findByActionReasonIdInOrderByVersionDescIdDesc(versionIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrayResponse> tray(String checkerId) {
        return trayRepository.findByCheckerIdOrderByAssignedAtDesc(checkerId).stream()
                .map(tray -> {
                    ActionReason actionReason = actionReasonRepository.findById(tray.getActionReasonId()).orElse(null);
                    return TrayResponse.builder()
                            .trayId(tray.getId())
                            .actionReasonId(tray.getActionReasonId())
                            .actionReasonName(actionReason != null ? actionReason.getActionReasonName() : null)
                            .actionReasonCode(actionReason != null ? actionReason.getActionReasonCode() : null)
                            .checkerId(tray.getCheckerId())
                            .module(tray.getModuleName())
                            .status(tray.getStatus())
                            .assignedAt(tray.getAssignedAt())
                            .claimedAt(tray.getClaimedAt())
                            .build();
                })
                .toList();
    }

    private ActionReason getActionReason(Long id) {
        return actionReasonRepository.findById(id)
                .orElseThrow(() -> new ResourceException("Action Reason not found"));
    }

    private void validateUniqueName(String name, Long currentId) {
        actionReasonRepository.findByActionReasonNameIgnoreCase(name)
                .filter(existing -> isDifferentLogicalRecord(existing, currentId))
                .ifPresent(existing -> {
                    throw new ResourceException("Action Reason Name already exists");
                });
    }

    private void validateUniqueCode(String code, Long currentId) {
        actionReasonRepository.findByActionReasonCodeIgnoreCase(code)
                .filter(existing -> isDifferentLogicalRecord(existing, currentId))
                .ifPresent(existing -> {
                    throw new ResourceException("Action Reason Code already exists");
                });
    }

    private void ensureEditable(ActionReason actionReason) {
        if (actionReason.getStatus() == Status.PENDING_APPROVAL || actionReason.getStatus() == Status.INACTIVE) {
            throw new ResourceException("Action Reason cannot be modified in current status");
        }
    }

    private boolean isDifferentLogicalRecord(ActionReason existing, Long currentId) {
        if (currentId == null) {
            return true;
        }
        ActionReason current = getActionReason(currentId);
        Long currentRefId = current.getActionReasonRefId() != null ? current.getActionReasonRefId() : current.getId();
        Long existingRefId = existing.getActionReasonRefId() != null ? existing.getActionReasonRefId() : existing.getId();
        return !existingRefId.equals(currentRefId);
    }

    private void ensureCheckerOwnership(ActionReason actionReason, String checkerId) {
        if (actionReason.getStatus() != Status.PENDING_APPROVAL) {
            throw new ResourceException("Checker action is allowed only for pending approval records");
        }
        if (!checkerId.trim().equals(actionReason.getCurrentAssignee())) {
            throw new ResourceException("Only current assignee can perform checker action");
        }
    }

    private Module findModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceException("Module not found"));
    }

    private ModuleMaster findModuleMaster(Long moduleMasterId) {
        if (moduleMasterId == null) {
            return null;
        }
        return moduleMasterRepository.findById(moduleMasterId)
                .orElseThrow(() -> new ResourceException("Module Master not found"));
    }

    private List<String> normalizeLinkedActions(List<String> linkedActions) {
        if (linkedActions == null) {
            return new ArrayList<>();
        }
        return linkedActions.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {
        return trimToNull(description) == null ? null : CodeGeneratorUtil.toTitleCase(description);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private int nextVersion(ActionReason actionReason) {
        Long refId = actionReason.getActionReasonRefId() != null ? actionReason.getActionReasonRefId() : actionReason.getId();
        return actionReasonRepository.findByActionReasonRefIdOrderByVersionAsc(refId).stream()
                .map(ActionReason::getVersion)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private void applyUpdatableFields(ActionReason actionReason, UpdateActionReasonRequest request) {
        if (request.getActionReasonName() != null && !request.getActionReasonName().isBlank()) {
            String normalizedName = normalizeName(request.getActionReasonName());
            validateUniqueName(normalizedName, actionReason.getId());
            actionReason.setActionReasonName(normalizedName);
        }

        if (request.getDescription() != null) {
            actionReason.setDescription(normalizeDescription(request.getDescription()));
        }

        if (request.getEffectiveStartDate() != null) {
            actionReason.setEffectiveStartDate(request.getEffectiveStartDate());
        }

        if (request.getLinkedActions() != null) {
            actionReason.setLinkedActions(normalizeLinkedActions(request.getLinkedActions()));
        }

        if (request.getLinkedToActivePosition() != null) {
            actionReason.setLinkedToActivePosition(request.getLinkedToActivePosition());
        }

        if (request.getRemarks() != null) {
            actionReason.setRemarks(trimToNull(request.getRemarks()));
        }

        if (request.getModifiedBy() != null && !request.getModifiedBy().isBlank()) {
            actionReason.setModifiedBy(request.getModifiedBy().trim());
        }
    }

    private ActionReason buildNextDraftVersion(ActionReason activeVersion, UpdateActionReasonRequest request) {
        Long refId = activeVersion.getActionReasonRefId() != null ? activeVersion.getActionReasonRefId() : activeVersion.getId();

        ActionReason draftVersion = ActionReason.builder()
                .actionReasonRefId(refId)
                .actionReasonName(activeVersion.getActionReasonName())
                .actionReasonCode(activeVersion.getActionReasonCode())
                .description(activeVersion.getDescription())
                .module(activeVersion.getModule())
                .moduleMaster(activeVersion.getModuleMaster())
                .effectiveStartDate(activeVersion.getEffectiveStartDate())
                .effectiveEndDate(null)
                .linkedActions(normalizeLinkedActions(activeVersion.getLinkedActions()))
                .status(Status.DRAFT)
                .remarks(activeVersion.getRemarks())
                .checkerRemarks(null)
                .makerReplyRemarks(null)
                .version(nextVersion(activeVersion))
                .creationDate(activeVersion.getCreationDate())
                .createdBy(activeVersion.getCreatedBy())
                .modifiedDate(LocalDate.now())
                .modifiedBy(trimToNull(request.getModifiedBy()))
                .checkedBy(null)
                .checkedDate(null)
                .linkedToActivePosition(activeVersion.getLinkedToActivePosition())
                .currentAssignee(null)
                .makerReplyRequired(false)
                .build();

        applyUpdatableFields(draftVersion, request);

        if (draftVersion.getEffectiveStartDate() == null) {
            throw new ResourceException("Effective Start Date is required");
        }

        if (!draftVersion.getEffectiveStartDate().isAfter(activeVersion.getEffectiveStartDate())) {
            throw new ResourceException("New version effective start date must be after current active version start date");
        }

        return draftVersion;
    }

    private void activateApprovedVersion(ActionReason approvedVersion, String checkerId, String remarks) {
        approvedVersion.setStatus(Status.ACTIVE);
        approvedVersion.setCheckedBy(checkerId);
        approvedVersion.setCheckedDate(LocalDate.now());
        approvedVersion.setCheckerRemarks(remarks);
        approvedVersion.setCurrentAssignee(null);
        approvedVersion.setEffectiveEndDate(null);

        ActionReason currentActive = actionReasonRepository
                .findByActionReasonRefIdAndStatus(approvedVersion.getActionReasonRefId(), Status.ACTIVE)
                .orElse(null);

        if (currentActive != null && !currentActive.getId().equals(approvedVersion.getId())) {
            LocalDate newStartDate = approvedVersion.getEffectiveStartDate();
            if (newStartDate == null) {
                throw new ResourceException("Effective Start Date is required for approval");
            }
            if (!newStartDate.isAfter(currentActive.getEffectiveStartDate())) {
                throw new ResourceException("Approved version start date must be after current active version start date");
            }
            currentActive.setEffectiveEndDate(newStartDate.minusDays(1));
            currentActive.setModifiedDate(LocalDate.now());
            currentActive.setModifiedBy(checkerId);
            actionReasonRepository.save(currentActive);
            saveHistory(currentActive, HistoryActionType.UPDATED, checkerId);
        }
    }

    private void createTrayEntries(ActionReason actionReason, List<String> checkerIds) {
        List<ActionReasonTray> trays = checkerIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .map(checkerId -> ActionReasonTray.builder()
                        .actionReasonId(actionReason.getId())
                        .checkerId(checkerId)
                        .moduleName(actionReason.getModule() != null
                                ? actionReason.getModule().getModuleName()
                                : null)
                        .status(TrayStatus.PENDING)
                        .assignedAt(LocalDateTime.now())
                        .build())
                .toList();
        trayRepository.saveAll(trays);
    }

    private void clearTray(Long actionReasonId) {
        List<ActionReasonTray> trays = trayRepository.findByActionReasonId(actionReasonId);
        if (!trays.isEmpty()) {
            trayRepository.deleteAll(trays);
        }
    }

    private void markTrayCompleted(Long actionReasonId) {
        List<ActionReasonTray> trays = trayRepository.findByActionReasonId(actionReasonId);
        for (ActionReasonTray tray : trays) {
            tray.setStatus(TrayStatus.COMPLETED);
            tray.setCompletedAt(LocalDateTime.now());
        }
        trayRepository.saveAll(trays);
    }

    private void saveHistory(ActionReason actionReason, HistoryActionType actionType, String actorId) {
        ActionReasonHistory history = ActionReasonHistory.builder()
                .actionReasonId(actionReason.getId())
                .actionReasonName(actionReason.getActionReasonName())
                .actionReasonCode(actionReason.getActionReasonCode())
                .description(actionReason.getDescription())
                .module(actionReason.getModule() != null ? actionReason.getModule().getModuleName() : null)
                .moduleMaster(actionReason.getModuleMaster() != null
                        ? actionReason.getModuleMaster().getModuleMasterName()
                        : null)
                .linkedActions(String.join(",", normalizeLinkedActions(actionReason.getLinkedActions())))
                .version(actionReason.getVersion())
                .creationDate(actionReason.getCreationDate())
                .createdBy(actionReason.getCreatedBy())
                .checkedBy(actionReason.getCheckedBy())
                .checkedDate(actionReason.getCheckedDate())
                .modifiedDate(actionReason.getModifiedDate())
                .modifiedBy(actionReason.getModifiedBy())
                .effectiveStartDate(actionReason.getEffectiveStartDate())
                .effectiveEndDate(actionReason.getEffectiveEndDate())
                .status(actionReason.getStatus())
                .actionType(actionType)
                .remarks(actionReason.getRemarks())
                .checkerRemarks(actionReason.getCheckerRemarks())
                .makerReplyRemarks(actionReason.getMakerReplyRemarks())
                .actorId(actorId)
                .actionedAt(LocalDateTime.now())
                .build();

        historyRepository.save(history);
    }

    private ActionReasonResponse toResponse(ActionReason actionReason) {
        return ActionReasonResponse.builder()
                .id(actionReason.getId())
                .actionReasonRefId(actionReason.getActionReasonRefId())
                .actionReasonName(actionReason.getActionReasonName())
                .actionReasonCode(actionReason.getActionReasonCode())
                .description(actionReason.getDescription())
                .module(actionReason.getModule() != null ? actionReason.getModule().getModuleName() : null)
                .moduleMaster(actionReason.getModuleMaster() != null
                        ? actionReason.getModuleMaster().getModuleMasterName()
                        : null)
                .effectiveStartDate(actionReason.getEffectiveStartDate())
                .effectiveEndDate(actionReason.getEffectiveEndDate())
                .linkedActions(normalizeLinkedActions(actionReason.getLinkedActions()))
                .status(actionReason.getStatus())
                .version(actionReason.getVersion())
                .createdBy(actionReason.getCreatedBy())
                .modifiedBy(actionReason.getModifiedBy())
                .checkedBy(actionReason.getCheckedBy())
                .currentAssignee(actionReason.getCurrentAssignee())
                .build();
    }

}
