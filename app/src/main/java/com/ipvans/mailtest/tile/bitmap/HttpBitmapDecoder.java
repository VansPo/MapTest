package com.ipvans.mailtest.tile.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import com.ipvans.mailtest.tile.Tile;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpBitmapDecoder implements BitmapDecoder {

  private final String pattern;
  private int startX;
  private int startY;

  public HttpBitmapDecoder(String pattern) {
    this.pattern = pattern;
  }

  public HttpBitmapDecoder(int startX, int startY, String pattern) {
    this.pattern = pattern;
    this.startX = startX;
    this.startY = startY;
  }

  @Override public Bitmap getBitmap(Tile tile) {
    return getBitmapFromURL(decodeSource(new Point(tile.xId, tile.yId)));
  }

  private Bitmap getBitmapFromURL(String src) {
    try {
      URL url = new URL(src);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      Bitmap myBitmap = BitmapFactory.decodeStream(input);
      return myBitmap;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override public String decodeSource(Point point) {
    String result = "";
    result = pattern.replace("%row%", String.valueOf(startX + point.x));
    result = result.replace("%col%", String.valueOf(startY + point.y));
    return result;
  }
}
