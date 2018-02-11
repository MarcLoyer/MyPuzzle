package com.oduratereptile.game;

import android.app.Activity;
import android.content.Intent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.utils.StreamUtils;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Marc on 11/19/2017.
 */

public class AndroidGalleryOpener implements GalleryOpener {
    public static final int SELECT_PICTURE = 1;
    public Activity activity;
    public String currentImagePath;
    public boolean isReady;
    public Pixmap pixmap = null;

    private ArrayList<GalleryListener> listeners;

    public AndroidGalleryOpener(Activity activity) {
        this.activity = activity;
        listeners = new ArrayList<GalleryListener>();
    }

    @Override
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_PICTURE);
        isReady = false;
    }

    public void setImageResult(String path){
        isReady = true;
        currentImagePath = path;
    }

    @Override
    public String getSelectedFilePath(){
        return currentImagePath;
    }

    public void openImage(InputStream stream) {
        byte [] bytes = new byte[0];
        try {
            bytes = StreamUtils.copyStreamToByteArray(stream);
            pixmap = new Pixmap(new Gdx2DPixmap(bytes, 0, bytes.length, 0));
            stream.close();
        } catch (IOException e) {
            Gdx.app.error("loadPixmap", "Failed to read image file");
            e.printStackTrace();
        }

        isReady = true;
        for (GalleryListener gl: listeners) {
            gl.gallerySelection(this);
        }
    }

    @Override
    public Pixmap getPixmap() { return pixmap; }

    @Override
    public boolean resultIsReady() {
        return isReady;
    }

    @Override
    public void addListener(GalleryListener listener) {
        listeners.add(listener);
    }
}
