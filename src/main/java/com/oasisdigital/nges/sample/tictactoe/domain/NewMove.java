package com.oasisdigital.nges.sample.tictactoe.domain;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class NewMove {
    @NotNull
    private Player player;

    @Min(0)
    @Max(8)
    private int cell;

    public Player getPlayer() {
        return player;
    }

    public int getCell() {
        return cell;
    }
}
