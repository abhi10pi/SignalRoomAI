package com.example.Backend.controller;

import com.example.Backend.entity.Report;
import com.example.Backend.service.ModerationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    /** Any authenticated user can submit a report */
    @PostMapping("/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public Report submitReport(@RequestBody Map<String, String> body, Authentication auth) {
        UUID signalId  = body.containsKey("signalId")  ? UUID.fromString(body.get("signalId"))  : null;
        UUID opinionId = body.containsKey("opinionId") ? UUID.fromString(body.get("opinionId")) : null;
        UUID commentId = body.containsKey("commentId") ? UUID.fromString(body.get("commentId")) : null;
        return moderationService.submitReport(
                userId(auth), signalId, opinionId, commentId,
                body.get("reason"), body.get("detail"));
    }

    /** Admin: list pending reports */
    @GetMapping("/admin/reports")
    public List<Report> getPendingReports() {
        return moderationService.getPendingReports();
    }

    /** Admin: list all reports */
    @GetMapping("/admin/reports/all")
    public List<Report> getAllReports() {
        return moderationService.getAllReports();
    }

    /** Admin: action or dismiss a report */
    @PostMapping("/admin/reports/{id}/review")
    public Report reviewReport(@PathVariable UUID id,
                               @RequestBody Map<String, String> body,
                               Authentication auth) {
        return moderationService.reviewReport(id, userId(auth), body.getOrDefault("action", "dismiss"));
    }

    /** Admin: hide / restore opinion */
    @PostMapping("/admin/opinions/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideOpinion(@PathVariable UUID id) {
        moderationService.hideOpinion(id);
    }

    @PostMapping("/admin/opinions/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreOpinion(@PathVariable UUID id) {
        moderationService.restoreOpinion(id);
    }

    /** Admin: hide / restore comment */
    @PostMapping("/admin/comments/{id}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hideComment(@PathVariable UUID id) {
        moderationService.hideComment(id);
    }

    @PostMapping("/admin/comments/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreComment(@PathVariable UUID id) {
        moderationService.restoreComment(id);
    }

    /** Admin: retry failed AI processing for a signal */
    @PostMapping("/admin/signals/{id}/retry-ai")
    public Map<String, String> retryAi(@PathVariable UUID id) {
        return moderationService.retryFailedAi(id);
    }

    private UUID userId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
