package com.ipvans.mailtest.tile;

import android.util.Pair;

public enum GridAnchor {

  TOP_LEFT, TOP_CENTER, TOP_RIGHT,
  CENTER_LEFT, CENTER, CENTER_RIGHT,
  BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

  // Calculate the left/top tile coordinates
  public final Pair<Integer, Integer> getPosition(int surfaceWidth, int surfaceHeight,
      int tileWidth) {
    int x = 0, y = 0;

    if (TOP_CENTER == this || CENTER == this || BOTTOM_CENTER == this) {
      x = (surfaceWidth - tileWidth) / 2;
    }
    if (TOP_RIGHT == this || CENTER_RIGHT == this || BOTTOM_RIGHT == this) {
      x = surfaceWidth - tileWidth;
    }
    if (CENTER_LEFT == this || CENTER == this || CENTER_RIGHT == this) {
      y = (surfaceHeight - tileWidth) / 2;
    }
    if (BOTTOM_LEFT == this || BOTTOM_CENTER == this || BOTTOM_RIGHT == this) {
      y = surfaceHeight - tileWidth;
    }
    return new Pair<Integer, Integer>(x, y);
  }
}
