package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ShortArray;

import java.util.ArrayList;
import java.util.Iterator;

import static com.oduratereptile.game.HudScreen.Corner.LR;

/**
 * This screen was used to debug the puzzle shape texture generation when used with gallery images. (Turns
 * out the pack strategy I was using - Skyline - has a bug when packing multiple pages: it places some of
 * the pixmaps at the bootm of the page, causing the image to be truncated. I fixed it by using the other
 * pack strategy provided with the PixmapPacker.)
 * Created by Marc on 10/3/2017.
 */

public class Debug4Screen extends HudScreen {
    public OrthographicCamera camera;
    public Puzzle puzzle;

    public SpriteBatch spriteBatch;
    public ShaderProgram shader;

    public int numRows;
    public int numCols;
    public PuzzlePacker.MeshPiece meshPiece;
    public Texture meshPieceTexture;
    public Texture meshPiecePixmap;
    public PuzzlePiece puzzlePiece;
    public TextureRegion puzzleRegion;
    public TextureAtlas atlas;
    public ObjectSet<Texture> atlasTextures;

    public float worldWidth = 1000;

    public Debug4Screen(final MyPuzzle game, Puzzle puzzle) {
        this(game, puzzle, 0, 0);
    }

    public Debug4Screen(final MyPuzzle game, Puzzle puzzle, int row, int col) {
        super(game);
        camera = new OrthographicCamera();
        addInputController(new GestureDetector(new OrthoGestureListener(camera)));
        spriteBatch = new SpriteBatch();

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

        this.puzzle = puzzle;
        numRows = puzzle.numRows;
        numCols = puzzle.numCols;
        meshPiece = puzzle.meshPieces[row][col];
        meshPieceTexture = meshPiece.getTexture();
        meshPiecePixmap = new Texture(meshPiece.getPixmap());
        puzzleRegion = puzzle.puzzlePacker.findRegion(row,col);
        puzzlePiece = new PuzzlePiece(row, col,
                puzzle.puzzlePacker.getData(row,col),
                puzzle.puzzlePacker.findRegion(row,col), true);

        w = (1 + meshPiece.getWidth()/20)*20;
        h = (1 + meshPiece.getHeight()/20)*20;
        width = meshPiece.getWidth();
        height = meshPiece.getHeight();

        atlas = puzzle.puzzlePacker.getAtlas();
        atlasTextures = atlas.getTextures();
        Gdx.app.error("debug", "Piece size = ("+width+", "+height+")");
        Gdx.app.error("debug", "Number of atlas texture pages = " + atlasTextures.size);
        Gdx.app.error("debug", "  page size = " + puzzle.puzzlePacker.pageSize);
        Iterator<Texture> iter = atlasTextures.iterator();
        while (iter.hasNext()) {
            Texture t = iter.next();
            Gdx.app.error("debug", "  texture size = ("+t.getWidth()+", "+t.getHeight()+")");
        }
    }

    float w,h;
    float spacing = 20f;
    float width, height;

    public Vector2 pos(int i, int j) {
        return new Vector2(i*(w+spacing)+spacing, j*(h+spacing)+spacing);
    }

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
        drawMeshTexture(game.batch, pos(0,1));
        drawMeshPixmap(game.batch, pos(1,1));
        drawRegion(game.batch, pos(2,1));
        drawSprite(game.batch, pos(3,1));

        drawAtlasTextures(game.batch, pos(4,0));
        game.batch.end();

        // draw the mesh
        drawMesh(pos(2,0));

        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        drawSplineShape(game.shapeRenderer, pos(0,0));
        drawTriangles(game.shapeRenderer, pos(1,0));

        game.shapeRenderer.setColor(0f, 0f, 1f, 1f);
        v1.set(pos(0,0)); game.shapeRenderer.rect(v1.x, v1.y, width, height);
        v1.set(pos(0,1)); game.shapeRenderer.rect(v1.x, v1.y, width, height);
        v1.set(pos(1,0)); game.shapeRenderer.rect(v1.x, v1.y, width, height);
        v1.set(pos(1,1)); game.shapeRenderer.rect(v1.x, v1.y, width, height);
        v1.set(pos(2,0)); game.shapeRenderer.rect(v1.x, v1.y, width, height);
        v1.set(pos(2,1)); game.shapeRenderer.rect(v1.x, v1.y, width, height);
        v1.set(pos(3,1)); game.shapeRenderer.rect(v1.x, v1.y, width, height);

        v1.set(pos(4,0));
        for (int i=0; i<atlasTextures.size; i++) {
            game.shapeRenderer.rect(v1.x, v1.y, puzzle.puzzlePacker.pageSize, puzzle.puzzlePacker.pageSize);
            v1.x += puzzle.puzzlePacker.pageSize + spacing;
        }

        game.shapeRenderer.end();

        super.render(delta);
    }

    public Vector2 v1 = new Vector2();
    public Vector2 v2 = new Vector2();
    public Vector2 v3 = new Vector2();

    public void drawBoundingBox(ShapeRenderer sr, Vector2 p) {
        float x = p.x;
        float y = p.y;
        sr.setColor(1f, 0f, 0f, 1f);
        v1.set(x+meshPiece.bounds.min.x, y+meshPiece.bounds.min.y);
        v2.set(x+meshPiece.bounds.max.x, y+meshPiece.bounds.max.y);
        v2.sub(v1);
        sr.rect(v1.x, v1.y, v2.x, v2.y);
    }

    public void drawSplineShape(ShapeRenderer sr, Vector2 p) {
        float x = p.x;
        float y = p.y;
        ArrayList<Vector2> splineShape = meshPiece.splineShape;
        sr.setColor(1f, 1f, 1f, 1f);
        float minX = meshPiece.minX;
        float minY = meshPiece.minY;

        v1.set(splineShape.get(splineShape.size()-1)).sub(minX,minY).add(x,y);

        for (int i=0; i<splineShape.size(); i++) {
            v2.set(splineShape.get(i)).sub(minX,minY).add(x,y);
            sr.line(v1, v2);
            v1.set(v2);
        }
    }

    public void drawTriangles(ShapeRenderer sr, Vector2 p) {
        float x = p.x;
        float y = p.y;
        sr.setColor(1f, 1f, 1f, 1f);

        ShortArray tris = meshPiece.tris;
        FloatArray verts = meshPiece.verts;
        float minX = meshPiece.minX;
        float minY = meshPiece.minY;

        for (int i=0; i<tris.size; i+=3) {
            v1.set(verts.get(2*tris.get(i)), verts.get(2*tris.get(i)+1)).sub(minX,minY).add(x,y);
            v2.set(verts.get(2*tris.get(i+1)), verts.get(2*tris.get(i+1)+1)).sub(minX,minY).add(x,y);
            v3.set(verts.get(2*tris.get(i+2)), verts.get(2*tris.get(i+2)+1)).sub(minX,minY).add(x,y);

            sr.triangle(v1.x, v1.y, v2.x, v2.y, v3.x, v3.y);
        }
    }

    public void drawMesh(Vector2 p) {
        float x = p.x;
        float y = p.y;
        Matrix4 mat = new Matrix4(
                new Vector3(x,y,0),
                new Quaternion(),
                new Vector3(1,1,1));

        meshPiece.mesh.transform(mat);

        puzzle.puzzleImgTex.bind();
        shader.begin();
        shader.setUniformMatrix("u_projTrans", game.batch.getProjectionMatrix());
        shader.setUniformi("u_texture", 0);
        meshPiece.mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();

        meshPiece.mesh.transform(mat.inv());
    }

    public void drawMeshTexture(SpriteBatch batch, Vector2 p) {
        float x = p.x;
        float y = p.y;
        batch.draw(meshPieceTexture, x, y);
    }

    public void drawMeshPixmap(SpriteBatch batch, Vector2 p) {
        float x = p.x;
        float y = p.y;
        batch.draw(meshPiecePixmap, x, y);
    }

    public void drawSprite(SpriteBatch batch, Vector2 p) {
        float x = p.x;
        float y = p.y;
        puzzlePiece.moveTo(x, y);
        puzzlePiece.draw(batch, 1.0f);
    }

    public void drawRegion(SpriteBatch batch, Vector2 p) {
        float x = p.x;
        float y = p.y;
        batch.draw(puzzleRegion, x, y);
    }

    public void drawAtlasTextures(SpriteBatch batch, Vector2 p) {
        float x = p.x;
        float y = p.y;
        Iterator<Texture> iter = atlasTextures.iterator();
        while (iter.hasNext()) {
            Texture t = iter.next();
            batch.draw(t, x, y);
            x += spacing + t.getWidth();
        }
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
