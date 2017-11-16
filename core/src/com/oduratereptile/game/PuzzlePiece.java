package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;


/**
 * Created by Marc on 10/7/2017.
 */

class PuzzlePiece extends Sprite {
    public int row;
    public int col;
    public Vector2 pos;
    public Vector2 posRotated;
    public BoundingBox tapSquare;
    public Vector2 mid = new Vector2();

    public boolean isSelected=false;
    public TextureRegion highlight=null;
    public Color highlightColor = new Color(Color.WHITE);

    public PuzzlePiece[] neighbor = new PuzzlePiece[4];
    public Vector2[] neighborFit = new Vector2[4];
    public int snapsWith = 0;

    public PuzzleGroup group = null;

    public PuzzlePiece(int r, int c, PuzzlePacker.PieceData data, TextureRegion img, boolean flipY) {
        super(img);
        if (flipY) flip(false, true);
        row = r;
        col = c;
        this.pos = data.position;
        posRotated = new Vector2(pos);
        this.tapSquare = data.tapSquare;
        tapSquare.min.add(pos.x, pos.y, 0);
        tapSquare.max.add(pos.x, pos.y, 0);
        setPosition(pos.x, pos.y);

        setOrigin(img.getRegionWidth()/2, img.getRegionHeight()/2);

        for (int i=0; i<4; i++) { neighborFit[i] = new Vector2(); }
    }

    @Override
    public void setOrigin(float x, float y) {
        if (mid==null) { // don't know why I have to do this - mid was already allocated. It throws an error if I don't though
            mid = new Vector2(x,y);
        } else {
            mid.set(x,y);
        }
        super.setOrigin(x,y);
    }

    /**
     * Assumes the neighbors and this piece are all in their solved state, and un-rotated.
     */
    public void setNeighbors(PuzzlePiece top, PuzzlePiece right, PuzzlePiece bottom, PuzzlePiece left) {
        neighbor[0] = top;
        neighbor[1] = right;
        neighbor[2] = bottom;
        neighbor[3] = left;

        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) continue;
            neighborFit[i].set(neighbor[i].pos).sub(pos);
        }
    }

    /**
     * Rotates the given point about the origin of the piece. Modifies and returns point.
     */
    public Vector2 rotatePoint(Vector2 point, float degrees) {
        return point.sub(mid).rotate(degrees).add(mid);
    }

    public void fitReport() {
        Gdx.app.error("fitReport", "fit report for ("+row+","+col+")");
        Gdx.app.error("fitReport", "  rotation:");
        Gdx.app.error("fitReport", "    this       : " + getRotation());
        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) continue;
            float epsilon = 5.0f;
            float rotation = neighbor[i].getRotation();
            String status = (Math.abs(rotation-getRotation())>epsilon)? " (FAIL)": "(pass)";
            Gdx.app.error("fitReport", "    neighbor[" + i + "]: " + rotation + status);
        }
        Gdx.app.error("fitReport", "  position:");
        v2.set(pos);
        Gdx.app.error("fitReport", "    this       : " + v2.toString());
        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) continue;
            v1.set(neighbor[i].posRotated);
            v1.sub(neighborFit[i]);
            float epsilon = 5.0f;
            float dst2 = v1.dst2(v2);
            String status = (dst2>epsilon)? " (FAIL)": "(pass)";
            Gdx.app.error("fitReport", "    neighbor[" + i + "]: " + v1.toString() + "("+dst2+")"+status);
        }
    }

    public boolean checkForFit() {
        return checkForFit(true);
    }

    public boolean checkForFit(boolean checkForGroup) {
        if (checkForGroup && (group!=null)) {
            return group.checkForFit();
        }

        snapsWith = 0;
        for (int i=0; i<4; i++) {
            if (fit(i)) {
                snapsWith += (1<<i);
            }
        }
        return (snapsWith>0);
    }

    public Vector2 v1 = new Vector2();
    public Vector2 v2 = new Vector2();

    public boolean fit(int i) {
        if (neighbor[i]==null) return false;
        // the distance between the two pieces as well as the orientation of both pieces must
        // be close. We generate a fit vector based on the fit vectors of the two pieces and the
        // rotation angle of this piece, and compare that to the relative position vector of the
        // two pieces and the angle of rotation of the neighbor.
        float epsilon = 5.0f;
        if (Math.abs(neighbor[i].getRotation()-getRotation())>epsilon) return false;

        v1.set(neighbor[i].posRotated);

        v2.set(pos);
        v2.add(neighborFit[i]);

        epsilon = 5.0f;
        if (v1.dst2(v2)>epsilon) return false;

        return true;
    }

    public void snapIn() {
        snapIn(true);
    }

    public void snapIn(boolean checkForGroup) {
        if (checkForGroup && (group!=null)) {
            group.snapIn();
            return;
        }

        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) continue;
            if ((snapsWith & (1<<i))!=0) {
                // if this piece is part of a group, move/rotate the whole group
                setRotation(neighbor[i].getRotation());
                v1.set(neighbor[i].posRotated).sub(neighborFit[i]);
                moveTo(v1.x, v1.y);

                // merge the neighbor (and its group) with this piece (and its group)
                if (group!=null) {
                    group.add(neighbor[i]);
                } else {
                    if (neighbor[i].group!=null) {
                        neighbor[i].group.add(this);
                    } else {
                        new PuzzleGroup(this, neighbor[i]);
                        select(true);
                    }
                }
                neighbor[i].neighbor[(i+2)%4] = null;
                neighbor[i] = null;
            }
        }
    }

    public boolean hit(Vector3 loc) {
        return tapSquare.contains(loc);
    }

    @Override
    public void setRotation(float degrees) {
        setRotation(degrees, true);
    }

    public void setRotation(float degrees, boolean checkForGroup) {
        // apply to the whole group
        if (checkForGroup && (group!=null)) {
            group.setRotation(degrees);
            return;
        }

        v1.set(0,0);
        rotatePoint(v1, degrees);
        posRotated.set(pos).add(v1);

        float currentDegrees = getRotation();
        for (Vector2 v: neighborFit) {
            if (v==null) continue;
            rotatePoint(v, degrees-currentDegrees);
        }
        if (checkForFit()) {
            setHighlightColor(Color.LIME);
        } else {
            setHighlightColor(Color.WHITE);
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
        moveBy(x, y, true);
    }

    public void moveBy(float x, float y, boolean checkForGroup) {
        // apply to the whole group
        if (checkForGroup && (group!=null)) {
            group.moveBy(x, y);
            return;
        }

        pos.add(x,y);
        posRotated.add(x,y);
        tapSquare.min.add(x,y,0);
        tapSquare.max.add(x,y,0);
        setPosition(pos.x, pos.y);

        if (checkForFit()) {
            setHighlightColor(Color.LIME);
        } else {
            setHighlightColor(Color.WHITE);
        }
    }

    public Vector2 getMid() {
        Vector2 rv = new Vector2(mid);
        return rv.add(pos);
    }

    public void setHighlightColor(Color c) {
        if (group!=null) {
            group.highlightColor.set(c);
        } else {
            highlightColor.set(c);
        }
    }

    public Color getHighlightColor() {
        return (group==null)? highlightColor: group.highlightColor;
    }

    public void select(boolean sel) {
        if (group!=null) {
            group.isSelected = sel;
        } else {
            isSelected = sel;
        }
    }

    public void select() { select(true); }

    public boolean isSelected() {
        return (group!=null) ? group.isSelected: isSelected;
    }

    public void draw(SpriteBatch batch, float parentAlpha) {
        draw(batch, parentAlpha, true);
    }

    public void draw(SpriteBatch batch, float parentAlpha, boolean checkForGroup) {
        if (checkForGroup && (group!=null) && isSelected()) {
            group.draw(batch, parentAlpha);
            return;
        }
        super.draw(batch, parentAlpha);
    }

    public void drawDebugLines(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) continue;
            sr.setColor(Color.LIME);
            sr.circle(neighbor[i].posRotated.x, neighbor[i].posRotated.y, 3f);
            sr.setColor(Color.CYAN);
            sr.circle(pos.x+neighborFit[i].x, pos.y+neighborFit[i].y, 2f);
        }
        sr.setColor(Color.CYAN);
        sr.circle(posRotated.x, posRotated.y, 3f);
        sr.rect(getMid().x, getMid().y, 3f, 3f);
        sr.end();
    }

    public void drawHighlight(SpriteBatch batch, float parentAlpha) {
        drawHighlight(batch, parentAlpha, true);
    }

    public void drawHighlight(SpriteBatch batch, float parentAlpha, boolean checkForGroup) {
        if (checkForGroup && (group!=null)) {
            group.drawHighlight(batch, parentAlpha);
            return;
        }

        batch.setColor(getHighlightColor());
        batch.draw(highlight,
                getX() - OutlineShader.PAD, getY() - OutlineShader.PAD,
                getOriginX() + OutlineShader.PAD, getOriginY() + OutlineShader.PAD,
                highlight.getRegionWidth(), highlight.getRegionHeight(),
                1.0f, 1.0f,
                getRotation());
        batch.setColor(Color.WHITE);
    }
}
