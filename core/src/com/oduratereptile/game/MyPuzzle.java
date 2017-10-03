package com.oduratereptile.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class MyPuzzle extends Game {
	SpriteBatch batch;
	Texture img;
    TextureAtlas atlas;
    AssetManager manager;

    final static public int SCREENSIZEX = 800;
    final static public int SCREENSIZEY = 480;

	@Override
	public void create () {
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
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
        manager.dispose();
	}
}
