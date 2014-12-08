package com.ipvans.mailtest.tile;

import android.util.Log;
import android.util.Pair;

public final class ViewState {

    public final int tileWidth;
    public final int tilesHoriz, tilesVert;
    public final int surfaceW, surfaceH;
    private final Integer[] tileIDLimits;

    private int surfaceOffsetX, surfaceOffsetY;
    private int canvasOffsetX, canvasOffsetY;
    private TileRange visibleTileIdRange;

    private Snapshot snapshot;

    class Snapshot {
        public int surfaceOffsetX, surfaceOffsetY, canvasOffsetX, canvasOffsetY;
        public TileRange visibleTileIdRange;
    }

    public ViewState(int surfaceW, int surfaceH, int tileWidth, Integer[] tileLimits) {

        this.surfaceW = surfaceW;
        this.surfaceH = surfaceH;
        this.tileWidth = tileWidth;

        if (tileLimits != null && tileLimits.length != 4) {
            Log.w(TileView.LOG_TAG, "Incorrect tile limits");
            tileLimits = null;
        }
        this.tileIDLimits = tileLimits;

        tilesHoriz = calculateNumTiles(surfaceW);
        tilesVert = calculateNumTiles(surfaceH);

    }

    public synchronized Snapshot getSnapshot() {

        if (snapshot == null) {
            snapshot = new Snapshot();
        }
        snapshot.visibleTileIdRange = this.visibleTileIdRange;
        snapshot.surfaceOffsetX = this.surfaceOffsetX;
        snapshot.surfaceOffsetY = this.surfaceOffsetY;
        snapshot.canvasOffsetX = this.canvasOffsetX;
        snapshot.canvasOffsetY = this.canvasOffsetY;

        return snapshot;
    }

    public synchronized TileRange getVisibleTileRange() {
        return visibleTileIdRange;
    }

    public synchronized boolean applySurfaceOffsetRelative(int relOffsetX, int relOffsetY) {
        return applySurfaceOffset(surfaceOffsetX + relOffsetX, surfaceOffsetY + relOffsetY);
    }

    public synchronized boolean applySurfaceOffset(int offsetX, int offsetY) {

        Pair<Integer, Integer> rangeH = calculateTileRange(offsetX, tilesHoriz);
        Pair<Integer, Integer> rangeV = calculateTileRange(offsetY, tilesVert);

        if (tileIDLimits != null && visibleTileIdRange != null) {

            if ((tileIDLimits[0] != null && rangeH.first < tileIDLimits[0])  // left
                    || (tileIDLimits[2] != null && rangeH.second > tileIDLimits[2])) {  // right
                rangeH = new Pair<Integer, Integer>(visibleTileIdRange.left, visibleTileIdRange.right);
                offsetX = surfaceOffsetX;
            }

            if ((tileIDLimits[1] != null && rangeV.first < tileIDLimits[1]) // top
                    || (tileIDLimits[3] != null && rangeV.second > tileIDLimits[3])) { // bottom
                rangeV = new Pair<Integer, Integer>(visibleTileIdRange.top, visibleTileIdRange.bottom);
                offsetY = surfaceOffsetY;
            }

        }

        TileRange newRange = new TileRange(rangeH.first, rangeV.first, rangeH.second, rangeV.second);
        boolean rangeHasChanged = (visibleTileIdRange == null || !newRange.equals(visibleTileIdRange));
        visibleTileIdRange = newRange;

        surfaceOffsetX = offsetX;
        surfaceOffsetY = offsetY;

        canvasOffsetX = surfaceOffsetX % tileWidth;
        canvasOffsetY = surfaceOffsetY % tileWidth;

        if (canvasOffsetX > 0) {
            canvasOffsetX -= tileWidth;
        }
        if (canvasOffsetY > 0) {
            canvasOffsetY -= tileWidth;
        }

        return rangeHasChanged;

    }

    private Pair<Integer, Integer> calculateTileRange(int coordPx, int numGridTiles) {
        int startTileId = -(coordPx / tileWidth);
        if (coordPx % tileWidth > 0) {
            startTileId--;
        }
        int endTileId = startTileId + numGridTiles - 1;

        return new Pair<Integer, Integer>(startTileId, endTileId);
    }

    private int calculateNumTiles(int availablePx) {
        int num = (availablePx / tileWidth) + 1;
        num += (availablePx % tileWidth == 0 ? 0 : 1);
        return num;
    }

}
