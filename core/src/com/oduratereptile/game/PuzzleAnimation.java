package com.oduratereptile.game;

/**
 * Created by Marc on 1/23/2018.
 */

public interface PuzzleAnimation {
    public void start();
    public boolean isRunning();

    public void act(float deltaTime);
}
