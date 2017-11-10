package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.utils.PerformanceCounters;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Marc on 10/7/2017.
 */

public class Puzzle extends OrthoGestureListener {
    public GameScreen gameScreen;
    public Pixmap puzzleImg;
    public ShapeRenderer sr;
    public Random rand = new Random();
    public PuzzleFill puzzleFill = new PuzzleFill();
    public PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 2, true);
    public TextureAtlas pieceAtlas = new TextureAtlas();

    public ArrayList<PuzzlePiece> puzzlePiece = new ArrayList<PuzzlePiece>();
//    public ArrayList<PuzzleGroup> puzzleGroup;
    public BoundingBox puzzleBounds = new BoundingBox();

    public boolean displayImage = false;
    public boolean displaySplines = false;
    public boolean displaySplineImage = false;
    public boolean displayEvenPieces = false;
    public boolean displayAllPieces = true;


    public Puzzle(GameScreen gameScreen) {
        super(gameScreen.camera);
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

    public Pixmap splineImg;
    public Texture splineImgTex;

    public PerformanceCounters pc = new PerformanceCounters();


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

        //  - Catmull-Rom splines to define the shapes
        //  - divide into pieces
        //  - create separate texture for each piece (and add alpha mask to create the shape)
        //  - combine into an atlas
        //  - convert each texture into a texture region and create each piece

        generateSplines();
        createMeshPieceAtlas();
        createPuzzlePieces();
    }

    public PuzzlePacker puzzlePacker;

    public void createMeshPieceAtlas() {
        puzzlePacker = new PuzzlePacker(this, 1024);
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                puzzlePacker.pack(i,j);
            }
        }
        puzzlePacker.createAtlas();
    }

    public void createPuzzlePieces() {
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                puzzlePiece.add(new PuzzlePiece(i, j, puzzlePacker.getPosition(i,j), puzzlePacker.getRegion(i,j), true));
            }
        }
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
            colControlPoints[i][1+numCols*controlsPerPiece] = new Vector2(offset + randC(Fr), (float)rowOffset(numRows));
            colControlPoints[i][2+numCols*controlsPerPiece] = new Vector2(offset, (float)rowOffset(numRows+1));

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

    public void render(SpriteBatch batch, float delta) {
        if (displayImage) batch.draw(puzzleImgTex, 0,0);

        for (PuzzlePiece p : puzzlePiece) {
            boolean isEven = ((p.col + p.row)%2 == 0);
            if (displayAllPieces) {
                p.draw(batch, 1);
            } else {
                if ((displayEvenPieces && isEven) || (!displayEvenPieces && !isEven))
                    p.draw(batch, 1);
            }
        }

        for (PuzzlePiece p: selectedPiece) {
//            p.drawHighlight(batch, 1.0f);
        }

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

    // I currently only select one piece at a time, but maybe in the future I'll
    // do something more fancy...
    public ArrayList<PuzzlePiece> selectedPiece = new ArrayList<PuzzlePiece>();

    // use this to select/deselect pieces
    @Override
    public boolean tap(float x, float y, int count, int button) {
        Vector3 c = cam.unproject(new Vector3(x,y,0));

        // deselect any selected pieces
        for (PuzzlePiece p: selectedPiece) {
            p.select(false);
        }
        selectedPiece.clear();

        // check if the tap location selects a new piece
        for (PuzzlePiece p: puzzlePiece) {
            if (hit(p, c.x, c.y)) {
                p.select();
                if (p.highlight==null) {
                    generateHighlight(p);
                }
                selectedPiece.add(p);
            }
        }

        return true;
    }

    public void generateHighlight(PuzzlePiece p) {
        gameScreen.game.outlineShader.setup(p);
        // TODO: bug - highlight is transposed!
        p.highlight = gameScreen.game.outlineShader.renderToTexture(gameScreen.game.batch);
//        p.highlight.flip(false, false);
        //  true,  true:  top->right,  right->bottom
        //  true,  false: top->bottom, right->left
        //  false, true:  top->right,  right->top
        //  false, false: top->left,   right->top
    }

    private boolean hit(PuzzlePiece p, float x, float y) {
        float rad = Math.min(rowSpacing(p.row), colSpacing(p.col))/2.0f;
        float rad2 = rad*rad;
        Vector2 m = p.getMid();
        float d2 = m.dst2(x,y);

        return (d2 < rad2);
    }

    // use this to move the selected piece (or maybe drag and drop?)
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);
            Vector3 c = cam.unproject(new Vector3(deltaX, deltaY, 0)).sub(cam.unproject(new Vector3(0, 0, 0)));
            p.moveBy(c.x,c.y);
        } else {
            return super.pan(x,y,deltaX,deltaY);
        }

        return true;
    }

    public float initialRotation;

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if ((pointer==1) && (selectedPiece.size()==1)) {
            initialRotation = selectedPiece.get(0).getRotation();
        }

        return super.touchDown(x,y,pointer, button);
    }

    // Use this to rotate a selected piece
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);

            Vector2 v1 = new Vector2(initialPointer2);
            v1.sub(initialPointer1);

            Vector2 v2 = new Vector2(pointer2);
            v2.sub(pointer1);

            float angle = ((float)Math.atan2(v1.x, v1.y) - (float)Math.atan2(v2.x, v2.y)) * MathUtils.radiansToDegrees;
            p.setRotation(initialRotation - angle);
        } else {
            return super.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        }

        return true;
    }


    // TODO: save and restore functions

    public void dispose() {
        puzzleImg.dispose();
        puzzleImgTex.dispose();
        packer.dispose();
        pieceAtlas.dispose();
    }
}
