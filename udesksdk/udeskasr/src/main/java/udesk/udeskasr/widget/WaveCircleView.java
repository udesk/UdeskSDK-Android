package udesk.udeskasr.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import udesk.udeskasr.R;

/**
 * Created by Administrator
 */
public class WaveCircleView extends View  {
    //波纹生成时的半径
    private float mWaveRadiusMin;
    //波纹消失前的半径
    private float mWaveRadiusMax;
    //每条波纹持续时间
    private long mWaveDuration;
    //波纹生成速度
    private long mWaveCreatedSpeed;
    private Paint mPaint;
    //画笔是否为stroke模式（即线条）
    private boolean stroke = false;
    //中间图标画笔
    private Paint mCenterBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //中间图标区域
    private Rect mCenterBitmapArea = new Rect();
    //波纹颜色
    private int mWaveColor;
    //波纹动画效果
    private Interpolator mInterpolator = new AccelerateInterpolator();
    //所有的水波纹
    private List<ValueAnimator> mAnimatorList = new ArrayList<>();
    //是否开启水波纹
    private boolean mIsRuning = false;
    //是否点击了中间图标
    private boolean mIsCenterClick = false;
    //中间的图标
    private Bitmap mCenterBitmap;
    //中间的圆形图标
    private Bitmap mCenterCircleBitmap;
    //旋转动画
    private Animation operatingAnim;
    //语音识别监听
    private ASRListener mASRListener;

    public WaveCircleView(Context context) {
        this(context, null);
    }

    public WaveCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveCircleView, 0, defStyleAttr);
        mWaveColor = typedArray.getColor(R.styleable.WaveCircleView_udesk_color,getResources().getColor(R.color.udesk_color_307AE8));
        mCenterBitmap = BitmapFactory.decodeResource(getResources(), typedArray.getResourceId(R.styleable.WaveCircleView_udesk_image, R.drawable.udesk_mic));
        mWaveDuration = typedArray.getInteger(R.styleable.WaveCircleView_udesk_duration, 1000);
        mWaveCreatedSpeed = typedArray.getInteger(R.styleable.WaveCircleView_udesk_waveCreateSpeed, 500);
        stroke = typedArray.getBoolean(R.styleable.WaveCircleView_udesk_stroke, false);
        typedArray.recycle();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(mWaveColor);
        mPaint.setDither(true);
        if (stroke) {
            mPaint.setStyle(Paint.Style.STROKE);
        } else {
            mPaint.setStyle(Paint.Style.FILL);
        }
        if (mCenterBitmap == null) {
            mCenterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.udesk_mic);
            mWaveRadiusMin = Math.min(mCenterBitmap.getWidth(), mCenterBitmap.getHeight()) / 2;
        }
        mWaveRadiusMin = Math.min(mCenterBitmap.getWidth(), mCenterBitmap.getHeight()) / 2;
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mASRListener!=null){
                    mASRListener.start();
                }
                return false;
            }
        });
    }

    private Runnable mWaveRunable = new Runnable() {
        @Override
        public void run() {
            if (mIsRuning) {
                newWaveAnimator();
                invalidate();
                postDelayed(mWaveRunable, mWaveCreatedSpeed);
            }
        }
    };

    //开启动画
    public void start() {
        if (!mIsRuning) {
            mIsRuning = true;
            mWaveRunable.run();
        }
    }

    //是否开启水波纹
    public boolean isStart() {
        return mIsRuning;
    }

    //关闭动画
    public void stop() {
        mIsRuning = false;
        clearAnimation();
    }

    //设置水波纹颜色
    public void setColor(int color) {
        mWaveColor = color;
        postInvalidate();
    }

    /**
     * 设置水波纹效果
     *
     * @param interpolator 时间插值器 默认使用的是 2 AccelerateInterpolator
     *                     0 PathInterpolator 可以定义路径坐标，然后可以按照路径坐标来
     *                     1 AccelerateDecelerateInterpolator 在动画开始与结束的地方速率改变比较慢，在中间的时候加速
     *                     2 AccelerateInterpolator  在动画开始的地方速率改变比较慢，然后开始加速
     *                     3 AnticipateInterpolator 开始的时候向后然后向前甩
     *                     4 AnticipateOvershootInterpolator 开始的时候向后然后向前甩一定值后返回最后的值
     *                     5 BounceInterpolator   动画结束的时候弹起
     *                     6 CycleInterpolator 动画循环播放特定的次数，速率改变沿着正弦曲线
     *                     7 DecelerateInterpolator 在动画开始的地方快然后慢
     *                     8 LinearInterpolator   以常量速率改变
     *                     9 OvershootInterpolator    向前甩一定值后再回到原来位置
     */
    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
        postInvalidate();
    }

    //设计水波纹持续时间
    public void setDuration(long duration) {
        mWaveDuration = duration;
        postInvalidate();
    }

    //设置水波纹间隔时间
    public void setWaveCreatedSpeed(long speed) {
        mWaveCreatedSpeed = speed;
        postInvalidate();
    }

    //初始波纹半径
    public float getWaveRadiusMin() {
        return mWaveRadiusMin;
    }

    //是否画笔stroke
    public void setStroke(boolean stroke) {
        this.stroke = stroke;
        postInvalidate();
    }

    public boolean isStroke() {
        return stroke;
    }

    private ValueAnimator newWaveAnimator() {
        final ValueAnimator mWaveAnimator = new ValueAnimator();
        mWaveAnimator.setFloatValues(mWaveRadiusMin, mWaveRadiusMax);
        mWaveAnimator.setDuration(mWaveDuration);
        mWaveAnimator.setRepeatCount(0);
        mWaveAnimator.setInterpolator(mInterpolator);
        mWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                (Float) animation.getAnimatedValue();
            }
        });
        mAnimatorList.add(mWaveAnimator);
        mWaveAnimator.start();
        return mWaveAnimator;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                stop();
                if (mASRListener!=null){
                    mASRListener.stop();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWaveRadiusMax = Math.min(w, h) / 2;
        //计算中间图标区域
        mCenterBitmapArea.set((w - mCenterBitmap.getWidth()) / 2, (h - mCenterBitmap.getHeight()) / 2
                , (w + mCenterBitmap.getWidth()) / 2, (h + mCenterBitmap.getHeight()) / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Iterator<ValueAnimator> iterator = mAnimatorList.iterator();
        while (iterator.hasNext()) {
            ValueAnimator valueAnimator = iterator.next();
//            Log.e("AnimatedValue",(float)valueAnimator.getAnimatedValue() + "mWaveRadiusMax:" + mWaveRadiusMax);
            if (!valueAnimator.getAnimatedValue().equals(mWaveRadiusMax)) {
                //设置透明度
                mPaint.setAlpha(getAlpha((Float) valueAnimator.getAnimatedValue()));
                //画水波纹
                canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, (Float) valueAnimator.getAnimatedValue(), mPaint);
            } else {
                valueAnimator.cancel();
                iterator.remove();
            }
        }

        //绘制中间图标
        drawCenterBitmap(canvas);
        if (mAnimatorList.size() > 0) {
            postInvalidateDelayed(10);
        }
    }

    //绘制中间图标
    private void drawCenterBitmap(Canvas canvas) {
        if (mCenterCircleBitmap == null) {
            mCenterCircleBitmap = createCircleImage(mCenterBitmap, mCenterBitmap.getWidth());
        }
        canvas.drawBitmap(mCenterCircleBitmap, null, mCenterBitmapArea, mCenterBitmapPaint);
    }

    //根据原图和边长绘制圆形图片
    private Bitmap createCircleImage(Bitmap source, int min) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        //产生一个同样大小的画布
        Canvas canvas = new Canvas(target);
        //首先绘制圆形
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        //使用SRC_IN
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //绘制图片
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    //获取水波纹透明度
    private int getAlpha(float mRadius) {
        int alpha = 1;
        if (mWaveRadiusMax > 0) {
            alpha = (int) ((1 - (mRadius - mWaveRadiusMin) / (mWaveRadiusMax - mWaveRadiusMin)) * 255);
        }
        return alpha;
    }

    public void setmCenterBitmap(Bitmap mCenterBitmap) {
        this.mCenterBitmap = mCenterBitmap;
    }
    public void setmASRListener(ASRListener mASRListener) {
        this.mASRListener = mASRListener;
    }

    public interface ASRListener{
        void start();
        void stop();
    }
}