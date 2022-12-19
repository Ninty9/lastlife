package io.github.ninty9.lastlife;

import java.util.UUID;

public class PlayerLives {

    public UUID uuid;
    public int lives;
    public boolean hasDecay;


    public PlayerLives(UUID Uuid, int Lives) {
        this.uuid = Uuid;
        this.lives = Lives;
    }

}
