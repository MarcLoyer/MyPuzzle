package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * Created by Marc on 10/3/2017.
 */

public class SplashScreen implements Screen {
    public OrthographicCamera camera;
    final public MyPuzzle game;

    float x;
    float y;

    public SplashScreen(final MyPuzzle game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY);
        x = (MyPuzzle.SCREENSIZEX - game.img.getWidth())/2.0f;
        y = (MyPuzzle.SCREENSIZEY - game.img.getHeight())/2.0f;
    }

    @Override
    public void show() {
        game.manager.load("atlas.atlas", TextureAtlas.class);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        if (game.manager.update()) {
            game.atlas = game.manager.get("atlas.atlas", TextureAtlas.class);

            game.setScreen(new MainMenuScreen(game));
        }

        game.batch.begin();
        game.batch.draw(game.img, x, y);
        game.batch.end();
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
