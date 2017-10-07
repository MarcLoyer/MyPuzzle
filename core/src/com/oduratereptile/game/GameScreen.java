package com.oduratereptile.game;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import static com.oduratereptile.game.HudScreen.Corner.*;

/**
 * Created by Marc on 10/3/2017.
 */

public class GameScreen extends HudScreen {
    public GameScreen(final MyPuzzle game) {
        super(game);

        debugHUD(false);

        Button button = new Button(game.skin, "leftarrow");
        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        getTable(LR).add(button).width(40f).height(40f);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
//    next steps:
//        (1) add ortho camera and panning/zoom (add a Pixmap for testing)
//        (2) Catmull-Rom splines <-- actually, this is a separate class
//        (3) "load an image" dialog
//        (4) "invite a friend" dialog
//        (5) "view full image" window
//        (6) chat area
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
