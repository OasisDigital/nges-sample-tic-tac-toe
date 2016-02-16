package com.oasisdigital.nges.sample.tictactoe.domain;

import java.util.UUID;

public class GameCreated extends GameEvent {
    private GameInfo meta;

    public GameCreated() {
    }

    public GameCreated(UUID gameId, GameInfo meta) {
        this.meta = meta;
        setGameId(gameId);
    }

    public GameInfo getMeta() {
        return meta;
    }
}
