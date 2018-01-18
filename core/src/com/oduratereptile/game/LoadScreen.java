package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.FloatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Created by Marc on 12/31/2017.
 */

public class LoadScreen extends Stage implements Screen {
    public MyPuzzle game;
    public ScrollPane scroller;
    public Table gameTable, rootTable;

    public LoadScreen(final MyPuzzle game) {
        super(new FitViewport(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY));
        Gdx.input.setInputProcessor(this);

        this.game = game;

        rootTable = new Table();
        rootTable.setFillParent(true);
        addActor(rootTable);

        gameTable = new Table();
        scroller = new ScrollPane(gameTable, game.skin);
        loadGameTable();
        rootTable.add(scroller).expand();

        // Create a button to go back to the mainmenu
        rootTable.row();
        Button button = new Button(game.skin, "leftarrow");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        button.align(Align.bottomRight);
        rootTable.add(button).bottom().right().expand();
//        rootTable.setDebug(true);
    }

    public void loadGameTable() {
        FileHandle dir = Gdx.files.local(".");
        for (FileHandle fh: dir.list()) {
            if (fh.isDirectory()) {
                loadGame(fh.name());
            }
        }

        loadGame("game #1");
        loadGame("game #2");
        loadGame("game #3");
        loadGame("game #4");
        loadGame("game #5");
        loadGame("game #6");
    }

    private static final float WIDTH = 200.0f;

    public void loadGame(String basename) {
        final FileHandle baseDir = Gdx.files.local(basename);
        final GameData gameData;

        if (baseDir.child("savedGame.json").exists()) {
            gameData = GameData.restoreGameHeader(basename);
        } else {
//            return;
            // TODO DEBUG: make up some phony data
            gameData = new GameData();
            gameData.puzzleName = basename;
            gameData.rows = 10;
            gameData.cols = 12;
        }

        final TextButton t = new TextButton(gameData.getBasename(), game.skin);
        t.getLabelCell().expand(true, false);

        t.row();
        Image image = (gameData.thumbnail==null) ?
                new Image(new Texture(Gdx.files.internal("badlogic.jpg"))):
                new Image(new Texture(gameData.thumbnail));
        image.setScaling(Scaling.fit);
        image.setAlign(Align.top);
        t.add(image).center().width(WIDTH).height(WIDTH);

        t.row();
        Label label = new Label("size = " + gameData.rows + "x" + gameData.cols + " (" + gameData.rows*gameData.cols + ")", game.skin, "small");
        t.add(label).width(WIDTH).height(40.0f);

        t.row();
        Button b = new Button(game.skin, "trashcan");
        b.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (baseDir.exists()) baseDir.deleteDirectory();

                // remove this panel from the table
                Cell c = gameTable.getCell(t);
                t.remove();
                CollapseCellAction action = new CollapseCellAction(c, WIDTH*1.2f, 0.5f);
                gameTable.addAction(action);

                event.stop();
            }
        });
        b.align(Align.bottomRight);
        t.add(b).width(50).height(50).expand().bottom().right().pad(10);

        float h = t.getPrefHeight();
        gameTable.add(t).top().width(WIDTH*1.2f).height(h*1.2f).space(3);
        t.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (event.isStopped()) return;
                game.setScreen(new GameScreen(game, gameData.getBasename()));
                dispose();
            }
        });
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.2f, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        act(delta);
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

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public class CollapseCellAction extends FloatAction {
        Cell c;

        public CollapseCellAction(Cell c, float x, float duration) {
            super(x, 0);
            setDuration(duration);
            this.c = c;
        }

        @Override
        public boolean act(float delta) {
            boolean rv = super.act(delta);
            c.width(getValue());
            gameTable.invalidate();
            return rv;
        }
    }
}
