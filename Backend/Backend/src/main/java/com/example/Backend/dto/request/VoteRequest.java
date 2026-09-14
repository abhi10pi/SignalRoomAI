package com.example.Backend.dto.request;

import com.example.Backend.enums.VoteType;
import jakarta.validation.constraints.NotNull;

public class VoteRequest {

    @NotNull
    private VoteType voteType;

    public VoteType getVoteType() { return voteType; }
    public void setVoteType(VoteType voteType) { this.voteType = voteType; }
}
