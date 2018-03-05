package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

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
    }

    @Override
    public void show() {
        game.manager.load("skin/uiskin.json", Skin.class);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        if (game.manager.update()) {
            game.skin = game.manager.get("skin/uiskin.json", Skin.class);

            game.setScreen(new MainMenuScreen(game));
        }

        game.batch.begin();
        game.batch.draw(game.img, x, y);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float)height / (float)width;
        if (aspectRatio > 1f) { // portrait
            camera.setToOrtho(false, MyPuzzle.SCREENSIZEY, MyPuzzle.SCREENSIZEX);
        } else { // landscape
            camera.setToOrtho(false, MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY);
        }
        camera.update();
        x = (camera.viewportWidth - game.img.getWidth())/2.0f;
        y = (camera.viewportHeight - game.img.getHeight())/2.0f;
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
