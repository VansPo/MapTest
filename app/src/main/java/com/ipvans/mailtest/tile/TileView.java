package com.ipvans.mailtest.tile;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.widget.Scroller;
import com.ipvans.mailtest.tile.provider.BaseTileProvider;
import com.ipvans.mailtest.tile.provider.TileProvider;
import java.util.concurrent.atomic.AtomicBoolean;

public class TileView extends SurfaceView implements SurfaceHolder.Callback {

  public static final String LOG_TAG = "TileMap";

  final GestureDetector gestureDetector;

  final Paint paintBg;

  TileSurfaceDrawThread surfaceDrawThread;
  ViewState state;
  TileProvider tileProvider;

  private static final float FRICTION = 0.99f;
  private Scroller scroller;
  private VelocityTracker velocity;
  protected VelocityDecelerator mVelocityDecelerator;

  public TileView(Context context, AttributeSet attrs) {

    super(context, attrs);

    getHolder().addCallback(this);

    // add listener for scroll
    gestureDetector = new GestureDetector(context, new GestureListener());
    // create default tile provider
    tileProvider = new BaseTileProvider(context);

    Resources res = getResources();
    paintBg = new Paint();
    paintBg.setColor(res.getColor(android.R.color.black));
    paintBg.setStyle(Paint.Style.FILL);

    // create scroller for fling gesture
    scroller = new Scroller(context);
    scroller.setFriction(FRICTION);
  }

  public void registerProvider(TileProvider tileProvider) {

    if (tileProvider == null) {
      Log.e(LOG_TAG, "Provider can't be null");
      tileProvider = new BaseTileProvider(getContext());
    }
    this.tileProvider = tileProvider;

    requestSurfaceRefresh(true);
  }

  public TileProvider getProvider() {
    return tileProvider;
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (surfaceDrawThread == null || !surfaceDrawThread.isAlive()) {
      surfaceDrawThread = new TileSurfaceDrawThread(holder);
      surfaceDrawThread.setRunning(true);
      surfaceDrawThread.start();
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {

    // let provider finish it's tasks
    if (tileProvider != null) {
      tileProvider.onSurfaceDestroyed();
    }

    // stop rendering thread
    boolean retry = true;
    surfaceDrawThread.setRunning(false);
    while (retry) {
      try {
        surfaceDrawThread.join();
        retry = false;
      } catch (InterruptedException ignored) {
      }
    }
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    // update ViewState based on new values
    state =
        new ViewState(width, height, tileProvider.getTileSize(), tileProvider.getTileRangeLimits());
    moveToTile(0, 0, true);
  }

  public void requestSurfaceRefresh(boolean notifyProvider) {
    if (tileProvider == null) {
      Log.w(LOG_TAG, "Provider is null");
      return;
    }
    if (state == null) {
      Log.w(LOG_TAG, "State is null");
      return;
    }
    if (notifyProvider) {
      tileProvider.onTileRangeChange(state.getVisibleTileRange());
    }
    if (surfaceDrawThread != null) {
      surfaceDrawThread.requestRender();
    }
  }

  public void moveToTile(int tileX, int tileY, boolean alwaysNotifyProvider) {
    if (tileProvider == null) {
      Log.d(LOG_TAG, "Provider is null, can't go to tile (" + tileX + "," + tileY + ")");
      return;
    }

    if (state == null || state.surfaceW == 0) {
      Log.d(LOG_TAG, "Surface is null, can't go to tile (" + tileX + "," + tileY + ")");
      return;
    }
    // get left/top canvas position (px) where the anchor tile would be rendered
    GridAnchor anchor = tileProvider.getGridAnchor();
    Pair<Integer, Integer> anchorCoords =
        anchor.getPosition(state.surfaceW, state.surfaceH, state.tileWidth);

    int newX = anchorCoords.first - (state.tileWidth * tileX);
    int newY = anchorCoords.second - (state.tileWidth * tileY);
    // update state
    boolean rangeChange = state.applySurfaceOffset(newX, newY);

    requestSurfaceRefresh(rangeChange || alwaysNotifyProvider);
  }

  // main drawer thread
  class TileSurfaceDrawThread extends Thread {

    private final SurfaceHolder holder;
    boolean running = false;
    private AtomicBoolean rerenderRequested = new AtomicBoolean(false);
    private ViewState.Snapshot snapshot;
    private boolean hasOffsetChanged, wasRenderRequested;
    private Tile[][] visibleTiles;
    private int[][] oldTileHashcodes;

    public TileSurfaceDrawThread(SurfaceHolder holder) {
      this.holder = holder;
    }

    public void setRunning(boolean running) {
      this.running = running;
    }

    public void requestRender() {
      rerenderRequested.set(true);
    }

    @Override
    public void run() {
      Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY);

      setName(this.getClass().getSimpleName() + "(" + getName() + ")");

      Canvas c;

      int xCanvasOffsetOld = 0, yCanvasOffsetOld = 0; // detect offset changes

      while (running) {
        if (state == null || state.tileWidth <= 0) {
          continue;
        }

        // fling if there are any acceleration going
        performFling();

        snapshot = state.getSnapshot();
        if (snapshot.visibleTileIdRange == null) {
          continue;
        }
        wasRenderRequested = rerenderRequested.getAndSet(false);

        c = null;

        hasOffsetChanged = snapshot.canvasOffsetX != xCanvasOffsetOld
            || snapshot.canvasOffsetY != yCanvasOffsetOld;
        xCanvasOffsetOld = snapshot.canvasOffsetX;
        yCanvasOffsetOld = snapshot.canvasOffsetY;

        boolean haveTileBmpsChanged = false;
        if (tileProvider.hasFreshData() || wasRenderRequested || hasOffsetChanged) {
          haveTileBmpsChanged = refreshTileBitmapsAndCompare(snapshot.visibleTileIdRange);
        }

        if (haveTileBmpsChanged || wasRenderRequested || hasOffsetChanged) {

          try {
            c = holder.lockCanvas(null);
            if (c == null) {
              continue;
            }
            synchronized (holder) {
              //todo zoom here?
              drawVisibleTiles(c, snapshot);
            }
          } finally {
            if (c != null) {
              holder.unlockCanvasAndPost(c);
            }
          }
        }
      }
    }

    private boolean refreshTileBitmapsAndCompare(TileRange visibleRange) {

      if (visibleTiles == null ||
          visibleTiles.length != state.tileWidth || visibleTiles[0].length != state.tileWidth) {
        visibleTiles = new Tile[state.tilesVert][state.tilesHoriz];
        oldTileHashcodes = new int[state.tilesVert][state.tilesHoriz];
      }

      boolean bmpChangeDetected = false;
      int newTileHash;

      int xId, yId;

      for (int y = 0; y < state.tilesVert; y++) {

        for (int x = 0; x < state.tilesHoriz; x++) {

          yId = y + visibleRange.top;
          xId = x + visibleRange.left;

          // refresh the tile from the provider
          visibleTiles[y][x] = tileProvider.getTile(xId, yId);
          if (visibleTiles[y][x] == null) {
            visibleTiles[y][x] = new EmptyTile(xId, yId);
          }

          // generate hashcode, compare to that from last time around
          newTileHash = visibleTiles[y][x].getBitmapContentHash();
          if (newTileHash != oldTileHashcodes[y][x]) {
            bmpChangeDetected = true;
          }
          oldTileHashcodes[y][x] = newTileHash;
        }
      }

      return bmpChangeDetected;
    }

    public void drawVisibleTiles(Canvas canvas, ViewState.Snapshot snapshot) {
      canvas.save();
      // clear canvas
      canvas.drawRect(0, 0, state.surfaceW, state.surfaceH, paintBg);
      // offset canvas, so we can draw tiles with simple 0,0 co-ordinates
      canvas.translate(snapshot.canvasOffsetX, snapshot.canvasOffsetY);
      int curTileTop = 0;
      for (Tile[] tileRow : visibleTiles) {
        int curTileLeft = 0;
        for (Tile t : tileRow) {
          Bitmap bmp = t.getBmpData();
          if (bmp != null) {
            canvas.drawBitmap(bmp, curTileLeft, curTileTop, null);
          }
          curTileLeft += state.tileWidth; // move right one tile screenWidth
        }
        curTileTop += state.tileWidth; // move down one tile screenWidth
      }
      canvas.translate(-snapshot.canvasOffsetX, -snapshot.canvasOffsetY);

      canvas.restore();
    }

    private class EmptyTile extends Tile {
      public EmptyTile(int x, int y) {
        super(x, y, state.tileWidth);
      }
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent me) {

    if (mVelocityDecelerator != null) mVelocityDecelerator.stop();
    if (velocity == null) {
      velocity = VelocityTracker.obtain();
    }
    velocity.addMovement(me);

    invalidate();

    gestureDetector.onTouchEvent(me);
    return true;
  }

  class GestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    public void onShowPress(MotionEvent motionEvent) {
      Log.d(LOG_TAG, "show press");
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX,
        float distanceY) {

      int newOffX = -(int) distanceX;
      int newOffY = -(int) distanceY;

      boolean rangeChange = state.applySurfaceOffsetRelative(newOffX, newOffY);
      requestSurfaceRefresh(rangeChange);

      return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
      Log.d(LOG_TAG, "long press");
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
      Log.d(LOG_TAG, "fling");

      velocity.computeCurrentVelocity(1);
      if (mVelocityDecelerator == null) {
        mVelocityDecelerator =
            new VelocityDecelerator(velocity.getXVelocity(), velocity.getYVelocity());
      } else {
        mVelocityDecelerator.start(velocity.getXVelocity(), velocity.getYVelocity());
      }
      velocity.recycle();
      velocity = null;

      return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      Log.d(LOG_TAG, "double tap");
      return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      Log.d(LOG_TAG, "single tap");
      return true;
    }
  }

  private void performFling() {
    if (mVelocityDecelerator != null && mVelocityDecelerator.isMoving()) {
      mVelocityDecelerator.calculateFreezeFrameData();
      int mDeltaX = Math.round(mVelocityDecelerator.getDeltaDistanceX())
          * mVelocityDecelerator.getDirectionX();
      int mDeltaY = Math.round(mVelocityDecelerator.getDeltaDistanceY())
          * mVelocityDecelerator.getDirectionY();
      //TODO use mDeltaX and mDeltaY to move your drawing objects around
      boolean rangeChange = state.applySurfaceOffsetRelative(mDeltaX, mDeltaY);
      requestSurfaceRefresh(rangeChange);
    }
  }
}
