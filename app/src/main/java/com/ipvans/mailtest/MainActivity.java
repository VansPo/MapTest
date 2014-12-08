package com.ipvans.mailtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import com.ipvans.mailtest.tile.TileView;
import com.ipvans.mailtest.tile.bitmap.HttpBitmapDecoder;
import com.ipvans.mailtest.tile.pin.Pin;
import com.ipvans.mailtest.tile.provider.DefaultTileProvider;

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


    tileView.addPin(new Pin(300, 300, getCircleBitmap()));
  }

  private Bitmap getCircleBitmap() {
    final Bitmap output = Bitmap.createBitmap(100,
        100, Bitmap.Config.ARGB_4444);
    final Canvas canvas = new Canvas(output);

    final int color = Color.RED;
    final Paint paint = new Paint();
    final RectF rect = new RectF(0, 0, 100, 100);

    paint.setAntiAlias(true);
    paint.setColor(color);
    canvas.drawOval(rect, paint);

    return output;
  }

}
