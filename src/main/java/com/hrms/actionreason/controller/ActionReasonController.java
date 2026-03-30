package com.hrms.actionreason.controller;

import com.hrms.actionreason.dto.ApiResponse;
import com.hrms.actionreason.dto.ApproveRequest;
import com.hrms.actionreason.dto.CheckerActionRequest;
import com.hrms.actionreason.dto.ClaimRequest;
import com.hrms.actionreason.dto.CreateActionReasonRequest;
import com.hrms.actionreason.dto.HistoryRequest;
import com.hrms.actionreason.dto.InactivateActionReasonRequest;
import com.hrms.actionreason.dto.PushBackRequest;
import com.hrms.actionreason.dto.SearchRequest;
import com.hrms.actionreason.dto.SubmitActionReasonRequest;
import com.hrms.actionreason.dto.UpdateActionReasonRequest;
import com.hrms.actionreason.dto.ViewRequest;
import com.hrms.actionreason.service.ActionReasonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v4/action-reasons", "/api/v1/action-reasons"})
@RequiredArgsConstructor
public class ActionReasonController {

    private final ActionReasonService service;

    @PostMapping({"", "/create"})
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CreateActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason created successfully",
                service.create(request)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<?>> update(@Valid @RequestBody UpdateActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason updated successfully",
                service.update(request)));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<?>> submit(@Valid @RequestBody SubmitActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason submitted successfully",
                service.submit(request)));
    }

    @PostMapping("/claim")
    public ResponseEntity<ApiResponse<?>> claim(@Valid @RequestBody ClaimRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason claimed successfully",
                service.claim(request)));
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<?>> approve(@Valid @RequestBody ApproveRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason approved successfully",
                service.approve(request)));
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<?>> reject(@Valid @RequestBody CheckerActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason rejected successfully",
                service.reject(request)));
    }

    @PostMapping({"/send-back", "/push-back"})
    public ResponseEntity<ApiResponse<?>> sendBack(@Valid @RequestBody PushBackRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason sent back successfully",
                service.sendBack(request)));
    }

    @PostMapping("/inactivate")
    public ResponseEntity<ApiResponse<?>> inactivate(@Valid @RequestBody InactivateActionReasonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason inactivated successfully",
                service.inactivate(request)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<?>> search(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason search fetched successfully",
                service.search(request)));
    }

    @PostMapping("/maker-view")
    public ResponseEntity<ApiResponse<?>> makerView(@RequestBody ViewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason maker view fetched successfully",
                service.makerView(request)));
    }

    @PostMapping("/checker-view")
    public ResponseEntity<ApiResponse<?>> checkerView(@RequestBody ViewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason checker view fetched successfully",
                service.checkerView(request)));
    }

    @PostMapping("/history")
    public ResponseEntity<ApiResponse<?>> history(@Valid @RequestBody HistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason history fetched successfully",
                service.history(request)));
    }

    @GetMapping("/tray")
    public ResponseEntity<ApiResponse<?>> tray(@RequestParam String checkerId) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Action Reason tray fetched successfully",
                service.tray(checkerId)));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<?>> health() {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Manage Action Reasons backend is up",
                null));
    }

}
