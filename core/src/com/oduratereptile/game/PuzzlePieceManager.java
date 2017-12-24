package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
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

    public Vector2[] locsInit;
    public Vector2 [] locs;
    public float [] degs;

    public void initialize() {
        locsInit = new Vector2[puzzle.numRows * puzzle.numCols];
        locs = new Vector2[puzzle.numCols * puzzle.numRows];
        degs = new float[puzzle.numCols * puzzle.numRows];
    }

    public void saveInitialState() {
        for (int i=0; i<(puzzle.numRows*puzzle.numCols); i++) {
            locsInit[i] = puzzle.puzzlePiece.get(i).getPosition().cpy();
        }
    }

    /**
     * Moves all puzzle pieces back to their solved locations,
     * clears any PuzzleGroups, resets neighbors.
     */
    public void restoreInitialState() {
        for (int i=0; i<puzzle.numRows*puzzle.numCols; i++) {
            tempPP = puzzle.puzzlePiece.get(i);

            tempPP.setRotation(0, false);
            tempPP.moveTo(locsInit[i].x, locsInit[i].y, false);
            // reset the neighbors, remove the groups
            tempPP.group = null;
            for (int j=0; j<4; j++) {
                tempPP.neighborMask[j] = (tempPP.neighbor[j]!=null);
            }
        }
        resetGroups();
    }

    public void resetGroups() {
        for (int i=0; i<puzzle.numRows*puzzle.numCols; i++) {
            tempPP = puzzle.puzzlePiece.get(i);

            // reset the neighbors, remove the groups
            tempPP.group = null;
            for (int j=0; j<4; j++) {
                tempPP.neighborMask[j] = (tempPP.neighbor[j]!=null);
                tempPP.setOrigin(tempPP.getRegionWidth()/2, tempPP.getRegionHeight()/2);
            }
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
                locs[i*puzzle.numRows+j] = new Vector2(tmp.x * (float)i, tmp.y * (float)j);
            }
        }

        // 2) randomly assign pieces to locations
        for (int i=puzzle.numCols*puzzle.numRows-1; i>0; i--) {
            int j = rand.nextInt(i);
            tmp = locs[j];
            locs[j] = locs[i];
            locs[i] = tmp;
        }

        // 3) randomly assign a rotation to each piece
        for (int i=0; i<puzzle.numCols; i++) {
            for (int j=0; j<puzzle.numRows; j++) {
                degs[i*puzzle.numRows+j] = (360.0f * 4.0f) + 360.0f*rand.nextFloat();
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
    public Interpolation interp = new Interpolation.SwingOut(1);

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
            for (int i=0; i<puzzle.numCols * puzzle.numRows; i++) {
                tempV.set(locs[i]).sub(locsInit[i]).scl(del).add(locsInit[i]);

                tempF = degs[i]*del;
                tempPP = puzzle.puzzlePiece.get(i);
                tempPP.setRotation(tempF, false);
                tempPP.moveTo(tempV.x, tempV.y, false);
            }

        }
    }
}
