package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Base64Coder;

import static com.oduratereptile.game.HudScreen.Corner.*;

/**
 * Created by Marc on 10/3/2017.
 */

public class GameScreen extends HudScreen {
    public OrthographicCamera camera;

    public Puzzle puzzle;

    public Button menu;
    public Table popup;

    public GameScreen(final MyPuzzle game) {
        this(game, null, "undetermined", 10, 10);
    }

    public GameScreen(final MyPuzzle game, Pixmap image, String name, int rows, int cols) {
        super(game);
        setupGameScreen();

        GameData gameData = makePuzzle(image, name, rows, cols);
        puzzle = new Puzzle(this, gameData);
        addInputController(new GestureDetector(puzzle));
    }

    public GameScreen(final MyPuzzle game, String basename) {
        super(game);
        setupGameScreen();

        GameData gameData = GameData.restoreGameData(basename);
        puzzle = new Puzzle(this, gameData);
        addInputController(new GestureDetector(puzzle));

        // set the camera to something reasonable.
        Rectangle rect = puzzle.getBounds();
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float aspectRatio = h/w;
        if ((rect.getWidth()*aspectRatio)>rect.getHeight()) {
            worldWidth = rect.getWidth();
        } else {
            worldWidth = rect.getHeight()/aspectRatio;
        }
        camera.position.set(rect.getX()+rect.getWidth()/2f, rect.getY()+rect.getHeight()/2f, 0);
        updateCameraViewport();
    }

    public void setupGameScreen() {
        camera = new OrthographicCamera();

        debugHUD(false);

        // Create a button to go back to the mainmenu
        Button button = new Button(game.skin, "leftarrow");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        getTable(LR).add(button);

        // Create a popup menu
        popup = new Table();
        popup.top();
        popup.setWidth(200);
        popup.setHeight(200);

        // option menu

//        button = new TextButton("display image", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                puzzle.displayImage = !puzzle.displayImage;
//            }
//        });
//        popup.add(button).expandX().fillX().row();

//        button = new TextButton("display tap squares", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                puzzle.displayTapSquares = !puzzle.displayTapSquares;
//            }
//        });
//        popup.add(button).expandX().fillX().row();

        button = new TextButton("toggle pieces", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                if (puzzle.displayAllPieces) {
                    puzzle.displayAllPieces = false;
                    puzzle.displayEvenPieces = true;
                } else {
                    if (puzzle.displayEvenPieces) {
                        puzzle.displayEvenPieces = false;
                    } else {
                        puzzle.displayAllPieces = true;
                    }
                }
            }
        });
        popup.add(button).expandX().fillX().row();

//        button = new TextButton("fit report", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                for (PuzzlePiece p: puzzle.puzzlePiece.values()) {
//                    if (p.isSelected()) {
//                        p.fitReport();
//                    }
//                }
//            }
//        });
//        popup.add(button).expandX().fillX().row();

        button = new TextButton("shuffle", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                puzzle.animations.get("shuffle").start();
            }
        });
        popup.add(button).expandX().fillX().row();

        button = new TextButton("unshuffle", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                Shuffle sh = (Shuffle)puzzle.animations.get("shuffle");
                sh.restoreInitialState();
                puzzle.selectedPiece.clear();
                Fireworks fw = (Fireworks)puzzle.animations.get("fireworks");
                fw.start();
            }
        });
        popup.add(button).expandX().fillX().row();

        button = new TextButton("save", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                puzzle.gameData.saveGameData();
                for (FileHandle fh: Gdx.files.local(puzzle.gameData.getBasename()).list()) {
                    Gdx.app.error("save", fh.name());
                }
            }
        });
        popup.add(button).expandX().fillX().row();

        button = new TextButton("load", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                GameData gameData = GameData.restoreGameData(puzzle.gameData.getBasename());
                Gdx.app.error("load", "number of pieces = " + gameData.puzzlePieces.size);
                Gdx.app.error("load", "number of groups = " + gameData.puzzleGroups.size);
            }
        });
        popup.add(button).expandX().fillX().row();

        popup.setVisible(false);

        // Create a button to bring up the popup menu
        menu = new Button(game.skin, "menu");
        HUDadd(UL, menu);
        menu.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                Vector2 loc = menu.localToStageCoordinates(new Vector2(menu.getWidth()/2f, menu.getHeight()/2f));
                loc.sub(0, popup.getHeight());
                popup.setPosition(loc.x, loc.y);
                popup.setVisible(!popup.isVisible());
            }
        });
        stage.addActor(popup);

    }

    public float worldWidth = 1000;

    public GameData makePuzzle(Pixmap image, String name, int rows, int cols) {
        if (image == null) {
//            name = "monumentValley"; // big: 5000x3000
            name = "klimt"; // small: 500x300
//            name = "oregonpath"; // tiny: 150x100

//            rows = cols = 3;
//            rows = cols = 5;
            rows = cols = 10;

            image = new Pixmap(Gdx.files.internal(name + ".JPG")); // small: 500x300
        }

        PuzzleMaker puzzleMaker = new PuzzleMaker(this);
        puzzleMaker.setPicture(image, name);

        // TODO: can I run this step in different thread, so that I can show the
        // puzzle pieces as they are created? Also, maybe show a progress bar?
        // Everything after this could be placed in Gdx.app.postRunnable
        puzzleMaker.createPieces(rows, cols);

        puzzleMaker.gameData.puzzleName = name;

        worldWidth = image.getWidth();
        updateCameraViewport();

        puzzleMaker.gameData.saveGameData();

        return puzzleMaker.gameData;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//    TODO: next steps:
//        (3) "load an image" dialog
//        (4) "invite a friend" dialog
//        (5) "view full image" window
//        (6) chat area
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        puzzle.render(game.batch, delta);
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
        float aspectRatio = h/w;
        camera.setToOrtho(false, worldWidth, worldWidth * aspectRatio);
    }

    @Override
    public void pause() {
        super.pause();
        // save the game, and put the basename in prefs
        puzzle.gameData.saveGameData();
        game.prefs.putString("gameInProgress", puzzle.gameData.getBasename());
        game.prefs.flush();
    }

    @Override
    public void resume() {
        super.resume();
        // the constructor restores the game for us. We just need to clean up prefs.
        game.prefs.putString("gameInProgress", "");
        game.prefs.flush();
    }

    @Override
    public void dispose() {
        puzzle.dispose();
        super.dispose();
    }
}
