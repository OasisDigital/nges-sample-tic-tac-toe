package com.oasisdigital.nges.sample.tictactoe.domain;

import java.util.UUID;

public class GameFinished extends GameEvent {

    private Player winner;
    private int[] winningAxis;

    public GameFinished() {
    }

    public GameFinished(UUID gameId) {
        setGameId(gameId);
    }

    public GameFinished(UUID gameId, Player winner, int[] winningAxis) {
        setGameId(gameId);
        this.winner = winner;
        this.winningAxis = winningAxis;
    }

    public Player getWinner() {
        return winner;
    }

    public int[] getWinningAxis() {
        return winningAxis;
    }

}
