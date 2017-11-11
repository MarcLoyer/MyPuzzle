package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;

import static com.oduratereptile.game.HudScreen.Corner.LR;
import static com.oduratereptile.game.HudScreen.Corner.UL;

/**
 * Created by Marc on 10/3/2017.
 */

public class Debug3Screen extends HudScreen {
    public OrthographicCamera camera;
    public Puzzle puzzle;

    public int numRows;
    public int numCols;

    public float worldWidth = 200;

    public float padding = 20;

    public PuzzlePacker packer;

    public ObjectMap<String, Sprite> sprite = new ObjectMap<String, Sprite>();
    public ObjectMap<String, Vector2> position = new ObjectMap<String, Vector2>();

    public Debug3Screen(final MyPuzzle game, Puzzle puzzle) {
        super(game);
        camera = new OrthographicCamera();
        addInputController(new GestureDetector(new OrthoGestureListener(camera)));

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


        packer = new PuzzlePacker(puzzle, 1024);

        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                packer.pack(i,j);
            }
        }

        packer.createAtlas();

        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                Sprite s = new Sprite(packer.getRegion(i,j));
                s.flip(false, true);
                sprite.put(i+","+j, s);
                position.put(i+","+j, packer.getData(i,j).position);
            }
        }
    }

    public float rowSpacing(int i) { return puzzle.rowSpacing(i); }

    public float rowOffset(int i) { return puzzle.rowOffset(i); }

    public float colSpacing(int i) { return puzzle.colSpacing(i); }

    public float colOffset(int i) { return puzzle.colOffset(i); }

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

        game.batch.begin();
        for (int i=0; i<numRows; i++) {
            for (int j=0; j<numCols; j++) {
                drawSprite(game.batch, i, j, 0, 0);
            }
        }
        game.batch.end();

//        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        for (int i=0; i<numRows; i++) {
//            for (int j=0; j<numCols; j++) {
//                drawSpriteOutline(game.shapeRenderer, i, j, 0, 0);
//            }
//        }
//        game.shapeRenderer.end();

        super.render(delta);
    }

    public void drawSprite(SpriteBatch batch, int row, int col, float x, float y) {
        String name = row + "," + col;
        float offsetX = x + position.get(name).x + padding*col;
        float offsetY = y + position.get(name).y + padding*row;

        Sprite s = sprite.get(name);
        s.setPosition(offsetX, offsetY);
        s.draw(batch);
    }

    public void drawSpriteOutline(ShapeRenderer sr, int row, int col, float x, float y) {
        String name = row + "," + col;
        float offsetX = x + position.get(name).x + padding*col;
        float offsetY = y + position.get(name).y + padding*row;

        sr.setColor(0f, 0f, 0.6f, 0f);
        Sprite s = sprite.get(name);
        sr.rect(offsetX, offsetY, s.getWidth(), s.getHeight());
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
