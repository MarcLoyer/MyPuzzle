package com.oduratereptile.game;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Marc on 10/21/2017.
 */

public abstract class FloodFill {
    private ArrayList<ScanlineSegment> floodStack;
    private HashMap<Integer, ArrayList<ScanlineSegment>> doneSegments;

    public FloodFill() {
        floodStack = new ArrayList<ScanlineSegment>();
        doneSegments = new HashMap<Integer, ArrayList<ScanlineSegment>>();
    }

    public void reset() {
        floodStack.clear();
        doneSegments.clear();
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
            setColor(curr);
            getNeighborScanlines(curr, curr.y+1);
            getNeighborScanlines(curr, curr.y-1);
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
        // TODO: add maxX/minX checks

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
            while (needsToChange(x1, y)) x1--;
            x1++;
        }

        while (x1<=curr.x2) {
            // Expand x2 to the end of the segment
            while (needsToChange(x2, y) == needsChanged) {
                x2++;
                if ((!needsChanged) && (x2 >= curr.x2)) break;
            }
            x2--;

            // Add the segment to the appropriate list
            if (needsChanged) {
                floodStack.add(new ScanlineSegment(y, x1, x2));
            } else {
                addToDone(new ScanlineSegment(y, x1, x2));
            }

            // Start building the next segment
            x1 = getNextX(x2 + 1, y);
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

    public abstract int minX();
    public abstract int maxX();
    public abstract int minY();
    public abstract int maxY();
    public abstract boolean needsToChange(int x, int y) ;
    public abstract void setColor(ScanlineSegment seg) ;

    class ScanlineSegment {
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
