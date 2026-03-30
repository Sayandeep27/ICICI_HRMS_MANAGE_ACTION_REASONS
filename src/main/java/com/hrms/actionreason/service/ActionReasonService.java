package com.hrms.actionreason.service;

import java.util.List;

import com.hrms.actionreason.dto.ActionReasonResponse;
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

public interface ActionReasonService {

    ActionReasonResponse create(CreateActionReasonRequest request);

    ActionReasonResponse update(UpdateActionReasonRequest request);

    ActionReasonResponse submit(SubmitActionReasonRequest request);

    ActionReasonResponse claim(ClaimRequest request);

    ActionReasonResponse approve(ApproveRequest request);

    ActionReasonResponse reject(CheckerActionRequest request);

    ActionReasonResponse sendBack(PushBackRequest request);

    ActionReasonResponse inactivate(InactivateActionReasonRequest request);

    List<ActionReasonResponse> search(SearchRequest request);

    List<ActionReasonResponse> makerView(ViewRequest request);

    List<ActionReasonResponse> checkerView(ViewRequest request);

    List<?> history(HistoryRequest request);

    List<TrayResponse> tray(String checkerId);

}
