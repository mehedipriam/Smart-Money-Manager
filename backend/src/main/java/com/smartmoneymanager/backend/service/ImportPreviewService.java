package com.smartmoneymanager.backend.service;

import org.springframework.web.multipart.MultipartFile;

import com.smartmoneymanager.backend.dto.response.ImportPreviewResponse;

public interface ImportPreviewService {

    /** Detects the file's source, parses it, flags likely duplicates, and suggests categories. Read-only. */
    ImportPreviewResponse preview(Long userId, MultipartFile file);
}
