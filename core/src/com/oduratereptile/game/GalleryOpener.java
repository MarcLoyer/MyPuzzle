package com.oduratereptile.game;

import java.io.FileDescriptor;

/**
 * Created by Marc on 11/19/2017.
 */

public interface GalleryOpener {
    public void openGallery();

    public String getSelectedFilePath();

    public FileDescriptor getFileDescriptor();

    public boolean resultIsReady();
}
