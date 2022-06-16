package io.github.ninty9.lastlife;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerLives {

    public UUID uuid;
    public int lives;


    public PlayerLives(UUID Uuid, int Lives) {
        this.uuid = Uuid;
        this.lives = Lives;
    }

}
