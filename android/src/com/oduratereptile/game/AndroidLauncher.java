package com.oduratereptile.game;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.oduratereptile.game.MyPuzzle;

public class AndroidLauncher extends AndroidApplication {
    String userImagePath = null;
    AndroidGalleryOpener galleryOpener;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        galleryOpener = new AndroidGalleryOpener(this);
        initialize(new MyPuzzle(galleryOpener), config);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK && requestCode == AndroidGalleryOpener.SELECT_PICTURE) {
            Uri imageUri = data.getData();
            this.userImagePath = getPath(imageUri);
            Gdx.app.error("AndroidGalleryOpener", "Image path is " + userImagePath + " (" + imageUri + ")");
            galleryOpener.setImageResult(userImagePath);
        }
    }

    private String getPath(Uri uri) {
        if(uri.getScheme().equalsIgnoreCase("file")) {
            return uri.getPath();
        }
        Cursor cursor = getContentResolver().query(uri, new String[] { MediaStore.Images.Media.DATA } , null, null, null);

        if (cursor == null) {
            return null;
        }

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        String filePath = cursor.getString(column_index);

        cursor.close();

        return filePath;
    }
}
