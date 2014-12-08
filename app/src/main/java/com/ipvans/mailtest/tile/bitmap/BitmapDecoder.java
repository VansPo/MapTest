package com.ipvans.mailtest.tile.bitmap;

import android.graphics.Bitmap;
import android.graphics.Point;
import com.ipvans.mailtest.tile.Tile;

public interface BitmapDecoder {
  public Bitmap getBitmap(Tile tile);

  public String decodeSource(Point point);
}
