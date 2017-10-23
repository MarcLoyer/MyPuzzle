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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.PerformanceCounter;
import com.badlogic.gdx.utils.PerformanceCounters;

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
        rowSpacing = puzzleImg.getHeight() / (float)numRows;

        this.numCols = numCols;
        colSpacing = puzzleImg.getWidth() / (float)numCols;

        // TODO: createPieces()
        //  - Catmull-Rom splines to define the shapes
        //  - divide into pieces
        //  - create separate texture for each piece (and add alpha mask to create the shape)
        //  - combine into an atlas
        //  - convert each texture into a texture region and create each piece

        pc.add("generateSplines");
        pc.add("generateSplineImage");
        pc.add("generatePieces");
        pc.add("  clear alpha");
        pc.add("  flood fill");
        pc.add("  create texture");
        pc.add("generate puzzle pieces");

        pc.tick();
        generateSplines();
        generatePieces();
        pc.tick();
        perfmonReport();
    }

    public void perfmonReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Performance report:\n");
        for (PerformanceCounter cntr: pc.counters) {
            sb.append(String.format("  %-30s  %5.2f (%5.4f)\n", cntr.name, cntr.time.average, cntr.load.average));
        }

        Gdx.app.error("debug", sb.toString());
    }

    public void generateSplines() {
        int pointsPerPiece = 6;
        int pointsPerSpline = numCols*50;

        pc.counters.get(0).start();
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
        pc.counters.get(0).stop();

        // Create a pixmap of the splines
        pc.counters.get(1).start();
        splineImg = new Pixmap(puzzleImg.getWidth(), puzzleImg.getHeight(), Pixmap.Format.RGBA8888); // use Alpha format - saves masking
        splineImg.setBlending(Pixmap.Blending.None);
        splineImg.setColor(0,0,0,0);
        splineImg.fill();
        splineImg.setColor(1f, 1f, 1f, 0.6f);
        for (Vector2 [] path: rowLine) {
            for (int i=1; i<pointsPerSpline; i++) {
                int x1 = (int)(path[i-1].x + 0.5f);
                int y1 = splineImg.getHeight() - (int)(path[i-1].y + 0.5f);
                int x2 = (int)(path[i].x + 0.5f);
                int y2 = splineImg.getHeight() - (int)(path[i].y + 0.5f);
                splineImg.drawLine(x1, y1, x2, y2);
            }
        }
        for (Vector2 [] path: colLine) {
            for (int i=1; i<pointsPerSpline; i++) {
                int x1 = (int)(path[i-1].x + 0.5f);
                int y1 = splineImg.getHeight() - (int)(path[i-1].y + 0.5f);
                int x2 = (int)(path[i].x + 0.5f);
                int y2 = splineImg.getHeight() - (int)(path[i].y + 0.5f);
                splineImg.drawLine(x1, y1, x2, y2);
            }
        }
        splineImgTex = new Texture(splineImg);
        pc.counters.get(1).stop();
    }

    public float randR(float max) {
        return ((rand.nextFloat() * 2.0f * max) - max) * rowSpacing;
    }

    public float randC(float max) {
        return ((rand.nextFloat() * 2.0f * max) - max) * colSpacing;
    }

    public Pixmap pieceImg;
    public Texture pieceImgTex;
    public Vector2 pieceImgTexLocation = new Vector2();

    public PuzzlePieceCoords [] debugCoords;

    public void generatePieces() {
        debugCoords = new PuzzlePieceCoords[numCols*numRows];

        pc.counters.get(2).start();
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                generatePiece(i, j);
                String s = i + "," + j;
                packer.pack(s, pieceImg);
            }
        }
        pc.counters.get(2).stop();

        pc.counters.get(6).start();
        packer.updateTextureAtlas(pieceAtlas, Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false);
        // TODO: save the TextureAtlas

        PuzzlePieceCoords coords;
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                coords = new PuzzlePieceCoords(i, j, puzzleImg, numRows, numCols);
                String s = i + "," + j;
                puzzlePiece.add(new PuzzlePiece(i, j, coords.pos, 0, pieceAtlas.findRegion(s)));
            }
        }
        pc.counters.get(6).stop();
    }

    public void generatePiece(int i, int j) {
        PuzzlePieceCoords coords = new PuzzlePieceCoords(i, j, puzzleImg, numRows, numCols);
        debugCoords[i*numCols+j] = coords;

        pieceImg = new Pixmap((int)coords.size.x, (int)coords.size.y, Pixmap.Format.RGBA8888);
        pieceImg.drawPixmap(puzzleImg, 0, 0,
                (int)coords.pos.x, puzzleImg.getHeight() - (int)coords.pos.y - (int)coords.size.y,
                (int)coords.size.x, (int)coords.size.y);
        pieceImg.setBlending(Pixmap.Blending.None);

        // clear alpha across the whole image, then floodfill starting in the middle
        pc.counters.get(3).start();
        for (int x=0; x<pieceImg.getWidth(); x++) {
            for (int y=0; y<pieceImg.getHeight(); y++) {
                pieceImg.drawPixel(x, y, pieceImg.getPixel(x, y) & 0xFFFFFF00);
            }
        }
        pc.counters.get(3).stop();

        boolean includeBorder = ((i+j)%2==0);

        pc.counters.get(4).start();
        puzzleFill.initialize(pieceImg, splineImg, coords, includeBorder);
        puzzleFill.fill((int)coords.mid.x, (int)coords.mid.y);
        pc.counters.get(4).stop();

        pc.counters.get(5).start();
        pieceImgTex = new Texture(pieceImg);
        pieceImgTexLocation.set(coords.pos.x, coords.pos.y);
        pc.counters.get(5).stop();
    }

    public void render(SpriteBatch batch, float delta) {
        if (displayImage) batch.draw(puzzleImgTex, 0,0);

        // DEBUG code...
//        batch.draw(pieceImgTex, pieceImgTexLocation.x, pieceImgTexLocation.y);

        for (PuzzlePiece p : puzzlePiece) {
            boolean isEven = ((p.col + p.row)%2 == 0);
            if (displayAllPieces) {
                p.draw(batch, 1);
            } else {
                if ((displayEvenPieces && isEven) || (!displayEvenPieces && !isEven))
                    p.draw(batch, 1);
            }
        }

        if (displaySplineImage) batch.draw(splineImgTex, 0,0);

        batch.end();

        sr.setProjectionMatrix(gameScreen.camera.combined);
        if (displaySplines) {
            for (int i=0; i<numRows-1; i++) drawRowSpline(i);
            for (int i=0; i<numCols-1; i++) drawColSpline(i);
        }

        // DEBUG code...
//        sr.begin(ShapeRenderer.ShapeType.Line);
//        for (PuzzlePieceCoords ppc: debugCoords) {
//            sr.setColor(0,1,0,1);
//            sr.rect(ppc.pos.x, ppc.pos.y, ppc.size.x, ppc.size.y);
//            sr.setColor(1,1,0,1);
//            sr.rect(ppc.pos.x + ppc.bbLL.x, ppc.pos.y + ppc.bbLL.y,
//                    ppc.bbUR.x - ppc.bbLL.x, ppc.bbUR.y - ppc.bbLL.y);
//            sr.begin(ShapeRenderer.ShapeType.Filled);
//            sr.circle(ppc.mid.x, ppc.mid.y, 3);
//        }
//        sr.end();

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
        packer.dispose();
        pieceAtlas.dispose();
    }
}
