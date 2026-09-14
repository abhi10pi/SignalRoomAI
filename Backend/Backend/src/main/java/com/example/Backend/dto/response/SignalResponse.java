package com.example.Backend.dto.response;

import com.example.Backend.enums.SignalStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SignalResponse {

    private UUID id;
    private String title;
    private String description;
    private String category;
    private SignalStatus status;
    private LocalDateTime discussionStart;
    private LocalDateTime discussionEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UUID authorId;
    private String authorUsername;

    private List<String> tags;
    private List<SourceResponse> sources;

    // vote stats
    private long upVotes;
    private long downVotes;
    private long totalVotes;
    private double upPercent;
    private double downPercent;
    private String myVote; // UP, DOWN, or null

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<SourceResponse> getSources() { return sources; }
    public void setSources(List<SourceResponse> sources) { this.sources = sources; }
    public long getUpVotes() { return upVotes; }
    public void setUpVotes(long upVotes) { this.upVotes = upVotes; }
    public long getDownVotes() { return downVotes; }
    public void setDownVotes(long downVotes) { this.downVotes = downVotes; }
    public long getTotalVotes() { return totalVotes; }
    public void setTotalVotes(long totalVotes) { this.totalVotes = totalVotes; }
    public double getUpPercent() { return upPercent; }
    public void setUpPercent(double upPercent) { this.upPercent = upPercent; }
    public double getDownPercent() { return downPercent; }
    public void setDownPercent(double downPercent) { this.downPercent = downPercent; }
    public String getMyVote() { return myVote; }
    public void setMyVote(String myVote) { this.myVote = myVote; }

    public static class SourceResponse {
        private UUID id;
        private String url;
        private String title;
        private String description;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
