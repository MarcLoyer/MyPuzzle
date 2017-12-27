package com.oduratereptile.game.desktop;

import com.badlogic.gdx.Gdx;
import com.oduratereptile.game.GalleryOpener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Created by Marc on 11/19/2017.
 */

public class DesktopGalleryOpener implements GalleryOpener {
    public JFileChooser fileChooser;
    public File file = null;
    public FileDescriptor fd = null;

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
        JFrame f = new JFrame();
        f.setVisible(true);
        f.toFront();
        f.setVisible(false);
        int returnValue = fileChooser.showOpenDialog(null);
        f.dispose();

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            try {
                FileInputStream fis = new FileInputStream(file);
                fd = fis.getFD();
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (GalleryListener gl: listeners) {
                gl.gallerySelection(fd);
            }
        }
    }

    @Override
    public String getSelectedFilePath() {
        return file.getName();
    }

    @Override
    public FileDescriptor getFileDescriptor() {
        return fd;
    }

    @Override
    public boolean resultIsReady() {
        return (file != null);
    }

    @Override
    public void addListener(GalleryListener listener) {
        listeners.add(listener);
    }
}
