package com.oduratereptile.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
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

	OutlineShader outlineShader;
    GalleryOpener galleryOpener;

    final static public int SCREENSIZEX = 800;
    final static public int SCREENSIZEY = 480;

	public MyPuzzle(GalleryOpener galleryOpener) {
		super();
        this.galleryOpener = galleryOpener;
	}

	@Override
	public void create () {
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		img = new Texture("badlogic.jpg");
        manager = new AssetManager();

		outlineShader = new OutlineShader();


		setScreen(new SplashScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
		img.dispose();
        manager.dispose();
	}
}
