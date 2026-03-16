package com.hrms.actionreason.controller;

import com.hrms.actionreason.dto.ApiResponse;
import com.hrms.actionreason.dto.ApproveRequest;
import com.hrms.actionreason.dto.CreateActionReasonRequest;
import com.hrms.actionreason.dto.SearchRequest;
import com.hrms.actionreason.dto.UpdateActionReasonRequest;
import com.hrms.actionreason.service.ActionReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v4/action-reasons")
@RequiredArgsConstructor
public class ActionReasonController {

    private final ActionReasonService service;


    // CREATE
    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @RequestBody CreateActionReasonRequest request) {

        service.create(request);

        return ResponseEntity.ok(
                new ApiResponse("Action Reason Created Successfully")
        );
    }


    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateActionReasonRequest request) {

        service.update(id, request);

        return ResponseEntity.ok(
                new ApiResponse("Action Reason Updated Successfully")
        );
    }


    // SUBMIT
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse> submit(@PathVariable Long id) {

        service.submit(id);

        return ResponseEntity.ok(
                new ApiResponse("Action Reason Submitted Successfully")
        );
    }


    // APPROVE
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse> approve(
            @PathVariable Long id,
            @RequestBody ApproveRequest request) {

        service.approve(id, request);

        return ResponseEntity.ok(
                new ApiResponse("Action Reason Approved Successfully")
        );
    }


    // REJECT
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse> reject(@PathVariable Long id) {

        service.reject(id);

        return ResponseEntity.ok(
                new ApiResponse("Action Reason Rejected Successfully")
        );
    }


    // INACTIVATE
    @PostMapping("/{id}/inactivate")
    public ResponseEntity<ApiResponse> inactivate(@PathVariable Long id) {

        service.inactivate(id);

        return ResponseEntity.ok(
                new ApiResponse("Action Reason Inactivated Successfully")
        );
    }


    // SEARCH
    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {

        return ResponseEntity.ok(service.search(request));
    }


    // HISTORY
    @GetMapping("/{id}/history")
    public ResponseEntity<?> history(@PathVariable Long id) {

        return ResponseEntity.ok(service.history(id));
    }

}