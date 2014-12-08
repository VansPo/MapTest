package com.ipvans.mailtest.tile;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Tile {

  public static final int DEFAULT_TILE_SIZE = 256;

  public final int size;
  public final int xId;
  public final int yId;

  public final long cacheKey;
  private Bitmap bmpData;

  public Tile(int xId, int yId) {
    this(xId, yId, DEFAULT_TILE_SIZE);
  }

  public Tile(int xId, int yId, int size) {

    this.xId = xId;
    this.yId = yId;
    this.size = size;
    this.cacheKey = createCacheKey(xId, yId);
  }

  public Rect getRect(int left, int top) {
    return new Rect(left, top, left + size, top + size);
  }

  public static long createCacheKey(int x, int y) {

    return (long) x << 32 | y & 0xFFFFFFFFL;
  }

  public Bitmap getBmpData() {
    return bmpData;
  }

  public void setBmpData(Bitmap bmpData) {
    this.bmpData = bmpData;
  }

  public void clearBmpData() {
    setBmpData(null);
  }

  public int getBitmapContentHash() {
    return bmpData == null ? 0 : bmpData.hashCode();
  }
}