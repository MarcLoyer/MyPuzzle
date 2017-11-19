package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Created by Marc on 10/3/2017.
 */

public class MainMenuScreen extends Stage implements Screen {
    public OrthographicCamera camera;
    final public MyPuzzle game;

    public boolean waitForImageSelection = false;

    public MainMenuScreen(final MyPuzzle game) {
        super(new FitViewport(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY));
        Gdx.input.setInputProcessor(this);

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY);

        TextButton textbutton = new TextButton("Play", game.skin);
        textbutton.setWidth(150f);
        textbutton.setPosition(MyPuzzle.SCREENSIZEX/2f - 75f, MyPuzzle.SCREENSIZEY/2f - 20f);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        addActor(textbutton);

        textbutton = new TextButton("Load image", game.skin);
        textbutton.setWidth(150f);
        textbutton.setPosition(MyPuzzle.SCREENSIZEX/2f - 75f, MyPuzzle.SCREENSIZEY/2f - 100f);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.galleryOpener.openGallery();
                waitForImageSelection = true;
                Gdx.app.error("main", "setting waitForImageSelection");
            }
        });
        addActor(textbutton);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.2f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        draw();
        game.batch.end();

    }

    @Override
    public void resize(int width, int height) {
        getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
        // if the "Load Image" button was pressed, resume() shows that we have focus again
        if (waitForImageSelection) {
            waitForImageSelection = false;
            Gdx.app.error("ImagePicker", "Path = " + game.galleryOpener.getSelectedFilePath());
        }
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
