package com.example.Backend.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class CreateCommentRequest {

    @NotBlank
    private String content;

    private UUID opinionId;

    private UUID parentCommentId;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public UUID getOpinionId() { return opinionId; }
    public void setOpinionId(UUID opinionId) { this.opinionId = opinionId; }
    public UUID getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(UUID parentCommentId) { this.parentCommentId = parentCommentId; }
}
