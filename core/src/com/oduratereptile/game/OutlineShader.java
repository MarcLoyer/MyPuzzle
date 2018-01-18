package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888;

/**
 * Created by Marc on 11/1/2017.
 */

public class OutlineShader {
    public static final int PAD = 10;

    public static OrthographicCamera camera = new OrthographicCamera();
    public static TextureRegion tex;
    public static ShaderProgram shader = new ShaderProgram(
            Gdx.files.internal("shaders/outline.vert"),
            Gdx.files.internal("shaders/outline.frag")
    );
    public static SpriteBatch batch;

    public static int bufferWidth;
    public static int bufferHeight;
    public static FrameBuffer blurTargetA, blurTargetB;
    public static TextureRegion fboRegionA, fboRegionB;

    public static void setBatch(SpriteBatch b) {
        batch = b;
    }

    public static void setup(TextureRegion t) {
        tex = t;
        bufferWidth = tex.getRegionWidth() + 2*PAD;
        bufferHeight = tex.getRegionHeight() + 2*PAD;

        shader.begin();
        shader.setUniformf("width", bufferWidth);
        shader.setUniformf("height", bufferHeight);
        shader.setUniformf("radius", 1f);
        shader.setUniformf("color", new Vector3(1.0f, 1.0f, 1.0f));
        shader.end();

        blurTargetA = new FrameBuffer(RGBA8888, bufferWidth, bufferHeight, false);
        blurTargetB = new FrameBuffer(RGBA8888, bufferWidth, bufferHeight, false);
        fboRegionA = new TextureRegion(blurTargetA.getColorBufferTexture(), bufferWidth, bufferHeight);
        fboRegionA.flip(false, true);
        fboRegionB = new TextureRegion(blurTargetB.getColorBufferTexture(), bufferWidth, bufferHeight);
        fboRegionB.flip(false, true);
    }

    public static TextureRegion renderToTexture() {
        camera.setToOrtho(false, bufferWidth, bufferHeight);
        batch.setProjectionMatrix(camera.combined);

        // draw from tex to FBO A
        blurTargetA.begin();
        Gdx.gl.glClearColor(1f, 1f, 1f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(tex, PAD, PAD);
        batch.end();
        batch.flush();
        blurTargetA.end();

        batch.setShader(shader);

        // setup edgedetection
        shader.begin();
        shader.setUniformi("mode", 1);
        shader.setUniformf("radius", 1.0f);
        shader.end();

        // draw from FBO A to FBO B
        blurTargetB.begin();
        Gdx.gl.glClearColor(1f, 1f, 1f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(fboRegionA,0,0);
        batch.end();
        batch.flush();
        blurTargetB.end();

        // setup H-blur / 2D blur
        shader.begin();
        shader.setUniformi("mode", 0); // turn on blur
        shader.setUniformf("radius", 1.0f);
        shader.end();

        // draw from FBO B to FBO A
        blurTargetA.begin();
        Gdx.gl.glClearColor(1f, 1f, 1f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(fboRegionB,0,0);
        batch.end();
        batch.flush();
        blurTargetA.end();

        // use the default shader
        batch.setShader(null);

        return fboRegionA;
//        return fboRegionB;
    }
}
