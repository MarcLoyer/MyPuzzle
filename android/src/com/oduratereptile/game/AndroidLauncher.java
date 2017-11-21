package com.oduratereptile.game;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.oduratereptile.game.MyPuzzle;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

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
            ParcelFileDescriptor pfd;
            try {
                pfd = getContentResolver().openFileDescriptor(imageUri, "r");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("AndroidGalleryOpener", "File not found.");
                return;
            }
            FileDescriptor fd = pfd.getFileDescriptor();
            galleryOpener.setFileDescriptor(fd);

//            userImagePath = getPath(imageUri);
//            if(userImagePath!=null){
//                // IF LOCAL IMAGE, NO MATTER IF ITS DIRECTLY FROM GALLERY (EXCEPT PICASSA ALBUM),
//                // OR OI/ASTRO FILE MANAGER. EVEN DROPBOX IS SUPPORTED BY THIS BECAUSE DROPBOX DOWNLOAD THE IMAGE
//                // IN THIS FORM - file:///storage/emulated/0/Android/data/com.dropbox.android/...
//                Gdx.app.error("AndroidGalleryOpener", "local image");
//            }
//            else{
//                Gdx.app.error("AndroidGalleryOpener", "picasa image!");
////                loadPicasaImageFromGallery(imageUri);
//            }
//
//            Gdx.app.error("AndroidGalleryOpener", "Image path is " + userImagePath + " (" + imageUri + ")");
//            galleryOpener.setImageResult(userImagePath);
        }
    }

    public String getPath(Uri uri) {
        String filePath;
        String[] projection = {  MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null) {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        } else {
            filePath = uri.getPath();               // FOR OI/ASTRO/Dropbox etc
        }

        return filePath;
    }

    // NEW METHOD FOR PICASA IMAGE LOAD
    private void loadPicasaImageFromGallery(final Uri uri) {
        String[] projection = {  MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null) {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            if (columnIndex != -1) {
                new Thread(new Runnable() {
                    // NEW THREAD BECAUSE NETWORK REQUEST WILL BE MADE THAT WILL BE A LONG PROCESS & BLOCK UI
                    // IF CALLED IN UI THREAD
                    public void run() {
                        try {
                            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            // THIS IS THE BITMAP IMAGE WE ARE LOOKING FOR.
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        cursor.close();
    }

}
