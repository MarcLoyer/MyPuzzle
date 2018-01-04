package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Marc on 12/24/2017.
 */

public class PuzzlePieceManager {
    public Puzzle puzzle;
    public Random rand;

    public PuzzlePieceManager(Puzzle puzzle) {
        this.puzzle = puzzle;
        this.rand = puzzle.rand;
    }

    public ObjectMap<String, Vector2> locs = new ObjectMap<String, Vector2>();
    public ObjectMap<String, Float> degs = new ObjectMap<String, Float>();

    /**
     * Moves all puzzle pieces back to their solved locations,
     * clears any PuzzleGroups, resets neighbors.
     */
    public void restoreInitialState() {
        // remove the groups, move the pieces back to their
        // original locations, and reset the neighbors
        resetGroups();
        for (PuzzlePiece p: puzzle.puzzlePiece.values()) {
            p.setRotation(0, false);
            p.moveTo(p.posInitial.x, p.posInitial.y, false);
        }
        puzzle.setPieceNeighbors();
    }

    public void resetGroups() {
        for (PuzzlePiece p: puzzle.puzzlePiece.values()) {
            // TODO: make a pool for the groups?
            p.group = null;
            p.resetOrigin();
            p.select(false);
        }
        Iterator<PuzzleGroup> iter = puzzle.puzzleGroup.values().iterator();
        while (iter.hasNext()) {
            iter.next().destroy();
        }
        puzzle.largestGroup = null;
    }

    /**
     * Shuffles the pieces across the screen
     */
    public void shuffle() {
        resetGroups();

        // 1) create a grid of locations to send the pieces to
        Vector2 tmp = new Vector2(
                3.0f * (float)puzzle.puzzleImg.getWidth() / (float)puzzle.numCols,
                3.0f * (float)puzzle.puzzleImg.getHeight() / (float)puzzle.numRows
        );

        for (int i=0; i<puzzle.numCols; i++) {
            for (int j=0; j<puzzle.numRows; j++) {
                locs.put(i+","+j, new Vector2(tmp.x * (float)i, tmp.y * (float)j));
            }
        }

        // 2) randomly assign pieces to locations
        for (int i=0; i<puzzle.numRows; i++) {
            for (int j=0; j<puzzle.numCols; j++) {
                int index0 = i + j*puzzle.numRows;
                String key0 = i+","+j;

                int index1 = rand.nextInt(index0+1);
                String key1 = (index1%puzzle.numRows) + "," + (index1/puzzle.numRows);

                tmp = locs.get(key0);
                locs.put(key0, locs.get(key1));
                locs.put(key1, tmp);
            }
        }

        // 3) randomly assign a rotation to each piece
        for (int i=0; i<puzzle.numCols; i++) {
            for (int j=0; j<puzzle.numRows; j++) {
                degs.put(i+","+j, (360.0f * 4.0f) + 360.0f*rand.nextFloat());
            }
        }

        // 4) animate the motion
        startAnimation(4);
    }

    private Vector2 tempV = new Vector2();
    private float tempF;
    private PuzzlePiece tempPP;

    public boolean animate = false;
    public float animationDuration = 0;
    public float animationDelta = 0;
    public Interpolation interp = new Interpolation.SwingOut(0);

    public void startAnimation(float duration) {
        animationDuration = duration;
        animationDelta = 0;
        animate = true;
    }

    public void act(float deltaTime) {
        float del = 0;
        if (animate) {
            animationDelta += deltaTime;
            if (animationDelta>=animationDuration) {
                del = interp.apply(1.0f);
                animate = false;
            } else {
                del = interp.apply(animationDelta / animationDuration);
            }
            for (String k: puzzle.puzzlePiece.keys()) {
                tempPP = puzzle.puzzlePiece.get(k);
                tempV.set(locs.get(k))
                        .sub(tempPP.posInitial)
                        .scl(del)
                        .add(tempPP.posInitial);

                tempF = degs.get(k)*del;

                tempPP.setRotation(tempF, false);
                tempPP.moveTo(tempV.x, tempV.y, false);
            }

        }
    }
}
