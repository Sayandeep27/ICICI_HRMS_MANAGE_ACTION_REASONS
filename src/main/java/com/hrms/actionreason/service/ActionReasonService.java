package com.hrms.actionreason.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hrms.actionreason.dto.ActionReasonDropdownRequest;
import com.hrms.actionreason.dto.ActionReasonRemarkRequest;
import com.hrms.actionreason.dto.ActionReasonRemarkResponse;
import com.hrms.actionreason.dto.ActionReasonResponse;
import com.hrms.actionreason.dto.ActionReasonViewResponse;
import com.hrms.actionreason.dto.ApproveRequest;
import com.hrms.actionreason.dto.CheckerActionRequest;
import com.hrms.actionreason.dto.ClaimRequest;
import com.hrms.actionreason.dto.CreateActionReasonRequest;
import com.hrms.actionreason.dto.DropdownRequest;
import com.hrms.actionreason.dto.DropdownValueResponse;
import com.hrms.actionreason.dto.HistoryRequest;
import com.hrms.actionreason.dto.InactivateActionReasonRequest;
import com.hrms.actionreason.dto.PushBackRequest;
import com.hrms.actionreason.dto.SearchRequest;
import com.hrms.actionreason.dto.SubmitActionReasonRequest;
import com.hrms.actionreason.dto.TrayResponse;
import com.hrms.actionreason.dto.UpdateActionReasonRequest;
import com.hrms.actionreason.dto.ViewRequest;

public interface ActionReasonService {

    ActionReasonResponse create(CreateActionReasonRequest request);

    ActionReasonResponse update(UpdateActionReasonRequest request);

    ActionReasonResponse submit(SubmitActionReasonRequest request);

    ActionReasonResponse claim(ClaimRequest request);

    ActionReasonResponse approve(ApproveRequest request);

    ActionReasonResponse reject(CheckerActionRequest request);

    ActionReasonResponse sendBack(PushBackRequest request);

    ActionReasonResponse inactivate(InactivateActionReasonRequest request);

    List<String> dropdown(ActionReasonDropdownRequest request);

    List<DropdownValueResponse> moduleDropdown(DropdownRequest request);

    List<DropdownValueResponse> moduleMasterDropdown(DropdownRequest request);

    List<DropdownValueResponse> linkedActionReasonDropdown(DropdownRequest request);

    ActionReasonRemarkResponse addRemark(ActionReasonRemarkRequest request);

    ActionReasonRemarkResponse uploadRemarkFile(
            Long tenantId,
            String actionReasonCode,
            String actorId,
            MultipartFile file);

    List<ActionReasonRemarkResponse> remarkHistory(HistoryRequest request);

    ActionReasonViewResponse view(HistoryRequest request);

    List<ActionReasonResponse> search(SearchRequest request);

    List<ActionReasonResponse> makerView(ViewRequest request);

    List<ActionReasonResponse> checkerView(ViewRequest request);

    List<?> history(HistoryRequest request);

    List<TrayResponse> tray(String checkerId);

}
