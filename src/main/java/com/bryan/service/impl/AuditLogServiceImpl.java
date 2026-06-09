package com.bryan.service.impl;

import com.bryan.entity.AuditLog;
import com.bryan.entity.User;
import com.bryan.repository.AuditLogRepository;
import com.bryan.repository.UserRepository;
import com.bryan.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public void log(String action, String entityType, Long entityId, String details) {
        AuditLog auditLog = new AuditLog(action, entityType, entityId, details);
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            if (email != null && !email.equals("anonymousUser")) {
                userRepository.findByEmail(email).ifPresent(auditLog::setPerformedBy);
            }
        } catch (Exception ignored) {
        }
        auditLogRepository.save(auditLog);
        log.debug("AuditLog: {} {} {} - {}", action, entityType, entityId, details);
    }
}
