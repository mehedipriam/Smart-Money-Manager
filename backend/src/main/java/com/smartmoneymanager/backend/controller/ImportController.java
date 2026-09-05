package com.smartmoneymanager.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.dto.common.ApiResponse;
import com.smartmoneymanager.backend.dto.response.ImportPreviewResponse;
import com.smartmoneymanager.backend.security.UserPrincipal;
import com.smartmoneymanager.backend.service.ImportPreviewService;

import lombok.RequiredArgsConstructor;

/**
 * Read-only file preview for the transaction-import feature. Nothing here ever creates a
 * transaction — the frontend submits whichever previewed rows it keeps through the normal
 * {@code POST /api/transactions} endpoint.
 */
@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final ImportPreviewService importPreviewService;

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<ImportPreviewResponse>> preview(
            @AuthenticationPrincipal UserPrincipal principal, @RequestParam("file") MultipartFile file) {
        ImportPreviewResponse preview = importPreviewService.preview(principal.getId(), file);
        return ResponseEntity.ok(ApiResponse.success("File processed", preview));
    }
}
