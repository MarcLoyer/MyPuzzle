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

public class OutlineShader extends ShaderProgram {
    public static final int PAD = 10;

    OrthographicCamera camera;
    TextureRegion tex;

    public OutlineShader() {
        super(
            Gdx.files.internal("shaders/outline.vert"),
            Gdx.files.internal("shaders/outline.frag")
        );
        //Good idea to log any warnings if they exist
        if (getLog().length()!=0)
            Gdx.app.error("debug", getLog());

        camera = new OrthographicCamera();
    }

    public int bufferWidth;
    public int bufferHeight;
    public FrameBuffer blurTargetA, blurTargetB;
    public TextureRegion fboRegionA, fboRegionB;

    public void setup(TextureRegion tex) {
        this.tex = tex;
        bufferWidth = tex.getRegionWidth() + 2*PAD;
        bufferHeight = tex.getRegionHeight() + 2*PAD;

        if (blurTargetA != null) blurTargetA.dispose();
        if (blurTargetB != null) blurTargetB.dispose();

        begin();
        setUniformf("dir", 0f, 0f);
        setUniformf("width", bufferWidth);
        setUniformf("height", bufferHeight);
        setUniformf("radius", 1f);
        setUniformf("color", new Vector3(1.0f, 1.0f, 1.0f));
        end();

        blurTargetA = new FrameBuffer(RGBA8888, bufferWidth, bufferHeight, false);
        blurTargetB = new FrameBuffer(RGBA8888, bufferWidth, bufferHeight, false);
        fboRegionA = new TextureRegion(blurTargetA.getColorBufferTexture(), bufferWidth, bufferHeight);
        fboRegionA.flip(false, true);
        fboRegionB = new TextureRegion(blurTargetB.getColorBufferTexture(), bufferWidth, bufferHeight);
        fboRegionB.flip(false, true);
    }

    public TextureRegion renderToTexture(SpriteBatch batch) {
        camera.setToOrtho(false, bufferWidth, bufferHeight);
        batch.setProjectionMatrix(camera.combined);

        // draw from tex to FBO A
        blurTargetA.begin();
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(tex, PAD, PAD);
        batch.end();
        batch.flush();
        blurTargetA.end();

        batch.setShader(this);

        // setup edgedetection
        begin();
        setUniformi("edgedetect", 1);
        setUniformf("radius", 2f);
        end();

        // draw from FBO A to FBO B
        blurTargetB.begin();
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(fboRegionA,0,0);
        batch.end();
        batch.flush();
        blurTargetB.end();

        // setup H-blur / 2D blur
        begin();
        setUniformi("edgedetect", 0); // turn off edge detection
//        setUniformf("dir", 0f, 1f);
        setUniformf("dir", 1f, 1f);
        setUniformf("radius", 1.1f);
        end();

        // draw from FBO B to FBO A
        blurTargetA.begin();
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(fboRegionB,0,0);
        batch.end();
        batch.flush();
        blurTargetA.end();

//        // setup V-blur
//        begin();
//        setUniformf("dir", 1f, 0f);
//        setUniformi("edgedetect", 0); // turn off edge detection
//        setUniformf("radius", 0.6f);
//        end();
//
//        // draw from FBO A to FBO B
//        blurTargetB.begin();
//        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 0.0f);
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
//        batch.begin();
//        batch.draw(fboRegionA,0,0);
//        batch.end();
//        batch.flush();
//        blurTargetB.end();

        // use the default shader
        batch.setShader(null);

        return fboRegionA;
//        return fboRegionB;
    }

    public void dispose() {
        blurTargetA.dispose();
        blurTargetB.dispose();
        super.dispose();
    }
}
