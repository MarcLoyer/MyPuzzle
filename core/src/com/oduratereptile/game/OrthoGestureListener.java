package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Marc on 10/6/2017.
 */

public class OrthoGestureListener implements GestureListener {
    GameScreen gameScreen;
    OrthographicCamera cam;

    public OrthoGestureListener(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.cam = gameScreen.camera;
    }

    public Vector2 camSize = new Vector2();
    public Vector2 camCenter = new Vector2();

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        // Note the camera state before pan/zoom operations...
        camSize.set(cam.viewportWidth, cam.viewportHeight);
        camCenter.set(cam.position.x, cam.position.y);
//        gameScreen.setStatus("Cam: " + camCenter.toString() + "   Viewport: " + camSize.toString());

        return false;
    }

    // use this one to select/deselect pieces
    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    // use this to move the view or to move the selected piece
    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        cam.position.add(cam.unproject(new Vector3(0,0,0)).sub(cam.unproject(new Vector3(deltaX, deltaY, 0))));
        cam.update();

        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    // ignore this one and use pinch() instead
    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    // Use this one to detect zoom in / zoom out as well as rotation
    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        Vector3 pinchLoc = new Vector3(
                (pointer1.x+pointer2.x)/2f,
                (pointer1.y+pointer2.y)/2f,
                0);

        Vector3 preZoom = cam.unproject(pinchLoc.cpy());

        float scalefactor = initialPointer1.dst(initialPointer2) / pointer1.dst(pointer2);
        cam.viewportWidth = scalefactor * camSize.x;
        cam.viewportHeight = scalefactor * camSize.y;
        cam.update();

        Vector3 postZoom = cam.unproject(pinchLoc.cpy());

        cam.position.add(preZoom.sub(postZoom));
        cam.update();
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
