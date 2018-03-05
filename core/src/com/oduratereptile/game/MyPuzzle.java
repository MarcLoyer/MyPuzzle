package com.oduratereptile.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class MyPuzzle extends Game {
	SpriteBatch batch;
	ShapeRenderer shapeRenderer;
	Texture img;
    TextureAtlas atlas;
	Skin skin;
    AssetManager manager;
    Preferences prefs;

    GalleryOpener galleryOpener;

    final static public int SCREENSIZEX = 800;
    final static public int SCREENSIZEY = 480;

    final static private String PREFERENCES = "com.obduratereptile.game.mypuzzle.settings";

	public MyPuzzle(GalleryOpener galleryOpener) {
		super();
        this.galleryOpener = galleryOpener;
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		OutlineShader.setBatch(batch);
		shapeRenderer = new ShapeRenderer();
		img = new Texture("mypuzzle.png");
        manager = new AssetManager();
        prefs = Gdx.app.getPreferences(PREFERENCES);

		setScreen(new SplashScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}

	@Override
	public void resume() {
		super.resume();
		String basename = prefs.getString("gameInProgress", "");
		if (basename.equals("")) return;
		setScreen(new GameScreen(this, basename));
	}

	@Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
		img.dispose();
        manager.dispose();
	}
}
