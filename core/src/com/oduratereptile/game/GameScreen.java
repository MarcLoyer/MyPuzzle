package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.oduratereptile.game.HudScreen.Corner.*;

/**
 * Created by Marc on 10/3/2017.
 */

public class GameScreen extends HudScreen {
    public OrthographicCamera camera;

    public Puzzle puzzle;

    public Button menu;
    public Table popup;

    public GameScreen(final MyPuzzle game) {
        super(game);
        camera = new OrthographicCamera();
        addInputController(new GestureDetector(new OrthoGestureListener(this)));

        debugHUD(false);

        // Create a button to go back to the mainmenu
        Button button = new Button(game.skin, "leftarrow");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        getTable(LR).add(button);

        // Create a popup menu
        popup = new Table();
        popup.top();
        popup.setWidth(200);
        popup.setHeight(200);

        // TODO: add option menu implementations
        button = new TextButton("Load new image", game.skin);
        popup.add(button).expandX().fillX().row();

        button = new TextButton("thing 1", game.skin);
        popup.add(button).expandX().fillX().row();

        button = new TextButton("thing 2", game.skin);
        popup.add(button).expandX().fillX().row();

        popup.setVisible(false);

        // Create a button to bring up the popup menu
        menu = new Button(game.skin, "menu");
        HUDadd(UL, menu);
        menu.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                Vector2 loc = menu.localToStageCoordinates(new Vector2(menu.getWidth()/2f, menu.getHeight()/2f));
                loc.sub(0, popup.getHeight());
                popup.setPosition(loc.x, loc.y);
                popup.setVisible(!popup.isVisible());
            }
        });
        stage.addActor(popup);

        getPuzzleImage();
    }

    public float worldWidth = 1000;

    public void getPuzzleImage() {
        // TODO: the big image doesn't display properly on my phone!
        String picture1 = "monumentValley.JPG"; // big: 5000x3000
        String picture2 = "klimt.JPG"; // small: 500x300

        puzzle = new Puzzle(this);
        puzzle.setPicture(Gdx.files.internal(picture2));
        puzzle.createPieces(10, 10);

        worldWidth = puzzle.puzzleImg.getWidth();
        updateCameraViewport();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//    next steps:
//        (2) Catmull-Rom splines <-- actually, this is a separate class
//        (3) "load an image" dialog
//        (4) "invite a friend" dialog
//        (5) "view full image" window
//        (6) chat area
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        puzzle.render(game.batch, delta);
        game.batch.end();
        super.render(delta);
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
