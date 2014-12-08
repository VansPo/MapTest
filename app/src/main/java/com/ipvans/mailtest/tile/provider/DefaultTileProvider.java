package com.ipvans.mailtest.tile.provider;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import com.ipvans.mailtest.tile.Tile;
import com.ipvans.mailtest.tile.TileRange;
import com.ipvans.mailtest.tile.TileView;
import com.ipvans.mailtest.tile.bitmap.BitmapCache;
import com.ipvans.mailtest.tile.bitmap.BitmapDecoder;
import com.ipvans.mailtest.tile.bitmap.DefaultBitmapCache;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultTileProvider extends BaseTileProvider {

  // used for the starting and stopping of background tasks
  private ExecutorService executorService;
  private Future lastSubmittedTask;

  // cache of last seen tiles
  private final Map<Long, Tile> tileCache;

  // keep cache of last rendered bitmaps, so we don't have to load it again
  private final BitmapCache bitmapCache;

  private AtomicBoolean hasFreshData = new AtomicBoolean(false);

  public DefaultTileProvider(Context context) {

    super(context);
    tileCache = new ConcurrentHashMap<Long, Tile>();
    bitmapCache = new DefaultBitmapCache(context);
  }

  @Override
  public Tile getTile(int x, int y) {
    return tileCache.get(Tile.createCacheKey(x, y));
  }

  @Override
  public boolean hasFreshData() {
    return hasFreshData.getAndSet(false);
  }

  @Override
  public void onTileRangeChange(TileRange newRange) {

    // clear tiles that are currently off screen
    Collection<Tile> entries = tileCache.values();
    for (Tile t : entries) {
      if (t.getBmpData() != null && !newRange.contains(t, getBufferSize())) {
        t.clearBmpData();
      }
    }

    List<Tile> renderQueue = new LinkedList<Tile>();
    Tile t;

    for (int y = newRange.top; y <= newRange.bottom; y++) {
      for (int x = newRange.left; x <= newRange.right; x++) {

        t = getTile(x, y);
        if (t == null || t.getBmpData() == null) {
          renderQueue.add(new Tile(x, y));
        }
      }
    }

    if (executorService == null || executorService.isShutdown()) {
      executorService = Executors.newSingleThreadExecutor();
    }
    if (lastSubmittedTask != null) {
      lastSubmittedTask.cancel(true);
    }
    lastSubmittedTask = executorService.submit(new QueueProcessTask(renderQueue));
  }

  public BitmapDecoder getBitmapDecoder() {
    return bitmapDecoder;
  }

  @Override
  public Integer[] getTileRangeLimits() {
    return new Integer[] { -50, -50, 50, 50 };
  }

  public void setBitmapDecoder(BitmapDecoder bitmapDecoder) {
    this.bitmapDecoder = bitmapDecoder;
  }

  class QueueProcessTask implements Runnable {

    private List<Tile> renderQueue;

    public QueueProcessTask(List<Tile> renderQueue) {
      this.renderQueue = renderQueue;
    }

    @Override
    public void run() {
      while (!renderQueue.isEmpty()) {

        if (Thread.currentThread().isInterrupted()) {
          Log.d(TileView.LOG_TAG, "Tile processing thread is interrupted");
          return;
        }

        Tile t = renderQueue.remove(0);

        if (t == null || t.getBmpData() != null) {
          continue;
        }

        Log.d(TileView.LOG_TAG, "Processing tile " + t);

        Point p = new Point(t.xId, t.yId);
        String resName = bitmapDecoder.decodeSource(p);

        //trying to get Bitmap from memory
        Bitmap bmp = bitmapCache.getBitmapFromMemory(resName);

        //TODO separate threads
        // actually we need to process each tile in separate thread, so we won't block other
        // tiles rendering process if something goes wrong. But i can't figure out how to
        // manage threads queue yet.
        if (bmp == null) {  //not available, looking for it on SDCard
          bmp = bitmapCache.getBitmapFromSDCard(resName);
          if (bmp == null) {  //still nothing, download it and cache
            bmp = bitmapDecoder.getBitmap(t);
            if (bmp != null) {
              bitmapCache.putBitmapInMemoryCache(resName, bmp);
              bitmapCache.saveBitmapOnSDCard(resName, bmp);
            }
          } else {
            bitmapCache.putBitmapInMemoryCache(resName, bmp);
          }
        }

        t.setBmpData(bmp);
        // cache the tile
        tileCache.put(t.cacheKey, t);
        hasFreshData.set(true);
      }

      Log.d(TileView.LOG_TAG, "Render process finished");
    }
  }

  @Override
  public void onSurfaceDestroyed() {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }
}