package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Marc on 10/7/2017.
 */

public class Puzzle extends OrthoGestureListener implements PuzzleGroup.PuzzleGroupListener {
    public GameScreen gameScreen;
    public Pixmap puzzleImg;
    public ShapeRenderer sr;
    public Random rand = new Random();
    public Json json;
    public Base64Coder base64Coder;

    public ObjectMap<String, PuzzlePiece> puzzlePiece = new ObjectMap<String, PuzzlePiece>();
    public ObjectMap<String, PuzzleGroup> puzzleGroup = new ObjectMap<String, PuzzleGroup>();
    public GameData gameData = new GameData(puzzlePiece, puzzleGroup);
    public PuzzleGroup largestGroup = null;
    public PuzzlePieceManager manager;

    public boolean displayImage = false;
    public boolean displaySplines = false;
    public boolean displayEvenPieces = false;
    public boolean displayAllPieces = true;
    public boolean displayTapSquares = false;

    public Puzzle(GameScreen gameScreen) {
        super(gameScreen.camera);
        this.gameScreen = gameScreen;
        sr = gameScreen.game.shapeRenderer;
        manager = new PuzzlePieceManager(this);
        json = new Json();
        PuzzleGroup.addListener(this);
    }

    public void onCreate(PuzzleGroup group) {
        puzzleGroup.put(group.getId(), group);
        if ((largestGroup==null) || (group.size()>largestGroup.size())) {
            setLargestGroup(group);
            gameData.largestGroupID = group.getId();
        }
        gameData.groupCount = PuzzleGroup.count;
    }

    public void onDestroy(PuzzleGroup group) {
        puzzleGroup.remove(group.getId());
    }

    public Texture puzzleImgTex;

    public void setPicture(FileHandle fh) {
        setPicture(new Pixmap(fh));
    }

    public void setPicture(Pixmap pixmap) {
        puzzleImg = pixmap;

        IntBuffer buffer = BufferUtils.newIntBuffer(16);
        Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buffer);
        int maxImageSize = buffer.get(0);
        Gdx.app.error("debug", "Max image size for this device = " + maxImageSize);

        puzzleImgTex = new Texture(puzzleImg); //TODO: split into regions if the Pixmap is too big, or maybe mipmaps?
        Gdx.app.error("debug", "Current image size = (" + pixmap.getWidth() + ", " + pixmap.getHeight() + ")");
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
        setPieceNeighbors();
    }

    public PuzzlePiece getPiece(int row, int col) {
        return puzzlePiece.get(row+","+col);
    }

    /**
     * this routine assumes all pieces are in their solved state.
     */
    public void setPieceNeighbors() {
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                puzzlePiece.get(i+","+j).setNeighbors(
                        (i==numRows-1)? null: puzzlePiece.get((i+1)+","+j),
                        (j==numCols-1)? null: puzzlePiece.get(i+","+(j+1)),
                        (i==0)? null: puzzlePiece.get((i-1)+","+j),
                        (j==0)? null: puzzlePiece.get(i+","+(j-1))
                );
            }
        }
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
        puzzlePacker.createAtlas();
        puzzlePacker.save(Gdx.files.local(gameData.getBasename() + "/" + gameData.getBasename() + ".atlas"));
    }

    public void createPuzzlePieces() {
        PuzzlePiece p;

        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                p = new PuzzlePiece(i, j, puzzlePacker.getData(i,j), puzzlePacker.findRegion(i,j), true);
                puzzlePiece.put(p.getID(), p);
                generateHighlight(p);
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

        manager.act(delta);

        for (PuzzlePiece p : puzzlePiece.values()) {
            boolean isEven = ((p.col + p.row)%2 == 0);
            if (displayAllPieces) {
                if(!p.isSelected()) p.draw(batch, 1);
            } else {
                if ((displayEvenPieces && isEven) || (!displayEvenPieces && !isEven))
                    if (!p.isSelected()) p.draw(batch, 1);
            }
        }

        for (PuzzlePiece p: selectedPiece) {
            p.draw(batch, 1.0f);
            p.drawHighlight(batch, 1.0f);
        }

        batch.end();

        sr.setProjectionMatrix(gameScreen.camera.combined);
        if (displaySplines) {
            for (int i=0; i<numRows-1; i++) drawRowSpline(i);
            for (int i=0; i<numCols-1; i++) drawColSpline(i);
        }

        // Draw tap squares
        if (displayTapSquares) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(0f, 0.7f, 0f, 1f);
            for (PuzzlePiece p: puzzlePiece.values()) {
                p.drawTapSquare(sr);
            }
            sr.end();
        }

        for (PuzzlePiece p: selectedPiece) {
            p.drawDebugLines(sr);
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
//        sr.end();
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
//        sr.end();
    }

    // I currently only select one piece at a time, but maybe in the future I'll
    // do something more fancy...
    public ArrayList<PuzzlePiece> selectedPiece = new ArrayList<PuzzlePiece>();

    // use this to select/deselect pieces
    @Override
    public boolean tap(float x, float y, int count, int button) {
        Vector3 c = cam.unproject(new Vector3(x,y,0));

        // check if the tap location selects a new piece
        boolean isHit = false;
        for (PuzzlePiece p: puzzlePiece.values()) {
            if (p.hit(c)) {
                if (p.isSelected()) {
                    p.select(false);
                    selectedPiece.clear();
                } else {
                    for (PuzzlePiece s: selectedPiece) {
                        s.select(false);
                    }
                    selectedPiece.clear();

                    p.select();
                    selectedPiece.add(p);
                }
                isHit = true;
                break;
            }
        }
        if (!isHit) {
            for (PuzzlePiece s: selectedPiece) {
                s.select(false);
            }
            selectedPiece.clear();
        }

        return true;
    }

    public void generateHighlight(PuzzlePiece p) {
        gameScreen.game.outlineShader.setup(p);
        p.highlight = gameScreen.game.outlineShader.renderToTexture(gameScreen.game.batch);
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

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);
            if (p.snapsWith>0) {
                p.snapIn();
            }
        }
        return super.panStop(x, y, pointer, button);
    }

    @Override
    public void pinchStop() {
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);
            if (p.snapsWith>0) {
                p.snapIn();
            }
        }
    }

    public void setLargestGroup(PuzzleGroup group) {
        largestGroup = group;
        // TODO: implement win()
//        if (largestGroup.size() = numRows*numCols) win();
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
    }

    static public class GameData {
        public String puzzleName = "undefined";
        public int rows = 0;
        public int cols = 0;
        public ObjectMap<String, PuzzlePiece> puzzlePieces;
        public ObjectMap<String, PuzzleGroup> puzzleGroups;
        public String largestGroupID = "";
        public int groupCount = 0;
        public String textureAtlasFilename = null;

        public GameData() {
            puzzlePieces = new ObjectMap<String, PuzzlePiece>();
            puzzleGroups = new ObjectMap<String, PuzzleGroup>();
        }

        public GameData(ObjectMap<String, PuzzlePiece> p, ObjectMap<String, PuzzleGroup> g) {
            puzzlePieces = p;
            puzzleGroups = g;
        }

        public String getBasename() {
            return puzzleName + "_" + rows + "_" + cols;
        }
    }
}
