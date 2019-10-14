package cn.udesk.xphotoview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhenghui on 2017/5/19.
 */

public class XPhotoView extends AppCompatImageView implements IXphotoView {
    private static final String TAG = "XPhotoView";

    private IViewAttacher mPhotoAttacher;

    private GestureManager mGestureManager;

    private OnTabListener mSingleTabListener;

    private DoubleTabScale mDefaultDoubleTabScale;

    private IImageLoadListener mListener;

    private Movie mMovie;

    private boolean sScaleEnable = true;

    private boolean sGif = false;

    private long movieStart;

    public XPhotoView(Context context) {
        this(context, null, 0);
    }

    public XPhotoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XPhotoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
        mPhotoAttacher = new PhotoViewAttacher(this);
        mGestureManager = new GestureManager(this.getContext(), this, mPhotoAttacher);
    }

    public void setLoadListener(IImageLoadListener listener) {
        mListener = listener;
    }

    /**
     * 获取默认配置属性，如 ScaleType 等*/
    private void initialize(Context context, AttributeSet attrs) {
        mDefaultDoubleTabScale = DoubleTabScale.CENTER_CROP;

        super.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    public void setScaleEnable(boolean flag) {
        sScaleEnable = flag;
    }

    public void setSingleTabListener(OnTabListener listener) {
        mSingleTabListener = listener;
    }

    public void setImageResource(@DrawableRes int resId) {
        Drawable drawable = this.getContext().getResources().getDrawable(resId);
        if(drawable == null) {
            setImage((FileInputStream) null);

            return;
        }

        this.setImageDrawable(drawable);
    }

    /** 设置图片的主入口 */
    public void setImage(Bitmap image) {
        if(sGif) {
            return;
        }
        super.setImageBitmap(image);
        if(mListener != null) {
            mListener.onImageLoadStart(this);
        }
        mPhotoAttacher.setBitmap(image, false);
    }

    public void setImage(String path) {
        setImage(new File(path));
    }

    public void setImage(File file) {
        presetImage(file);
        if(file == null || !file.exists()) {
            setImage((FileInputStream) null);
            return;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
//            mMovie = Movie.decodeStream(fileInputStream);
//            if(mMovie == null) {
//                sGif = false;
//            } else {
//                sGif = true;
//            }
            sGif = false;
            fileInputStream.close();
            fileInputStream = new FileInputStream(file);
            setImage(fileInputStream);
        }  catch (Exception e) {

        }
    }

    private void presetImage(File file) {
        if(file != null && file.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                super.setImageBitmap(bitmap);
            } catch (OutOfMemoryError outOfMemoryError){
                outOfMemoryError.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setImage(InputStream ios) {
        this.setImageAsStream(ios);
    }

    /** 设置图片的主入口 */
    public void setImage(FileInputStream ios) {
        this.setImageAsStream(ios);
    }

    private void setImageAsStream(InputStream ios) {
        if(mListener != null) {
            mListener.onImageLoadStart(this);
        }
        if(sGif) {
            try {
                byte[] byteArray = inputStreamToByte(ios);
                mMovie = Movie.decodeByteArray(byteArray, 0, byteArray.length);
            } catch (IOException e) {
            }
            if (mMovie != null) {
                //it's a gif
                sScaleEnable = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }
            onSetImageFinished(null, true, null);
        } else {
            mPhotoAttacher.setInputStream(ios, Bitmap.Config.RGB_565);
        }
    }

    public static byte[] inputStreamToByte(InputStream in) throws IOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int count = -1;
        while((count = in.read(data,0,1024)) != -1)
            outStream.write(data, 0, count);

        data = null;
        return outStream.toByteArray();
    }

//    public void setGif(byte[] byteArray) {
//        sGif = true;
//        mMovie = Movie.decodeByteArray(byteArray, 0, byteArray.length);
//        if(mMovie != null) {
//            sScaleEnable = false;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//            }
//        }
//        onSetImageFinished(null, true, null);
//    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        onSetImageFinished(null, true, null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            if (sScaleEnable) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mPhotoAttacher != null && !mPhotoAttacher.isNotAvailable()) {
                            interceptParentTouchEvent(true);
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        interceptParentTouchEvent(false);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mGestureManager.onTouchEvent(event);
    }

    @Override
    public void onSingleTab() {
        if(mSingleTabListener != null) {
            mSingleTabListener.onSingleTab();
        }
    }

    @Override
    public void onLongTab() {
        if(mSingleTabListener != null) {
            mSingleTabListener.onLongTab();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if(sGif && mMovie != null) {
                onGifDraw(canvas);
            } else if(!sGif){
                mPhotoAttacher.draw(canvas, getWidth(), getHeight());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onGifDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        int vH = getHeight();
        int vW = getWidth();
        int mH = mMovie.height();
        int mW = mMovie.width();
        float scaleX = (float) getWidth() * 1f / mMovie.width();
        float scaleY = (float) getHeight() * 1f / mMovie.height();
        float scale = Math.min(scaleX, scaleY);
        canvas.scale(scale, scale);

        //make sure picture shown in center
        int startY = Math.round((vH * 1f / scale - mH) / 2);
        int startX = Math.round((vW * 1f / scale - mW) / 2);

        long now = android.os.SystemClock.uptimeMillis();

        if (movieStart == 0) {
            movieStart = (int) now;
        }

        int duration;
        if (mMovie != null) {
            duration = mMovie.duration() == 0 ? 500:mMovie.duration();
            int relTime = (int) ((now - movieStart) % duration);
            mMovie.setTime(relTime);
            mMovie.draw(canvas, startX, startY);
            this.invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPhotoAttacher.onViewSizeChanged(w, h);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(sGif && mMovie != null) {
            mMovie = null;
        }
        mPhotoAttacher.destroy();
    }

    @Override
    public void recycleAll() {
        this.onDetachedFromWindow();
    }

    @Override
    public DoubleTabScale getDoubleTabScale() {
        return mDefaultDoubleTabScale;
    }

    @Override
    public String getCachedDir() {
        return null;
    }

    @Override
    public void onImageSetFinished(boolean finished) {

    }

    @Override
    public void callPostInvalidate() {
        postInvalidate();
    }

    @Override
    public void onSetImageFinished(IViewAttacher bm, boolean success, Rect image) {
        if(mListener != null && success) {
            mListener.onImageLoaded(this);
        }
    }

    @Override
    public void interceptParentTouchEvent(boolean intercept) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(intercept);
        }
    }

//    public void setGif(boolean sGif) {
//        this.sGif = sGif;
//    }
}