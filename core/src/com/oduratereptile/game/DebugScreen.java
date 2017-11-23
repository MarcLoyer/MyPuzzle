package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.Iterator;

import static com.oduratereptile.game.HudScreen.Corner.*;

/**
 * This screen was used to debug the flood fill algorithm. It also shows how to run and pause a separate thread (which
 * I used to kind of step through the algorithm).
 * Created by Marc on 10/3/2017.
 */

public class DebugScreen extends HudScreen {
    public OrthographicCamera camera;

    public Pixmap image;
    public Pixmap mask;
    public PuzzlePieceCoords coords;

    public Pixmap debug;
    public DebugFill debugFill;
    public boolean displayMask = false;

    public Texture imageTex;
    public Texture maskTex;
    public Texture debugTex;

    public DebugScreen(final MyPuzzle game, Pixmap image, Pixmap mask, PuzzlePieceCoords coords) {
        super(game);
        camera = new OrthographicCamera();
        addInputController(new GestureDetector(new OrthoGestureListener(camera)));

        debugHUD(false);

        Button button = new Button(game.skin, "leftarrow");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        getTable(LR).add(button);

        // Create a button to step the flood fill
        button = new Button(game.skin, "play");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                step();
            }
        });
        getTable(LL).add(button);

        button = new TextButton("mask", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                displayMask = !displayMask;
            }
        });
        getTable(UL).add(button);

        this.image = image;
        this.mask = mask;
        this.coords = coords;

        debugFill = new DebugFill();

        setupData();
    }

    public float worldWidth = 1000;

    public void setupData() {
        // Cut out the portion of the mask and image that we want
        Pixmap tmp = new Pixmap((int)coords.size.x, (int)coords.size.y, Pixmap.Format.RGBA8888);
        tmp.drawPixmap(mask, 0, 0,
                (int)coords.pos.x, mask.getHeight() - (int)coords.pos.y - (int)coords.size.y,
                (int)coords.size.x, (int)coords.size.y);
        tmp.setBlending(Pixmap.Blending.None);
        mask = tmp;

        tmp = new Pixmap((int)coords.size.x, (int)coords.size.y, Pixmap.Format.RGBA8888);
        tmp.drawPixmap(image, 0, 0,
                (int)coords.pos.x, image.getHeight() - (int)coords.pos.y - (int)coords.size.y,
                (int)coords.size.x, (int)coords.size.y);
        tmp.setBlending(Pixmap.Blending.None);
        image = tmp;

        debug = new Pixmap((int)coords.size.x, (int)coords.size.y, Pixmap.Format.RGBA8888);

        // clear alpha across the whole image, then floodfill starting in the middle
        for (int x=0; x<tmp.getWidth(); x++) {
            for (int y=0; y<tmp.getHeight(); y++) {
                image.drawPixel(x, y, tmp.getPixel(x, y) & 0xFFFFFF00);
            }
        }

        imageTex = new Texture(image);
        maskTex = new Texture(mask);
        debugTex = new Texture(debug);

        worldWidth = 2.0f * image.getWidth() + 60f;
        updateCameraViewport();

        boolean includeBorder = false;

        // put this on a separate thread so that I can pause it independently of
        // the render/UI thread.
        debugFill.initialize(image, mask, coords, includeBorder);
        fillThread = new Thread(debugFill);
        fillThread.start();
    }

    Thread fillThread;

    public void step() {
        updateDebug();
        debugFill.step();
        setStatus(debugFill.status);
    }

    public void updateDebug() {
        debug.setColor(0xFF0000FF);
        for (int i=0; i<debugFill.floodStack.size(); i++) {
            DebugFill.ScanlineSegment seg = debugFill.floodStack.get(i);
            debug.drawLine(seg.x1, seg.y, seg.x2, seg.y);
        }
        debug.setColor(0x000000FF);
        Iterator iter = debugFill.doneSegments.values().iterator();
        ArrayList<DebugFill.ScanlineSegment> list;
        while (iter.hasNext()) {
            list = (ArrayList<DebugFill.ScanlineSegment>)iter.next();
            for (DebugFill.ScanlineSegment seg: list) {
                debug.drawLine(seg.x1, seg.y, seg.x2, seg.y);
            }
            iter.remove();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update the textures
        imageTex.draw(image,0,0);
        maskTex.draw(mask,0,0);
        debugTex.draw(debug,0,0);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        game.batch.draw(imageTex, 20, 60);
        if (displayMask) game.batch.draw(maskTex, 20, 60);
        game.batch.draw(debugTex, 40+image.getWidth(), 60);
        if (displayMask) game.batch.draw(maskTex, 40+image.getWidth(), 60);
            game.batch.end();
        super.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        updateCameraViewport();
    }

    public void updateCameraViewport() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        camera.setToOrtho(false, worldWidth, worldWidth * h / w);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
