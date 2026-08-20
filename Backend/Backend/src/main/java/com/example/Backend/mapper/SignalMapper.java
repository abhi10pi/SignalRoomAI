package com.example.Backend.mapper;

import com.example.Backend.dto.response.SignalResponse;
import com.example.Backend.dto.response.SignalSummaryResponse;
import com.example.Backend.entity.Signal;
import org.springframework.stereotype.Component;

@Component
public class SignalMapper {

    public SignalResponse toResponse(Signal s) {
        SignalResponse r = new SignalResponse();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        r.setDescription(s.getDescription());
        r.setResolutionType(s.getResolutionType());
        r.setResolutionCriteria(s.getResolutionCriteria());
        r.setResolutionDate(s.getResolutionDate());
        r.setStatus(s.getStatus());
        r.setVisibility(s.getVisibility());
        r.setActualOutcome(s.getActualOutcome());
        r.setSubmittedAt(s.getSubmittedAt());
        r.setEvaluatedAt(s.getEvaluatedAt());
        r.setCreatedAt(s.getCreatedAt());
        r.setUpdatedAt(s.getUpdatedAt());
        r.setSubmitterId(s.getSubmitter().getId());
        r.setSubmitterUsername(s.getSubmitter().getUsername());
        r.setDomainId(s.getDomain().getId());
        r.setDomainName(s.getDomain().getName());
        r.setDomainSlug(s.getDomain().getSlug());
        return r;
    }

    public SignalSummaryResponse toSummary(Signal s) {
        SignalSummaryResponse r = new SignalSummaryResponse();
        r.setId(s.getId());
        r.setTitle(s.getTitle());
        r.setStatus(s.getStatus());
        r.setVisibility(s.getVisibility());
        r.setDomainName(s.getDomain().getName());
        r.setDomainSlug(s.getDomain().getSlug());
        r.setSubmitterUsername(s.getSubmitter().getUsername());
        r.setResolutionDate(s.getResolutionDate());
        r.setSubmittedAt(s.getSubmittedAt());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
