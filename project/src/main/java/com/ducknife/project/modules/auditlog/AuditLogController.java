package com.ducknife.project.modules.auditlog;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/audits")
public class AuditLogController {
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAudits() {
        return ApiResponse.ok(auditService.getAuditLogs());
    }
}
