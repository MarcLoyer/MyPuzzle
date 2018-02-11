package com.oduratereptile.game.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.utils.StreamUtils;
import com.oduratereptile.game.GalleryOpener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Created by Marc on 11/19/2017.
 */

public class DesktopGalleryOpener implements GalleryOpener {
    public JFileChooser fileChooser;
    public File file = null;
    public boolean isReady = false;
    public Pixmap pixmap;

    private ArrayList<GalleryListener> listeners;

    public DesktopGalleryOpener() {
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setAccessory(new ImagePreview(fileChooser));
        fileChooser.setCurrentDirectory(new File("C:\\Users\\Marc\\Pictures"));
//        fileChooser.requestFocus();

        listeners = new ArrayList<GalleryListener>();
    }

    @Override
    public void openGallery() {
        isReady = false;
        JFrame f = new JFrame();
        f.setVisible(true);
        f.toFront();
        f.setVisible(false);
        int returnValue = fileChooser.showOpenDialog(null);
        f.dispose();

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            try {
                openImage(new FileInputStream(file));

            } catch (IOException e) {
                e.printStackTrace();
            }

            for (GalleryListener gl: listeners) {
                gl.gallerySelection(this);
            }
        }
    }

    @Override
    public String getSelectedFilePath() {
        return file.getName();
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
