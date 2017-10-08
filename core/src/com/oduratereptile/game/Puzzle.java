package com.oduratereptile.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayList;

/**
 * Created by Marc on 10/7/2017.
 */

public class Puzzle {
    public GameScreen gameScreen;
    Texture puzzleImg;

    public ArrayList<PuzzlePiece> puzzlePiece = new ArrayList<PuzzlePiece>();
//    public ArrayList<PuzzleGroup> puzzleGroup;
    public BoundingBox puzzleBounds = new BoundingBox();



    public Puzzle(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        puzzleImg = gameScreen.puzzleImg;

        createPieces();
    }

    private void createPieces() {
        // TODO: createPieces()
        //  - Catmull-Rom splines to define the shapes
        //  - divide into peices
        //  - create separate texture for each piece (and add alpha mask to create the shape)
        //  - combine into an atlas
        //  - convert each texture into a texture region and create each piece
    }

    // TODO: save and restore functions
}
