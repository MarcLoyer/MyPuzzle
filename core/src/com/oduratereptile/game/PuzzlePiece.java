package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


/**
 * Created by Marc on 10/7/2017.
 */

class PuzzlePiece extends Sprite {
    public int row;
    public int col;
    public Vector2 pos;
    public BoundingBox tapSquare;
    public Vector2 mid;

    public boolean isSelected=false;
    public TextureRegion highlight=null;
    public Color highlightColor = Color.WHITE;

    public PuzzlePiece[] neighbor = new PuzzlePiece[4];
    public Vector2[] neighborFit = new Vector2[4];
    public int snapsWith = 0;

    public PuzzlePiece(int r, int c, PuzzlePacker.PieceData data, TextureRegion img, boolean flipY) {
        super(img);
        if (flipY) flip(false, true);
        row = r;
        col = c;
        this.pos = data.position;
        this.tapSquare = data.tapSquare;
        tapSquare.min.add(pos.x, pos.y, 0);
        tapSquare.max.add(pos.x, pos.y, 0);
        setPosition(pos.x, pos.y);

        mid = new Vector2(img.getRegionWidth()/2, img.getRegionHeight()/2);
        setOrigin(mid.x, mid.y);

        for (int i=0; i<4; i++) { neighborFit[i] = new Vector2(); }
    }

    public void setNeighbors(PuzzlePiece top, PuzzlePiece right, PuzzlePiece bottom, PuzzlePiece left) {
        neighbor[0] = top;
        neighbor[1] = right;
        neighbor[2] = bottom;
        neighbor[3] = left;

        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) continue;
            neighborFit[i].set(neighbor[i].pos).sub(pos).rotate(getRotation());
        }
    }

    public boolean checkForFit() {
        snapsWith = 0;
        for (int i=0; i<4; i++) {
            if (fit(i)) {
                snapsWith += (1<<i);
            }
        }
        return (snapsWith>0);
    }

    Vector2 actual = new Vector2();

    public boolean fit(int i) {
        // TODO: bug - this is not correct when rotations are present
        if (neighbor[i]==null) return false;
        // the distance between the two pieces as well as the orientation of both pieces must
        // be close. We generate a fit vector based on the fit vectors of the two pieces and the
        // rotation angle of this piece, and compare that to the relative position vector of the
        // two pieces and the angle of rotation of the neighbor.
        float epsilon = 5.0f;
        if (Math.abs(neighbor[i].getRotation()-getRotation())>epsilon) return false;

        actual.set(neighbor[i].pos).sub(pos);
        epsilon = 5.0f;
        if (actual.dst2(neighborFit[i])>epsilon) return false;

        return true;
    }

    public void snapIn() {
        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) continue;
            if ((snapsWith & (1<<i))!=0) {
                // if this piece is part of a group, move/rotate the whole group
                setRotation(neighbor[i].getRotation());
                actual.set(neighbor[i].pos).sub(neighborFit[i]);
                moveTo(actual.x, actual.y);

                // merge the neighbor (and its group) with this piece (and its group)
                // TODO: implement!
            }
        }
    }

    public boolean hit(Vector3 loc) {
        return tapSquare.contains(loc);
    }

    @Override
    public void setRotation(float degrees) {
        // TODO: apply to the whole group
        float currentDegrees = getRotation();
        for (Vector2 v: neighborFit) {
            if (v==null) continue;
            v.rotate(degrees-currentDegrees);
        }
        if (checkForFit()) {
            highlightColor = Color.LIME;
        } else {
            highlightColor = Color.WHITE;
        }

        // TODO: rotate the tapSquare?
        super.setRotation(degrees);
    }

    public void moveTo(float x, float y) {
        float deltaX = x - pos.x;
        float deltaY = y - pos.y;
        moveBy(deltaX, deltaY);
    }

    public void moveBy(float x, float y) {
        // TODO: apply to the whole group
        pos.add(x,y);
        tapSquare.min.add(x,y,0);
        tapSquare.max.add(x,y,0);
        setPosition(pos.x, pos.y);

        if (checkForFit()) {
            highlightColor = Color.LIME;
        } else {
            highlightColor = Color.WHITE;
        }
    }

    public Vector2 getMid() {
        Vector2 rv = new Vector2(mid);
        return rv.add(pos);
    }

    public void select(boolean sel) {
        isSelected = sel;
    }

    public void select() { select(true); }

    public void draw(SpriteBatch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void drawDebugLines(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.LIME);
        for (Vector2 v: neighborFit) {
            sr.line(getMid().x, getMid().y, getMid().x+v.x, getMid().y+v.y);
        }
        sr.end();
    }

    public void drawHighlight(SpriteBatch batch, float parentAlpha) {
        batch.setColor(highlightColor);
        batch.draw(highlight,
                getX() - OutlineShader.PAD, getY() - OutlineShader.PAD,
                getOriginX() + OutlineShader.PAD, getOriginY() + OutlineShader.PAD,
                highlight.getRegionWidth(), highlight.getRegionHeight(),
                1.0f, 1.0f,
                getRotation());
        batch.setColor(Color.WHITE);
    }
}
