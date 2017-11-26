package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.io.FileDescriptor;
import java.io.FileInputStream;

/**
 * Created by Marc on 10/3/2017.
 */

public class MainMenuScreen extends Stage implements Screen {
    public OrthographicCamera camera;
    final public MyPuzzle game;
    public Table table;


    public boolean waitForImageSelection = false;

    public MainMenuScreen(final MyPuzzle game) {
        super(new FitViewport(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY));
        Gdx.input.setInputProcessor(this);

        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY);

        table = new Table(game.skin);
        table.setFillParent(true);
        addActor(table);

        TextButton textbutton = new TextButton("Play", game.skin);
//        textbutton.setWidth(150f);
//        textbutton.setPosition(MyPuzzle.SCREENSIZEX/2f - 75f, MyPuzzle.SCREENSIZEY/2f - 20f);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new GameScreen(game, image));
                dispose();
            }
        });
        table.add(textbutton).width(150);

        table.row();
        textbutton = new TextButton("Load image", game.skin);
        textbutton.setWidth(150f);
//        textbutton.setPosition(MyPuzzle.SCREENSIZEX/2f - 75f, MyPuzzle.SCREENSIZEY/2f - 100f);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.galleryOpener.openGallery();
                waitForImageSelection = true;
                Gdx.app.error("main", "setting waitForImageSelection");
            }
        });
        table.add(textbutton).width(150f);
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
        float aspectRatio = (float)height / (float)width;
        if (aspectRatio > 1f) { // portrait
            getViewport().setWorldSize(MyPuzzle.SCREENSIZEY, MyPuzzle.SCREENSIZEX);
        } else { // landscape
            getViewport().setWorldSize(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY);
        }
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
            image = loadPixmapFromFileDescriptor(game.galleryOpener.getFileDescriptor());
        }
    }

    public Pixmap image = null;

    public Pixmap loadPixmapFromFileDescriptor(FileDescriptor fd) {
        FileInputStream stream = new FileInputStream(fd);
        Pixmap pixmap=null;
        try {
            long size = stream.getChannel().size();
            if (size >= 0x80000000L) {
                Gdx.app.error("loadPixmap", "File is too big: " + size);
            }
            byte [] bytes = new byte[(int)size];
            stream.read(bytes);
            pixmap = new Pixmap(new Gdx2DPixmap(bytes, 0, (int)size, 0));
        } catch (Exception e) {
            e.printStackTrace();
            Gdx.app.error("loadPixmap", "Failed to read image file");
        }
        return pixmap;
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
