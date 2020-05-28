package cn.udesk.rich;

import android.graphics.Bitmap;

import java.io.IOException;


public interface ImageLoader {
    Bitmap getBitmap(String url,int width) throws IOException;
}
