package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;

/**
 * Created by Marc on 10/7/2017.
 */

public class Puzzle extends OrthoGestureListener implements PuzzleGroup.PuzzleGroupListener {
    public GameScreen gameScreen;
    public ShapeRenderer sr;
    public Json json = new Json();

    public GameData gameData;
    public PuzzlePieceManager manager;

    public boolean displayEvenPieces = false;
    public boolean displayAllPieces = true;
    public boolean displayTapSquares = false;

    public Puzzle(GameScreen gameScreen, GameData gameData) {
        super(gameScreen.camera);
        this.gameScreen = gameScreen;
        sr = gameScreen.game.shapeRenderer;
        manager = new PuzzlePieceManager(this);
        PuzzleGroup.addListener(this);

        this.gameData = gameData;
    }

    public Puzzle(GameScreen gameScreen, String basename) {
        super(gameScreen.camera);
        this.gameScreen = gameScreen;
        sr = gameScreen.game.shapeRenderer;
        manager = new PuzzlePieceManager(this);
        PuzzleGroup.addListener(this);

        this.gameData = GameData.restoreGameData(basename);
    }

    public void onCreate(PuzzleGroup group) {
        gameData.puzzleGroups.put(group.getId(), group);
        if (group.size()>=(gameData.rows*gameData.cols)) {
            win(group);
        }
        gameData.groupCount = PuzzleGroup.count;
    }

    public void onDestroy(PuzzleGroup group) {
        gameData.puzzleGroups.remove(group.getId());
    }

    public void win(PuzzleGroup group) {
        // TODO: implement
    }

    public Rectangle getBounds() {
        Rectangle rect = new Rectangle();
        Vector2 min = new Vector2(gameData.puzzlePieces.get("0,0").pos);
        Vector2 max = new Vector2(gameData.puzzlePieces.get("0,0").getPosPlusSize());
        Vector2 t;
        for (PuzzlePiece p : gameData.puzzlePieces.values()) {
            if (p.pos.x < min.x) min.x = p.pos.x;
            if (p.pos.y < min.y) min.y = p.pos.y;
            t = p.getPosPlusSize();
            if (t.x > max.x) max.x = t.x;
            if (t.y > max.y) max.y = t.y;
        }
        max.sub(min);
        rect.setPosition(min);
        rect.setSize(max.x, max.y);

        return rect;
    }

    public void render(SpriteBatch batch, float delta) {

        manager.act(delta);
        if (isAutopanning) autopan(delta);

        for (PuzzlePiece p : gameData.puzzlePieces.values()) {
            boolean isEven = ((p.col + p.row)%2 == 0);
            if (displayAllPieces) {
                if(!p.isSelected()) p.draw(batch, 1);
            } else {
                if ((displayEvenPieces && isEven) || (!displayEvenPieces && !isEven))
                    if (!p.isSelected()) p.draw(batch, 1);
            }
        }

        for (PuzzlePiece p: selectedPiece) {
            p.draw(batch, 1.0f);
            p.drawHighlight(batch, 1.0f);
        }

        batch.end();

        sr.setProjectionMatrix(gameScreen.camera.combined);

        // Draw tap squares
        if (displayTapSquares) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(0f, 0.7f, 0f, 1f);
            for (PuzzlePiece p: gameData.puzzlePieces.values()) {
                p.drawTapSquare(sr);
            }
            sr.end();
        }

        for (PuzzlePiece p: selectedPiece) {
            p.drawDebugLines(sr);
        }

        batch.begin();
    }

    // I currently only select one piece at a time, but maybe in the future I'll
    // do something more fancy...
    public ArrayList<PuzzlePiece> selectedPiece = new ArrayList<PuzzlePiece>();

    // use this to select/deselect pieces
    @Override
    public boolean tap(float x, float y, int count, int button) {
        Vector3 c = cam.unproject(new Vector3(x,y,0));

        // check if the tap location selects a new piece
        boolean isHit = false;
        for (PuzzlePiece p: gameData.puzzlePieces.values()) {
            if (p.hit(c)) {
                if (p.isSelected()) {
                    p.select(false);
                    selectedPiece.clear();
                } else {
                    for (PuzzlePiece s: selectedPiece) {
                        s.select(false);
                    }
                    selectedPiece.clear();

                    p.select();
                    selectedPiece.add(p);
                }
                isHit = true;
                break;
            }
        }
        if (!isHit) {
            for (PuzzlePiece s: selectedPiece) {
                s.select(false);
            }
            selectedPiece.clear();
        }

        return true;
    }

    public boolean isAutopanning = false;
    public Vector2 autopanVelocity = new Vector2();

    // use this to move the selected piece
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);
            Vector3 c = cam.unproject(new Vector3(deltaX, deltaY, 0)).sub(cam.unproject(new Vector3(0, 0, 0)));
            p.moveBy(c.x,c.y);
            // start panning if the touch is close to the edge of the screen
            isAutopanning = isAtEdge(x,y);
        } else {
            return super.pan(x,y,deltaX,deltaY);
        }

        return true;
    }

    public void autopan(float deltaTime) {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        float dx = autopanVelocity.x * deltaTime;
        float dy = autopanVelocity.y * deltaTime;

        if (isAtEdge(x,y)) {
            super.pan(x,y,dx,dy);
            if (selectedPiece.size()==1) {
                PuzzlePiece p = selectedPiece.get(0);
                Vector3 c = cam.unproject(new Vector3(-dx, -dy, 0)).sub(cam.unproject(new Vector3(0, 0, 0)));
                p.moveBy(c.x,c.y);
            }
            isAutopanning = true;
        } else {
            isAutopanning = false;
        }
    }

    public boolean isAtEdge(float x, float y) {
        float w = cam.viewportWidth;
        float h = cam.viewportHeight;

        Vector3 pos = cam.unproject(new Vector3(x,y,0));
        pos.sub(cam.position);
        pos.x = (pos.x/w)+0.5f;
        pos.y = (pos.y/h)+0.5f;

        boolean atEdge = false;
        autopanVelocity.set(0,0);
        if (pos.x < 0.05f) {
            autopanVelocity.x = 2f*w;
            atEdge = true;
        }
        if (pos.x > 0.95f) {
            autopanVelocity.x = -2f*w;
            atEdge = true;
        }
        if (pos.y < 0.05f) {
            autopanVelocity.y = -2f*h;
            atEdge = true;
        }
        if (pos.y > 0.95f) {
            autopanVelocity.y = 2f*h;
            atEdge = true;
        }
        return atEdge;
    }

    public float initialRotation;

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if ((pointer==1) && (selectedPiece.size()==1)) {
            initialRotation = selectedPiece.get(0).getRotation();
        }

        return super.touchDown(x,y,pointer, button);
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        isAutopanning = false;
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);
            if (p.snapsWith>0) {
                p.snapIn();
            }
        }
        return super.panStop(x, y, pointer, button);
    }

    @Override
    public void pinchStop() {
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);
            if (p.snapsWith>0) {
                p.snapIn();
            }
        }
    }

    // Use this to rotate a selected piece
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        if (selectedPiece.size()==1) {
            PuzzlePiece p = selectedPiece.get(0);

            Vector2 v1 = new Vector2(initialPointer2);
            v1.sub(initialPointer1);

            Vector2 v2 = new Vector2(pointer2);
            v2.sub(pointer1);

            float angle = ((float)Math.atan2(v1.x, v1.y) - (float)Math.atan2(v2.x, v2.y)) * MathUtils.radiansToDegrees;
            // magnify the angle - it takes too long to turn a piece otherwise
            angle *= 3f;

            p.setRotation(initialRotation - angle);
        } else {
            return super.pinch(initialPointer1, initialPointer2, pointer1, pointer2);
        }

        return true;
    }

    public void dispose() {
        gameData.dispose();
    }
}
