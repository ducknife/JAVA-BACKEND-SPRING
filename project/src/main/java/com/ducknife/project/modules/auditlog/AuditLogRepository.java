package com.ducknife.project.modules.auditlog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>{
    
}
