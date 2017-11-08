package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.oduratereptile.game.HudScreen.Corner.*;

/**
 * Created by Marc on 10/3/2017.
 */

public class Debug2Screen extends HudScreen {
    public OrthographicCamera camera;
    public Puzzle puzzle;

    public SpriteBatch spriteBatch;
    public ShaderProgram shader;

    public int numRows;
    public int numCols;

    public float worldWidth = 1000;

    public ArrayList<Vector2> splineShape = new ArrayList<Vector2>();
    public Pixmap sourceImg;
    public Texture sourceTex;
    public PuzzlePieceCoords coords0, coords1, coords2, coords3;
    public TextureRegion shapeTex0, shapeTex1, shapeTex2, shapeTex3;
    public Sprite shapeSprite0, shapeSprite1, shapeSprite2, shapeSprite3;

    public float padding = 20;

    public FloatArray verts;
    public ShortArray tris;
    public Mesh mesh;

    public Debug2Screen(final MyPuzzle game, Puzzle puzzle) {
        super(game);
        camera = new OrthographicCamera();
        addInputController(new GestureDetector(new OrthoGestureListener(camera)));
        spriteBatch = new SpriteBatch();

        this.puzzle = puzzle;
        numRows = puzzle.numRows;
        numCols = puzzle.numCols;

        debugHUD(false);

        Button button = new Button(game.skin, "leftarrow");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        getTable(LR).add(button);

        TextButton textButton = new TextButton("pad", game.skin);
        textButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                padding = (padding==0)? 20: 0;
            }
        });
        getTable(UL).add(textButton);

        shader = new ShaderProgram(
                Gdx.files.internal("shaders/mesh.vert"),
                Gdx.files.internal("shaders/mesh.frag")
        );
        if (shader.getLog().length()!=0)
            Gdx.app.error("debug", shader.getLog());

        int initialRow = 0;
        int initialCol = 0;

        coords0 = new PuzzlePieceCoords(initialRow, initialCol, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords0);
        shapeTex0 = renderToTexture(coords0);
        shapeSprite0 = new Sprite(shapeTex0);

        coords1 = new PuzzlePieceCoords(initialRow, initialCol+1, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords1);
        shapeTex1 = renderToTexture(coords1);
        shapeSprite1 = new Sprite(shapeTex1);

        coords2 = new PuzzlePieceCoords(initialRow+1, initialCol, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords2);
        shapeTex2 = renderToTexture(coords2);
        shapeSprite2 = new Sprite(shapeTex2);

        coords3 = new PuzzlePieceCoords(initialRow+1, initialCol+1, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords3);
        shapeTex3 = renderToTexture(coords3);
        shapeSprite3 = new Sprite(shapeTex3);

    }

    public void setupData(PuzzlePieceCoords coords) {
        int row = coords.row;
        int col = coords.col;

        // Clip the image around the piece we are drawing
        sourceImg = new Pixmap((int) coords.size.x, (int) coords.size.y, Pixmap.Format.RGBA8888);
        sourceImg.drawPixmap(puzzle.puzzleImg, 0, 0,
                (int) coords.pos.x, puzzle.puzzleImg.getHeight() - (int) coords.pos.y - (int) coords.size.y,
                (int) coords.size.x, (int) coords.size.y);
        sourceImg.setBlending(Pixmap.Blending.None);

        sourceTex = new Texture(sourceImg);

        Vector2 upperRight = new Vector2(puzzle.puzzleImg.getWidth(), puzzle.puzzleImg.getHeight());

        // Collect the portions of the splines that pertain to our piece
        splineShape.clear();
        // top
        if (row == (numRows-1)) {
            if (col == 0) {
                splineShape.add(new Vector2(0, upperRight.y));
            } else {
                splineShape.add(puzzle.colLine[col - 1][numRows * 50].cpy());
            }
        } else {
            for (int i=col*50; i<(col+1)*50; i++) { splineShape.add(puzzle.rowLine[row][i].cpy()); }
        }

        // right
        if (col == (numCols-1)) {
            if (row == (numRows-1)) {
                splineShape.add(new Vector2(upperRight.x, upperRight.y));
            } else {
                splineShape.add(puzzle.rowLine[row][numCols * 50].cpy());
            }
        } else {
            for (int i=(row+1)*50; i>row*50; i--) { splineShape.add(puzzle.colLine[col][i].cpy()); }
        }
        // bottom
        if (row == 0) {
            if (col == (numCols-1)) {
                splineShape.add(new Vector2(upperRight.x, 0));
            } else {
                splineShape.add(puzzle.colLine[col][0].cpy());
            }
        } else {
            for (int i=(col+1)*50; i>col*50; i--) { splineShape.add(puzzle.rowLine[row-1][i].cpy()); }
        }
        // left
        if (col == 0) {
            if (row == 0) {
                splineShape.add(new Vector2(0, 0));
            } else {
                splineShape.add(puzzle.rowLine[row - 1][0].cpy());
            }
        } else {
            for (int i=row*50; i<(row+1)*50; i++) { splineShape.add(puzzle.colLine[col-1][i].cpy()); }
        }

        for (Vector2 v: splineShape) v.sub(coords.pos);

//        // DEBUG: use a simpler shape for now
//        splineShape.clear();
//        splineShape.add(new Vector2(10,10));
//        splineShape.add(new Vector2(10,30));
//        splineShape.add(new Vector2(30,30));
//        splineShape.add(new Vector2(30,60));
//        splineShape.add(new Vector2(60,30));
//        splineShape.add(new Vector2(60,10));

        worldWidth = 2.0f * sourceImg.getWidth() + 60f;
        updateCameraViewport();

        // Build the mesh
        EarClippingTriangulator triangulator = new EarClippingTriangulator();
        verts = new FloatArray(2*splineShape.size());
        Vector3 min = new Vector3(splineShape.get(0).x, splineShape.get(0).y, 0);
        Vector3 max = new Vector3(splineShape.get(0).x, splineShape.get(0).y, 0);
        for (int i=0; i<splineShape.size(); i++) {
            Vector2 v = splineShape.get(i).cpy();
            verts.addAll(v.x, v.y);
            if (v.x < min.x) min.x = v.x;
            if (v.y < min.y) min.y = v.y;
            if (v.x > max.x) max.x = v.x;
            if (v.y > max.y) max.y = v.y;
        }
        min.x = (float)Math.floor(min.x);
        min.y = (float)Math.floor(min.y);
        max.x = (float)Math.ceil(max.x);
        max.y = (float)Math.ceil(max.y);
        coords.boundingBox = new BoundingBox(min, max);
        tris = triangulator.computeTriangles(verts);
        createMesh(coords);
    }

    public void createMesh(PuzzlePieceCoords coords) {
        int numVerts = verts.size/2;
        int numInds = tris.size;

        int vSize = 5; // pos(x,y,z), texture(u,v)

        float [] vertices = new float[vSize*numVerts];
        for (int i=0; i<numVerts; i++) {
            vertices[i*vSize]   = verts.get(i*2)   - coords.boundingBox.min.x;
            vertices[i*vSize+1] = verts.get(i*2+1) - coords.boundingBox.min.y;
            vertices[i*vSize+2] = 0f;
            vertices[i*vSize+3] = verts.get(i*2) / coords.size.x;
            vertices[i*vSize+4] = 1f - (verts.get(i*2+1) / coords.size.y);
        }
        short [] indices = new short[numInds];
        for (int i=0; i<numInds; i++) {
            indices[i] = tris.get(i);
        }

        mesh = new Mesh(true, numVerts, numInds,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
                new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0")
        );
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
    }

    public TextureRegion renderToTexture(PuzzlePieceCoords coords) {
        int bufferWidth = (int) coords.boundingBox.getWidth();
        int bufferHeight = (int) coords.boundingBox.getHeight();

        FrameBuffer fbo = new FrameBuffer(RGBA8888, bufferWidth, bufferHeight, false);
        TextureRegion fboTex = new TextureRegion(fbo.getColorBufferTexture(), 0, 0, bufferWidth, bufferHeight);
        fbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fboTex.flip(false, true);

        camera.setToOrtho(false, bufferWidth, bufferHeight);
        game.batch.setProjectionMatrix(camera.combined);

        fbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glDisable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        drawMesh(0,0);
        fbo.end();

        return fboTex;
    }

    public float w, h;

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.9f, 0.6f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        spriteBatch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(sourceTex, 20, 60);
        game.batch.end();

        // draw the mesh
        drawMesh(20, 80 + h);

        // draw the sprite
        drawSprite(shapeSprite0, coords0, 40+w, 60);
        drawSprite(shapeSprite1, coords1, 40+w, 60);
        drawSprite(shapeSprite2, coords2, 40+w, 60);
        drawSprite(shapeSprite3, coords3, 40+w, 60);

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        w = sourceImg.getWidth();
        h = sourceImg.getHeight();
        game.shapeRenderer.setColor(0f, 0f, 1f, 1f);
        game.shapeRenderer.rect(20, 60, w, h);
        drawBoundingBox(game.shapeRenderer, coords3, 20, 60);
        drawSpriteBounds(game.shapeRenderer, shapeSprite0);
        drawSpriteBounds(game.shapeRenderer, shapeSprite1);
        drawSpriteBounds(game.shapeRenderer, shapeSprite2);
        drawSpriteBounds(game.shapeRenderer, shapeSprite3);
        drawSplineShape(game.shapeRenderer, 20, 60);
        drawTriangles(game.shapeRenderer, 20, 60);

        game.shapeRenderer.end();

        super.render(delta);
    }

    public Vector2 v1 = new Vector2();
    public Vector2 v2 = new Vector2();
    public Vector2 v3 = new Vector2();

    public void drawBoundingBox(ShapeRenderer sr, PuzzlePieceCoords coords, float x, float y) {
        sr.setColor(1f, 0f, 0f, 1f);
        v1.set(x+coords.boundingBox.min.x, y+coords.boundingBox.min.y);
        v2.set(x+coords.boundingBox.max.x, y+coords.boundingBox.max.y);
        v2.sub(v1);
        sr.rect(v1.x, v1.y, v2.x, v2.y);
    }

    public void drawSplineShape(ShapeRenderer sr, float x, float y) {
        sr.setColor(1f, 1f, 1f, 1f);

        v1.set(splineShape.get(splineShape.size()-1)).add(x,y);

        for (int i=0; i<splineShape.size(); i++) {
            v2.set(splineShape.get(i)).add(x,y);
            sr.line(v1, v2);
            v1.set(v2);
        }
    }

    public void drawTriangles(ShapeRenderer sr, float x, float y) {
        sr.setColor(1f, 1f, 1f, 1f);

        if (tris == null) return;
        if (verts == null) return;

        for (int i=0; i<tris.size; i+=3) {
            v1.set(verts.get(2*tris.get(i)), verts.get(2*tris.get(i)+1)).add(x,y);
            v2.set(verts.get(2*tris.get(i+1)), verts.get(2*tris.get(i+1)+1)).add(x,y);
            v3.set(verts.get(2*tris.get(i+2)), verts.get(2*tris.get(i+2)+1)).add(x,y);

            sr.triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
        }
    }

    public void drawMesh(float x, float y) {
        Matrix4 mat = new Matrix4(new Vector3(x,y,0), new Quaternion(), new Vector3(1,1,1));
        mesh.transform(mat);

        sourceTex.bind();
        shader.begin();
        shader.setUniformMatrix("u_projTrans", game.batch.getProjectionMatrix());
        shader.setUniformi("u_texture", 0);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();

        mesh.transform(mat.inv());
    }

    public void drawSprite(Sprite sprite, PuzzlePieceCoords coords, float x, float y) {
        float offsetX = x + coords.pos.x - coords0.pos.x + coords.boundingBox.min.x + padding*(coords.col-coords0.col);
        float offsetY = y + coords.pos.y - coords0.pos.y + coords.boundingBox.min.y + padding*(coords.row-coords0.row);

        sprite.setPosition(offsetX, offsetY);
        spriteBatch.begin();
        sprite.draw(spriteBatch);
        spriteBatch.end();
    }

    public void drawSpriteBounds(ShapeRenderer sr, Sprite sprite) {
        sr.setColor(0f, 0f, 1f, 1f);
        sr.rect(sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        updateCameraViewport();
    }

    public void updateCameraViewport() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera.setToOrtho(false, worldWidth, worldWidth * h / w);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
