package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.oduratereptile.game.HudScreen.Corner.*;

/**
 * Created by Marc on 10/3/2017.
 */

public class GameScreen extends HudScreen {
    public OrthographicCamera camera;

    public Texture puzzleImg = null;



    public GameScreen(final MyPuzzle game) {
        super(game);
        camera = new OrthographicCamera();

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

        button = new Button(game.skin, "menu");
        HUDadd(UL, button);

        getPuzzleImage();
    }

    public void getPuzzleImage() {
        puzzleImg = new Texture(Gdx.files.internal("monumentValley.JPG"));
//        Gdx.app.error("image", "image size = " + puzzleImg.getWidth() + " x " + puzzleImg.getHeight());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0, 1); // TODO: I changed the background color just to tell the difference from the MainMenuScreen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//    next steps:
//        (1) add ortho camera and panning/zoom (add a Pixmap for testing)
//        (2) Catmull-Rom splines <-- actually, this is a separate class
//        (3) "load an image" dialog
//        (4) "invite a friend" dialog
//        (5) "view full image" window
//        (6) chat area
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(puzzleImg, 0,0);
        game.batch.end();
        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
//        Gdx.app.error("setup", "w = " + w);
//        Gdx.app.error("setup", "h = " + h);
        camera.setToOrtho(false, 5000, 5000 * h / w);
//        Gdx.app.error("setup", "camera size = " + camera.viewportWidth + " x " + camera.viewportHeight);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
