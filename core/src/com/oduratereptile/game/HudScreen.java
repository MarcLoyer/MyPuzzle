package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Created by Marc on 10/3/2017.
 */

public class HudScreen implements Screen {
    final public MyPuzzle game;

    public Stage stage;
    public Table table_root;
    public Table table_ul;
    public Table table_ur;
    public Table table_ll;
    public Table table_lr;
    public Label status;

    public HudScreen(final MyPuzzle game) {
        this.game = game;

        build_hud();
    }

    public void build_hud() {
        stage = new Stage(new FitViewport(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY));
        Gdx.input.setInputProcessor(stage); // TODO: change this to input multiplexer
//        stage.setDebugAll(true);

        table_root = new Table();
        table_root.setFillParent(true);
        stage.addActor(table_root);

        table_ul = new Table();
        table_ul.left().top();
        table_root.add(table_ul).left().top().expand();

        table_ur = new Table();
        table_ur.right().top();
        table_root.add(table_ur).right().top().expand();

        table_root.row();

        table_ll = new Table();
        table_ll.left().bottom();
        table_root.add(table_ll).left().bottom().expand();

        table_lr = new Table();
        table_lr.right().bottom();
        table_root.add(table_lr).right().bottom().expand();

        table_root.row();

        status = new Label("... test ...", game.skin);
        status.setAlignment(Align.center);
        table_root.add(status).colspan(2).expandX().fillX();

        table_root.row();
    }

    public enum Corner {UL, UR, LL, LR, ROOT};

    public <T extends Actor> Cell<T> HUDadd(Corner corner, T actor) {
        switch(corner) {
            case UL : return table_ul.add(actor);
            case UR : return table_ur.add(actor);
            case LL : return table_ll.add(actor);
            case LR : return table_lr.add(actor);
            case ROOT: return table_root.add(actor);
        }
        return null;
    }
    public Cell HUDrow(Corner corner) {
        switch(corner) {
            case UL : return table_ul.row();
            case UR : return table_ur.row();
            case LL : return table_ll.row();
            case LR : return table_lr.row();
            case ROOT: return table_root.row();
        }
        return null;
    }

    public Table getTable(Corner corner) {
        switch(corner) {
            case UL : return table_ul;
            case UR : return table_ur;
            case LL : return table_ll;
            case LR : return table_lr;
            case ROOT: return table_root;
        }
        return null;
    }

    public void setStatus(String s) {
        status.setText(s);
    }

    public String getStatus() {
        return status.getText().toString();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0, 1); // TODO: I changed the background color just to tell the difference from the MainMenuScreen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
    }
}
