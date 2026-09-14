package com.example.Backend.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class SignalTagId implements Serializable {

    private UUID signalId;
    private UUID tagId;

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }
    public UUID getTagId() { return tagId; }
    public void setTagId(UUID tagId) { this.tagId = tagId; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SignalTagId that)) return false;
        return java.util.Objects.equals(signalId, that.signalId)
                && java.util.Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() { return java.util.Objects.hash(signalId, tagId); }
}