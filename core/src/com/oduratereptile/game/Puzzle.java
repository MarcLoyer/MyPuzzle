package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.PerformanceCounter;

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
    public PixmapPacker packer = new PixmapPacker(1024, 1024, Pixmap.Format.RGBA8888, 2, true);
    public TextureAtlas pieceAtlas = new TextureAtlas();

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

    public PerformanceCounter pc = new PerformanceCounter("generatePieces");


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
        pc.start();
        generatePieces();
        pc.stop();
        pc.tick();
        pc.tick();
        Gdx.app.error("debug", pc.toString());
        report(pc);
    }

    private void report(PerformanceCounter pc) {
        Gdx.app.error("debug", pc.name);
        Gdx.app.error("debug", "count   = " + pc.time.count);
        Gdx.app.error("debug", "average = " + pc.time.average);
        Gdx.app.error("debug", "max     = " + pc.time.max);
        Gdx.app.error("debug", "min     = " + pc.time.min);
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
        splineImg = new Pixmap(puzzleImg.getWidth(), puzzleImg.getHeight(), Pixmap.Format.RGBA8888); // use Alpha format - saves masking
        splineImg.setColor(0,0,0,0);
        splineImg.fill();
        splineImg.setColor(1f, 1f, 1f, 1f);
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

    public Vector2 debugPos = new Vector2();
    public Vector2 debugSize = new Vector2();
    public Vector2 debugMid = new Vector2();
    public int debugI = 0;
    public int debugJ = 0;

    public void generatePieces() {
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                generatePiece(i, j);
                String s = i + "," + j;
                packer.pack(s, pieceImg);
            }
        }

        packer.updateTextureAtlas(pieceAtlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
        // TODO: save the TextureAtlas

        Vector2 pos = new Vector2();
        Vector2 size = new Vector2();
        Vector2 mid = new Vector2();
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                computePositions(pos, size, mid, i, j);
                String s = i + "," + j;
                pos.scl(2.0f);
                puzzlePiece.add(new PuzzlePiece(i, j, pos, 0, pieceAtlas.findRegion(s)));
            }
        }
    }

    public void generatePiece(int i, int j) {
        Vector2 pos = new Vector2();
        Vector2 size = new Vector2();
        Vector2 mid = new Vector2();
        computePositions(pos, size, mid, i, j);

//        Gdx.app.error("debug", "pos  = " + pos.toString());
//        Gdx.app.error("debug", "size = " + size.toString());
//        Gdx.app.error("debug", "mid  = " + mid.toString());
//        debugPos = pos.cpy();
//        debugSize = size.cpy();
//        debugMid = pos.cpy(); debugMid.y += size.y; debugMid.x += mid.x; debugMid.y -= mid.y;

        // TODO: possible bug - rounding of row and col Spacing might cause bit dropouts
        pieceImg = new Pixmap((int)size.x, (int)size.y, Pixmap.Format.RGBA8888);
        pieceImg.drawPixmap(puzzleImg, 0, 0, (int)pos.x, puzzleImg.getHeight() - (int)pos.y - (int)size.y, (int)size.x, (int)size.y);
        pieceImg.setBlending(Pixmap.Blending.None);

        // clear alpha across the whole image, then floodfill starting in the middle
        Color color = new Color();
        for (int x=0; x<pieceImg.getWidth(); x++) {
            for (int y=0; y<pieceImg.getHeight(); y++) {
                pieceImg.drawPixel(x, y, pieceImg.getPixel(x, y) & 0xFFFFFF00);
            }
        }

        ArrayList<GridPoint2> flood = new ArrayList<GridPoint2>();
        GridPoint2 pixel;
        boolean includeBorder = ((i+j)%2==0);

        flood.add(new GridPoint2((int)mid.x, (int)mid.y));
        while (!flood.isEmpty()) {
            pixel = flood.remove(0);
            setColor(pixel, pieceImg);
            addNieghbors(includeBorder, pixel, pieceImg, new GridPoint2((int)pos.x, (int)pos.y), flood);
        }

        pieceImgTex = new Texture(pieceImg);
        pieceImgTexLocation.set(pos.x, pos.y);
    }

    public void computePositions(Vector2 pos, Vector2 size, Vector2 mid, int i, int j) {
        pos.set((float)j*colSpacing, (float)i*rowSpacing);
        size.set(colSpacing, rowSpacing);
        mid.set(colSpacing/2.0f, rowSpacing/2.0f);
        if (i != 0) {
            pos.y -= rowSpacing/2.0f;
            size.y += rowSpacing/2.0f;
            mid.y += rowSpacing/2.0f;
        }
        if (j != 0) {
            pos.x -= colSpacing/2.0f;
            size.x += colSpacing/2.0f;
            mid.x += colSpacing/2.0f;
        }
        if (i != (numRows-1)) {
            size.y += rowSpacing/2.0f;
        }
        if (j != (numCols-1)) {
            size.x += colSpacing/2.0f;
        }
        mid.y = size.y - mid.y;
    }

    public void setColor(GridPoint2 pixel, Pixmap p) {
        p.drawPixel(pixel.x, pixel.y, p.getPixel(pixel.x, pixel.y) | 0x000000FF);
    }

    public void addNieghbors(boolean includeBorder, GridPoint2 pixel, Pixmap p, GridPoint2 loc, ArrayList<GridPoint2> flood) {
        // we check the four neighboring pixels. If they have already been fixed for color or
        // if they are on a Catmull-Rom spline then we don't add them.
        // TODO: do the diagonals too - the intersection of splines can get missed with just the four.
        // oops - border lines drawn diagonally can cause the flood fill to escape!
        GridPoint2 tmp = pixel.cpy();
        tmp.x--; if (needToAdd(includeBorder, tmp, p, loc, flood)) flood.add(tmp.cpy()); tmp.x++;
        tmp.x++; if (needToAdd(includeBorder, tmp, p, loc, flood)) flood.add(tmp.cpy()); tmp.x--;
        tmp.y--; if (needToAdd(includeBorder, tmp, p, loc, flood)) flood.add(tmp.cpy()); tmp.y++;
        tmp.y++; if (needToAdd(includeBorder, tmp, p, loc, flood)) flood.add(tmp.cpy()); tmp.y--;
    }

    private boolean needToAdd(boolean includeBorder, GridPoint2 pix, Pixmap p, GridPoint2 loc, ArrayList<GridPoint2> flood) {
        if ((pix.x<0)||(pix.x>=p.getWidth())) return false;
        if ((pix.y<0)||(pix.y>=p.getHeight())) return false;
        if (flood.contains(pix)) return false;
        if ((p.getPixel(pix.x, pix.y) & 0x000000FF) == 0x000000FF) return false;
        GridPoint2 pix2 = new GridPoint2(loc.x, splineImg.getHeight() - loc.y - p.getHeight());
        pix2.add(pix);
        if (splineImg.getPixel(pix2.x, pix2.y) == 0xFFFFFFFF) { // on the spline edge
            if (includeBorder) setColor(pix, p);
            return false;
        }
        return true;
    }

    public void render(SpriteBatch batch, float delta) {
        if (displayImage) batch.draw(puzzleImgTex, 0,0);

        // DEBUG code...
//        batch.draw(pieceImgTex, pieceImgTexLocation.x, pieceImgTexLocation.y);

        for (PuzzlePiece p : puzzlePiece) {
            p.draw(batch, 1);
        }

        if (displaySplineImage) batch.draw(splineImgTex, 0,0);

        batch.end();

        sr.setProjectionMatrix(gameScreen.camera.combined);
        if (displaySplines) {
            for (int i=0; i<numRows-1; i++) drawRowSpline(i);
            for (int i=0; i<numCols-1; i++) drawColSpline(i);
        }

        // DBUG code...
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(0,1,0,1);
        sr.rect(debugPos.x, debugPos.y, debugSize.x, debugSize.y);
        sr.end();
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.circle(debugMid.x, debugMid.y, 3);
        sr.end();

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
