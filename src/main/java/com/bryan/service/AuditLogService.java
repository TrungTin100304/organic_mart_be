package com.bryan.service;

public interface AuditLogService {
    void log(String action, String entityType, Long entityId, String details);
}
