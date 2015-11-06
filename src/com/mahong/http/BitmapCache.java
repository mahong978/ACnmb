package com.mahong.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapCache implements ImageCache {
	private LruCache<String, Bitmap> mCache;
	private String FilePath = Environment.getExternalStorageDirectory().toString() + File.separator + "IfLove" + File.separator;

	public BitmapCache() {
		int maxSize = 4 * 1024 * 1024;
		mCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}
		};
	}

	@Override
	public Bitmap getBitmap(String url) {
		Log.i("leslie", "get cache " + url);
		return mCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		Log.i("leslie", "add cache " + url);
		String[] subs = url.split("/");
		File file = new File(FilePath + "portrait" + File.separator + subs[subs.length-1]);
		if (file.exists())
			file.delete();
		try {
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (bitmap != null) {
			//mCache.put(url, bitmap);
		}
	}
}
