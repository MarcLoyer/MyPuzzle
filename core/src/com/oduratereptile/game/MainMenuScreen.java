package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
        super(new FitViewport(game.SCREENSIZEX, game.SCREENSIZEY));
        Gdx.input.setInputProcessor(this);

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.SCREENSIZEX, game.SCREENSIZEY);

//        TextButton btn = new TextButton("Play", skin, "default");
//        btn.setBounds(game.SCREENSIZEX / 2 - 90, game.SCREENSIZEY - 300, 180, 50);
//        btn.addListener(new ClickListener() {
//            public void clicked(InputEvent event, float x, float y) {
//                game.clink.play(game.volumeSounds);
//                game.setScreen(new GameScreen(game));
//                dispose();
//            }
//        });
//        btn.getStyle().up = game.buttonBackground;
//        addActor(btn);
        final TextButton button = new TextButton("Click me", game.skin, "default");
        button.setWidth(200f);
        button.setHeight(20f);
        button.setPosition(game.SCREENSIZEX/2f - 100f, game.SCREENSIZEY/2f - 10f);
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                button.setText("You clicked the button");
            }
        });

        addActor(button);
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
