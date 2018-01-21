package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Marc on 1/18/2018.
 */

public class NewGameScreen extends Stage implements Screen {
    public MyPuzzle game;
    public Table rootTable;

    public Pixmap puzzleImage = null;
    public int puzzleRows;
    public int puzzleCols;

    public Array<GridPoint3> sliderValues;

    public Pixmap thumbnail = null;

    public Image puzzleThumbnail;
    public Label puzzleSizeLabel;
    public Slider puzzleSlider;
    public TextField puzzleNameTextField;
    public TextButton playButton;

    private boolean waitingForImage = false;

    private static final float WIDTH = 200.0f;

    public NewGameScreen(final MyPuzzle game) {
        super(new FitViewport(MyPuzzle.SCREENSIZEX, MyPuzzle.SCREENSIZEY));
        Gdx.input.setInputProcessor(this);

        this.game = game;

        sliderValues = new Array<GridPoint3>();

        rootTable = new Table();
        rootTable.setFillParent(true);
        addActor(rootTable);
        rootTable.addCaptureListener(new EventListener() {
            @Override
            public boolean handle(Event e) {
                if (!(e.getTarget() instanceof TextField)) {
                    setKeyboardFocus(null);
                    Gdx.input.setOnscreenKeyboardVisible(false);
                }
                return false;
            }
        });
//        rootTable.setDebug(true);

        Button button = new TextButton("Pick an image", game.skin);
        button.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               game.galleryOpener.openGallery();
               waitingForImage = true;
           }
        });
        game.galleryOpener.addListener(new GalleryOpener.GalleryListener() {
            @Override
            public void gallerySelection(final FileDescriptor fd) {
                if (!waitingForImage) return;

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        puzzleImage = loadPixmapFromFileDescriptor(fd);
                        if (puzzleImage != null) {
                            generateSliderValues();
                            updateSlider();
                            createThumbnail();
                            puzzleThumbnail.setDrawable(new TextureRegionDrawable(new TextureRegion(new Texture(thumbnail))));
                        }
                        waitingForImage = false;
                    }
                });
            }
        });
        rootTable.add(button).width(WIDTH);

        rootTable.row();
        puzzleThumbnail = new Image(game.skin, "cancel");
        puzzleThumbnail.setScaling(Scaling.fit);
        puzzleThumbnail.setAlign(Align.top);
        rootTable.add(puzzleThumbnail).center().width(WIDTH).height(WIDTH);

        // a text input field for name.
        rootTable.row();
        Table table = new Table();
        Label label = new Label("Puzzle Name:", game.skin, "small");
        label.setAlignment(Align.right);
        table.add(label).pad(5);
        puzzleNameTextField = new TextField("", game.skin);
        puzzleNameTextField.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                checkSetupComplete();
            }
        });
        table.add(puzzleNameTextField).pad(5);
        table.setBackground(game.skin.getDrawable("outline"));
        table.pad(5);
        rootTable.add(table);

        // a slider for rows, cols
        rootTable.row();
        table = new Table();
        puzzleSizeLabel = new Label("Puzzle Size: 0x0", game.skin, "small");
        puzzleSizeLabel.setAlignment(Align.left);
        table.add(puzzleSizeLabel).colspan(3).pad(5);
        table.row();
        button = new Button(game.skin, "reverse");
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float v = puzzleSlider.getValue()-1;
                if (v<0) return;
                puzzleSlider.setValue(v);
                updateSliderLabel();
            }
        });
        button.align(Align.right);
        table.add(button).width(50).pad(5);
        puzzleSlider = new Slider(0,100, 1, false, game.skin, "default-horizontal");
        puzzleSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Slider slider = (Slider)actor;
                if ((int)puzzleSlider.getValue() >= sliderValues.size) return;
                if ((int)puzzleSlider.getValue() <  0) return;

                updateSliderLabel();
//                Gdx.app.error("debug", "value          = " + slider.getValue());
//                Gdx.app.error("debug", "percent        = " + slider.getPercent());
//                Gdx.app.error("debug", "visual value   = " + slider.getVisualValue());
//                Gdx.app.error("debug", "visual percent = " + slider .getVisualPercent());
            }
        });
        table.add(puzzleSlider).width(WIDTH).padLeft(10).padRight(10);
        button = new Button(game.skin, "play");
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float v = puzzleSlider.getValue()+1;
                if (v>=sliderValues.size) return;
                puzzleSlider.setValue(v);
                updateSliderLabel();
            }
        });
        button.align(Align.left);
        table.add(button).width(50).pad(5);
        table.setBackground(game.skin.getDrawable("outline"));
        table.pad(5);
        rootTable.add(table);

        rootTable.row();
        playButton = new TextButton("Build puzzle & play", game.skin);
        playButton.setDisabled(true);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (playButton.isDisabled()) return;
                String puzzleName = puzzleNameTextField.getText();
                resampleImage();
                game.setScreen(new GameScreen(game, puzzleImage, puzzleName, puzzleRows, puzzleCols));
                dispose();
            }
        });
        rootTable.add(playButton).width(WIDTH);


        // Create a button to go back to the mainmenu
        rootTable.row();
        button = new Button(game.skin, "leftarrow");
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

    /**
     * Checks whether the platButton should be enabled or not.
     */
    public void checkSetupComplete() {
        if (puzzleNameTextField.getText().equals("") || (puzzleImage==null)) {
            playButton.setDisabled(true);
            return;
        }
        playButton.setDisabled(false);
    }

    public final static float PIECESIZE = 300.0f;

    public void generateSliderValues() {
        int maxCols = (int)(puzzleImage.getWidth() / (PIECESIZE * 0.2f));
        int maxRows = (int)(puzzleImage.getHeight() / (PIECESIZE * 0.2f));

        if (maxCols>20) maxCols = 20;
        if (maxRows>20) maxRows = 20;

        int minCols = 3;
        int minRows = 3;

        sliderValues.clear();

        for (int i=minCols; i<=maxCols; i++) {
            int minJ = Math.max(minRows, (int)(0.8f*(float)i));
            int maxJ = Math.min(maxRows, (int)(1.2f*(float)i));
            for (int j = minJ; j<=maxJ; j++) {
                GridPoint3 gp = new GridPoint3(i, j, i*j);
                if (!sliderValues.contains(gp, false)) sliderValues.add(gp);
            }
        }

        sliderValues.sort(new Comparator<GridPoint3>() {
            @Override
            public int compare(GridPoint3 t0, GridPoint3 t1) {
                return (t0.z - t1.z);
            }
        });
    }

    public void updateSlider() {
        puzzleSlider.setValue(0);
        puzzleSlider.setRange(0, sliderValues.size-1);
        updateSliderLabel();
    }

    public void updateSliderLabel() {
        GridPoint3 gp = sliderValues.get((int)puzzleSlider.getValue());
        puzzleRows = gp.x;
        puzzleCols = gp.y;
        puzzleSizeLabel.setText("Puzzle Size: " + gp.x + "x" + gp.y);
    }


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

    /**
     * Resamples puzzleImage based on the number of rows, cols so that the peices are
     * reasonably sized. This speeds up the puzzle generation process.
     */
    public void resampleImage() {
        float scale = ((float)puzzleCols * PIECESIZE) / (float)puzzleImage.getWidth();
        if (scale>0.7f) return;

        Pixmap pixmap = new Pixmap(
                (int)(puzzleImage.getWidth() * scale),
                (int)(puzzleImage.getHeight() * scale),
                puzzleImage.getFormat()
        );
        pixmap.setFilter(Pixmap.Filter.BiLinear);
        pixmap.drawPixmap(puzzleImage,
                0, 0,
                puzzleImage.getWidth(), puzzleImage.getHeight(),
                0, 0,
                pixmap.getWidth(), pixmap.getHeight());
        puzzleImage.dispose();
        puzzleImage = pixmap;
    }

    public final float THUMBNAIL_WIDTH = 300.0f;

    public void createThumbnail() {
        Gdx.app.error("debug", "Creating thumbnail...");
        float scale = THUMBNAIL_WIDTH / (float)puzzleImage.getWidth();
        thumbnail = new Pixmap(
                (int)(puzzleImage.getWidth() * scale),
                (int)(puzzleImage.getHeight() * scale),
                puzzleImage.getFormat());
        thumbnail.setFilter(Pixmap.Filter.BiLinear);
        thumbnail.drawPixmap(puzzleImage,
                0, 0,
                puzzleImage.getWidth(), puzzleImage.getHeight(),
                0, 0,
                thumbnail.getWidth(), thumbnail.getHeight());
        Gdx.app.error("debug", "  size = (" + thumbnail.getWidth() + ", " + thumbnail.getHeight() + ")");
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
        if (puzzleImage != null) puzzleImage.dispose();
        if (thumbnail != null) thumbnail.dispose();
    }
}
