package com.hrms.actionreason.service;

import com.hrms.actionreason.dto.*;

import java.util.List;

public interface ActionReasonService {

    void create(CreateActionReasonRequest request);

    void update(Long id, UpdateActionReasonRequest request);

    void submit(Long id);

    void approve(Long id, ApproveRequest request);

    void reject(Long id);

    void inactivate(Long id);

    List<?> search(SearchRequest request);

    List<?> history(Long id);

}