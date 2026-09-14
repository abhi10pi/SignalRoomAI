package com.example.Backend.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "signal_tags")
public class SignalTag {

    @EmbeddedId
    private SignalTagId id = new SignalTagId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("signalId")
    @JoinColumn(name = "signal_id", nullable = false)
    private Signal signal;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public Signal getSignal() { return signal; }
    public void setSignal(Signal signal) { this.signal = signal; this.id.setSignalId(signal.getId()); }
    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; this.id.setTagId(tag.getId()); }
}
