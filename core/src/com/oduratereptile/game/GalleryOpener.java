package com.oduratereptile.game;

import com.badlogic.gdx.graphics.Pixmap;

import java.io.FileDescriptor;

/**
 * Created by Marc on 11/19/2017.
 */

public interface GalleryOpener {
    public void openGallery();

    public String getSelectedFilePath();

    public Pixmap getPixmap();

    public boolean resultIsReady();

    public void addListener(GalleryListener listener);

    public interface GalleryListener {
        public void gallerySelection(GalleryOpener galleryOpener);
    }
}
