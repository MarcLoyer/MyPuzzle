package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.util.ArrayList;

/**
 * Created by Marc on 10/7/2017.
 */

public class Puzzle {
    public GameScreen gameScreen;
    public Texture puzzleImg;
    public ShapeRenderer sr;

    public ArrayList<PuzzlePiece> puzzlePiece = new ArrayList<PuzzlePiece>();
//    public ArrayList<PuzzleGroup> puzzleGroup;
    public BoundingBox puzzleBounds = new BoundingBox();



    public Puzzle(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        puzzleImg = gameScreen.puzzleImg;
        sr = gameScreen.game.shapeRenderer;

        createPieces(3, 3);
    }

    public int numRows;
    public float rowSpacing;
    public Vector2[][] rowControlPoints;
    public CatmullRomSpline<Vector2> [] rowSpline;
    public Vector2[][] rowLine;

    public int numCols;
    public float colSpacing;

    private void createPieces(int numRows, int numCols) {
        this.numRows = numRows;
        rowSpacing = puzzleImg.getHeight() / (float)numRows;

        this.numCols = numCols;
        colSpacing = puzzleImg.getWidth() / (float)numCols;

        // TODO: createPieces()
        //  - Catmull-Rom splines to define the shapes
        //  - divide into pieces
        //  - create separate texture for each piece (and add alpha mask to create the shape)
        //  - combine into an atlas
        //  - convert each texture into a texture region and create each piece

        int pointsPerPiece = 1;
        int pointsPerSpline = numCols*20;

        rowControlPoints = new Vector2[numRows-1][numCols*pointsPerPiece+3];
        rowSpline = new CatmullRomSpline[numRows-1];
        rowLine = new Vector2[numRows-1][pointsPerSpline];
        for (int i=0; i<numRows-1; i++) {
            float offset = ((float)i+1f)*rowSpacing;
            rowControlPoints[i][0] = new Vector2(-colSpacing, offset);
            for (int j=0; j<numCols; j++) {
                rowControlPoints[i][1+j] = new Vector2(j*colSpacing, offset);
            }
            rowControlPoints[i][1+numCols*pointsPerPiece] = new Vector2((float)(numCols)*colSpacing, offset);
            rowControlPoints[i][2+numCols*pointsPerPiece] = new Vector2((float)(numCols+1)*colSpacing, offset);

            rowSpline[i] = new CatmullRomSpline<Vector2>(rowControlPoints[i], false);

            for (int j=0; j<pointsPerSpline; j++) {
                rowLine[i][j] = new Vector2();
                rowSpline[i].valueAt(rowLine[i][j], (float)j/(float)(pointsPerSpline-1));
            }
        }
    }

    public void render(SpriteBatch batch, float delta) {
        batch.draw(puzzleImg, 0,0);
        batch.end();

        sr.setProjectionMatrix(gameScreen.camera.combined);
        for (int i=0; i<numRows-1; i++) drawRowSpline(i);

        batch.begin();
    }

    public void drawRowSpline(int i) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1,1,1,1);
        for (int j=1; j<rowLine[i].length; j++) {
            sr.line(rowLine[i][j-1], rowLine[i][j]);
        }
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0.6f, 0.6f, 0.6f, 1);
        for (int j=0; j<rowControlPoints[i].length; j++) {
            sr.circle(rowControlPoints[i][j].x, rowControlPoints[i][j].y, 5);
        }

        sr.end();

    }

    // TODO: save and restore functions
}
