package com.smartmoneymanager.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.request.CreateBillRequest;
import com.smartmoneymanager.backend.dto.request.UpdateBillRequest;
import com.smartmoneymanager.backend.dto.response.BillResponse;
import com.smartmoneymanager.backend.entity.enums.BillPaymentStatus;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.BillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) BillPaymentStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Bills fetched", billService.getBills(principal.getId(), status)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<BillResponse>>> upcoming(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming bills fetched", billService.getUpcomingBills(principal.getId(), limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> get(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill fetched", billService.getBill(principal.getId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody CreateBillRequest request) {
        BillResponse created = billService.createBill(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Bill created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody UpdateBillRequest request) {
        BillResponse updated = billService.updateBill(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Bill updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        billService.deleteBill(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Bill deleted"));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<BillResponse>> markAsPaid(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        BillResponse updated = billService.markAsPaid(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Bill marked as paid", updated));
    }
}
