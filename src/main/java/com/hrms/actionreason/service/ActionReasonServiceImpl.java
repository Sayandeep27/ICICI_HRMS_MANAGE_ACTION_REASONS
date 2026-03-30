package com.hrms.actionreason.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.hrms.actionreason.dto.ActionReasonDropdownRequest;
import com.hrms.actionreason.dto.ActionReasonRemarkRequest;
import com.hrms.actionreason.dto.ActionReasonRemarkResponse;
import com.hrms.actionreason.dto.ActionReasonResponse;
import com.hrms.actionreason.dto.ActionReasonVersionId;
import com.hrms.actionreason.dto.ApproveRequest;
import com.hrms.actionreason.dto.CheckerActionRequest;
import com.hrms.actionreason.dto.ClaimRequest;
import com.hrms.actionreason.dto.CreateActionReasonRequest;
import com.hrms.actionreason.dto.HistoryRequest;
import com.hrms.actionreason.dto.InactivateActionReasonRequest;
import com.hrms.actionreason.dto.PushBackRequest;
import com.hrms.actionreason.dto.SearchRequest;
import com.hrms.actionreason.dto.SubmitActionReasonRequest;
import com.hrms.actionreason.dto.TrayResponse;
import com.hrms.actionreason.dto.UpdateActionReasonRequest;
import com.hrms.actionreason.dto.ViewRequest;
import com.hrms.actionreason.entity.ActionReason;
import com.hrms.actionreason.entity.ActionReasonHistory;
import com.hrms.actionreason.entity.ActionReasonRemark;
import com.hrms.actionreason.entity.ActionReasonTray;
import com.hrms.actionreason.entity.Module;
import com.hrms.actionreason.entity.ModuleMaster;
import com.hrms.actionreason.enums.HistoryActionType;
import com.hrms.actionreason.enums.Status;
import com.hrms.actionreason.enums.TrayStatus;
import com.hrms.actionreason.exception.ResourceException;
import com.hrms.actionreason.repository.ActionReasonHistoryRepository;
import com.hrms.actionreason.repository.ActionReasonRepository;
import com.hrms.actionreason.repository.ActionReasonRemarkRepository;
import com.hrms.actionreason.repository.ActionReasonTrayRepository;
import com.hrms.actionreason.repository.ModuleMasterRepository;
import com.hrms.actionreason.repository.ModuleRepository;
import com.hrms.actionreason.specification.ActionReasonSpecification;
import com.hrms.actionreason.util.CodeGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
@Transactional
public class ActionReasonServiceImpl implements ActionReasonService {

    private final ActionReasonRepository actionReasonRepository;
    private final ActionReasonHistoryRepository historyRepository;
    private final ActionReasonRemarkRepository remarkRepository;
    private final ActionReasonTrayRepository trayRepository;
    private final ModuleRepository moduleRepository;
    private final ModuleMasterRepository moduleMasterRepository;

    @Override
    public ActionReasonResponse create(CreateActionReasonRequest request) {
        String normalizedName = normalizeName(request.getActionReasonName());
        validateUniqueName(normalizedName, null);

        String normalizedCode = CodeGeneratorUtil.generateCode(request.getActionReasonCode());
        validateUniqueCode(normalizedCode, null);

        ActionReason actionReason = ActionReason.builder()
                .tenantId(request.getTenantId())
                .actionReasonName(normalizedName)
                .actionReasonCode(normalizedCode)
                .description(normalizeDescription(request.getDescription()))
                .module(findModule(request.getModule()))
                .moduleMaster(findModuleMaster(request.getModuleMaster()))
                .effectiveStartDate(request.getEffectiveStartDate())
                .effectiveEndDate(null)
                .linkedActions(parseSingleValue(request.getLinkedAction()))
                .remarks(trimToNull(request.getRemarks()))
                .status(Status.DRAFT)
                .version(1)
                .creationDate(LocalDate.now())
                .createdBy(request.getMakerId().trim())
                .linkedToActivePosition(Boolean.TRUE.equals(request.getLinkedToActivePosition()))
                .makerReplyRequired(false)
                .build();

        ActionReason saved = actionReasonRepository.save(actionReason);
        if (saved.getActionReasonRefId() == null) {
            saved.setActionReasonRefId(saved.getId());
            saved = actionReasonRepository.save(saved);
        }
        saveHistory(saved, HistoryActionType.CREATED, saved.getCreatedBy());
        if (Boolean.TRUE.equals(request.getSubmit())) {
            saved = submitFromBody(saved, request.getMakerId().trim(), request.getRemarks());
        }
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse update(UpdateActionReasonRequest request) {
        ActionReason actionReason = getLatestByCode(request.getActionReasonCode());

        if (actionReason.getStatus() == Status.ACTIVE) {
            ActionReason draftVersion = buildNextDraftVersion(actionReason, request);
            ActionReason saved = actionReasonRepository.save(draftVersion);
            saveHistory(saved, HistoryActionType.UPDATED, saved.getModifiedBy());
            if (Boolean.TRUE.equals(request.getSubmit())) {
                saved = submitFromBody(saved, saved.getModifiedBy(), request.getRemarks());
            }
            return toResponse(saved);
        }

        ensureEditable(actionReason);
        applyUpdatableFields(actionReason, request);
        actionReason.setModifiedDate(LocalDate.now());
        actionReason.setStatus(Status.DRAFT);
        actionReason.setCurrentAssignee(null);

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.UPDATED, saved.getModifiedBy());
        if (Boolean.TRUE.equals(request.getSubmit())) {
            saved = submitFromBody(
                    saved,
                    trimToNull(request.getMakerId()),
                    request.getRemarks());
        }
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse submit(SubmitActionReasonRequest request) {
        ActionReason actionReason = getLatestSubmittableByCode(request.getActionReasonCode());
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

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.SUBMITTED, request.getMakerId().trim());
        return toResponse(saved);
    }

    private ActionReason submitFromBody(
            ActionReason actionReason,
            String makerId,
            String remarks) {

        if (isBlank(makerId)) {
            throw new ResourceException("Maker id is required when submit is true");
        }

        if (!(actionReason.getStatus() == Status.DRAFT
                || actionReason.getStatus() == Status.SENT_BACK
                || actionReason.getStatus() == Status.REJECTED)) {
            throw new ResourceException("Only draft, sent back or rejected records can be submitted");
        }

        if (actionReason.getMakerReplyRequired() && isBlank(remarks)) {
            throw new ResourceException("Maker remarks are mandatory after checker sends back remarks");
        }

        actionReason.setStatus(Status.PENDING_APPROVAL);
        actionReason.setCurrentAssignee(null);
        actionReason.setMakerReplyRemarks(trimToNull(remarks));
        actionReason.setMakerReplyRequired(false);
        actionReason.setModifiedBy(makerId.trim());
        actionReason.setModifiedDate(LocalDate.now());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.SUBMITTED, makerId.trim());
        return saved;
    }

    @Override
    public ActionReasonResponse claim(ClaimRequest request) {
        ActionReason actionReason = getLatestPendingByCode(request.getActionReasonCode());
        if (actionReason.getStatus() != Status.PENDING_APPROVAL) {
            throw new ResourceException("Only pending approval records can be claimed");
        }
        if (!isBlank(actionReason.getCurrentAssignee())
                && !request.getCheckerId().trim().equalsIgnoreCase(actionReason.getCurrentAssignee())) {
            throw new ResourceException("Action Reason is already claimed by another checker");
        }

        ActionReasonTray claimantTray = trayRepository.findByActionReasonIdAndCheckerId(actionReason.getId(), request.getCheckerId().trim())
                .orElse(ActionReasonTray.builder()
                        .actionReasonId(actionReason.getId())
                        .checkerId(request.getCheckerId().trim())
                        .moduleName(actionReason.getModule() != null
                                ? actionReason.getModule().getModuleName()
                                : null)
                        .assignedAt(LocalDateTime.now())
                        .status(TrayStatus.PENDING)
                        .build());

        List<ActionReasonTray> trayEntries = trayRepository.findByActionReasonId(actionReason.getId());
        if (trayEntries.stream().noneMatch(tray -> Objects.equals(tray.getCheckerId(), request.getCheckerId().trim()))) {
            trayEntries = new ArrayList<>(trayEntries);
            trayEntries.add(claimantTray);
        }
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
        ActionReason actionReason = getLatestPendingByCode(request.getActionReasonCode());
        ensureCheckerOwnership(actionReason, request.getCheckerId());

        activateApprovedVersion(actionReason, request.getCheckerId().trim(), trimToNull(request.getCheckerRemarks()));
        markTrayCompleted(actionReason.getId());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.APPROVED, request.getCheckerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse reject(CheckerActionRequest request) {
        ActionReason actionReason = getLatestPendingByCode(request.getActionReasonCode());
        ensureCheckerOwnership(actionReason, request.getCheckerId());

        actionReason.setStatus(Status.REJECTED);
        actionReason.setCheckedBy(request.getCheckerId().trim());
        actionReason.setCheckedDate(LocalDate.now());
        actionReason.setCheckerRemarks(request.getCheckerRemarks().trim());
        actionReason.setCurrentAssignee(null);
        actionReason.setMakerReplyRequired(false);

        markTrayCompleted(actionReason.getId());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.REJECTED, request.getCheckerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse sendBack(PushBackRequest request) {
        ActionReason actionReason = getLatestPendingByCode(request.getActionReasonCode());
        ensureCheckerOwnership(actionReason, request.getCheckerId());

        actionReason.setStatus(Status.SENT_BACK);
        actionReason.setCheckedBy(request.getCheckerId().trim());
        actionReason.setCheckedDate(LocalDate.now());
        actionReason.setCheckerRemarks(request.getCheckerRemarks().trim());
        actionReason.setCurrentAssignee(actionReason.getCreatedBy());
        actionReason.setMakerReplyRequired(true);

        markTrayCompleted(actionReason.getId());

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.SENT_BACK, request.getCheckerId().trim());
        return toResponse(saved);
    }

    @Override
    public ActionReasonResponse inactivate(InactivateActionReasonRequest request) {
        ActionReason actionReason = getLatestActiveByCode(request.getActionReasonCode());
        if (actionReason.getStatus() != Status.ACTIVE) {
            throw new ResourceException("Only active action reasons can be inactivated");
        }
        if (Boolean.TRUE.equals(actionReason.getLinkedToActivePosition())) {
            throw new ResourceException("Cannot be inactive");
        }

        actionReason.setStatus(Status.INACTIVE);
        actionReason.setEffectiveEndDate(LocalDate.now());
        actionReason.setModifiedBy(request.getMakerId().trim());
        actionReason.setModifiedDate(LocalDate.now());
        actionReason.setCurrentAssignee(null);
        if (!isBlank(request.getRemarks())) {
            actionReason.setRemarks(trimToNull(request.getRemarks()));
        }

        ActionReason saved = actionReasonRepository.save(actionReason);
        saveHistory(saved, HistoryActionType.INACTIVATED, request.getMakerId().trim());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> dropdown(ActionReasonDropdownRequest request) {
        return actionReasonRepository.findAll().stream()
                .filter(actionReason -> actionReason.getStatus() == Status.ACTIVE)
                .sorted(Comparator.comparing(ActionReason::getActionReasonName, String.CASE_INSENSITIVE_ORDER))
                .map(ActionReason::getActionReasonName)
                .distinct()
                .toList();
    }

    @Override
    public ActionReasonRemarkResponse addRemark(ActionReasonRemarkRequest request) {
        if (request.getTenantId() == null) {
            throw new ResourceException("Tenant id is required");
        }
        if (isBlank(request.getActionReasonCode())) {
            throw new ResourceException("Action Reason Code is required");
        }
        if (isBlank(request.getActorId())) {
            throw new ResourceException("Actor id is required");
        }
        if (isBlank(request.getRemarks())) {
            throw new ResourceException("Remarks are required");
        }

        ActionReason actionReason = getLatestByCode(request.getActionReasonCode());
        ActionReasonRemark saved = remarkRepository.save(ActionReasonRemark.builder()
                .tenantId(request.getTenantId())
                .actionReasonId(actionReason.getId())
                .actionReasonCode(actionReason.getActionReasonCode())
                .versionNumber(actionReason.getVersion())
                .remarks(trimToNull(request.getRemarks()))
                .actorId(request.getActorId().trim())
                .createdAt(LocalDateTime.now())
                .build());
        return toRemarkResponse(saved);
    }

    @Override
    public ActionReasonRemarkResponse uploadRemarkFile(
            Long tenantId,
            String actionReasonCode,
            String actorId,
            MultipartFile file) {

        if (tenantId == null) {
            throw new ResourceException("Tenant id is required");
        }
        if (isBlank(actionReasonCode)) {
            throw new ResourceException("Action Reason Code is required");
        }
        if (isBlank(actorId)) {
            throw new ResourceException("Actor id is required");
        }
        if (file == null || file.isEmpty()) {
            throw new ResourceException("File is required");
        }

        ActionReason actionReason = getLatestByCode(actionReasonCode);
        validateAttachment(file);

        String storedPath = storeAttachment(actionReason.getActionReasonCode(), file);
        ActionReasonRemark saved = remarkRepository.save(ActionReasonRemark.builder()
                .tenantId(tenantId)
                .actionReasonId(actionReason.getId())
                .actionReasonCode(actionReason.getActionReasonCode())
                .versionNumber(actionReason.getVersion())
                .actorId(actorId.trim())
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(storedPath)
                .createdAt(LocalDateTime.now())
                .build());
        return toRemarkResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionReasonRemarkResponse> remarkHistory(HistoryRequest request) {
        getLatestByCode(request.getActionReasonCode());
        return remarkRepository.findByActionReasonCodeIgnoreCaseOrderByCreatedAtDesc(request.getActionReasonCode().trim()).stream()
                .map(this::toRemarkResponse)
                .toList();
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
    public List<ActionReasonResponse> makerView(ViewRequest request) {
        return actionReasonRepository.findAll().stream()
                .filter(actionReason -> request.getUserId() == null
                        || request.getUserId().equalsIgnoreCase(actionReason.getCreatedBy()))
                .filter(actionReason -> matchesSearchKey(actionReason, request.getSearchKey()))
                .sorted(Comparator.comparing(ActionReason::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionReasonResponse> checkerView(ViewRequest request) {
        return actionReasonRepository.findAll().stream()
                .filter(actionReason -> request.getUserId() == null
                        || request.getUserId().equalsIgnoreCase(actionReason.getCurrentAssignee())
                        || trayRepository.findByActionReasonId(actionReason.getId()).stream()
                        .anyMatch(tray -> request.getUserId().equalsIgnoreCase(tray.getCheckerId())))
                .filter(actionReason -> matchesSearchKey(actionReason, request.getSearchKey()))
                .sorted(Comparator.comparing(ActionReason::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActionReasonHistory> history(HistoryRequest request) {
        ActionReason actionReason = getLatestByCode(request.getActionReasonCode());
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

    private ActionReason getActionReason(String code, Integer versionNumber) {
        return actionReasonRepository.findByActionReasonCodeIgnoreCaseAndVersion(code.trim(), versionNumber)
                .orElseThrow(() -> new ResourceException("Action Reason not found"));
    }

    private ActionReason getLatestByCode(String code) {
        return actionReasonRepository.findByActionReasonCodeIgnoreCaseOrderByVersionDesc(code.trim()).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceException("Action Reason not found"));
    }

    private ActionReason getLatestSubmittableByCode(String code) {
        return actionReasonRepository.findByActionReasonCodeIgnoreCaseOrderByVersionDesc(code.trim()).stream()
                .filter(actionReason -> actionReason.getStatus() == Status.DRAFT
                        || actionReason.getStatus() == Status.SENT_BACK
                        || actionReason.getStatus() == Status.REJECTED)
                .findFirst()
                .orElseThrow(() -> new ResourceException("Draft Action Reason not found"));
    }

    private ActionReason getLatestPendingByCode(String code) {
        return actionReasonRepository.findTopByActionReasonCodeIgnoreCaseAndStatusOrderByVersionDesc(
                        code.trim(),
                        Status.PENDING_APPROVAL)
                .orElseThrow(() -> new ResourceException("Pending approval Action Reason not found"));
    }

    private ActionReason getLatestActiveByCode(String code) {
        return actionReasonRepository.findTopByActionReasonCodeIgnoreCaseAndStatusOrderByVersionDesc(
                        code.trim(),
                        Status.ACTIVE)
                .orElseThrow(() -> new ResourceException("Active Action Reason not found"));
    }

    private void validateUniqueName(String name, Long currentId) {
        actionReasonRepository.findByActionReasonNameIgnoreCase(name)
                .filter(existing -> isDifferentLogicalRecord(existing, currentId))
                .ifPresent(existing -> {
                    throw new ResourceException("Action Reason Name already exists");
                });
    }

    private void validateUniqueCode(String code, Long currentId) {
        if (currentId == null && actionReasonRepository.existsByActionReasonCodeIgnoreCase(code)) {
            throw new ResourceException("Action Reason Code already exists");
        }
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
        ActionReason current = actionReasonRepository.findById(currentId)
                .orElseThrow(() -> new ResourceException("Action Reason not found"));
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

    private Module findModule(String moduleName) {
        return moduleRepository.findByModuleNameIgnoreCase(moduleName.trim())
                .orElseThrow(() -> new ResourceException("Module not found"));
    }

    private ModuleMaster findModuleMaster(String moduleMasterName) {
        if (moduleMasterName == null || moduleMasterName.isBlank()) {
            return null;
        }
        return moduleMasterRepository.findByModuleMasterNameIgnoreCase(moduleMasterName.trim())
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

    private List<String> parseDelimitedValues(String rawValue) {
        if (isBlank(rawValue)) {
            return new ArrayList<>();
        }
        return java.util.Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> parseSingleChecker(String checkerId) {
        if (isBlank(checkerId)) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        values.add(checkerId.trim());
        return values;
    }

    private List<String> parseSingleValue(String rawValue) {
        if (isBlank(rawValue)) {
            return new ArrayList<>();
        }
        List<String> values = new ArrayList<>();
        values.add(rawValue.trim());
        return values;
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {
        return trimToNull(description) == null ? null : CodeGeneratorUtil.toTitleCase(description);
    }

    private void validateAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        long maxSize = 2L * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new ResourceException("Attachment size cannot exceed 2MB");
        }
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!List.of("png", "jpg", "jpeg", "pdf").contains(extension)) {
            throw new ResourceException("Only png, jpg, jpeg and pdf files are allowed");
        }
    }

    private String storeAttachment(String actionReasonCode, MultipartFile file) {
        try {
            Path uploadDir = Paths.get("e:\\ICICI_HRMS_ACTION_REASON_MANAGE\\actionreason\\uploads\\action-reason-remarks",
                    actionReasonCode);
            Files.createDirectories(uploadDir);
            String originalName = file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename();
            String storedName = System.currentTimeMillis() + "_" + originalName.replaceAll("[^A-Za-z0-9._-]", "_");
            Path target = uploadDir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException exception) {
            throw new ResourceException("Failed to store attachment");
        }
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

    private boolean matchesSearchKey(ActionReason actionReason, String searchKey) {
        if (isBlank(searchKey)) {
            return true;
        }
        String normalized = searchKey.trim().toLowerCase();
        return (actionReason.getActionReasonName() != null
                && actionReason.getActionReasonName().toLowerCase().contains(normalized))
                || (actionReason.getActionReasonCode() != null
                && actionReason.getActionReasonCode().toLowerCase().contains(normalized))
                || (actionReason.getDescription() != null
                && actionReason.getDescription().toLowerCase().contains(normalized));
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

        if (request.getModule() != null && !request.getModule().isBlank()) {
            actionReason.setModule(findModule(request.getModule()));
        }

        if (request.getModuleMaster() != null) {
            actionReason.setModuleMaster(findModuleMaster(request.getModuleMaster()));
        }

        if (request.getEffectiveStartDate() != null) {
            actionReason.setEffectiveStartDate(request.getEffectiveStartDate());
        }

        if (request.getLinkedAction() != null) {
            actionReason.setLinkedActions(parseSingleValue(request.getLinkedAction()));
        }

        if (request.getLinkedToActivePosition() != null) {
            actionReason.setLinkedToActivePosition(request.getLinkedToActivePosition());
        }

        if (request.getRemarks() != null) {
            actionReason.setRemarks(trimToNull(request.getRemarks()));
        }

        if (request.getMakerId() != null && !request.getMakerId().isBlank()) {
            actionReason.setModifiedBy(request.getMakerId().trim());
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
                .modifiedBy(trimToNull(request.getMakerId()))
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

    private ActionReasonRemarkResponse toRemarkResponse(ActionReasonRemark remark) {
        return ActionReasonRemarkResponse.builder()
                .id(remark.getId())
                .tenantId(remark.getTenantId())
                .actionReasonCode(remark.getActionReasonCode())
                .versionNumber(remark.getVersionNumber())
                .remarks(remark.getRemarks())
                .actorId(remark.getActorId())
                .fileName(remark.getFileName())
                .fileType(remark.getFileType())
                .fileSize(remark.getFileSize())
                .createdAt(remark.getCreatedAt())
                .build();
    }

    private ActionReasonResponse toResponse(ActionReason actionReason) {
        LocalDateTime claimedAt = null;
        if (actionReason.getCurrentAssignee() != null) {
            claimedAt = trayRepository.findByActionReasonIdAndCheckerId(actionReason.getId(), actionReason.getCurrentAssignee())
                    .map(ActionReasonTray::getClaimedAt)
                    .orElse(null);
        }

        return ActionReasonResponse.builder()
                .tenantId(actionReason.getTenantId())
                .pkId(actionReason.getId())
                .id(ActionReasonVersionId.builder()
                        .actionReasonRefId(actionReason.getActionReasonRefId())
                        .actionReasonCode(actionReason.getActionReasonCode())
                        .versionNumber(actionReason.getVersion())
                        .build())
                .actionReasonName(actionReason.getActionReasonName())
                .description(actionReason.getDescription())
                .module(actionReason.getModule() != null ? actionReason.getModule().getModuleName() : null)
                .moduleMaster(actionReason.getModuleMaster() != null
                        ? actionReason.getModuleMaster().getModuleMasterName()
                        : null)
                .linkedAction(normalizeLinkedActions(actionReason.getLinkedActions()).stream().findFirst().orElse(null))
                .status(actionReason.getStatus() == Status.ACTIVE)
                .workflowStatus(actionReason.getStatus())
                .effectiveFrom(actionReason.getEffectiveStartDate())
                .effectiveTo(actionReason.getEffectiveEndDate())
                .createdBy(actionReason.getCreatedBy())
                .createdDate(actionReason.getCreationDate())
                .updatedBy(actionReason.getModifiedBy())
                .updatedDate(actionReason.getModifiedDate())
                .checkedBy(actionReason.getCheckedBy())
                .checkedDate(actionReason.getCheckedDate())
                .claimedBy(actionReason.getCurrentAssignee())
                .claimedAt(claimedAt)
                .remarks(actionReason.getRemarks())
                .checkerRemarks(actionReason.getCheckerRemarks())
                .build();
    }

}
