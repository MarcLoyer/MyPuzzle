package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.Iterator;

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
        this(game, null);
    }

    public GameScreen(final MyPuzzle game, Pixmap image) {
        super(game);
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
//        button = new TextButton("debug flood fill", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                game.setScreen(new DebugScreen(game, puzzle.puzzleImg, puzzle.splineImg, puzzle.debugCoords[1]));
////                dispose();
//            }
//        });
//        popup.add(button).expandX().fillX().row();

//        button = new TextButton("debug mesh #2", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                if (puzzle.selectedPiece.size()==1) {
//                    int row = puzzle.selectedPiece.get(0).row;
//                    int col = puzzle.selectedPiece.get(0).col;
//                    game.setScreen(new Debug4Screen(game, puzzle, row, col));
////                    dispose();
//                }
//            }
//        });
//        popup.add(button).expandX().fillX().row();

//        button = new TextButton("debug packer", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                game.setScreen(new Debug3Screen(game, puzzle));
////                dispose();
//            }
//        });
//        popup.add(button).expandX().fillX().row();

//        button = new TextButton("debug mesh", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                int row = 0;
//                int col = 0;
//                if (puzzle.selectedPiece.size()==1) {
//                    row = puzzle.selectedPiece.get(0).row;
//                    col = puzzle.selectedPiece.get(0).col;
//                }
//                game.setScreen(new Debug2Screen(game, puzzle, row, col));
////                dispose();
//            }
//        });
//        popup.add(button).expandX().fillX().row();

//        button = new TextButton("display image", game.skin);
//        button.addListener(new ClickListener(){
//            public void clicked(InputEvent event, float x, float y){
//                puzzle.displayImage = !puzzle.displayImage;
//            }
//        });
//        popup.add(button).expandX().fillX().row();

        button = new TextButton("display splines", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                puzzle.displaySplines = !puzzle.displaySplines;
            }
        });
        popup.add(button).expandX().fillX().row();

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
                puzzle.manager.shuffle();
            }
        });
        popup.add(button).expandX().fillX().row();

        button = new TextButton("unshuffle", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                puzzle.manager.restoreInitialState();
            }
        });
        popup.add(button).expandX().fillX().row();

        button = new TextButton("save", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                String s = puzzle.json.toJson(puzzle.gameData);
                Gdx.app.error("json", "json string length = "+s.length());
                s = puzzle.base64Coder.encodeString(s);
                // write the data to a file...
                FileHandle fh = Gdx.files.local(puzzle.gameData.getBasename() + "/savedGame.json");
                fh.writeString(puzzle.json.prettyPrint(s), false);
                for (FileHandle f: fh.parent().list()) {
                    Gdx.app.error("list", f.name());
                }
            }
        });
        popup.add(button).expandX().fillX().row();

        button = new TextButton("load", game.skin);
        button.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y){
                // read the data from a file...
                FileHandle fh = Gdx.files.local(puzzle.gameData.getBasename() + "/savedGame.json");
                String s = puzzle.base64Coder.decodeString(fh.readString());
                Gdx.app.error("json", "json string length = "+s.length());
                Puzzle.GameData p = puzzle.json.fromJson(Puzzle.GameData.class, s);
                s = puzzle.json.toJson(p);
//                Gdx.app.error("json", puzzle.json.prettyPrint(s));
                Gdx.app.error("json", "number of pieces = " + p.puzzlePieces.size);
                Gdx.app.error("json", "number of groups = " + p.puzzleGroups.size);
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

        puzzle = new Puzzle(this);
        addInputController(new GestureDetector(puzzle));
        getPuzzleImage(image);
    }

    public float worldWidth = 1000;

    public void getPuzzleImage(Pixmap image) {
        // TODO: the big image doesn't display properly on my phone!
        String picture1 = "monumentValley.JPG"; // big: 5000x3000
        String picture2 = "klimt.JPG"; // small: 500x300
        String picture3 = "oregonpath.JPG"; // tiny: 150x100

        if (image != null) {
            puzzle.setPicture(image);
        } else {
            puzzle.setPicture(Gdx.files.internal(picture2));
        }

//        puzzle.createPieces(3, 3);
//        puzzle.createPieces(5, 5);
        puzzle.createPieces(10, 10);

        worldWidth = puzzle.puzzleImg.getWidth();
        updateCameraViewport();
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
    public void dispose() {
        super.dispose();
    }
}
