package com.example.Backend.entity;

import com.example.Backend.enums.Outcome;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "poll_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"signal_id", "user_id"}))
public class PollVote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Outcome vote;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public Signal getSignal() { return signal; }
    public void setSignal(Signal signal) { this.signal = signal; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Outcome getVote() { return vote; }
    public void setVote(Outcome vote) { this.vote = vote; }
    public LocalDateTime getCreatedAt() { return createdAt; }

}
