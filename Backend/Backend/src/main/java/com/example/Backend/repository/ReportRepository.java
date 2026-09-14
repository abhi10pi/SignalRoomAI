package com.example.Backend.repository;

import com.example.Backend.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    List<Report> findByStatus(String status);
    List<Report> findBySignalId(UUID signalId);
    List<Report> findByOpinionId(UUID opinionId);
    List<Report> findByCommentId(UUID commentId);
}
