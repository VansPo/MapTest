package com.ipvans.mailtest.tile;

public class TileRange {

  public final int left, top, right, bottom;

  public TileRange(int left, int top, int right, int bottom) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }

  public boolean contains(Tile t, int padRange) {
    return t != null && contains(t.xId, t.yId, padRange);
  }

  public boolean contains(int x, int y, int rangePadding) {
    rangePadding = Math.max(0, rangePadding);

    return
        // check for empty first
        left < right
            && top < bottom
            && x >= left - rangePadding
            && x <= right + rangePadding
            && y >= top - rangePadding
            && y <= bottom + rangePadding;
  }

  public int numTilesHorizontal() {
    if (left > right) {
      return 0;
    }
    return Math.abs(right - left + 1);
  }

  public int numTilesVertical() {
    if (top > bottom) {
      return 0;
    }
    return Math.abs(bottom - top + 1);
  }

  public int numTiles() {
    return numTilesHorizontal() * numTilesVertical();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TileRange tileRange = (TileRange) o;

    return left == tileRange.left
        && top == tileRange.top
        && right == tileRange.right
        && bottom == tileRange.bottom;
  }

  @Override
  public int hashCode() {
    int result = left;
    result = 31 * result + top;
    result = 31 * result + right;
    result = 31 * result + bottom;
    return result;
  }
}
