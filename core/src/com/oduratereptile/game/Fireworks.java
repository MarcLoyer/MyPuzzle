package com.oduratereptile.game;

import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Marc on 1/23/2018.
 */

public class Fireworks implements PuzzleAnimation {
    Puzzle puzzle;
    boolean enabled = false;
    float animationDuration;
    float animationCycleTime;
    float animationDelta;
    int animationIndex;

    ArrayList<PuzzlePiece> pieces = new ArrayList<PuzzlePiece>();

    Random rand = new Random();

    public Fireworks(Puzzle puzzle) {
        this(puzzle, 10, .1f);
    }

    /**
     *
     * @param puzzle
     * @param duration the amount of time the overall effect lasts
     * @param cycleTime the time it takes to complete one pass through all pieces
     */
    public Fireworks(Puzzle puzzle, float duration, float cycleTime) {
        this.puzzle = puzzle;

        setup(duration, cycleTime);
    }

    public void setup(float duration, float cycleTime) {
        // create a randomized list of pieces...
        for (PuzzlePiece p: puzzle.gameData.puzzlePieces.values()) {
            pieces.add(p);
        }
        for (int i=pieces.size()-1; i>0; i--) {
            swap(i, rand.nextInt(i));
        }

        animationDuration = duration;
        animationCycleTime = cycleTime;
    }

    private void swap(int a, int b) {
        PuzzlePiece temp = pieces.get(a);
        pieces.set(a, pieces.get(b));
        pieces.set(b, temp);
    }

    @Override
    public void start() {
        enabled = true;
        animationDelta = 0;
        animationIndex = 0;

        puzzle.selectedPiece.clear();
        for (PuzzlePiece p: puzzle.gameData.puzzlePieces.values()) {
            puzzle.selectedPiece.add(p);
            p.select();
            p.useGroupColor(false);
            p.setHighlightColor(Color.CLEAR);
        }
    }

    private void end() {
        enabled = false;
        puzzle.selectedPiece.clear();

        for (PuzzlePiece p: puzzle.gameData.puzzlePieces.values()) {
            p.select(false);
            p.useGroupColor(true);
            p.setHighlightColor(Color.WHITE);
        }
    }

    @Override
    public boolean isRunning() {
        return enabled;
    }

    @Override
    public void act(float deltaTime) {
        animationDelta += deltaTime;
        if (animationDelta>=animationDuration) {
            end();
            return;
        }

        int del = (int)(pieces.size() * (animationDelta % animationCycleTime) / animationCycleTime);
        while (animationIndex != del) {
            setNewColor(pieces.get(animationIndex++));
            if (animationIndex>=pieces.size()) animationIndex=0;
        }
    }

    Color[] colors = {
            Color.GOLD,
            Color.ORANGE,
            Color.LIME,
            Color.BLUE,
            Color.PINK,
            Color.RED,
            Color.VIOLET,
            Color.CORAL,
            Color.CHARTREUSE,
            Color.GOLDENROD,
            Color.CYAN,
            Color.PURPLE
    };

    /**
     * randomly replace highlight colors with random colors
     * after awhile, replace all highlight colors with green
     */
    private void setNewColor(PuzzlePiece p) {
        Color c = ((animationDuration-animationDelta)<1.5f) ? Color.GREEN: colors[rand.nextInt(colors.length)];
        p.highlightColor.set(c);
    }
}
