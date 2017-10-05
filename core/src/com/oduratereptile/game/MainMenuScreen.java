package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
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

    public MainMenuScreen(final MyPuzzle game) {
        super(new FitViewport(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY));
        Gdx.input.setInputProcessor(this);

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY);

        final TextButton button = new TextButton("Click me", game.skin, "blue");
        button.setWidth(300f);
        button.setHeight(40f);
        button.setPosition(MyPuzzle.SCREENSIZEX/2f - 150f, MyPuzzle.SCREENSIZEY/2f - 20f);
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                button.setText("You clicked the button");
            }
        });
        addActor(button);

        ImageButton ibutton = new ImageButton(game.skin, "blue");
        ibutton.setWidth(60f);
        ibutton.setHeight(60f);
        ibutton.setPosition(MyPuzzle.SCREENSIZEX/2f - 80f, MyPuzzle.SCREENSIZEY/2f - 160f);
        ibutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
        addActor(ibutton);
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

//        if (Gdx.input.isTouched()) {
//            game.setScreen(new GameScreen(game));
//            dispose();
//        }
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

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
