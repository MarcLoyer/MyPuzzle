package com.oduratereptile.game.desktop;

import com.badlogic.gdx.Gdx;
import com.oduratereptile.game.GalleryOpener;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JFileChooser;

/**
 * Created by Marc on 11/19/2017.
 */

public class DesktopGalleryOpener implements GalleryOpener {
    public JFileChooser fileChooser;
    public File file = null;
    public FileDescriptor fd = null;

    public DesktopGalleryOpener() {
        fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new ImageFilter());
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setAccessory(new ImagePreview(fileChooser));
        fileChooser.setCurrentDirectory(new File("C:\\Users\\Marc\\Pictures"));
    }

    @Override
    public void openGallery() {
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            try {
                FileInputStream fis = new FileInputStream(file);
                fd = fis.getFD();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getSelectedFilePath() {
        return file.getName();
    }

    @Override
    public FileDescriptor getFileDescriptor() {
        Gdx.app.log("DesktopGalleryOpener", "Not implemented");
        return null;
    }

    @Override
    public boolean resultIsReady() {
        return (file != null);
    }
}
