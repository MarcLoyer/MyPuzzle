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
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;
import static com.oduratereptile.game.HudScreen.Corner.LR;

/**
 * Created by Marc on 10/3/2017.
 */

public class Debug2Screen extends HudScreen {
    public OrthographicCamera camera;
    public Puzzle puzzle;

    public ModelBatch modelBatch;
    public SpriteBatch spriteBatch;
    public ShaderProgram shader;

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
//        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();

        this.puzzle = puzzle;

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

        shader = new ShaderProgram(
                Gdx.files.internal("shaders/mesh.vert"),
                Gdx.files.internal("shaders/mesh.frag")
        );
        if (shader.getLog().length()!=0)
            Gdx.app.error("debug", shader.getLog());

        coords0 = new PuzzlePieceCoords(1, 1, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords0);
        shapeTex0 = renderToTexture();
        shapeSprite0 = new Sprite(shapeTex0);

        coords1 = new PuzzlePieceCoords(1, 2, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords1);
        shapeTex1 = renderToTexture();
        shapeSprite1 = new Sprite(shapeTex1);

        coords2 = new PuzzlePieceCoords(2, 1, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords2);
        shapeTex2 = renderToTexture();
        shapeSprite2 = new Sprite(shapeTex2);

        coords3 = new PuzzlePieceCoords(2, 2, puzzle.puzzleImg, puzzle.numRows, puzzle.numCols);
        setupData(coords3);
        shapeTex3 = renderToTexture();
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

        // Collect the portions of the splines that pertain to our piece
        // TODO: support for edge pieces
        splineShape.clear();
        for (int i=col*50; i<(col+1)*50; i++) { splineShape.add(puzzle.rowLine[row][i].cpy()); }
        for (int i=(row+1)*50; i>row*50; i--) { splineShape.add(puzzle.colLine[col][i].cpy()); }
        for (int i=(col+1)*50; i>col*50; i--) { splineShape.add(puzzle.rowLine[row-1][i].cpy()); }
        for (int i=row*50; i<(row+1)*50; i++) { splineShape.add(puzzle.colLine[col-1][i].cpy()); }

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
        for (int i=0; i<splineShape.size(); i++) {
            Vector2 v = splineShape.get(i);
            verts.addAll(v.x, v.y);
        }
        tris = triangulator.computeTriangles(verts);
        createMesh();
//        createModel();
    }

    public void createMesh() {
        int numVerts = verts.size/2;
        int numInds = tris.size;

        int vSize = 5; // pos(x,y,z), texture(u,v)

        float [] vertices = new float[vSize*numVerts];
        for (int i=0; i<numVerts; i++) {
            vertices[i*vSize]   = verts.get(i*2);
            vertices[i*vSize+1] = verts.get(i*2+1);
            vertices[i*vSize+2] = 0f;
            vertices[i*vSize+3] = verts.get(i*2) / coords0.size.x;
            vertices[i*vSize+4] = 1f - (verts.get(i*2+1) / coords0.size.y);
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

    public TextureRegion renderToTexture() {
        int bufferWidth = (int) coords0.size.x;
        int bufferHeight = (int) coords0.size.y;
        FrameBuffer fbo = new FrameBuffer(RGBA8888, bufferWidth, bufferHeight, false);
        TextureRegion fboTex = new TextureRegion(fbo.getColorBufferTexture(), bufferWidth, bufferHeight);
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

    Model model;
    ModelInstance modelInstance;

    public void createModel() {
        int numVerts = verts.size/2;
        int numInds = tris.size;

        int vSize = 8; // pos(x,y,z), nor(x,y,z), texture(u,v)

        float [] vertices = new float[vSize*numVerts];
        for (int i=0; i<numVerts; i++) {
            vertices[i*vSize]   = verts.get(i*2);
            vertices[i*vSize+1] = verts.get(i*2+1);
            vertices[i*vSize+2] = 0f;
            vertices[i*vSize+3] = 0f;
            vertices[i*vSize+4] = 0f;
            vertices[i*vSize+5] = 1f;
            vertices[i*vSize+6] = verts.get(i*2) / coords0.size.x;
            vertices[i*vSize+7] = 1f - (verts.get(i*2+1) / coords0.size.y);
        }
        short [] indices = new short[numInds];
        for (int i=0; i<numInds; i++) {
            indices[i] = tris.get(i);
        }

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        MeshPartBuilder mpb = mb.part("shape", GL20.GL_TRIANGLES,
                VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal |
                VertexAttributes.Usage.TextureCoordinates,
                new Material(TextureAttribute.createDiffuse(sourceTex))
        );
        mpb.vertex(vertices);
        for (int i=0; i<numInds; i++) mpb.index(tris.get(i));

        model = mb.end();
        modelInstance = new ModelInstance(model);
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
        drawMesh(40+w, 60);

        // draw the sprite
        drawSprite(shapeSprite0, coords0, 40+w, 60);
        drawSprite(shapeSprite1, coords1, 40+w, 60);
        drawSprite(shapeSprite2, coords2, 40+w, 60);
        drawSprite(shapeSprite3, coords3, 40+w, 60);

//        // TODO: draw the model
//        modelBatch.begin(camera);
//        modelBatch.render(modelInstance);
//        modelBatch.end();

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        w = sourceImg.getWidth();
        h = sourceImg.getHeight();
        game.shapeRenderer.setColor(0f, 0f, 1f, 1f);
        game.shapeRenderer.rect(20, 60, w, h);
        game.shapeRenderer.rect(40+w, 60, w, h);
        drawSplineShape(game.shapeRenderer, 20, 60);
//        drawSplineShape(game.shapeRenderer, 40+w, 60);
        drawTriangles(game.shapeRenderer, 20, 60);
//        mesh.render(shader, GL20.GL_TRIANGLES);

        game.shapeRenderer.end();

        super.render(delta);
    }

    public Vector2 v1 = new Vector2();
    public Vector2 v2 = new Vector2();
    public Vector2 v3 = new Vector2();

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
        // TODO: figure out how to translate a mesh
        sourceTex.bind();
        shader.begin();
        shader.setUniformMatrix("u_projTrans", game.batch.getProjectionMatrix());
        shader.setUniformi("u_texture", 0);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();

    }

    public void drawSprite(Sprite sprite, PuzzlePieceCoords coords, float x, float y) {
        float offsetX = x + coords.pos.x - coords0.pos.x + padding*(coords.col-coords0.col);
        float offsetY = y + coords.pos.y - coords0.pos.y + padding*(coords.row-coords0.row);

        sprite.setPosition(offsetX, offsetY);
        spriteBatch.begin();
        sprite.draw(spriteBatch);
        spriteBatch.end();
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
//        modelBatch.dispose();
//        model.dispose();
        super.dispose();
    }
}
