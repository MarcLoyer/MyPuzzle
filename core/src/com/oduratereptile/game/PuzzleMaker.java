package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Json;

import java.nio.IntBuffer;
import java.util.Random;

/**
 * Created by Marc on 10/7/2017.
 */

public class PuzzleMaker {
    public GameScreen gameScreen;
    public Random rand = new Random();
    public Json json = new Json();

    public GameData gameData = new GameData();

    public PuzzleMaker(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    public Pixmap puzzleImg;
    public Texture puzzleImgTex;

    public void setPicture(FileHandle fh, String name) {
        setPicture(new Pixmap(fh), name);
    }

    public void setPicture(Pixmap pixmap, String name) {
        puzzleImg = pixmap;

        IntBuffer buffer = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buffer);
        int maxImageSize = buffer.get(0);
//        Gdx.app.error("debug", "Max image size for this device = " + maxImageSize);

        puzzleImgTex = new Texture(puzzleImg);
//        Gdx.app.error("debug", "Current image size = (" + pixmap.getWidth() + ", " + pixmap.getHeight() + ")");

        gameData.puzzleImageWidth = pixmap.getWidth();
        gameData.puzzleImageHeight = pixmap.getHeight();
        gameData.createThumbnail(pixmap);
        gameData.puzzleName = name;
    }

    public int controlsPerPiece = 6;
    public int pointsPerPiece = 50;

    public int numRows;
    public Vector2[][] rowControlPoints;
    public CatmullRomSpline<Vector2> [] rowSpline;
    public Vector2[][] rowLine;

    public int numCols;
    public Vector2[][] colControlPoints;
    public CatmullRomSpline<Vector2> [] colSpline;
    public Vector2[][] colLine;

    // shape parameters
    private static final float A = 0.15f;
    private static final float B = 0.17f;
    private static final float C = 0.21f;
    private static final float D = 0.29f;
    private static final float Ar = 0.02f;
    private static final float Br = 0.04f;
    private static final float Fr = 0.12f;

    public void createPieces(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;

        gameData.rows = numRows;
        gameData.cols = numCols;

        //  - Catmull-Rom splines to define the shapes
        //  - divide into pieces
        //  - create separate texture for each piece (and add alpha mask to create the shape)
        //  - combine into an atlas
        //  - convert each texture into a texture region and create each piece

        generateSplines();
        createMeshPieceAtlas();
        createPuzzlePieces();
        gameData.setPieceNeighbors();
    }

    public PuzzlePiece getPiece(int row, int col) {
        return gameData.puzzlePieces.get(row+","+col);
    }

    public PuzzlePacker puzzlePacker;
    public PuzzlePacker.MeshPiece[][] meshPieces;  // save the meshPieces for debugging

    public void createMeshPieceAtlas() {
        puzzlePacker = new PuzzlePacker(this, 1024);
        meshPieces = new PuzzlePacker.MeshPiece[numRows][numCols];
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                meshPieces[i][j] = puzzlePacker.pack(i,j);
            }
        }

        gameData.textureAtlasFilename = gameData.getBasename() + "/" + gameData.getBasename() + ".atlas";
        puzzlePacker.createAtlas();
        puzzlePacker.save(Gdx.files.local(gameData.textureAtlasFilename));
    }

    public void createPuzzlePieces() {
        PuzzlePiece p;

        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                p = new PuzzlePiece(i, j, puzzlePacker.getData(i,j), puzzlePacker.findRegion(i,j), true);
                gameData.puzzlePieces.put(p.getID(), p);
                generateHighlight(p);
            }
        }
    }

    public void generateHighlight(PuzzlePiece p) {
        OutlineShader.setup(p);
        p.highlight = OutlineShader.renderToTexture();
    }

    public float rowSpacing(int row) {
        int minSpacing = puzzleImg.getHeight() / numRows;
        int remainder = puzzleImg.getHeight() % numRows;
        if (row < remainder) return minSpacing + 1;
        return minSpacing;
    }

    public float rowOffset(int row) {
        int minSpacing = puzzleImg.getHeight() / numRows;
        int remainder = puzzleImg.getHeight() % numRows;
        if (row < remainder) return (minSpacing + 1) * row;
        return ((minSpacing + 1) * remainder) + (minSpacing * (row - remainder));
    }

    public float colSpacing(int col) {
        int minSpacing = puzzleImg.getWidth() / numCols;
        int remainder = puzzleImg.getWidth() % numCols;
        if (col < remainder) return minSpacing + 1;
        return minSpacing;
    }

    public float colOffset(int col) {
        int minSpacing = puzzleImg.getWidth() / numCols;
        int remainder = puzzleImg.getWidth() % numCols;
        if (col < remainder) return (minSpacing + 1) * col;
        return ((minSpacing + 1) * remainder) + (minSpacing * (col - remainder));
    }

    public void generateSplines() {
        int pointsPerSpline = numCols*pointsPerPiece + 1;

        rowControlPoints = new Vector2[numRows-1][numCols*controlsPerPiece+3];
        rowSpline = new CatmullRomSpline[numRows-1];
        rowLine = new Vector2[numRows-1][pointsPerSpline];
        for (int i=0; i<numRows-1; i++) {
            float offset = rowOffset(i+1); // we're doing the spline above the piece
            rowControlPoints[i][0] = new Vector2(-colSpacing(0), offset);
            for (int j=0; j<numCols; j++) {
                float sign = (rand.nextBoolean())? rowSpacing(i): -rowSpacing(i);

                if (j==0) rowControlPoints[i][1+j*controlsPerPiece] = new Vector2(colOffset(j), offset + randR(Fr));
                else      rowControlPoints[i][1+j*controlsPerPiece] = new Vector2(colOffset(j) + randC(Fr), offset + randR(Fr));

                rowControlPoints[i][2+j*controlsPerPiece] = new Vector2(colOffset(j) + (0.5f - A)*colSpacing(j) + randC(Ar), offset        + randR(Ar));
                rowControlPoints[i][3+j*controlsPerPiece] = new Vector2(colOffset(j) + (0.5f - B)*colSpacing(j) + randC(Br), offset+sign*C + randR(Br));
                rowControlPoints[i][4+j*controlsPerPiece] = new Vector2(colOffset(j) + (0.5f    )*colSpacing(j) + randC(Br), offset+sign*D + randR(Br));
                rowControlPoints[i][5+j*controlsPerPiece] = new Vector2(colOffset(j) + (0.5f + B)*colSpacing(j) + randC(Br), offset+sign*C + randR(Br));
                rowControlPoints[i][6+j*controlsPerPiece] = new Vector2(colOffset(j) + (0.5f + A)*colSpacing(j) + randC(Ar), offset        + randR(Ar));
            }
            rowControlPoints[i][1+numCols*controlsPerPiece] = new Vector2((float)colOffset(numCols), offset + randR(Fr));
            rowControlPoints[i][2+numCols*controlsPerPiece] = new Vector2((float)colOffset(numCols+1), offset);

            rowSpline[i] = new CatmullRomSpline<Vector2>(rowControlPoints[i], false);

            for (int j=0; j<pointsPerSpline; j++) {
                rowLine[i][j] = new Vector2();
                rowSpline[i].valueAt(rowLine[i][j], (float)j/(float)(pointsPerSpline-1));
            }
        }

        pointsPerSpline = numRows*pointsPerPiece + 1;
        colControlPoints = new Vector2[numCols-1][numRows*controlsPerPiece+3];
        colSpline = new CatmullRomSpline[numCols-1];
        colLine = new Vector2[numCols-1][pointsPerSpline];
        for (int i=0; i<numCols-1; i++) {
            float offset = colOffset(i+1); // we're doing the spline to the right of the piece
            colControlPoints[i][0] = new Vector2(offset, -rowSpacing(0));
            for (int j=0; j<numRows; j++) {
                float sign = (rand.nextBoolean())? colSpacing(i): -colSpacing(i);

                // Use the same point as the row spline, so that the splines intersect at a control point
                if (j==0) colControlPoints[i][1+j*controlsPerPiece] = new Vector2(offset + randC(Fr), rowOffset(j));
                else      colControlPoints[i][1+j*controlsPerPiece] = rowControlPoints[j-1][1+(i+1)*controlsPerPiece];

                colControlPoints[i][2+j*controlsPerPiece] = new Vector2(offset        + randC(Ar), rowOffset(j) + (0.5f - A)*rowSpacing(j) + randC(Ar));
                colControlPoints[i][3+j*controlsPerPiece] = new Vector2(offset+sign*C + randC(Br), rowOffset(j) + (0.5f - B)*rowSpacing(j) + randC(Br));
                colControlPoints[i][4+j*controlsPerPiece] = new Vector2(offset+sign*D + randC(Br), rowOffset(j) + (0.5f    )*rowSpacing(j) + randC(Br));
                colControlPoints[i][5+j*controlsPerPiece] = new Vector2(offset+sign*C + randC(Br), rowOffset(j) + (0.5f + B)*rowSpacing(j) + randC(Br));
                colControlPoints[i][6+j*controlsPerPiece] = new Vector2(offset        + randC(Ar), rowOffset(j) + (0.5f + A)*rowSpacing(j) + randC(Ar));
            }
            colControlPoints[i][1+numRows*controlsPerPiece] = new Vector2(offset + randC(Fr), (float)rowOffset(numRows));
            colControlPoints[i][2+numRows*controlsPerPiece] = new Vector2(offset, (float)rowOffset(numRows+1));

            colSpline[i] = new CatmullRomSpline<Vector2>(colControlPoints[i], false);

            for (int j=0; j<pointsPerSpline; j++) {
                colLine[i][j] = new Vector2();
                colSpline[i].valueAt(colLine[i][j], (float)j/(float)(pointsPerSpline-1));
            }
        }
    }

    public float randR(float max) {
        return ((rand.nextFloat() * 2.0f * max) - max) * rowSpacing(0);
    }

    public float randC(float max) {
        return ((rand.nextFloat() * 2.0f * max) - max) * colSpacing(0);
    }

    public Pixmap pieceImg;
    public Texture pieceImgTex;
    public Vector2 pieceImgTexLocation = new Vector2();

    public void dispose() {
        puzzleImg.dispose();
        puzzleImgTex.dispose();
    }

}
