package com.example.Backend.dto.response;

import com.example.Backend.enums.SignalStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SignalSummaryResponse {

    private UUID id;
    private String title;
    private String category;
    private SignalStatus status;
    private LocalDateTime discussionEnd;
    private LocalDateTime createdAt;
    private String authorUsername;
    private List<String> tags;
    private long upVotes;
    private long downVotes;
    private long totalVotes;
    private double upPercent;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public SignalStatus getStatus() { return status; }
    public void setStatus(SignalStatus status) { this.status = status; }
    public LocalDateTime getDiscussionEnd() { return discussionEnd; }
    public void setDiscussionEnd(LocalDateTime discussionEnd) { this.discussionEnd = discussionEnd; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public long getUpVotes() { return upVotes; }
    public void setUpVotes(long upVotes) { this.upVotes = upVotes; }
    public long getDownVotes() { return downVotes; }
    public void setDownVotes(long downVotes) { this.downVotes = downVotes; }
    public long getTotalVotes() { return totalVotes; }
    public void setTotalVotes(long totalVotes) { this.totalVotes = totalVotes; }
    public double getUpPercent() { return upPercent; }
    public void setUpPercent(double upPercent) { this.upPercent = upPercent; }
}
