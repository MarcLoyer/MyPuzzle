package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.*;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by Marc on 1/27/2018.
 */

public class MyPixmapPacker extends PixmapPacker {
    public MyPixmapPacker(int pageWidth, int pageHeight, Pixmap.Format pageFormat, int padding, boolean duplicateBorder) {
        super(pageWidth, pageHeight, pageFormat, padding, duplicateBorder);
    }

    public MyPixmapPacker(int pageWidth, int pageHeight, Pixmap.Format pageFormat, int padding, boolean duplicateBorder, PackStrategy packStrategy) {
        super(pageWidth, pageHeight, pageFormat, padding, duplicateBorder, packStrategy);
    }

    @Override
    public Rectangle pack(String name, Pixmap pix) {
        if (Thread.currentThread().getName().startsWith("GLThread")) {
            packRenderThread(name, pix);
            return rect;
        }
        packNonrenderThread(name, pix);
        return rect;
    }

    private Rectangle rect = null;

    private synchronized void packNonrenderThread(final String name, final Pixmap pix) {
        rect = null;
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                packRenderThread(name, pix);
            }
        });

        try {
            while (rect==null) wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void packRenderThread(String name, Pixmap pix) {
        rect = super.pack(name, pix);
        notifyAll();
    }

    @Override
    public TextureAtlas generateTextureAtlas(TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        if (ta != null) return ta;
        if (Thread.currentThread().getName().startsWith("GLThread")) {
            generateTextureAtlasRenderThread(minFilter, magFilter, useMipMaps);
        } else {
            generateTextureAtlasNonrenderThread(minFilter, magFilter, useMipMaps);
        }
        return ta;
    }

    private TextureAtlas ta = null;

    private synchronized void generateTextureAtlasNonrenderThread(
            final TextureFilter minFilter, final TextureFilter magFilter, final boolean useMipMaps) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                generateTextureAtlasRenderThread(minFilter, magFilter, useMipMaps);
            }
        });

        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void generateTextureAtlasRenderThread(
            TextureFilter minFilter, TextureFilter magFilter, boolean useMipMaps) {
        ta = super.generateTextureAtlas(minFilter, magFilter, useMipMaps);
        notifyAll();
    }
}
