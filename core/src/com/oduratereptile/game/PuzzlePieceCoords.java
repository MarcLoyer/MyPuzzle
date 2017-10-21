package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Marc on 10/20/2017.
 */

public class PuzzlePieceCoords {
    public float imageWidth;
    public float imageHeight;
    public int numRows;
    public int numCols;
    public float colSpacing;
    public float rowSpacing;

    public int row;
    public int col;

    public Vector2 pos;
    public Vector2 mid;
    public Vector2 size;

    public GridPoint2 bbLL;
    public GridPoint2 bbUR;

    PuzzlePieceCoords(int i, int j, Pixmap img, int numRows, int numCols) {
        imageWidth = img.getWidth();
        imageHeight = img.getHeight();
        this.numRows = numRows;
        this.numCols = numCols;
        colSpacing = imageWidth/numCols;
        rowSpacing = imageHeight/numRows;

        row = i;
        col = j;

        pos = new Vector2((float)j*colSpacing, (float)i*rowSpacing);
        size = new Vector2(colSpacing, rowSpacing);
        mid = new Vector2(colSpacing/2.0f, rowSpacing/2.0f);
        if (i != 0) {
            pos.y -= rowSpacing/2.0f;
            size.y += rowSpacing/2.0f;
            mid.y += rowSpacing/2.0f;
        }
        if (j != 0) {
            pos.x -= colSpacing/2.0f;
            size.x += colSpacing/2.0f;
            mid.x += colSpacing/2.0f;
        }
        if (i != (numRows-1)) {
            size.y += rowSpacing/2.0f;
        }
        if (j != (numCols-1)) {
            size.x += colSpacing/2.0f;
        }
        mid.y = size.y - mid.y;
    }

    /**
     * Determines the bounds of a rectangle extirely inside the puzzlePiece
     */
    public void setInnerRect(Vector2 [][] colControlPoints, Vector2 [][] rowControlPoints, int pointsPerPiece) {
        // TODO: add edge exceptions
        float maxX, minX, maxY, minY;

        if (col==0)
            maxX = 0;
        else
            maxX = getMaxX(colControlPoints[col-1], row*pointsPerPiece+1, pointsPerPiece+1);

        if (col==(numCols-1))
            minX = imageWidth;
        else
            minX = getMinX(colControlPoints[col], row*pointsPerPiece+1, pointsPerPiece+1);

        if (row==0)
            maxY = 0;
        else
            maxY = getMaxY(rowControlPoints[row-1], col*pointsPerPiece+1, pointsPerPiece+1);

        if (row==(numRows-1))
            minY = imageHeight;
        else
            minY = getMinY(rowControlPoints[row], col*pointsPerPiece+1, pointsPerPiece+1);
//Gdx.app.error("debug",
//        "setInnerRect(): (row,col) = (" + row + ", " + col + ")" +
//        "  max = (" + maxX + ", " + maxY + ")" +
//        "  min = (" + minX + ", " + minY + ")");

        maxX -= pos.x;
        minX -= pos.x;
        maxY -= pos.y;
        minY -= pos.y;

        // TODO: this isn't sufficiently conservative - I've seen escapes.
        bbLL = new GridPoint2((int)(maxX+2.5f), (int)(maxY+2.5f));
        bbUR = new GridPoint2((int)(minX-1.5f), (int)(minY-1.5f));
    }

    private float getMaxX(Vector2 [] a, int index, int length) {
        float rv = a[index].x;
        for (int i=1; i<length; i++) if (a[index+i].x>rv) rv = a[index+i].x;
        return rv;
    }

    private float getMinX(Vector2 [] a, int index, int length) {
        float rv = a[index].x;
        for (int i=1; i<length; i++) if (a[index+i].x<rv) rv = a[index+i].x;
        return rv;
    }
    private float getMaxY(Vector2 [] a, int index, int length) {
        float rv = a[index].y;
        for (int i=1; i<length; i++) if (a[index+i].y>rv) rv = a[index+i].y;
        return rv;
    }

    private float getMinY(Vector2 [] a, int index, int length) {
        float rv = a[index].y;
        for (int i=1; i<length; i++) if (a[index+i].y<rv) rv = a[index+i].y;
        return rv;
    }
}
