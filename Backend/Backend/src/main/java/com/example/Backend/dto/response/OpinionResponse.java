package com.example.Backend.dto.response;

import com.example.Backend.enums.OpinionPosition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OpinionResponse {

    private UUID id;
    private UUID signalId;
    private UUID userId;
    private String username;
    private OpinionPosition position;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SourceResponse> sources;
    private long upVotes;
    private long downVotes;
    private String myVote;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public OpinionPosition getPosition() { return position; }
    public void setPosition(OpinionPosition position) { this.position = position; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<SourceResponse> getSources() { return sources; }
    public void setSources(List<SourceResponse> sources) { this.sources = sources; }
    public long getUpVotes() { return upVotes; }
    public void setUpVotes(long upVotes) { this.upVotes = upVotes; }
    public long getDownVotes() { return downVotes; }
    public void setDownVotes(long downVotes) { this.downVotes = downVotes; }
    public String getMyVote() { return myVote; }
    public void setMyVote(String myVote) { this.myVote = myVote; }

    public static class SourceResponse {
        private UUID id;
        private String url;
        private String title;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
}
