package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by Marc on 10/3/2017.
 */

public class GameScreen implements Screen {
    public OrthographicCamera camera;
    final public MyPuzzle game;

    public Stage stage;
    public Table table_root;
    public Table table_ul;
    public Table table_ur;
    public Table table_ll;
    public Table table_lr;

    public GameScreen(final MyPuzzle game) {
        this.game = game;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY);

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

        ImageButton ibutton = new ImageButton(game.skin, "blue");
        ibutton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        table_lr.add(ibutton).width(40f).height(40f);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0, 1); // TODO: I changed the background color just to tell the difference from the MainMenuScreen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        stage.act(delta);
        stage.draw();

        game.batch.begin();
        game.batch.end();

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
