package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Created by Marc on 10/20/2017.
 */

public class PuzzlePieceCoords {
    public float imageWidth;
    public float imageHeight;
    public int numRows;
    public int numCols;

    public int row;
    public int col;

    public Vector2 pos;
    public Vector2 mid;
    public Vector2 size;

    public BoundingBox boundingBox;

    public GridPoint2 bbLL;
    public GridPoint2 bbUR;

    PuzzlePieceCoords(int i, int j, Pixmap img, int numRows, int numCols) {
        imageWidth = img.getWidth();
        imageHeight = img.getHeight();
        this.numRows = numRows;
        this.numCols = numCols;

        row = i;
        col = j;

        pos = new Vector2(colOffset(j), rowOffset(i));
        size = new Vector2(colSpacing(j), rowSpacing(i));
        mid = new Vector2(colSpacing(j)/2, rowSpacing(i)/2);
        if (i != 0) {
            pos.y -= (int)(rowSpacing(i)/2); // Note: we really do want integer math here!
            size.y += (int)(rowSpacing(i)/2);
            mid.y += (int)(rowSpacing(i)/2);
        }
        if (j != 0) {
            pos.x -= (int)(colSpacing(j)/2); // here too!
            size.x += (int)(colSpacing(j)/2);
            mid.x += (int)(colSpacing(j)/2);
        }
        if (i != (numRows-1)) {
            size.y += (int)(rowSpacing(i)/2); // and here
        }
        if (j != (numCols-1)) {
            size.x += (int)(colSpacing(j)/2); // and here
        }
        mid.y = size.y - mid.y; // y-axis down
    }

    // Corrects for y-axis-down
    public Vector2 getMid() {
        Vector2 rv = new Vector2(mid);
        rv.y = size.y - rv.y;
        return rv;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("("+row+","+col+"):");
        if (pos != null) sb.append(" pos="+pos.toString());
        if (mid != null) sb.append(" mid="+mid.toString());
        if (size != null) sb.append(" size="+size.toString());
        return sb.toString();
    }

    public float rowSpacing(int row) {
        int minSpacing = (int)imageHeight / numRows;
        int remainder = (int)imageHeight % numRows;
        if (row < remainder) return minSpacing + 1;
        return minSpacing;
    }

    public float rowOffset(int row) {
        int minSpacing = (int)imageHeight / numRows;
        int remainder = (int)imageHeight % numRows;
        if (row < remainder) return (minSpacing + 1) * row;
        return ((minSpacing + 1) * remainder) + (minSpacing * (row - remainder));
    }

    public float colSpacing(int col) {
        int minSpacing = (int)imageWidth/ numCols;
        int remainder = (int)imageWidth % numCols;
        if (col < remainder) return minSpacing + 1;
        return minSpacing;
    }

    public float colOffset(int col) {
        int minSpacing = (int)imageWidth/ numCols;
        int remainder = (int)imageWidth % numCols;
        if (col < remainder) return (minSpacing + 1) * col;
        return ((minSpacing + 1) * remainder) + (minSpacing * (col - remainder));
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
