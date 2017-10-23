package com.oduratereptile.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.GridPoint2;

/**
 * Created by Marc on 10/22/2017.
 */

public class PuzzleFill extends FloodFill {
    public Pixmap puzzleImg;
    public Pixmap mask;
    public PuzzlePieceCoords coords;
    public boolean includeBorder = false;

    private int minX, maxX, minY, maxY;

    public PuzzleFill() {}

    public PuzzleFill(Pixmap puzzleImg, Pixmap mask, PuzzlePieceCoords coords) {
        initialize(puzzleImg, mask, coords);
    }

    public PuzzleFill(Pixmap puzzleImg, Pixmap mask, PuzzlePieceCoords coords, boolean includeBorder) {
        initialize(puzzleImg, mask, coords, includeBorder);
    }

    public void initialize(Pixmap puzzleImg, Pixmap mask, PuzzlePieceCoords coords) {
        initialize(puzzleImg, mask, coords, false);
    }

    public void initialize(Pixmap puzzleImg, Pixmap mask, PuzzlePieceCoords coords, boolean includeBorder) {
        this.puzzleImg = puzzleImg;
        this.mask = mask;
        this.coords = coords;
        this.includeBorder = includeBorder;

        minX = 0;
        maxX = puzzleImg.getWidth()-1;
        minY = 0;
        maxY = puzzleImg.getHeight()-1;
    }

    @Override
    public int minX() {
        return minX;
    }

    @Override
    public int maxX() {
        return maxX;
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int maxY() {
        return maxY;
    }

    @Override
    public boolean needsToChange(int x, int y) {
        if ((x<minX)||(x>maxX)||(y<minY)||(y>maxY)) return false;
        if ((puzzleImg.getPixel(x, y) & 0x000000FF) == 0x000000FF) return false;

        // check if the pixel is on the mask line
        GridPoint2 p = new GridPoint2((int)coords.pos.x, mask.getHeight() - (int)coords.pos.y - puzzleImg.getHeight());
        p.add(x, y);
        if (mask.getPixel(p.x, p.y) != 0) {
            if (includeBorder) setColor(x, y);
            return false;
        }
        return true;
    }

    @Override
    public void setColor(FloodFill.ScanlineSegment seg) {
        for (int x=seg.x1; x<=seg.x2; x++) {
            setColor(x, seg.y);
        }
    }

    public void setColor(int x, int y) {
        puzzleImg.drawPixel(x, y, puzzleImg.getPixel(x, y) | 0x000000FF);
    }
}
