package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by Marc on 10/7/2017.
 */

class PuzzlePiece extends Image {
    public int row;
    public int col;
    public PuzzlePieceCoords coords;

    public boolean isSelected=false;
    public TextureRegion highlight=null;

    public PuzzlePiece(int r, int c, PuzzlePieceCoords coords, TextureRegion img) {
        super(img);
        row = r;
        col = c;
        this.coords = coords;
        setPosition(coords.pos.x, coords.pos.y);

        Vector2 mid = coords.getMid();
        setOrigin(mid.x, mid.y);
    }

    @Override
    public void moveBy(float x, float y) {
        super.moveBy(x,y);
        coords.pos.add(x,y);
    }

    public Vector2 getMid() {
        return coords.getMid().add(coords.pos);
    }

    public void select(boolean sel) {
        if (sel) {
            Gdx.app.error("debug", "piece ("+row+", "+col+") is selected");
            if (highlight == null) generateHighlight();
            isSelected = true;
        } else {
            Gdx.app.error("debug", "piece ("+row+", "+col+") is deselected");
            isSelected = false;
        }
    }

    public void select() { select(true); }

    public void draw(SpriteBatch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        // TODO: draw highlight with rotation as well
//        if (isSelected) {
//            batch.draw(highlight, getX()-OutlineShader.PAD, getY()-OutlineShader.PAD);
//        }
    }

    public void drawHighlight(SpriteBatch batch, float parentAlpha) {
        batch.draw(highlight,
                getX() - OutlineShader.PAD, getY() - OutlineShader.PAD,
                getOriginX() + OutlineShader.PAD, getOriginY() + OutlineShader.PAD,
                highlight.getRegionWidth(), highlight.getRegionHeight(),
                1.0f, 1.0f,
                getRotation(), false
                );
    }

    public void generateHighlight() {
        // TODO: implement!
        Gdx.app.error("status", "Generating highlight for (" + row + ", " + col + ")");
        // TODO: need the batch
        //highlight = shader.renderToTexture(batch);
    }
}
