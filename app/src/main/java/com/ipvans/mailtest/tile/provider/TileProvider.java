package com.ipvans.mailtest.tile.provider;

import com.ipvans.mailtest.tile.GridAnchor;
import com.ipvans.mailtest.tile.Tile;
import com.ipvans.mailtest.tile.TileRange;

public interface TileProvider {

  public int getTileSize();

  public Integer[] getTileRangeLimits();

  public GridAnchor getGridAnchor();

  public Tile getTile(int x, int y);

  public boolean hasFreshData();

  public void onTileRangeChange(TileRange newRange);

  public void onSurfaceDestroyed();
}