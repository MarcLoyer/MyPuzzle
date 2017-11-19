package com.oduratereptile.game;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by Marc on 11/19/2017.
 */

public class AndroidGalleryOpener implements GalleryOpener {
    public static final int SELECT_PICTURE = 1;
    public Activity activity;
    public String currentImagePath;
    public boolean isReady;

    public AndroidGalleryOpener(Activity activity) {
        this.activity = activity;
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

    public String getSelectedFilePath(){
        return currentImagePath;
    }

    public boolean resultIsReady() {
        return isReady;
    }
}
