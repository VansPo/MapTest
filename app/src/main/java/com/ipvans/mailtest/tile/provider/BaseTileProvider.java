package com.ipvans.mailtest.tile.provider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.ipvans.mailtest.tile.GridAnchor;
import com.ipvans.mailtest.tile.Tile;
import com.ipvans.mailtest.tile.TileRange;
import com.ipvans.mailtest.tile.bitmap.BitmapDecoder;
import com.ipvans.mailtest.tile.bitmap.HttpBitmapDecoder;

public class BaseTileProvider implements TileProvider {

  private Context context;
  private Bitmap sharedTileBmp = null;

  public BitmapDecoder bitmapDecoder;

  //default buffer size is 1
  public int bufferSize = 1;

  public BaseTileProvider(Context context) {
    this.context = context;
    sharedTileBmp = getDefaultBitmap(context, getTileSize());

    //TODO: create default simple decoder
    bitmapDecoder =
        new HttpBitmapDecoder("http://b.tile.opencyclemap.org/cycle/16/%row%/%col%.png");
  }

  private static Bitmap getDefaultBitmap(Context ctx, int w) {

    Paint paintTileBG = new Paint();
    paintTileBG.setColor(ctx.getResources().getColor(android.R.color.black));

    Paint paintTileCircle = new Paint();
    paintTileCircle.setColor(ctx.getResources().getColor(android.R.color.holo_red_dark));
    paintTileCircle.setTextSize(4);
    paintTileCircle.setAntiAlias(true);

    Paint paintTileTxt = new Paint();
    paintTileTxt.setColor(Color.BLACK);
    paintTileTxt.setTextSize(20);
    paintTileTxt.setAntiAlias(true);
    paintTileTxt.setTextAlign(Paint.Align.CENTER);

    Bitmap bmp = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(bmp);

    // background
    c.drawRect(0, 0, w, w, paintTileBG);
    // circle
    int radius = w / 2 - 15;
    c.drawCircle(w / 2, w / 2, radius, paintTileCircle);

    return bmp;
  }

  protected Context getContext() {
    return context;
  }

  @Override
  public int getTileSize() {
    return Tile.DEFAULT_TILE_SIZE;
  }

  @Override
  public Tile getTile(int x, int y) {
    Tile t = new Tile(x, y);
    t.setBmpData(sharedTileBmp);
    return t;
  }

  @Override
  public GridAnchor getGridAnchor() {
    // put (0,0) in the middle of the screen
    return GridAnchor.TOP_LEFT;
  }

  public int getBufferSize() {
    return bufferSize;
  }

  @Override
  public boolean hasFreshData() {
    return false;
  }

  @Override
  public Integer[] getTileRangeLimits() {
    // infinite scrolling
    return new Integer[] { null, null, null, null };
  }

  @Override
  public void onTileRangeChange(TileRange newRange) {}

  @Override
  public void onSurfaceDestroyed() {}

  public void setBitmapDecoder(BitmapDecoder decoder) {
    this.bitmapDecoder = decoder;
  }

  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }
}
