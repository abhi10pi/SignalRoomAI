package com.example.Backend.entity;

import com.example.Backend.enums.ResolutionVoteType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "resolution_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"signal_id", "consultant_id"}))
public class ResolutionVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultant_id", nullable = false)
    private User consultant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResolutionVoteType vote;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justification;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public Signal getSignal() { return signal; }
    public void setSignal(Signal signal) { this.signal = signal; }
    public User getConsultant() { return consultant; }
    public void setConsultant(User consultant) { this.consultant = consultant; }
    public ResolutionVoteType getVote() { return vote; }
    public void setVote(ResolutionVoteType vote) { this.vote = vote; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
