package com.oasisdigital.nges.sample.tictactoe.domain;

import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import com.oasisdigital.nges.sample.EventSourcedEntity;
import com.oasisdigital.nges.sample.exception.Conflict;

public class Game extends EventSourcedEntity {
    private static final Random RANDOM = new Random();

    private final UUID id;
    private GameInfo meta;
    private Player[] board;
    private boolean gameFinished;

    public Game(UUID id) {
        this.id = id;
        this.board = new Player[9];
        this.gameFinished = false;
    }

    public void create(GameInfo gameInfo) {
        applyAndPersist(new GameCreated(id, gameInfo));
    }

    public void movePlayerAndRespond(NewMove move) {
        int cell = move.getCell();
        if (gameFinished) {
            throw new Conflict("Game already finished");
        }
        if (move.getPlayer() != meta.getHumanPlayer()) {
            throw new Conflict("Cannot move as CPU");
        }
        if (board[cell] != null) {
            throw new Conflict("Cell already taken by " + board[cell]);
        }
        applyAndPersist(new MovePerformed(id, move.getPlayer(), cell));

        checkWinningConditions();

        if (!gameFinished) {
            Player cpuPlayer = meta.getHumanPlayer() == Player.O ? Player.X : Player.O;
            int cpuMove = calculateMove(cpuPlayer);

            applyAndPersist(new MovePerformed(id, cpuPlayer, cpuMove));
            checkWinningConditions();
        }
    }

    private int calculateMove(Player cpuPlayer) {
        int[] available = getAvailableCells();
        return available[RANDOM.nextInt(available.length)];
    }

    private int[] getAvailableCells() {
        return IntStream.range(0, 9).filter(i -> board[i] == null).toArray();
    }

    private void checkWinningConditions() {
        int[][] axes = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 },
                { 0, 4, 8 }, { 2, 4, 6 } };
        for (int[] axis : axes) {
            Player player = board[axis[0]];
            if (player != null && player == board[axis[1]] && player == board[axis[2]]) {
                applyAndPersist(new GameFinished(id, player, axis));
                return;
            }
        }
        if (getAvailableCells().length == 0) {
            applyAndPersist(new GameFinished(id));
        }
    }

    public void on(GameCreated event) {
        this.meta = event.getMeta();
    }

    public void on(MovePerformed event) {
        this.board[event.getCell()] = event.getPlayer();
    }

    public void on(GameFinished event) {
        this.gameFinished = true;
    }
}
