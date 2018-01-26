package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;


/**
 * Created by Marc on 10/7/2017.
 */

class PuzzlePiece extends Sprite implements Json.Serializable {
    public int row;
    public int col;
    public Vector2 pos;
    public Vector2 posRotated;
    public Vector2 posInitial;
    public BoundingBox tapSquare;
    public Vector2 mid = new Vector2();

    public boolean isSelected=false;
    public TextureRegion highlight=null;
    public Color highlightColor = new Color(Color.WHITE);

    public boolean[] neighborMask = new boolean[4];
    public PuzzlePiece[] neighbor = new PuzzlePiece[4];
    public Vector2[] neighborFit = new Vector2[4];
    public int snapsWith = 0;
    public boolean fits = false;

    public PuzzleGroup group = null;

    public PuzzlePiece() {
        super();
    }

    public PuzzlePiece(int r, int c, PuzzlePacker.PieceData data, TextureRegion img, boolean flipY) {
        super(img);
        if (flipY) flip(false, true);
        row = r;
        col = c;
        this.pos = data.position;
        posRotated = new Vector2(pos);
        posInitial = new Vector2(pos);
        this.tapSquare = data.tapSquare;
        tapSquare.min.add(pos.x, pos.y, 0);
        tapSquare.max.add(pos.x, pos.y, 0);
        setPosition(pos.x, pos.y);

        resetOrigin();

        for (int i=0; i<4; i++) {
            neighborMask[i] = false;
            neighborFit[i] = new Vector2();
        }
    }

    public Vector2 getPosition() {
        return new Vector2(getX(), getY());
    }

    public Vector2 getPosPlusSize() {
        Vector2 rv = new Vector2(pos);
        rv.x += getWidth();
        rv.y += getHeight();
        return rv;
    }

    public void resetOrigin() {
        setOrigin(getRegionWidth()/2, getRegionHeight()/2);
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

    public Vector2 getOrigin() {
        if (group==null)
            return new Vector2(getOriginX(), getOriginY());
        Vector2 rv = new Vector2(group.center);
        rv.sub(pos);
        return rv;
    }

    @Override
    public float getRotation() {
        float rv = super.getRotation();
        if (rv<0) rv = 360f - ((-rv)%360f);
        return rv;
    }

    public float getDegreeDiff(float angle1, float angle2) {
        float rv = angle1-angle2;
        if (rv<-180f) rv += 360f;
        if (rv>180f) rv -= 360f;
        return Math.abs(rv);
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
            if (neighbor[i]==null) {
                neighborMask[i] = false;
                continue;
            }
            neighborMask[i] = true;
            neighborFit[i].set(neighbor[i].pos).sub(pos);
        }
    }

    /**
     * Rotates the given point about the origin of the piece. Modifies and returns point.
     */
    public Vector2 rotatePoint(Vector2 point, float degrees) {
        return point.sub(mid).rotate(degrees).add(mid);
    }

    public Vector2 rotatePoint2(Vector2 point, float degrees) {
        return point.sub(getMid()).rotate(degrees).add(getMid());
    }

    public void fitReport() {
        if (!neighborMask[0] && !neighborMask[1] && !neighborMask[2] && !neighborMask[3]) {
            Gdx.app.error("fitReport", "fit report for ("+row+","+col+") aborted - neighbor mask is all zero");
            return;
        }

        Gdx.app.error("fitReport", "fit report for ("+row+","+col+")");
        Gdx.app.error("fitReport", "  rotation:");
        Gdx.app.error("fitReport", "    this       : " + getRotation());
        for (int i=0; i<4; i++) {
            if (!neighborMask[i]) continue;
            float epsilon = 5.0f;
            float rotation = neighbor[i].getRotation();
            String status = (getDegreeDiff(rotation, getRotation())>epsilon)? " (FAIL)": "(pass)";
            Gdx.app.error("fitReport", "    neighbor[" + i + "]: " + rotation + status);
        }
        Gdx.app.error("fitReport", "  position:");
        v2.set(pos);
        Gdx.app.error("fitReport", "    this       : " + v2.toString());
        for (int i=0; i<4; i++) {
            if (!neighborMask[i]) continue;
            v1.set(neighbor[i].posRotated);
            v1.sub(neighborFit[i]);
            float epsilon = 0.05f * getWidth();
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
        if (!neighborMask[i]) return false;
        // the distance between the two pieces as well as the orientation of both pieces must
        // be close. We generate a fit vector based on the fit vectors of the two pieces and the
        // rotation angle of this piece, and compare that to the relative position vector of the
        // two pieces and the angle of rotation of the neighbor.
        float epsilon = 5.0f;
        if (getDegreeDiff(neighbor[i].getRotation(), getRotation())>epsilon) return false;

        v1.set(neighbor[i].posRotated);

        v2.set(pos);
        v2.add(neighborFit[i]);

        epsilon = 0.05f * getWidth();
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
            if (!neighborMask[i]) continue;
            if ((snapsWith & (1<<i))!=0) {
                // if the piece is snapping into multiple neighbors, it may have snapped into
                // the current group already. In that case, we don't need to snapIn again
                if ((group==null) || (group != neighbor[i].group)) {
                    setRotation(0);
                    v1.set(neighbor[i].pos).sub(neighborFit[i]);
                    moveTo(v1.x, v1.y);
                    v2.set(getOrigin());
                    v1.set(neighbor[i].getMid()).sub(pos);
                    setOrigin(v1.x, v1.y);
                    setRotation(neighbor[i].getRotation());

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

                    // update the origin to the new group origin
                    group.propagateCenter();
                }

                neighbor[i].neighborMask[(i+2)%4] = false;
                neighborMask[i] = false;
            }
        }
    }

    public float cos = 1f;
    public float sin = 0f;
    public Vector2 trans;
    public Vector3 rot = new Vector3();

    /**
     * We rotate the touch into the coordinate system of the bounding box, then
     * check bounds.
     */
    public boolean hit(Vector3 loc) {
        trans = new Vector2(loc.x, loc.y);
        trans.sub(getMid());

        rot.set(
                trans.x*cos - trans.y*sin,
                trans.y*cos + trans.x*sin,
                0
        );

        rot.add(getMid().x, getMid().y, 0);

        return tapSquare.contains(rot);
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

        // limit degrees to 0-360
        float deg = (degrees>0)? degrees%360f: 360f - (-degrees)%360f;

        v1.set(0,0);
        rotatePoint(v1, deg);
        posRotated.set(pos).add(v1);

        float currentDegrees = getRotation();

        for (Vector2 v: neighborFit) {
            if (v==null) continue;
            rotatePoint(v, deg-currentDegrees);
        }
        if (checkForFit()) {
            fits = true;
            setHighlightColor(Color.LIME);
        } else {
            fits = false;
            setHighlightColor(Color.WHITE);
        }

        // rotate the tapSquare
        cos = (float)Math.cos(-deg*Math.PI/180.0f);
        sin = (float)Math.sin(-deg*Math.PI/180.0f);

        super.setRotation(deg);
    }

    public void moveTo(float x, float y) {
        float deltaX = x - pos.x;
        float deltaY = y - pos.y;
        moveBy(deltaX, deltaY);
    }

    public void moveTo(float x, float y, boolean checkForGroup) {
        float deltaX = x - pos.x;
        float deltaY = y - pos.y;
        moveBy(deltaX, deltaY, checkForGroup);
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
            fits = true;
            setHighlightColor(Color.LIME);
        } else {
            fits = false;
            setHighlightColor(Color.WHITE);
        }
    }

    public Vector2 getMid() {
        Vector2 rv = new Vector2(mid);
        return rv.add(pos);
    }

    public void setHighlightColor(Color c) {
        if ((group!=null)&&(useGroupColor)) {
            group.highlightColor.set(c);
        } else {
            highlightColor.set(c);
        }
    }

    private boolean useGroupColor = true;

    public void useGroupColor() {
        useGroupColor(true);
    }

    public void useGroupColor(boolean b) {
        useGroupColor = b;
    }

    public Color getHighlightColor() {
        return ((group==null)||(!useGroupColor)) ? highlightColor: group.highlightColor;
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

    public void drawTapSquare(ShapeRenderer sr) {
        Vector2 [] corner = new Vector2[4];
        corner[0] = new Vector2(tapSquare.min.x, tapSquare.min.y);
        corner[1] = new Vector2(tapSquare.min.x, tapSquare.max.y);
        corner[2] = new Vector2(tapSquare.max.x, tapSquare.max.y);
        corner[3] = new Vector2(tapSquare.max.x, tapSquare.min.y);
        for (int i=0; i<4; i++) corner[i] = rotatePoint2(corner[i], getRotation());

        sr.line(corner[0], corner[1]);
        sr.line(corner[1], corner[2]);
        sr.line(corner[2], corner[3]);
        sr.line(corner[3], corner[0]);
    }

    public void drawDebugLines(ShapeRenderer sr) {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        for (int i=0; i<4; i++) {
            if (!neighborMask[i]) continue;
            sr.setColor(Color.LIME);
            sr.circle(neighbor[i].posRotated.x, neighbor[i].posRotated.y, 3f);
            sr.setColor(Color.CYAN);
            sr.circle(pos.x+neighborFit[i].x, pos.y+neighborFit[i].y, 2f);
        }
        sr.setColor(Color.CYAN);
        sr.circle(posRotated.x, posRotated.y, 3f);
        sr.rect(getMid().x, getMid().y, 3f, 3f);
        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        for (int i=0; i<4; i++) {
            if (!neighborMask[i]) continue;
            sr.setColor(Color.LIME);
            sr.circle(neighbor[i].pos.x, neighbor[i].pos.y, 3f);
            sr.setColor(Color.CYAN);
            sr.circle(pos.x+neighborFit[i].x, pos.y+neighborFit[i].y, 2f);
        }
        sr.setColor(Color.CYAN);
        sr.circle(pos.x, pos.y, 3f);
        sr.end();
    }

    public void drawHighlight(SpriteBatch batch, float parentAlpha) {
        drawHighlight(batch, parentAlpha, true);
    }

    public void drawHighlight(SpriteBatch batch, float parentAlpha, boolean checkForGroup) {
        if (checkForGroup && (group!=null) && (useGroupColor)) {
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

    private String [] neighborID = new String[4];
    private String groupID;

    public String getID() {
        return row + "," + col;
    }
    
    public void setGroupID() {
        groupID =  (group==null) ? "": group.getId();
    }

    public void setNeighborIDs() {
        for (int i=0; i<4; i++) {
            if (neighbor[i]==null) {
                neighborID[i] = "";
            } else {
                neighborID[i] = neighbor[i].getID();
            }
        }
    }

    public void restoreNeighborLinkages(ObjectMap<String, PuzzlePiece> map) {
        for (int i=0; i<4; i++) {
            neighbor[i] = map.get(neighborID[i]);
        }
    }

    public void restoreGroupLinkage(ObjectMap<String, PuzzleGroup> map) {
        group = map.get(groupID);
    }

    public void restoreTextureRegion(TextureAtlas atlas) {
        TextureRegion region = atlas.findRegion(getID());
        setRegion(region);
        setColor(1, 1, 1, 1);
        setSize(region.getRegionWidth(), region.getRegionHeight());
        flip(false, true);

        // regenerate the highlight
        OutlineShader.setup(this);
        highlight = OutlineShader.renderToTexture();
    }

    @Override
    public void write(Json json) {
        setNeighborIDs();
        setGroupID();

        json.writeField(this, "row");
        json.writeField(this, "col");
        json.writeField(this, "pos");
        json.writeField(this, "posRotated");
        json.writeField(this, "posInitial");
        json.writeField(this, "tapSquare");
        json.writeField(this, "mid");
        json.writeField(this, "isSelected");
        json.writeField(this, "highlightColor");
        json.writeField(this, "neighborMask");
        json.writeField(this, "neighborID");
        json.writeField(this, "neighborFit");
        json.writeField(this, "groupID");
        // write some fields from the superclass...
        json.writeField(this, "x");
        json.writeField(this, "y");
        json.writeField(this, "width");
        json.writeField(this, "height");
        json.writeField(this, "originX");
        json.writeField(this, "originY");
        json.writeField(this, "rotation");
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        json.readFields(this, jsonData);
    }
}
