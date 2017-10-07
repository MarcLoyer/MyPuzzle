package com.oduratereptile.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Created by Marc on 10/7/2017.
 */

class PuzzlePiece extends Image {
    public int row;
    public int col;

    public PuzzlePiece(int r, int c, Vector2 loc, float rot, TextureRegion img) {
        super(img);
        row = r;
        col = c;
        setPosition(loc.x, loc.y);
        setRotation(rot);

    }
}
