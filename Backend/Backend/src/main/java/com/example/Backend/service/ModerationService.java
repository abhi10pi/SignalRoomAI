package com.example.Backend.service;

import com.example.Backend.entity.Comment;
import com.example.Backend.entity.Opinion;
import com.example.Backend.entity.Report;
import com.example.Backend.entity.Signal;
import com.example.Backend.entity.User;
import com.example.Backend.enums.SignalStatus;
import com.example.Backend.exception.ResourceNotFoundException;
import com.example.Backend.repository.CommentRepository;
import com.example.Backend.repository.OpinionRepository;
import com.example.Backend.repository.ReportRepository;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ModerationService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final SignalRepository signalRepository;
    private final OpinionRepository opinionRepository;
    private final CommentRepository commentRepository;
    private final AiProcessingService aiProcessingService;

    public ModerationService(ReportRepository reportRepository,
                             UserRepository userRepository,
                             SignalRepository signalRepository,
                             OpinionRepository opinionRepository,
                             CommentRepository commentRepository,
                             AiProcessingService aiProcessingService) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.signalRepository = signalRepository;
        this.opinionRepository = opinionRepository;
        this.commentRepository = commentRepository;
        this.aiProcessingService = aiProcessingService;
    }

    @Transactional
    public Report submitReport(UUID reporterId, UUID signalId, UUID opinionId, UUID commentId, String reason, String detail) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        long targets = (signalId != null ? 1 : 0) + (opinionId != null ? 1 : 0) + (commentId != null ? 1 : 0);
        if (targets != 1) throw new IllegalArgumentException("Exactly one of signalId, opinionId, commentId must be set");

        Report report = new Report();
        report.setReporter(reporter);
        report.setSignalId(signalId);
        report.setOpinionId(opinionId);
        report.setCommentId(commentId);
        report.setReason(reason);
        report.setDetail(detail);
        return reportRepository.save(report);
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByStatus("PENDING");
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Transactional
    public Report reviewReport(UUID reportId, UUID adminId, String action) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        report.setStatus(action.equalsIgnoreCase("dismiss") ? "DISMISSED" : "ACTIONED");

        if ("action".equalsIgnoreCase(action)) {
            if (report.getOpinionId() != null) hideOpinion(report.getOpinionId());
            else if (report.getCommentId() != null) hideComment(report.getCommentId());
        }

        return reportRepository.save(report);
    }

    @Transactional
    public void hideOpinion(UUID opinionId) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opinion not found"));
        opinion.setHidden(true);
        opinionRepository.save(opinion);
    }

    @Transactional
    public void restoreOpinion(UUID opinionId) {
        Opinion opinion = opinionRepository.findById(opinionId)
                .orElseThrow(() -> new ResourceNotFoundException("Opinion not found"));
        opinion.setHidden(false);
        opinionRepository.save(opinion);
    }

    @Transactional
    public void hideComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        comment.setHidden(true);
        commentRepository.save(comment);
    }

    @Transactional
    public void restoreComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        comment.setHidden(false);
        commentRepository.save(comment);
    }

    @Transactional
    public Map<String, String> retryFailedAi(UUID signalId) {
        Signal signal = signalRepository.findById(signalId)
                .orElseThrow(() -> new ResourceNotFoundException("Signal not found"));
        if (signal.getStatus() != SignalStatus.FAILED && signal.getStatus() != SignalStatus.PROCESSING) {
            signal.setStatus(SignalStatus.PROCESSING);
            signalRepository.save(signal);
        }
        aiProcessingService.processSignal(signalId);
        return Map.of("status", "retried", "signalId", signalId.toString());
    }
}
