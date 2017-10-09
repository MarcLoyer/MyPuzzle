package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Marc on 10/7/2017.
 */

public class Puzzle {
    public GameScreen gameScreen;
    public Pixmap puzzleImg;
    public ShapeRenderer sr;
    public Random rand = new Random();

    public ArrayList<PuzzlePiece> puzzlePiece = new ArrayList<PuzzlePiece>();
//    public ArrayList<PuzzleGroup> puzzleGroup;
    public BoundingBox puzzleBounds = new BoundingBox();

    public boolean displayImage = false;
    public boolean displaySplines = false;
    public boolean displaySplineImage = false;


    public Puzzle(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        sr = gameScreen.game.shapeRenderer;
    }

    public Texture puzzleImgTex;

    public void setPicture(FileHandle fh) {
        puzzleImg = new Pixmap(fh);

        IntBuffer buffer = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buffer);
        int maxImageSize = buffer.get(0);
        Gdx.app.error("debug", "Max image size for this device = " + maxImageSize);

        puzzleImgTex = new Texture(puzzleImg); //TODO: split into regions if the Pixmap is too big, or maybe mipmaps?
    }

    public int numRows;
    public float rowSpacing;
    public Vector2[][] rowControlPoints;
    public CatmullRomSpline<Vector2> [] rowSpline;
    public Vector2[][] rowLine;

    public int numCols;
    public float colSpacing;
    public Vector2[][] colControlPoints;
    public CatmullRomSpline<Vector2> [] colSpline;
    public Vector2[][] colLine;

    public Pixmap splineImg;
    public Texture splineImgTex;

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
        rowSpacing = puzzleImg.getHeight() / (float)numRows;

        this.numCols = numCols;
        colSpacing = puzzleImg.getWidth() / (float)numCols;

        // TODO: createPieces()
        //  - Catmull-Rom splines to define the shapes
        //  - divide into pieces
        //  - create separate texture for each piece (and add alpha mask to create the shape)
        //  - combine into an atlas
        //  - convert each texture into a texture region and create each piece
        generateSplines();
        generatePieces();
    }

    public void generateSplines() {
        int pointsPerPiece = 6;
        int pointsPerSpline = numCols*50;

        rowControlPoints = new Vector2[numRows-1][numCols*pointsPerPiece+3];
        rowSpline = new CatmullRomSpline[numRows-1];
        rowLine = new Vector2[numRows-1][pointsPerSpline];
        for (int i=0; i<numRows-1; i++) {
            float offset = ((float)i+1f)*rowSpacing;
            rowControlPoints[i][0] = new Vector2(-colSpacing, offset);
            for (int j=0; j<numCols; j++) {
                float sign = (rand.nextBoolean())? rowSpacing: -rowSpacing;

                if (j==0) rowControlPoints[i][1+j*pointsPerPiece] = new Vector2(j*colSpacing, offset + randR(Fr));
                else      rowControlPoints[i][1+j*pointsPerPiece] = new Vector2(j*colSpacing + randC(Fr), offset + randR(Fr));

                rowControlPoints[i][2+j*pointsPerPiece] = new Vector2((j + 0.5f - A)*colSpacing + randC(Ar), offset        + randR(Ar));
                rowControlPoints[i][3+j*pointsPerPiece] = new Vector2((j + 0.5f - B)*colSpacing + randC(Br), offset+sign*C + randR(Br));
                rowControlPoints[i][4+j*pointsPerPiece] = new Vector2((j + 0.5f    )*colSpacing + randC(Br), offset+sign*D + randR(Br));
                rowControlPoints[i][5+j*pointsPerPiece] = new Vector2((j + 0.5f + B)*colSpacing + randC(Br), offset+sign*C + randR(Br));
                rowControlPoints[i][6+j*pointsPerPiece] = new Vector2((j + 0.5f + A)*colSpacing + randC(Ar), offset        + randR(Ar));
            }
            rowControlPoints[i][1+numCols*pointsPerPiece] = new Vector2((float)(numCols)*colSpacing, offset + randR(Fr));
            rowControlPoints[i][2+numCols*pointsPerPiece] = new Vector2((float)(numCols+1)*colSpacing, offset);

            rowSpline[i] = new CatmullRomSpline<Vector2>(rowControlPoints[i], false);

            for (int j=0; j<pointsPerSpline; j++) {
                rowLine[i][j] = new Vector2();
                rowSpline[i].valueAt(rowLine[i][j], (float)j/(float)(pointsPerSpline-1));
            }
        }

        colControlPoints = new Vector2[numCols-1][numRows*pointsPerPiece+3];
        colSpline = new CatmullRomSpline[numCols-1];
        colLine = new Vector2[numCols-1][pointsPerSpline];
        for (int i=0; i<numCols-1; i++) {
            float offset = ((float)i+1f)*colSpacing;
            colControlPoints[i][0] = new Vector2(offset, -rowSpacing);
            for (int j=0; j<numRows; j++) {
                float sign = (rand.nextBoolean())? colSpacing: -colSpacing;

                if (j==0) colControlPoints[i][1+j*pointsPerPiece] = new Vector2(offset + randR(Fr), j*rowSpacing);
                else      colControlPoints[i][1+j*pointsPerPiece] = new Vector2(offset + randR(Fr), j*rowSpacing + randC(Fr));

                colControlPoints[i][2+j*pointsPerPiece] = new Vector2(offset        + randR(Ar), (j + 0.5f - A)*rowSpacing + randC(Ar));
                colControlPoints[i][3+j*pointsPerPiece] = new Vector2(offset+sign*C + randR(Br), (j + 0.5f - B)*rowSpacing + randC(Br));
                colControlPoints[i][4+j*pointsPerPiece] = new Vector2(offset+sign*D + randR(Br), (j + 0.5f    )*rowSpacing + randC(Br));
                colControlPoints[i][5+j*pointsPerPiece] = new Vector2(offset+sign*C + randR(Br), (j + 0.5f + B)*rowSpacing + randC(Br));
                colControlPoints[i][6+j*pointsPerPiece] = new Vector2(offset        + randR(Ar), (j + 0.5f + A)*rowSpacing + randC(Ar));
            }
            colControlPoints[i][1+numCols*pointsPerPiece] = new Vector2(offset + randR(Fr), (float)(numRows)*rowSpacing);
            colControlPoints[i][2+numCols*pointsPerPiece] = new Vector2(offset, (float)(numRows+1)*rowSpacing);

            colSpline[i] = new CatmullRomSpline<Vector2>(colControlPoints[i], false);

            for (int j=0; j<pointsPerSpline; j++) {
                colLine[i][j] = new Vector2();
                colSpline[i].valueAt(colLine[i][j], (float)j/(float)(pointsPerSpline-1));
            }
        }

        // Create a pixmap of the splines
        splineImg = new Pixmap(puzzleImg.getWidth(), puzzleImg.getHeight(), Pixmap.Format.RGBA8888);
        splineImg.setColor(0,0,0,0);
        splineImg.fill();
        splineImg.setColor(1f, 1f, 1f, 1f);
        for (Vector2 [] path: rowLine) {
            for (int i=1; i<pointsPerSpline; i++) {
                splineImg.drawLine((int)path[i-1].x, (int)path[i-1].y, (int)path[i].x, (int)path[i].y);
            }
        }
        for (Vector2 [] path: colLine) {
            for (int i=1; i<pointsPerSpline; i++) {
                splineImg.drawLine((int)path[i-1].x, (int)path[i-1].y, (int)path[i].x, (int)path[i].y);
            }
        }
        splineImgTex = new Texture(splineImg);
    }

    public float randR(float max) {
        return ((rand.nextFloat() * 2.0f * max) - max) * rowSpacing;
    }

    public float randC(float max) {
        return ((rand.nextFloat() * 2.0f * max) - max) * colSpacing;
    }

    public void generatePieces() {
        int i=0;
        int j=0;

        Vector2 pos = new Vector2(j*colSpacing, i*rowSpacing);
        Vector2 size = new Vector2(colSpacing, rowSpacing);
        if (i != 0) {
            pos.y -= rowSpacing/2.0f;
            size.y += rowSpacing/2.0f;
        }
        if (j != 0) {
            pos.x -= colSpacing/2.0f;
            size.x += colSpacing/2.0f;
        }
        if (i != (numRows-1)) {
            size.y += rowSpacing/2.0f;
        }
        if (j != (numCols-1)) {
            size.x += colSpacing/2.0f;
        }
        // TODO: use a Pixmap instead of a Texture for puzzleImg because:
        //  - Pixmap has methods for resampling, thus allowing large images to work
        //  - Pixmap allows bit banging of the data.
        //  - Pixmap.drawPixmap() copies a region of a Pixmap
        // The only thing Pixmap apparently doesn't allow is drawing to the screen (and maybe it does?)
        //  https://github.com/mattdesl/lwjgl-basics/wiki/LibGDX-Textures
        //  http://blog.gemserk.com/2012/01/04/modifying-textures-using-libgdx-pixmap-in-runtime-explained/
        // Also, look into PixmapPacker:
        //  https://libgdx.badlogicgames.com/nightlies/docs/api/com/badlogic/gdx/graphics/g2d/PixmapPacker.html

//        for (int i=0; i<numRows; i++) {
//            for (int j=0; j<numCols; j++) {
//                //TODO: implement puzzle piece generation
//            }
//        }
    }


    public void render(SpriteBatch batch, float delta) {
        if (displayImage) batch.draw(puzzleImgTex, 0,0);
        if (displaySplineImage) batch.draw(splineImgTex, 0,0);

        batch.end();

        sr.setProjectionMatrix(gameScreen.camera.combined);
        if (displaySplines) {
            for (int i=0; i<numRows-1; i++) drawRowSpline(i);
            for (int i=0; i<numCols-1; i++) drawColSpline(i);
        }

        batch.begin();
    }

    public void drawRowSpline(int i) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1,1,1,1);
        for (int j=1; j<rowLine[i].length; j++) {
            sr.line(rowLine[i][j-1], rowLine[i][j]);
        }
        sr.end();

//        sr.begin(ShapeRenderer.ShapeType.Filled);
//        sr.setColor(0.6f, 0.6f, 0.6f, 1);
//        for (int j=0; j<rowControlPoints[i].length; j++) {
//            sr.circle(rowControlPoints[i][j].x, rowControlPoints[i][j].y, 3);
//        }

        sr.end();
    }

    public void drawColSpline(int i) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1,1,1,1);
        for (int j=1; j<colLine[i].length; j++) {
            sr.line(colLine[i][j-1], colLine[i][j]);
        }
        sr.end();

//        sr.begin(ShapeRenderer.ShapeType.Filled);
//        sr.setColor(0.6f, 0.6f, 0.6f, 1);
//        for (int j=0; j<colControlPoints[i].length; j++) {
//            sr.circle(colControlPoints[i][j].x, colControlPoints[i][j].y, 3);
//        }

        sr.end();
    }

    // TODO: save and restore functions

    public void dispose() {
        puzzleImg.dispose();
        puzzleImgTex.dispose();
    }
}