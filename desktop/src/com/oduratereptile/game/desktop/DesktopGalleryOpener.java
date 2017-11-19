package com.oduratereptile.game.desktop;

import com.badlogic.gdx.Gdx;
import com.oduratereptile.game.GalleryOpener;

/**
 * Created by Marc on 11/19/2017.
 */

public class DesktopGalleryOpener implements GalleryOpener {
    @Override
    public void openGallery() {
        Gdx.app.log("DesktopGalleryOpener", "Not implemented");
    }

    @Override
    public String getSelectedFilePath() {
        Gdx.app.log("DesktopGalleryOpener", "Not implemented");
        return null;
    }

    @Override
    public boolean resultIsReady() {
        Gdx.app.log("DesktopGalleryOpener", "Not implemented");
        return false;
    }
}
