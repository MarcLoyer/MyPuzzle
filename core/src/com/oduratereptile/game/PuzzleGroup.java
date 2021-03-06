package com.oduratereptile.game;

/**
 * Created by Marc on 11/15/2017.
 */

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * Responsible for moving/rotating a group of snapped-in puzzle pieces as a single unit
 */
public class PuzzleGroup implements Json.Serializable {
    public String id;
    ArrayList<PuzzlePiece> piece = new ArrayList<PuzzlePiece>();
    Color highlightColor = new Color();
    boolean isSelected;
    Vector2 center = new Vector2();

    public PuzzleGroup() {
        setId(this);
        notifyCreation(this);
    }

    public PuzzleGroup(PuzzlePiece ... puzzlePieces) {
        for (PuzzlePiece p: puzzlePieces) {
            add(p);
        }
        setId(this);

        notifyCreation(this);
    }

    public void add(PuzzlePiece p) {
        PuzzleGroup oldGroup = p.group;
        if (oldGroup == this) return;
        if (oldGroup != null) {
            Iterator<PuzzlePiece> iter = oldGroup.piece.iterator();
            while (iter.hasNext()) {
                PuzzlePiece pp = iter.next();
                piece.add(pp);
                pp.group = this;
            }
            oldGroup.destroy();
        } else {
            piece.add(p);
            p.group = this;
        }
        highlightColor.set(p.highlightColor);
        isSelected = true;
        updateCenter();
        notifyModify(this);
    }

    public String getId() { return id; }

    public void updateCenter() {
        Iterator<PuzzlePiece> iter = piece.iterator();
        center.set(0,0);
        while (iter.hasNext()) {
            PuzzlePiece p = iter.next();
            center.add(p.pos);
            center.add(p.getRegionWidth()/2, p.getRegionHeight()/2);
        }
        float scale = 1.0f / (float)piece.size();
        center.scl(scale);
    }

    public void propagateCenter() {
        // changing the origin can only be done when the group has 0 rotation
        float deg = piece.get(0).getRotation();
        Vector2 p = new Vector2(piece.get(0).posRotated);

        Iterator<PuzzlePiece> iter = piece.iterator();
        while (iter.hasNext()) {
            PuzzlePiece pp = iter.next();
            pp.setRotation(0, false);
            pp.setOrigin(center.x-pp.pos.x, center.y-pp.pos.y);
            pp.setRotation(deg, false);
        }

        p.sub(piece.get(0).posRotated);
        moveBy(p.x, p.y);
    }

    public int size() { return piece.size(); }

    public void moveBy(float x, float y) {
        Iterator<PuzzlePiece> iter = piece.iterator();
        while (iter.hasNext()) {
            iter.next().moveBy(x, y, false);
        }
        center.add(x,y);
    }

    public void setRotation(float degrees) {
        // move the origin of rotation to "center", then rotate
        Iterator<PuzzlePiece> iter = piece.iterator();
        while (iter.hasNext()) {
            PuzzlePiece p = iter.next();
            p.setRotation(degrees, false);
        }
    }

    public boolean checkForFit() {
        boolean rv = false;
        Iterator<PuzzlePiece> iter = piece.iterator();
        while (iter.hasNext()) {
            if (iter.next().checkForFit(false)) rv = true;
        }
        return rv;
    }

    public void snapIn() {
        int size = piece.size();
        for (int i=0; i<size; i++) {
            piece.get(i).snapIn(false);
        }
        highlightColor.set(Color.WHITE);
    }

    public void draw(SpriteBatch batch, float parentAlpha) {
        Iterator<PuzzlePiece> iter = piece.iterator();
        while (iter.hasNext()) {
            iter.next().draw(batch, parentAlpha, false);
        }
    }

    public void drawHighlight(SpriteBatch batch, float parentAlpha) {
        Iterator<PuzzlePiece> iter = piece.iterator();
        while (iter.hasNext()) {
            iter.next().drawHighlight(batch, parentAlpha, false);
        }
    }

    public void destroy() {
        notifyDestruction(this);
    }

    ArrayList<String> pieceIds = new ArrayList<String>();

    private void setPieceIds() {
        Iterator<PuzzlePiece> iter = piece.listIterator();
        pieceIds.clear();
        while (iter.hasNext()) {
            pieceIds.add(iter.next().getID());
        }
    }

    public void restorePieceLinkages(ObjectMap<String, PuzzlePiece> map) {
        Iterator<String> iter = pieceIds.listIterator();
        piece.clear();
        while (iter.hasNext()) {
            piece.add(map.get(iter.next()));
        }
    }

    @Override
    public void write(Json json) {
        setPieceIds();

        json.writeField(this, "id");
        json.writeField(this, "pieceIds");
        json.writeField(this, "highlightColor");
        json.writeField(this, "isSelected");
        json.writeField(this, "center");
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        json.readFields(this, jsonData);
    }







    public static ArrayList<PuzzleGroupListener> groupListeners = new ArrayList<PuzzleGroupListener>();
    public static int count = 0;

    public static void setId(PuzzleGroup group) {
        group.id = "g"+count;
        count++;
    }

    public static void addListener(PuzzleGroupListener listener) {
        groupListeners.add(listener);
    }

    public static void notifyModify(PuzzleGroup group) {
        ListIterator<PuzzleGroupListener> iter = groupListeners.listIterator();
        while (iter.hasNext()) {
            iter.next().onModify(group);
        }
    }

    public static void notifyCreation(PuzzleGroup group) {
        ListIterator<PuzzleGroupListener> iter = groupListeners.listIterator();
        while (iter.hasNext()) {
            iter.next().onCreate(group);
        }
    }

    public static void notifyDestruction(PuzzleGroup group) {
        ListIterator<PuzzleGroupListener> iter = groupListeners.listIterator();
        while (iter.hasNext()) {
            iter.next().onDestroy(group);
        }
    }

    public interface PuzzleGroupListener {
        void onModify(PuzzleGroup group);
        void onCreate(PuzzleGroup group);
        void onDestroy(PuzzleGroup group);
    }
}
