package com.example.Backend.entity;

import com.example.Backend.enums.SignalStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "signals")
public class Signal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalStatus status = SignalStatus.OPEN;

    @Column(name = "discussion_start", nullable = false)
    private LocalDateTime discussionStart = LocalDateTime.now();

    @Column(name = "discussion_end", nullable = false)
    private LocalDateTime discussionEnd;

    @OneToMany(mappedBy = "signal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SignalTag> signalTags = new ArrayList<>();

    @OneToMany(mappedBy = "signal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SignalSource> sources = new ArrayList<>();

    public UUID getId() { return id; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public SignalStatus getStatus() { return status; }
    public void setStatus(SignalStatus status) { this.status = status; }
    public LocalDateTime getDiscussionStart() { return discussionStart; }
    public void setDiscussionStart(LocalDateTime discussionStart) { this.discussionStart = discussionStart; }
    public LocalDateTime getDiscussionEnd() { return discussionEnd; }
    public void setDiscussionEnd(LocalDateTime discussionEnd) { this.discussionEnd = discussionEnd; }
    public List<SignalTag> getSignalTags() { return signalTags; }
    public List<SignalSource> getSources() { return sources; }
}
