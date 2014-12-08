package com.ipvans.mailtest;

import android.app.Activity;
import android.os.Bundle;
import com.ipvans.mailtest.tile.provider.DefaultTileProvider;
import com.ipvans.mailtest.tile.bitmap.HttpBitmapDecoder;
import com.ipvans.mailtest.tile.TileView;

public class MainActivity extends Activity {

  private DefaultTileProvider demoProvider;
  private TileView tileView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    tileView = (TileView) findViewById(R.id.tiledmap);

    demoProvider = new DefaultTileProvider(this);
    demoProvider.setBitmapDecoder(new HttpBitmapDecoder(33198, 22539, "http://b.tile.opencyclemap.org/cycle/16/%row%/%col%.png"));
    tileView.registerProvider(demoProvider);
  }

}
