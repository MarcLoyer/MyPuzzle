package com.oduratereptile.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Marc on 10/22/2017.
 */

public class DebugFill implements Runnable {
    public ArrayList<ScanlineSegment> floodStack;
    public HashMap<Integer, ArrayList<ScanlineSegment>> doneSegments;

    public Pixmap puzzleImg;
    public Pixmap mask;
    public PuzzlePieceCoords coords;
    public boolean includeBorder = false;

    private int minX, maxX, minY, maxY;

    public DebugFill() {
        floodStack = new ArrayList<ScanlineSegment>();
        doneSegments = new HashMap<Integer, ArrayList<ScanlineSegment>>();
    }

    public DebugFill(Pixmap puzzleImg, Pixmap mask, PuzzlePieceCoords coords) {
        floodStack = new ArrayList<ScanlineSegment>();
        doneSegments = new HashMap<Integer, ArrayList<ScanlineSegment>>();
        initialize(puzzleImg, mask, coords);
    }

    public DebugFill(Pixmap puzzleImg, Pixmap mask, PuzzlePieceCoords coords, boolean includeBorder) {
        floodStack = new ArrayList<ScanlineSegment>();
        doneSegments = new HashMap<Integer, ArrayList<ScanlineSegment>>();
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

    public void reset() {
        floodStack.clear();
        doneSegments.clear();
    }

    @Override
    public void run() {
        fill((int)coords.mid.x, (int)coords.mid.y);
    }

    public boolean isWaiting = false;
    public String status;

    public synchronized void waitForStep(String s) {
        status = s;
        isWaiting = true;
        try {
            while (isWaiting) wait();
        } catch (InterruptedException e) {
            Gdx.app.error("exception", "waitForStep() failed to wait.");
        }
    }

    public synchronized  void step() {
        isWaiting = false;
        notify();
    }

    public void fill(int x, int y) {
        reset();
        ScanlineSegment curr = new ScanlineSegment(y,x,x);
        // expand the current segment until we hit the ends
        while (needsToChange(curr.x1, curr.y)) { curr.x1--; }
        curr.x1++;
        while (needsToChange(curr.x2, curr.y)) { curr.x2++; }
        curr.x2--;
        push(x,y);

        while ((curr = pop()) != null) {
waitForStep("setColor(curr)");
            setColor(curr);
            if ((y+1)<=maxY()) getNeighborScanlines(curr, curr.y+1);
            if ((y-1)>=minY()) getNeighborScanlines(curr, curr.y-1);
waitForStep("addToDone(curr)");
            addToDone(curr);
        }
    }

    private void addToDone(ScanlineSegment curr) {
        ArrayList<ScanlineSegment> segArray = doneSegments.get(curr.y);

        // if this is the first segment in this scanline, create a new hash entry
        if (segArray == null) {
            segArray = new ArrayList<ScanlineSegment>();
            doneSegments.put(curr.y, segArray);
            segArray.add(curr);
            return;
        }

        // check if we can merge the current segment with existing segments
        boolean isMerged = false;
        ScanlineSegment merged = null;
        for (ScanlineSegment seg: segArray) {
            if ((curr.x1-1)==seg.x2) {
                if (isMerged) {
                    merged.x1 = seg.x1;
                    segArray.remove(seg);
                    return;
                } else {
                    seg.x2 = curr.x2;
                    isMerged = true;
                    merged = seg;
                }
            }

            if ((curr.x2+1)==seg.x1) {
                if (isMerged) {
                    merged.x2 = seg.x2;
                    segArray.remove(seg);
                    return;
                } else {
                    seg.x1 = curr.x1;
                    isMerged = true;
                    merged = seg;
                }
            }
        }
        if (isMerged) return;

        // just add it to the entry
        segArray.add(curr);
    }

    private void getNeighborScanlines(ScanlineSegment curr, int y) {
        // Check if the lower end of the scanline is already in the done list. If so, we
        // get the lowest x1 that hasn't been checked yet.
        int x1 = getNextX(curr.x1, y);
        int x2 = x1 + 1;

        // We start building a new segment, either for the floodStack or the done list
        boolean needsChanged = needsToChange(x1, y);

        // On this first one, we need to expand the low end past curr.x1 (but only if
        // this segment goes in the floodStack and if the low end wasn't already in the
        // done list)
        if ((x1 == curr.x1) && (needsChanged)) {
            x1--;
            while (needsToChange(x1, y)) {
                x1--;
                if (x1<minX()) break;
            }
            x1++;
        }

        while (x1<=curr.x2) {
            // Expand x2 to the end of the segment
            while (needsToChange(x2, y) == needsChanged) {
                x2++;
                if (x2>maxX()) break;
                if ((!needsChanged) && (x2 >= curr.x2)) break;
            }
            x2--;

            // Add the segment to the appropriate list
waitForStep("getNeighborScanlines()");
            if (needsChanged) {
                floodStack.add(new ScanlineSegment(y, x1, x2));
            } else {
                addToDone(new ScanlineSegment(y, x1, x2));
            }

            // Start building the next segment
            x1 = getNextX(x2 + 1, y);
            if (x1>maxX()) break;
            needsChanged = needsToChange(x1, y);
            x2 = x1 + 1;
        }
    }

    private int getNextX(int x, int y) {
        int rv = x;
        ArrayList<ScanlineSegment> scanline = doneSegments.get(y);
        if (scanline == null) return rv;

        boolean isAlreadyInScanline = true;

        while (isAlreadyInScanline) {
            isAlreadyInScanline = false;
            for (ScanlineSegment seg : scanline) {
                if ((rv >= seg.x1) && (rv <= seg.x2)) {
                    rv = seg.x2 + 1;
                    isAlreadyInScanline = true;
                    break;
                }
            }
        }
        return rv;
    }

    private boolean push(int x, int y) {
        floodStack.add(new ScanlineSegment(y, x, x));
        return true;
    }

    private boolean push(ScanlineSegment seg) {
        floodStack.add(seg);
        return true;
    }

    private ScanlineSegment pop() {
        if (floodStack.isEmpty()) return null;
        return floodStack.remove(floodStack.size()-1);
    }

    public int minX() {
        return minX;
    }

    public int maxX() {
        return maxX;
    }

    public int minY() {
        return minY;
    }

    public int maxY() {
        return maxY;
    }

    public boolean needsToChange(int x, int y) {
        if ((x<minX)||(x>maxX)||(y<minY)||(y>maxY)) return false;
        if ((puzzleImg.getPixel(x, y) & 0x000000FF) == 0x000000FF) return false;

        // check if the pixel is on the mask line
        if (mask.getPixel(x, y) != 0) {
            if (includeBorder) setColor(x, y);
            return false;
        }
        return true;
    }

    public void setColor(ScanlineSegment seg) {
        for (int x=seg.x1; x<=seg.x2; x++) {
            setColor(x, seg.y);
        }
    }

    public void setColor(int x, int y) {
        puzzleImg.drawPixel(x, y, puzzleImg.getPixel(x, y) | 0x000000FF);
    }

    public class ScanlineSegment {
        int y;
        int x1;
        int x2;

        public ScanlineSegment() {
        }

        public ScanlineSegment(int y, int x1, int x2) {
            this.y = y;
            this.x1 = x1;
            this.x2 = x2;
        }
    }
}
