package com.example.Backend.controller;

import com.example.Backend.dto.request.PromotionRequestDTO;
import com.example.Backend.dto.response.PromotionRequestResponse;
import com.example.Backend.entity.User;
import com.example.Backend.enums.Role;
import com.example.Backend.service.AdminService;
import com.example.Backend.entity.Signal;
import com.example.Backend.repository.SignalRepository;
import com.example.Backend.repository.ValidationRepository;
import com.example.Backend.enums.SignalStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class AdminController {

    private final AdminService adminService;
    private final SignalRepository signalRepository;
    private final ValidationRepository validationRepository;

    public AdminController(AdminService adminService, SignalRepository signalRepository, ValidationRepository validationRepository) {
        this.adminService = adminService;
        this.signalRepository = signalRepository;
        this.validationRepository = validationRepository;
    }

    // POST /api/promotion-requests — any user submits a request to become consultant
    @PostMapping("/promotion-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionRequestResponse submitRequest(@Valid @RequestBody PromotionRequestDTO dto,
                                                   Authentication auth) {
        return adminService.submitPromotionRequest(currentUserId(auth), dto);
    }

    // GET /api/promotion-requests/mine — my own requests
    @GetMapping("/promotion-requests/mine")
    public List<PromotionRequestResponse> getMyRequests(Authentication auth) {
        return adminService.getMyRequests(currentUserId(auth));
    }

    // GET /api/admin/promotion-requests — all pending (admin only)
    @GetMapping("/admin/promotion-requests")
    public List<PromotionRequestResponse> getPendingRequests() {
        return adminService.getPendingRequests();
    }

    // GET /api/admin/promotion-requests/all — all requests (admin only)
    @GetMapping("/admin/promotion-requests/all")
    public List<PromotionRequestResponse> getAllRequests() {
        return adminService.getAllRequests();
    }

    // POST /api/admin/promotion-requests/{id}/approve
    @PostMapping("/admin/promotion-requests/{id}/approve")
    public PromotionRequestResponse approveRequest(@PathVariable UUID id, Authentication auth) {
        return adminService.approveRequest(id, currentUserId(auth));
    }

    // POST /api/admin/promotion-requests/{id}/reject
    @PostMapping("/admin/promotion-requests/{id}/reject")
    public PromotionRequestResponse rejectRequest(@PathVariable UUID id, Authentication auth) {
        return adminService.rejectRequest(id, currentUserId(auth));
    }

    // GET /api/admin/users — all users (admin only)
    @GetMapping("/admin/users")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }

    // PATCH /api/admin/users/{id}/role — change user role

    // GET /api/admin/signals — admin view of all signals with status filtering
    @GetMapping("/admin/signals")
    public List<Map<String, Object>> getSignalsForAdmin(@RequestParam(required = false) String status) {
        List<Signal> signals;
        if (status != null && !status.isEmpty()) {
            signals = signalRepository.findByStatus(SignalStatus.valueOf(status));
        } else {
            signals = signalRepository.findAll();
        }
        
        return signals.stream().map(signal -> {
            long validationCount = validationRepository.countBySignalId(signal.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("id", signal.getId());
            result.put("title", signal.getTitle());
            result.put("status", signal.getStatus().toString());
            result.put("submitterUsername", signal.getSubmitter().getUsername());
            result.put("domainName", signal.getDomain().getName());
            result.put("resolutionDate", signal.getResolutionDate());
            result.put("validationCount", validationCount);
            result.put("actualOutcome", signal.getActualOutcome() != null ? signal.getActualOutcome() : "");
            return result;
        }).toList();
    }

    @PatchMapping("/admin/users/{id}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setUserRole(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        adminService.setUserRole(id, Role.valueOf(body.get("role")));
    }

    private UUID currentUserId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
