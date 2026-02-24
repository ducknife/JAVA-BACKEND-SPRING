package com.ducknife.project.modules.auditlog;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.AppException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }
}
