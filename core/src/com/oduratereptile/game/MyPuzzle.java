package com.oduratereptile.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class MyPuzzle extends Game {
	SpriteBatch batch;
	Texture img;
    TextureAtlas atlas;
	Skin skin;
    AssetManager manager;

    final static public int SCREENSIZEX = 800;
    final static public int SCREENSIZEY = 480;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
		skin = new Skin(Gdx.files.internal("data/uiskin.json")); // TODO: move this to the asset manager
        manager = new AssetManager();

		setScreen(new SplashScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		skin.dispose(); // TODO: move to assetmanager
        manager.dispose();
	}
}
