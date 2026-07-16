package com.bryan.controller.admin;

import com.bryan.dto.response.AdminAuditLogResponse;
import com.bryan.dto.response.ApiResponse;
import com.bryan.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AuditLogRepository repository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminAuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AdminAuditLogResponse> result =
                entityType == null || entityType.isBlank()
                        ? repository.findAll(pageable).map(AdminAuditLogResponse::from)
                        : repository.findByEntityType(entityType, pageable).map(AdminAuditLogResponse::from);
        return ApiResponse.success(result);
    }
}
