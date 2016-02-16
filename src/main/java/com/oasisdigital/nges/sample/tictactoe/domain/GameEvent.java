package com.oasisdigital.nges.sample.tictactoe.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class GameEvent {
    private UUID gameId;
    private long sequence;

    public void setGameId(UUID gameId) {
        this.gameId = gameId;
    }

    public UUID getGameId() {
        return gameId;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @JsonIgnore
    public long getSequence() {
        return sequence;
    }

    @JsonIgnore
    public boolean isPersistent() {
        return sequence > 0;
    }
}
