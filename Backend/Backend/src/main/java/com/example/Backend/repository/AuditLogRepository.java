package com.example.Backend.repository;

import com.example.Backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByActorId(UUID actorId);
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

}
