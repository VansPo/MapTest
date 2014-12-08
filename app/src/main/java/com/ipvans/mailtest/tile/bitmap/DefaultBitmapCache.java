package com.ipvans.mailtest.tile.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultBitmapCache implements BitmapCache {

  private Context context;
  private final LinkedHashMap<String, Bitmap> resCache;

  private int size;
  private int maxSize = 200; //only 200 bitmaps to cache

  private final BitmapFactory.Options options;
  private final String absoluteDir;
  private String defaultDir = "MapTest";

  public DefaultBitmapCache(Context context) {
    this.context = context;
    resCache = new LinkedHashMap<String, Bitmap>(0, 1, true);

    File folder = new File(Environment.getExternalStorageDirectory() + "/" + defaultDir);
    if (!folder.exists()) {
      folder.mkdir();
    }
    absoluteDir = folder.getAbsolutePath();

    options = new BitmapFactory.Options();
    options.inPreferredConfig = Bitmap.Config.RGB_565; //we don't need alpha channel
  }

  @Override public Bitmap getBitmapFromMemory(String key) {
    if (key == null) {throw new NullPointerException("key is null");}

    Bitmap bm = resCache.get(key);
    if (bm != null) return bm;

    return resCache.get(key);
  }

  @Override public Bitmap getBitmapFromSDCard(String key) {
    String path = absoluteDir.concat("/" + key.hashCode() + ".png");
    File file = new File(path);
    if(file.exists()) {
      Bitmap bitmap = BitmapFactory.decodeFile(path, options);
      return bitmap;
    }
    return null;
  }

  @Override public void putBitmapInMemoryCache(String key, Bitmap bitmap) {
    if (key == null || bitmap == null) {throw new NullPointerException("wrong data");}

    size += sizeOf(key, bitmap);

    resCache.put(key, bitmap);
    trimToSize(maxSize);
  }

  @Override public void saveBitmapOnSDCard(String key, Bitmap bitmap) {

    File path =new File(absoluteDir,key.hashCode()+".png");

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(path);
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
      fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void trimToSize(int maxSize) {
    while (true) {
      String key;
      Bitmap value;
      synchronized (this) {
        if (size < 0 || (resCache.isEmpty() && size != 0)) {
          throw new IllegalStateException("can't trim memory cache!");
        }

        if (size <= maxSize || resCache.isEmpty()) {
          break;
        }

        Map.Entry<String, Bitmap> toEvict = resCache.entrySet().iterator().next();
        key = toEvict.getKey();
        value = toEvict.getValue();
        resCache.remove(key);
        size -= sizeOf(key, value);
      }
    }
  }

  //array size is measured in number of elements
  protected int sizeOf(String key, Bitmap bm) {
    return 1;
  }
}
