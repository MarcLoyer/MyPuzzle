package com.oduratereptile.game;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.io.FileDescriptor;
import java.io.FileInputStream;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;

import de.tomgrill.gdxfacebook.core.GDXFacebook;
import de.tomgrill.gdxfacebook.core.GDXFacebookConfig;
import de.tomgrill.gdxfacebook.core.GDXFacebookSystem;

/**
 * Created by Marc on 10/3/2017.
 */

public class MainMenuScreen extends Stage implements Screen {
    final public MyPuzzle game;
    public WarpController warpController;
    public GDXFacebook facebook;
    public Table table;
    public float buttonWidth = 200f;

    private boolean waitingForImage=false;

    public MainMenuScreen(final MyPuzzle game) {
        super(new FitViewport(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY));
        Gdx.input.setInputProcessor(this);

        this.game = game;

        warpController = new WarpController();

        // Install gdx-facebook...
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        GDXFacebookConfig config = new GDXFacebookConfig();
        config.APP_ID = "532991523721021";
        config.PREF_FILENAME = ".facebookSessionData";
        config.GRAPH_API_VERSION = "v2.6";
        facebook = GDXFacebookSystem.install(config);

        table = new Table(game.skin);
        table.setFillParent(true);
        addActor(table);

        TextButton textbutton = new TextButton("Play", game.skin);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new GameScreen(game, image, "undetermined", 10, 10));
                dispose();
            }
        });
        table.add(textbutton).width(buttonWidth);

        table.row();
        textbutton = new TextButton("New Game", game.skin);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new NewGameScreen(game));
                dispose();
            }
        });
        table.add(textbutton).width(buttonWidth);

        table.row();
        textbutton = new TextButton("Load image", game.skin);
        textbutton.setWidth(150f);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.galleryOpener.openGallery();
                waitingForImage = true;
            }
        });
        table.add(textbutton).width(buttonWidth);
        game.galleryOpener.addListener(new GalleryOpener.GalleryListener() {
            @Override
            public void gallerySelection(FileDescriptor fd) {
                if (!waitingForImage) return;
                image = loadPixmapFromFileDescriptor(fd);
                waitingForImage = false;
            }
        });

        table.row();
        textbutton = new TextButton("Connect to server", game.skin);
        textbutton.setWidth(150f);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                connectToServer("HiHo");
            }
        });
        table.add(textbutton).width(buttonWidth);

        table.row();
        textbutton = new FacebookLoginButton(facebook, game.skin);
        textbutton.setWidth(150f);
        table.add(textbutton).width(buttonWidth);

        table.row();
        textbutton = new TextButton("Load screen", game.skin);
        textbutton.setWidth(150f);
        textbutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new LoadScreen(game));
                dispose();
            }
        });
        table.add(textbutton).width(buttonWidth);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.2f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        draw();
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

    }

    public Pixmap image = null;

    public Pixmap loadPixmapFromFileDescriptor(FileDescriptor fd) {
        if (fd==null) return null;
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

    public boolean isConnected = false;

    public void connectToServer(String userName) {
        if (isConnected) {
            warpController.warpClient.disconnect();
        } else {
            warpController.warpClient.connectWithUserName(userName);
        }
        isConnected = !isConnected;
    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        super.dispose();
        if (image != null) image.dispose();
    }
}
