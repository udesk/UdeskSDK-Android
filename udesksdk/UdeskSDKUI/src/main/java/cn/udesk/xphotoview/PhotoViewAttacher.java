package cn.udesk.xphotoview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 如果是原图则先缓存为文件，再通过 IOStream 进入 2 3 流程
 * 基本逻辑：
 * 默认将图片以 CENTER_INSIDE 进行压缩，计算出默认 SampleSize 进行切割
 * 保存切割后 bitmap 作为 ThumbSampleBitmap，保存 SampleSize 为 DefaultSampleSize
 * 视图大小 mViewRect，可以理解为以 View 为坐标系，保持不变
 * 原图大小 mImageRect，大小保持不变
 * 压缩后图片实际大小 mShowBitmapRect, 视图在图片上的位置 mViewBitmapRect，均以 Bitmap 坐标系为准
 * 缩放时更新 mShowBitmapRect 大小，重新计算 SampleSize 并更新视图
 * 移动时更新 mViewBitmapRect 并更新视图，此时不需要更新 SampleSize
 * 解析逻辑：
 * 在线程中用 BitmapRegionDecoder 将原图进行 N * M 个方格进行切割再分别解析
 * 线程 mInstanceDecoderRunnable 为解析过程
 */

public class PhotoViewAttacher implements IViewAttacher {

    private static final float MAX_SCALE_VALUE = 4;
    private static final float MIN_SCALE_VALUE = 1;
    private static final float DOUBLE_TAP_MAX = 2.0f;
    private XPhotoView mPhotoView;
    /**
     * 缩放动画
     */
    private ValueAnimator mValueAnimator = null;
    private float mLastAnimatedValue = 1f;
    private float mMaxScaleValue;
    private float mMinScaleValue;
    private boolean sIsSettingImage;
    /**
     * 保存直接设置的 bitmap
     */
    private Bitmap mSrcBitmap = null;
    /**
     * Cache 文件
     */
    private final File mCacheFile;
    /**
     * 质量参数, 默认为 RGB_565
     */
    private Bitmap.Config mBitmapConfig = Bitmap.Config.RGB_565;

    /**
     * 当前图片的的采样率
     */
    private int mSampleSize = 0;

    /**
     * 缩略图时的 SampleSize
     */
    private int mThumbSampleSize = 0;

    /***
     * View Rect
     * View 坐标系*/
    private Rect mViewRect = new Rect();

    /**
     * 原图 Rect
     * Bitmap 坐标系
     */
    private Rect mImageRect = new Rect();

    /**
     * 实际展示的 Bitmap 大小
     * Bitmap 坐标系
     */
    private RectF mShowBitmapRect = new RectF();
    /**
     * view 相对 Show Bitmap 的坐标
     * Bitmap 坐标系
     */
    private Rect mViewBitmapRect = new Rect();

    /**
     * 第一次初始化后的默认 Show Bitmap Rect
     * Bitmap 坐标系
     */
    private Rect mInitiatedShowBitmapRect = new Rect();

    /**
     * 局部解析原始图片工具
     */
    private BitmapRegionDecoder mBmDecoder;
    /**
     * Decoder 解析用 IS
     */
    private InputStream mDecodeInputStream;

    /**
     * Bitmap 网格
     */
    private BitmapGridStrategy mBitmapGrid = new BitmapGridStrategy();


    /**
     * 异步处理图片的解码
     */
    private Handler mLoadingHandler = null;
    private final Handler mMainHandler = new Handler();
    private HandlerThread mLoadingThread;
    private static final String THREAD_TAG = "LoadingThread";
    private final Object mDecodeSyncLock = new Object();

    /**
     * 将输入的Bitmap原图缓存为文件再输出为 fos 的线程
     */
    protected Runnable mCacheBitmapRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                FileOutputStream fos = new FileOutputStream(mCacheFile);
                mSrcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                setBitmapDecoder(new FileInputStream(mCacheFile));
            }catch (OutOfMemoryError outOfMemoryError){
                onSetImageFinished(false);
            }catch (Exception e) {
                e.printStackTrace();
                onSetImageFinished(false);
            }
        }
    };

    /**
     * 读取输入流初始化 BitmapRegionDecoder
     */
    protected Runnable mInstanceDecoderRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                try {
                    mBmDecoder = BitmapRegionDecoder.newInstance(mDecodeInputStream, false);
                    mImageRect.set(0, 0, mBmDecoder.getWidth(), mBmDecoder.getHeight());
                } catch (Exception e) {
                    e.printStackTrace();
                    mBmDecoder = null;
                }
                if (mBmDecoder != null) {
                    initiateViewRect(mViewRect.width(), mViewRect.height());
                } else {
                    onSetImageFinished(false);
                }
            } catch (OutOfMemoryError outOfMemoryError){
                onSetImageFinished(false);
            }catch (Exception e) {
                e.printStackTrace();
                onSetImageFinished(false);
            }
        }
    };

    public PhotoViewAttacher(XPhotoView mPhotoView) {
        this.mPhotoView = mPhotoView;
        mCacheFile = new File(mPhotoView.getCachedDir(), UUID.randomUUID().toString());
        mLoadingThread = new HandlerThread(THREAD_TAG + this.hashCode());
    }

    /**
     * 初始化所需参数和线程
     */
    private synchronized void initialize(Bitmap.Config config) {
        onSetImageStart();

        if (mLoadingHandler != null) {
            mLoadingHandler.removeCallbacks(mInstanceDecoderRunnable);
            mLoadingHandler.removeCallbacks(mCacheBitmapRunnable);
            mLoadingHandler.removeCallbacks(mBitmapGrid.mDecodeThumbRunnable);
        }

        if (mBmDecoder != null) {
            mBmDecoder.recycle();
            mBmDecoder = null;
        }

        if (mBitmapGrid != null) {
            recycleAll();
        }

        mBitmapConfig = config == null ? Bitmap.Config.RGB_565 : config;

        if (mLoadingThread == null || mLoadingThread.getState() == Thread.State.NEW) {
            mLoadingThread = new HandlerThread(THREAD_TAG + this.hashCode());
            mLoadingThread.start();
        }
        mLoadingHandler = new Handler(mLoadingThread.getLooper());
    }

    @Override
    public void setBitmap(Bitmap bitmap, boolean cache) {
        initialize(Bitmap.Config.ARGB_8888);
        setSrcBitmap(bitmap, cache);
    }

    /**
     * 直接设置 Bitmap
     */
    private void setSrcBitmap(final Bitmap bitmap, boolean enableCache) {
        mSrcBitmap = bitmap;
        if (bitmap == null) {
            onSetImageFinished(true);
            return;
        }

        try {
            if (enableCache) {
                mLoadingHandler.post(mCacheBitmapRunnable);
            } else {
                mImageRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());

                initiateViewRect(mViewRect.width(), mViewRect.height());
            }
        } catch (OutOfMemoryError outOfMemoryError){
            outOfMemoryError.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setInputStream(InputStream is, Bitmap.Config config) {
        try {
            initialize(config);
            setBitmapDecoder(is);
        } catch (OutOfMemoryError outOfMemoryError){
            outOfMemoryError.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置 BitmapRegionDecoder 这个函数只会走一次
     */
    private void setBitmapDecoder(final InputStream is) {
        mDecodeInputStream = is;
        if (is == null) {
            onSetImageFinished(false);
            return;
        }

        mLoadingHandler.post(mInstanceDecoderRunnable);
    }

    private void initiateViewRect(int viewWidth, int viewHeight) {
        try {
            mViewRect.set(0, 0, viewWidth, viewHeight);

            int imgWidth = mImageRect.width();
            int imgHeight = mImageRect.height();

            //有任何一个为0则异常
            if (viewWidth * viewHeight * imgWidth * imgHeight == 0) {
                return;
            }

            /** maxScale = max(MAX_SCALE_VALUE, view 和 image 的边的比值的最大值)，minScale = min(MIN_SCALE_VALUE, view 和 image 的边的比值的最小值)
             *  1. image 一边大于 view 对应边：最大填 view 或者 4 倍，最小适配屏幕或者 1 倍
             *  2. image 两边大于 view 对应边：最大 4 倍，最小适配屏幕
             *  */
            mMaxScaleValue = Math.max(MAX_SCALE_VALUE, getMaxFitViewValue());
            mMinScaleValue = Math.min(MIN_SCALE_VALUE, getMinFitViewValue());

            /** 以 view 宽/长比例和 image 宽/长比例做比较
             *  iW/iH < vW/vH : 左右留空，取高比值
             *  iW/iH > vW/vH : 上下留空，取宽比值 */
            float ratio = ((imgWidth * 1.0f / imgHeight * 1.0f) < (viewWidth * 1.0f / viewHeight * 1.0f)) ? (imgHeight * 1.0f / viewHeight) : (imgWidth * 1.0f / viewWidth);
            //取消设置ratio最小值，初始化以最大适配view计算ratio
//        ratio = ratio < 1 ? 1f : ratio;

            mShowBitmapRect.set(0, 0, (int) (imgWidth / ratio), (int) (imgHeight / ratio));

            /** 保存初始大小 */
            mShowBitmapRect.round(mInitiatedShowBitmapRect);

            /** 取缩小到适配view时的bitmap的起始位置 */
            int left = (int) ((mShowBitmapRect.width() - mViewRect.width()) / 2);
            int top = (int) ((mShowBitmapRect.height() - mViewRect.height()) / 2);

            left = mShowBitmapRect.width() < mViewRect.width() ? left : 0;
            int right = left + mViewRect.width();
            top = mShowBitmapRect.height() < mViewRect.height() ? top : 0;
            int bottom = top + mViewRect.height();

            mViewBitmapRect.set(left, top, right, bottom);

            mSampleSize = ((imgWidth * 1.0f / imgHeight * 1.0f) < (viewWidth * 1.0f / viewHeight * 1.0f)) ? calculateSampleSize((int) (imgWidth / mShowBitmapRect.width()))
                    : calculateSampleSize((int) (imgHeight / mShowBitmapRect.height()));

            mThumbSampleSize = mSampleSize;

            /** 初始化矩阵 */
            mBitmapGrid.initializeBitmapGrid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized boolean checkOrUpdateViewRect(int width, int height) {
        try {
            if (mViewRect.width() != width || mViewRect.height() != height) {
                onSetImageStart();
                initiateViewRect(width, height);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 重置为初始化状态
     */
    private void resetShowBitmapRect() {
        try {
            mShowBitmapRect.set(mInitiatedShowBitmapRect);

            int left = (int) ((mShowBitmapRect.width() - mViewRect.width()) / 2);
            int right = left + mViewRect.width();
            int top = (int) ((mShowBitmapRect.height() - mViewRect.height()) / 2);
            int bottom = top + mViewRect.height();
            mViewBitmapRect.set(left, top, right, bottom);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新ViewBitmapRect
     *
     * @param rect ShowBitmapRect相对view坐标系的rect
     */
    private void updateViewBitmapRect(RectF rect) {
        try {
            Rect vRect = new Rect(0, 0, mViewRect.width(), mViewRect.height());
            vRect.left = (int) -rect.left;
            vRect.right = vRect.left + mViewRect.width();
            vRect.top = (int) -rect.top;
            vRect.bottom = vRect.top + mViewRect.height();

            mViewBitmapRect.set(vRect);
            mShowBitmapRect.set(0, 0, rect.width(), rect.height());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 填满view的放大值
     * 取宽比和高比的最大值，相当于 scaleType = CENTER_CROP
     */
    private float getMaxFitViewValue() {
        float iw = mImageRect.width();
        float ih = mImageRect.height();

        float vw = mViewRect.width();
        float vh = mViewRect.height();

        return Math.max(vw / iw, vh / ih);
    }

    /**
     * 相当于 scaleType = CENTER_INSIDE
     */
    private float getMinFitViewValue() {
        float iw = mImageRect.width();
        float ih = mImageRect.height();

        float vw = mViewRect.width();
        float vh = mViewRect.height();

        return Math.min(vw / iw, vh / ih);
    }

    private float getMaxDoubleTapScaleFactor() {
        return getMaxFitViewScaleFactor() == 1.0f ? DOUBLE_TAP_MAX : getMaxFitViewScaleFactor();
    }

    /**
     * 获取最大适应view的缩放倍数
     */
    private float getMaxFitViewScaleFactor() {
        float ws = mShowBitmapRect.width() == 0 ? 0 : mViewRect.width() * 1f / mShowBitmapRect.width();
        float hs = mShowBitmapRect.height() == 0 ? 0 : mViewRect.height() * 1f / mShowBitmapRect.height();

        return Math.max(ws, hs);
    }

    /**
     * 获取最小适应view 的缩放倍数
     */
    private float getMinFitViewScaleFactor() {
        float ws = mShowBitmapRect.width() == 0 ? 0 : mViewRect.width() * 1f / mShowBitmapRect.width();
        float hs = mShowBitmapRect.height() == 0 ? 0 : mViewRect.height() * 1f / mShowBitmapRect.height();

        return Math.min(ws, hs);
    }

    /**
     * 获取采样率
     */
    private int calculateSampleSize(int size) {
        int sampleSize = 1;
        while (size >> 1 != 0) {
            sampleSize = sampleSize << 1;
            size = size >> 1;
        }
        return sampleSize;
    }

    /**
     * 获取当前的SampleSize 值
     */
    private int getCurSampleSize() {
        int sampleSize = 0;
        try {
            int iw = mImageRect.width();
            int ih = mImageRect.height();
            int bw = (int) mShowBitmapRect.width();
            int bh = (int) mShowBitmapRect.height();
            if (bw * bh == 0) {
                return 1;
            }

            /**
             * 以 bitmap 的宽高为标准
             * 分别以 宽高为标准，计算对应的的宽高
             * 如果是宽图, 则以View的宽为标准
             * 否则为高图， 以view的高为标准
             * 求出 SampleSize
             */
            int width = (int) (iw * 1.0f / ih * bh);
            sampleSize = (width > bw) ? calculateSampleSize(iw / bw) : calculateSampleSize(ih / bh);
            if (sampleSize < 1) {
                sampleSize = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sampleSize;
    }

    /**
     * 设置图片开始
     */
    private synchronized void onSetImageStart() {
        sIsSettingImage = true;
    }

    /**
     * 设置图片结束
     */
    private synchronized void onSetImageFinished(final boolean success) {
        sIsSettingImage = false;

        final Rect image = new Rect();
        if (success) {
            image.set(mImageRect);
        }

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                mPhotoView.onSetImageFinished(PhotoViewAttacher.this, success, image);
                mPhotoView.callPostInvalidate();
            }
        });
    }

    /**
     * 回收所有内存
     */
    private void recycleAll() {
        mBitmapGrid.recycleAllGrids();

        synchronized (mDecodeSyncLock) {
            if (mBmDecoder != null) {
                mBmDecoder.recycle();
                mBmDecoder = null;
            }

            mSrcBitmap = null;
        }
    }

    @Override
    public boolean draw(@NonNull Canvas canvas, int width, int height) {
        if (isNotAvailable()) {
            return false;
        }

        if (mSrcBitmap != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            int mw = canvas.getMaximumBitmapWidth();
            int mh = canvas.getMaximumBitmapHeight();

            /**
             * 如果图片太大，直接使用bitmap 会占用很大内存，所以建议缓存为文件再显示
             */
            if (mSrcBitmap.getHeight() > mh || mSrcBitmap.getWidth() > mw) {
                //TODO
            }
        }

        /**
         * 更新视图或者画出图片
         */
        return !checkOrUpdateViewRect(width, height) && mBitmapGrid.drawVisibleGrid(canvas);
    }

    @Override
    public int move(int dx, int dy) {
        if (isNotAvailable()) {
            return NONE;
        }

        int result = 0;
        try {
            Rect oRect = new Rect();
            toViewCoordinate(mShowBitmapRect).round(oRect);

            /**
             * 检测边界
             */
            int rx = dx;
            int ry = dy;

            if (oRect.left >= 0 && oRect.right <= mViewRect.right) {
                rx = Integer.MAX_VALUE;
            }

            if (oRect.top >= 0 && oRect.bottom <= mViewRect.bottom) {
                ry = Integer.MAX_VALUE;
            }

            if (rx != Integer.MAX_VALUE) {
                if (oRect.left + dx > 0) {
                    rx = -oRect.left;
                }

                if (oRect.right + dx < mViewRect.right) {
                    rx = mViewRect.right - oRect.right;
                }

                if (oRect.left + dx > 0 && oRect.right + dx < mViewRect.right) {
                    rx = mViewRect.centerX() - oRect.centerX();
                }
            }

            if (ry != Integer.MAX_VALUE) {
                if (oRect.top + dy > 0) {
                    ry = -oRect.top;
                }

                if (oRect.bottom + dy < mViewRect.bottom) {
                    ry = mViewRect.bottom - oRect.bottom;
                }

                if (oRect.top + dy > 0 && oRect.bottom + dy < mViewRect.bottom) {
                    ry = mViewRect.centerY() - oRect.centerY();
                }
            }

            mViewBitmapRect.offset(-(rx == Integer.MAX_VALUE ? 0 : rx), -(ry == Integer.MAX_VALUE ? 0 : ry));
            mPhotoView.callPostInvalidate();

            /**
             * 检查到达边界的方向
             */
            Rect detectRect = new Rect(mViewBitmapRect);
            result = NONE;
            if (detectRect.left <= 0) {
                result |= LEFT;
            }
            if (detectRect.right >= (int) mShowBitmapRect.right) {
                result |= RIGHT;
            }

            if (detectRect.top <= 0) {
                result |= TOP;
            }
            if (detectRect.bottom >= (int) mShowBitmapRect.bottom) {
                result |= BOTTOM;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void onViewSizeChanged(int width, int height) {
        checkOrUpdateViewRect(width, height);
    }

    @Override
    public void scale(float cx, float cy, float scale) {
        if (isNotAvailable()) {
            return;
        }

        try {
            RectF viewRect = new RectF(mViewRect);
            /**
             * 如果图片的长或宽，全在视图内，则以中线进行缩放
             */
            RectF oRect = toViewCoordinate(mShowBitmapRect);

            /**
             * 如果宽全在视图内
             */
            if (oRect.left > 0 && oRect.right < mViewRect.right) {
                cx = viewRect.centerX();
            }

            /**
             * 如果高全在视图内
             */
            if (oRect.top > 0 && oRect.bottom < mViewRect.bottom) {
                cy = viewRect.centerY();
            }

            /**
             * 以cx, cy缩放
             */
            float left = (cx - Math.abs(cx - oRect.left) * scale);
            float top = (cy - Math.abs(cy - oRect.top) * scale);

            float right = left + oRect.width() * scale;
            float bottom = top + oRect.height() * scale;

            RectF nRect = new RectF(left, top, right, bottom);

            if (nRect.width() <= mInitiatedShowBitmapRect.width() - 1 || nRect.height() <= mInitiatedShowBitmapRect.height() - 1) {
                resetShowBitmapRect();
                return;
            }

            float scaleValue = nRect.width() / mImageRect.width();
            if (scaleValue > mMaxScaleValue || scaleValue < mMinScaleValue) {
                // 不能再放大或者缩小了
                return;
            }

            /**
             * 更新ViewBitmapRect坐标, 并更新显示的bitmap rect 大小
             */
            updateViewBitmapRect(nRect);

            /**
             * 如果还是小于视图宽度，则需要移动到正中间
             */
            float nx = 0;
            float ny = 0;
            RectF aRect = toViewCoordinate(mShowBitmapRect);
            if (aRect.width() < viewRect.width()) {
                nx = viewRect.centerX() - aRect.centerX();
            } else {
                if (aRect.left > 0) {
                    nx = -aRect.left;
                } else if (aRect.right < viewRect.width()) {
                    nx = viewRect.width() - aRect.right;
                }
            }

            if (aRect.height() < viewRect.height()) {
                ny = viewRect.centerY() - aRect.centerY();
            } else {
                if (aRect.top > 0) {
                    ny = -aRect.top;
                } else if (aRect.bottom < viewRect.height()) {
                    ny = viewRect.height() - aRect.bottom;
                }
            }

            aRect.offset(nx, ny);
            updateViewBitmapRect(aRect);

            mPhotoView.callPostInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void scaleTo(final int cx, final int cy, float dest, boolean smooth, long smoothTime) {
        if (isNotAvailable()) {
            return;
        }

        try {
            if (mValueAnimator != null && mValueAnimator.isRunning()) {
                mValueAnimator.end();
                mValueAnimator.cancel();
            }

            if (smooth) {
                mLastAnimatedValue = 1f;
                ObjectAnimator.ofFloat(1f, dest);
                mValueAnimator = ValueAnimator.ofFloat(1f, dest);
                mValueAnimator.setDuration(smoothTime);
                mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        scale(cx, cy, value / mLastAnimatedValue);
                        mLastAnimatedValue = value;
                    }
                });

                mValueAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        //
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        updateSampleSize();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        updateSampleSize();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                mValueAnimator.start();
            } else {
                scale(cx, cy, dest);
                updateSampleSize();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void scaleTo(float dest, boolean smooth, long smoothTime) {
        try {
            scaleTo(mViewRect.centerX(), mViewRect.centerY(), dest, smooth, smoothTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doubleTapScale(int cx, int cy, boolean smooth, long smoothTime) {
        try {
            if (mValueAnimator != null && mValueAnimator.isRunning()) {
                return;
            }

            if (isNotAvailable() && mShowBitmapRect.height() > 0 && mShowBitmapRect.width() > 0) {
                return;
            }

            float destScale = 0;

            float sw = mShowBitmapRect.width();
            float sh = mShowBitmapRect.height();

            int tw = mInitiatedShowBitmapRect.width();
            int th = mInitiatedShowBitmapRect.height();

            float maxFitScale = getMaxDoubleTapScaleFactor();
            float minFitScale = getMinFitViewScaleFactor();

            IXphotoView.DoubleTabScale scale = mPhotoView.getDoubleTabScale();
            if (scale == null) {
                scale = IXphotoView.DoubleTabScale.CENTER_CROP;
            }

            switch (scale) {
                case CENTER_INSIDE:
                    if (sw < mViewRect.width() + 5f && sh < mViewRect.height() + 5f) {
                        destScale = maxFitScale;
                    } else {
                        destScale = minFitScale;
                    }
                    break;

                case CENTER_CROP:
                    if ((Math.abs(sw - tw) < 5 && Math.abs(sh - th) < 5)) {
                        destScale = maxFitScale;
                    } else {
                        float ws = mImageRect.width() * 1f / mShowBitmapRect.width();
                        float hs = mImageRect.height() * 1f / mShowBitmapRect.height();

                        destScale = Math.min(minFitScale, Math.min(ws, hs));
                    }
                    break;
            }
            scaleTo(cx, cy, destScale, smooth, smoothTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void scaleToFitViewMax(int cx, int cy, boolean smooth, long smoothTime) {
        scaleTo(cx, cy, getMaxFitViewScaleFactor(), smooth, smoothTime);
    }

    @Override
    public void scaleToFitViewMin(int cx, int cy, boolean smooth, long smoothTime) {
        scaleTo(cx, cy, getMinFitViewScaleFactor(), smooth, smoothTime);
    }

    @Override
    public boolean isNotAvailable() {
        return (sIsSettingImage ||
                (mSrcBitmap == null && mBmDecoder == null) ||
                mImageRect.width() <= 0 || mImageRect.height() <= 0);
    }

    @Override
    public boolean isTapOnImage(int x, int y) {
        return !isNotAvailable() && toViewCoordinate(mShowBitmapRect).contains(x, y);
    }

    @Override
    public boolean isSettingImage() {
        return sIsSettingImage;
    }

    @Override
    public Rect getRealImageRect() {
        return mImageRect;
    }

    @Override
    public Rect getCurImageRect() {
        return new Rect(0, 0, (int) mShowBitmapRect.width(), (int) mShowBitmapRect.height());
    }

    @Override
    public float getCurScaleFactor() {
        if (isNotAvailable()) {
            return 0;
        }

        return mShowBitmapRect.height() * 1f / mImageRect.height();
    }

    @Override
    public void updateSampleSize() {
        if (isNotAvailable()) {
            return;
        }

        int sampleSize = getCurSampleSize();
        if (sampleSize == mSampleSize) {
            return;
        }
        mSampleSize = sampleSize;
        mPhotoView.callPostInvalidate();
    }

    @Override
    public void destroy() {
        if (mLoadingThread != null) {
            mLoadingThread.quit();
        }
        mLoadingThread = null;
        if (mCacheFile != null) {
            mCacheFile.delete(); // 删除临时文件
        }
        recycleAll();

        mPhotoView.callPostInvalidate();
    }

    /**
     * 将原图解析出一块 bitmap
     */
    private Bitmap decodeRectBitmap(Rect rect, int sampleSize) {
        if (rect == null || !mImageRect.contains(rect)) {
            return null;
        }
        try {
            synchronized (mDecodeSyncLock) {
                if (mSrcBitmap != null) {
                    try {
                        checkRectSize(rect);
                        return Bitmap.createBitmap(mSrcBitmap, rect.left, rect.top, rect.width(), rect.height());
                    } catch (OutOfMemoryError exp) {
                        mPhotoView.onSetImageFinished(null, false, null);
                        return null;
                    }
                } else if (mBmDecoder != null && !mBmDecoder.isRecycled()) {
                    BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
                    tmpOptions.inPreferredConfig = mBitmapConfig;
                    tmpOptions.inSampleSize = sampleSize;
                    tmpOptions.inJustDecodeBounds = false;

                    try {
                        return mBmDecoder.decodeRegion(rect, tmpOptions);
                    } catch (OutOfMemoryError error) {
                        tmpOptions.inSampleSize = tmpOptions.inSampleSize * 2;
                        return mBmDecoder.decodeRegion(rect, tmpOptions);
                    }
                }
            }
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void checkRectSize(Rect rect) {
        if (rect.right > mSrcBitmap.getWidth()) {
            rect.right -= rect.right - mSrcBitmap.getWidth();
        }

        if (rect.bottom > mSrcBitmap.getHeight()) {
            rect.bottom -= rect.bottom - mSrcBitmap.getHeight();
        }
    }

    /**
     * BitmapUnit 是将 Bitmap 分割为 N * M 个方块后的单个方块
     * 长期持有当前方块的初始化 bitmap 方便在渲染时提供占用内存较小的 bitmap
     * 渲染时优先级  mInitiatedThumbBitmap -> mBitmap
     * 若 mBitmap 不存在或 SampleSize 与当前全局 SampleSize 不符则重新 decode
     */
    private class BitmapUnit {
        /**
         * 是否正在加载bitmap
         */
        private boolean mIsLoading = false;

        /**
         * 当前bitmap 的SampleSize,
         * 如果此时的SampleSize 和全局的SampleSize不相等，则需要重新decode一次
         */
        public int mCurSampleSize = 0;

        /**
         * 目前的 mBitmap
         */
        public Bitmap mBitmap = null;

        /**
         * 初始化的缩略图的bitmap
         */
        public Bitmap mInitiatedThumbBitmap = null;

        /**
         * 这里回收所有的bitmap
         */
        private void recycleMemory() {
            mBitmap = null;
            mInitiatedThumbBitmap = null;

            mCurSampleSize = 0;
        }

        /**
         * 这里只回收正常的bitmap, 不回收缩略图的bitmap
         */
        private void recycle() {
            mBitmap = null;
            mCurSampleSize = mThumbSampleSize;
        }
    }

    /**
     * 图片网格化解析策略
     * 初始化时将原图以View尺寸切割为 N * M 个方块并解析为 N * M 个缩略图保存在 mGrids中
     */
    private class BitmapGridStrategy {
        /**
         * 总共的单元格数
         */
        private int mGridHeight = 0;
        private int mGridWidth = 0;

        /**
         * 所有的单元格
         */
        private BitmapUnit[][] mGrids = null;

        private void initializeBitmapGrid() {
            try {
                if (mGrids != null) {
                    recycleAllGrids();
                }

                int vw = mViewRect.width();
                int vh = mViewRect.height();

                int iw = mImageRect.width();
                int ih = mImageRect.height();

                mGridHeight = ih / vh + (ih % vh == 0 ? 0 : 1);
                mGridWidth = iw / vw + (iw % vw == 0 ? 0 : 1);

                mGrids = new BitmapUnit[mGridHeight][mGridWidth];
                for (int i = 0; i < mGridHeight; ++i) {
                    for (int j = 0; j < mGridWidth; ++j) {
                        mGrids[i][j] = new BitmapUnit();
                        mGrids[i][j].mCurSampleSize = mSampleSize;
                    }
                }

                /**
                 * 异步加载缩略图
                 */
                if (mLoadingThread.isAlive()) {
                    mLoadingHandler.post(mDecodeThumbRunnable);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Runnable mDecodeThumbRunnable = new Runnable() {
            @Override
            public void run() {
                decodeThumbUnitBitmap();
                onSetImageFinished(true);
            }
        };

        /**
         * 解码为缩略图的 bitmap
         */
        private void decodeThumbUnitBitmap() {
            try {
                for (int n = 0; n < mGridHeight; ++n) {
                    for (int m = 0; m < mGridWidth; ++m) {
                        Rect rect = getUnitRect(n, m);
                        if (rect != null) {
                            mGrids[n][m].mCurSampleSize = mSampleSize;
                            mGrids[n][m].mInitiatedThumbBitmap = decodeRectBitmap(rect, mGrids[n][m].mCurSampleSize);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 获取bitmap
         */
        private Bitmap getGridBitmap(final int n, final int m) {
            try {
                if (isValidGrid(n, m)) {
                    BitmapUnit unit = mGrids[n][m];
                    if (mSrcBitmap != null) {
                        return unit.mInitiatedThumbBitmap;
                    }

                    if (mSampleSize == mThumbSampleSize) {
                        return unit.mInitiatedThumbBitmap;
                    }

                    if (unit.mCurSampleSize != mSampleSize) {
                        loadUnitBitmap(n, m);
                    }

                    return (unit.mBitmap != null && !unit.mBitmap.isRecycled()) ? unit.mBitmap : unit.mInitiatedThumbBitmap;
                }
            } catch (OutOfMemoryError outOfMemoryError){
                outOfMemoryError.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * 异步就加载单元格bitmap
         */
        private void loadUnitBitmap(final int n, final int m) {
            try {
                if (mSampleSize != mThumbSampleSize && isValidGrid(n, m)) {
                    BitmapUnit unit = mGrids[n][m];
                    if (unit.mIsLoading) {
                        return;
                    }
                    unit.mIsLoading = true;

                    mLoadingHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isValidGrid(n, m)) {
                                    decodeVisibleUnitBitmap(n, m);
                                    mGrids[n][m].mIsLoading = false;
                                    if (mGrids[n][m].mCurSampleSize != mSampleSize) {
                                        return;
                                    }
                                    mPhotoView.callPostInvalidate();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 回收所有的单元格
         */
        private void recycleAllGrids() {
            try {
                for (int i = 0; i < mGridHeight; ++i) {
                    for (int j = 0; j < mGridWidth; ++j) {
                        mGrids[i][j].recycleMemory();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 判断是否为有效的单元格
         */
        private boolean isValidGrid(int n, int m) {
            return n >= 0 && n < mGridHeight && m >= 0 && m < mGridWidth;
        }

        /**
         * 得出原图的单元格
         */
        private Rect getUnitRect(int n, int m) {
            if (n < 0 || n >= mGridHeight || m < 0 || m >= mGridWidth) {
                return null;
            }

            int vw = mViewRect.width();
            int vh = mViewRect.height();

            int iw = mImageRect.width();
            int ih = mImageRect.height();

            int left = Math.min(iw, m * vw);
            int right = Math.min(iw, left + vw);

            int top = Math.min(ih, n * vh);
            int bottom = Math.min(ih, top + vh);

            if (left == right || top == bottom) {
                return null;
            }

            return new Rect(left, top, right, bottom);
        }

        /**
         * 获取显示的单元格rect
         * 原始图坐标系
         */
        private Rect getShowBitmapUnit(int n, int m) {
            float bitmapRatio = mShowBitmapRect.height() * 1f / mImageRect.height();
            RectF vRect = rectMulti(mViewRect, bitmapRatio);

            float vw = vRect.width();
            float vh = vRect.height();

            float sWidth = mShowBitmapRect.width();
            float sHeight = mShowBitmapRect.height();

            float left = Math.min(m * vw, sWidth);
            float right = Math.min(left + vw, sWidth);

            float top = Math.min(n * vh, sHeight);
            float bottom = Math.min(top + vh, sHeight);

            return new Rect((int) left, (int) top, (int) right, (int) bottom);
        }

        /**
         * 判断是否是可见的单元格
         */
        private boolean isVisibleUnit(int n, int m) {
            Rect v = getVisibleGrid();

            return n >= v.top && n <= v.bottom && m >= v.left && m <= v.right;
        }

        /**
         * 回收不可见区域的bitmap
         *
         * @param visible 可见区域
         */
        private void recycleInvisibleGrids(Rect visible) {
            if (mGrids == null) {
                return;
            }

            int sn = visible.top;
            int sm = visible.left;
            int en = visible.bottom;
            int em = visible.right;

            /**
             * 如果上一次有不可见的，并距离可见区域 > 1 的，就释放掉
             * +--+--+--+--+--+
             * |XX|XX|11|11|XX|
             * +--+--+--+--+--+
             * |XX|XX|11|11|XX|
             * +--+--+--+--+--+
             * |XX|XX|XX|XX|XX|
             * +--+--+--+--+--+
             * XX 部分就是可以被释放掉的区域
             */
            try {
                int mn = 1;
                for (int i = 0; i < mGridHeight; ++i) {
                    for (int j = 0; j < mGridWidth; ++j) {
                        if (sn - i >= mn || i - en >= mn || sm - j >= mn || j - em >= mn) {
                            mGrids[i][j].recycle();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 画出可见的几个格子
         */
        private boolean drawVisibleGrid(Canvas canvas) {
            if ((mSrcBitmap == null && mBmDecoder == null) ||
                    mGrids == null || mImageRect.width() <= 0 || mImageRect.height() <= 0) {
                return false;
            }

            try {
                Rect visible = getVisibleGrid();
                recycleInvisibleGrids(visible);

                int sn = visible.top;
                int sm = visible.left;
                int en = visible.bottom;
                int em = visible.right;

                for (int n = sn; n <= en; ++n) {
                    for (int m = sm; m <= em; ++m) {
                        Rect rect = getShowBitmapUnit(n, m);
                        Bitmap bitmap = getGridBitmap(n, m);
                        if (bitmap != null) {
                            Rect vRect = toViewCoordinate(rect);
                            canvas.drawBitmap(bitmap, null, vRect, null);
                        }
                    }
                }
            } catch (OutOfMemoryError outOfMemoryError){
                outOfMemoryError.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        /**
         * decode出一个可见单元的bitmap
         * 并保存这个bitmap的 sample size
         */
        private synchronized void decodeVisibleUnitBitmap(int n, int m) {
            if (isValidGrid(n, m) && isVisibleUnit(n, m)) {
                BitmapUnit unit = mGrids[n][m];

                // 防止二次decode
                if (unit.mCurSampleSize == mSampleSize) {
                    return;
                }

                unit.recycle();
                Rect rect = getUnitRect(n, m);
                unit.mCurSampleSize = mSampleSize;
                try {
                    unit.mBitmap = decodeRectBitmap(rect, unit.mCurSampleSize);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 计算出可见的实际单元格
         *
         * @return Rect (left=sm, top=sn, right=em, bottom=en)
         */
        private Rect getVisibleGrid() {
            /**  计算图片压缩比然后将可视部分rect转换到原图坐标上*/
            float ratio = mImageRect.height() / mShowBitmapRect.height();
            RectF vBRect = rectMulti(getVisibleShowBitmapRect(), ratio);

            /** 因为Grid是将原图以ViewRect切割，于是用 ViewRect 再次计算可见 Grid 坐标*/
            int sm = (int) (vBRect.left / mViewRect.width());
            int sn = (int) (vBRect.top / mViewRect.height());

            int em = (int) (sm + Math.ceil(vBRect.width() / mViewBitmapRect.width()));
            int en = (int) (sn + Math.ceil(vBRect.height() / mViewBitmapRect.height()));

            em = em > mGridWidth ? mGridWidth : em;
            en = en > mGridHeight ? mGridHeight : en;
            return new Rect(sm, sn, em, en);
        }
    }

    /***
     * 返回 mViewBitmapRect，左右上下最大值不超过 mShowBitmapRect
     * @return
     */
    private Rect getVisibleShowBitmapRect() {
        int left = (int) Math.max(mShowBitmapRect.left, mViewBitmapRect.left);
        int right = (int) Math.min(mShowBitmapRect.right, mViewBitmapRect.right);
        int top = (int) Math.max(mShowBitmapRect.top, mViewBitmapRect.top);
        int bottom = (int) Math.min(mShowBitmapRect.bottom, mViewBitmapRect.bottom);

        return new Rect(left, top, right, bottom);
    }

    /**
     * 坐标转换, bitmap坐标转换为 view 坐标
     */
    private Rect toViewCoordinate(Rect rect) {
        if (rect == null) {
            return new Rect();
        }

        int left = rect.left - mViewBitmapRect.left;
        int right = left + rect.width();
        int top = rect.top - mViewBitmapRect.top;
        int bottom = top + rect.height();

        return new Rect(left, top, right, bottom);
    }

    private RectF toViewCoordinate(RectF rect) {
        if (rect == null) {
            return new RectF();
        }

        float left = rect.left - mViewBitmapRect.left;
        float right = left + rect.width();
        float top = rect.top - mViewBitmapRect.top;
        float bottom = top + rect.height();

        return new RectF(left, top, right, bottom);
    }

    private RectF rectMulti(Rect r, float ratio) {
        return rectMulti(new RectF(r), ratio);
    }

    private RectF rectMulti(RectF r, float ratio) {
        float left = r.left * ratio;
        float top = r.top * ratio;
        float right = left + r.width() * ratio;
        float bottom = top + r.height() * ratio;

        return new RectF(left, top, right, bottom);
    }
}
