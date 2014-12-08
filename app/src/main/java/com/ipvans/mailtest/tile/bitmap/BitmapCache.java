package com.ipvans.mailtest.tile.bitmap;

import android.graphics.Bitmap;

public interface BitmapCache {
  public Bitmap getBitmapFromMemory(String key);
  public Bitmap getBitmapFromSDCard(String key);

  public void putBitmapInMemoryCache(String key, Bitmap bitmap);
  public void saveBitmapOnSDCard(String key, Bitmap bitmap);
}
