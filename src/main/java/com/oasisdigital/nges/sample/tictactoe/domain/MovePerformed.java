package com.oasisdigital.nges.sample.tictactoe.domain;

import java.util.UUID;

public class MovePerformed extends GameEvent {
    private Player player;
    private int cell;

    public MovePerformed() {
    }

    public MovePerformed(UUID gameId, Player player, int cell) {
        setGameId(gameId);
        this.player = player;
        this.cell = cell;
    }

    public Player getPlayer() {
        return player;
    }

    public int getCell() {
        return cell;
    }
}
