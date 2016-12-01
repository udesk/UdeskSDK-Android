package cn.udesk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import cn.udesk.R;

/**
 * Created by Droidroid on 2016/4/24.
 */
public class ChatImageView extends ImageView {

    private final int DIRECTION_LEFT = 0;
    private final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
    private final int DEFAULT_COLOR_DRAWABLE_DIMENSION = dp2px(250);

    // 箭头宽度
    private float mArrowWidth;
    // 箭头高度
    private float mArrowHeight;
    // 箭头偏移量
    private float mArrowOffset;
    // 箭头至View顶部的距离
    private float mArrowTop;
    // 圆角半径
    private float mRadius;
    // 箭头朝向方向
    private int mDirection;

    private BitmapShader mBitmapShader;
    private Paint mPaint;

    // .9.png
    private NinePatchDrawable mNinePatchDrawable;

    private Canvas mCanvas;
    private Bitmap mSrc;
    private Bitmap mTarget;

    public ChatImageView(Context context) {
        this(context, null);
    }

    public ChatImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.UdeskChatImageView);
        mNinePatchDrawable = (NinePatchDrawable) ta.getDrawable(R.styleable.UdeskChatImageView_udesk_ninePNG);
        mArrowHeight = ta.getDimension(R.styleable.UdeskChatImageView_udesk_arrow_height, dp2px(20));
        mArrowWidth = ta.getDimension(R.styleable.UdeskChatImageView_udesk_arrow_width, mArrowHeight);
        mArrowOffset = ta.getDimension(R.styleable.UdeskChatImageView_udesk_offset, mArrowHeight / 2);
        mArrowTop = ta.getDimension(R.styleable.UdeskChatImageView_udesk_arrow_top, mArrowHeight);
        mRadius = ta.getDimension(R.styleable.UdeskChatImageView_udesk_radius, dp2px(10));
        mDirection = ta.getInteger(R.styleable.UdeskChatImageView_udesk_direction, 0);
        ta.recycle();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mNinePatchDrawable != null) {
            canvas.drawBitmap(mTarget, 0, 0, null);
            mPaint.setXfermode(null);
        } else {
            if (mDirection == DIRECTION_LEFT) {
                canvas.drawPath(drawLeftPath(), mPaint);
            } else {
                canvas.drawPath(drawRightPath(), mPaint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    private void setup() {
        if (getWidth() == 0 && getHeight() == 0) {
            return;
        }
        if (mSrc == null) {
            return;
        }
        if (mNinePatchDrawable != null) {
            setupWithNinePatch();
        } else {
            setupWidthBitmapShader();
        }
    }

    private void setupWidthBitmapShader() {
        mBitmapShader = new BitmapShader(mSrc, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint.setShader(mBitmapShader);
        updateMatrix();
    }

    private void setupWithNinePatch() {
        mTarget = Bitmap.createBitmap(mSrc.getWidth(), mSrc.getHeight(), BITMAP_CONFIG);
        mCanvas = new Canvas(mTarget);
        mNinePatchDrawable.setBounds(0, 0, getRight() - getLeft(), getBottom() - getTop());
        mNinePatchDrawable.draw(mCanvas);

        updateMatrix();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mCanvas.drawBitmap(mSrc, 0, 0, mPaint);
    }

    private Path drawRightPath() {
        Path path = new Path();
        RectF rectF = new RectF(getPaddingLeft(), getPaddingTop()
                , getRight() - getLeft() - getPaddingRight()
                , getBottom() - getTop() - getPaddingBottom());
        path.moveTo(rectF.left + mRadius, rectF.top);
        // top line
        path.lineTo(rectF.right - mRadius - mArrowWidth, rectF.top);
        // top right corner
        path.arcTo(new RectF(rectF.right - mArrowWidth - 2 * mRadius, rectF.top
                , rectF.right - mArrowHeight, rectF.top + 2 * mRadius), 270, 90);
        // right arrow
        path.lineTo(rectF.right - mArrowWidth, rectF.top + mArrowTop);
        path.lineTo(rectF.right, rectF.top + mArrowTop + mArrowOffset);
        path.lineTo(rectF.right - mArrowWidth, rectF.top + mArrowTop + mArrowHeight);
        path.lineTo(rectF.right - mArrowWidth, rectF.bottom - mRadius);
        // bottom right corner
        path.arcTo(new RectF(rectF.right - mArrowWidth - 2 * mRadius, rectF.bottom - 2 * mRadius
                , rectF.right - mArrowWidth, rectF.bottom), 0, 90);
        // bottom line
        path.lineTo(rectF.left + mRadius, rectF.bottom);
        //bottom left corner
        path.arcTo(new RectF(rectF.left, rectF.bottom - 2 * mRadius, rectF.left + 2 * mRadius, rectF.bottom), 90, 90);
        // left line
        path.lineTo(rectF.left, rectF.top + mRadius);
        // top left corner
        path.arcTo(new RectF(rectF.left, rectF.top, rectF.left + 2 * mRadius, rectF.top + 2 * mRadius), 180, 90);
        path.close();
        return path;
    }

    private Path drawLeftPath() {
        Path path = new Path();
        RectF rectF = new RectF(getPaddingLeft(), getPaddingTop()
                , getRight() - getLeft() - getPaddingRight()
                , getBottom() - getTop() - getPaddingBottom());
        path.moveTo(rectF.left + mRadius + mArrowWidth, rectF.top);
        // top line
        path.lineTo(rectF.right - mRadius, rectF.top);
        // top right corner
        path.arcTo(new RectF(rectF.right - 2 * mRadius, rectF.top
                , rectF.right, rectF.top + 2 * mRadius), 270, 90);
        // right line
        path.lineTo(rectF.right, rectF.bottom - mRadius);
        // bottom right corner
        path.arcTo(new RectF(rectF.right - 2 * mRadius, rectF.bottom - 2 * mRadius
                , rectF.right, rectF.bottom), 0, 90);
        // bottom line
        path.lineTo(rectF.left + mArrowWidth + mRadius, rectF.bottom);
        // bottom left corner
        path.arcTo(new RectF(rectF.left + mArrowWidth, rectF.bottom - 2 * mRadius
                , rectF.left + mArrowWidth + 2 * mRadius, rectF.bottom), 90, 90);
        // left arrow
        path.lineTo(rectF.left + mArrowWidth, rectF.top + mArrowTop + mArrowHeight);
        path.lineTo(rectF.left, rectF.top + mArrowTop + mArrowOffset);
        path.lineTo(rectF.left + mArrowWidth, rectF.top + mArrowTop);
        path.lineTo(rectF.left + mArrowWidth, rectF.top);
        // top left corner
        path.arcTo(new RectF(rectF.left + mArrowWidth, rectF.top
                , rectF.left + mArrowWidth + 2 * mRadius, rectF.top + 2 * mRadius), 180, 90);
        path.close();
        return path;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mSrc = bm;
        setup();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        try {
            if (drawable == null){
                return;
            }
            super.setImageDrawable(drawable);
            mSrc = getBitmapFromDrawable(drawable);
            setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setImageResource(int resId) {
        try {
            super.setImageResource(resId);
            mSrc = getBitmapFromDrawable(getDrawable());
            setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        try {
            super.setImageURI(uri);
            mSrc = uri != null ?getBitmapFromDrawable(getDrawable()) : null;
            setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap;
        if (drawable instanceof ColorDrawable) {
            bitmap = Bitmap.createBitmap(DEFAULT_COLOR_DRAWABLE_DIMENSION
                    , DEFAULT_COLOR_DRAWABLE_DIMENSION, BITMAP_CONFIG);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth()
                    , drawable.getIntrinsicHeight(), BITMAP_CONFIG);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void updateMatrix() {
        float scale = 0f;
        float dx = 0f;
        float dy = 0f;
        if (mSrc.getWidth() / (float) mSrc.getHeight() >
                getWidth() / (float) getHeight()) {
            scale = getHeight() / (float) mSrc.getHeight();
            dx = (getWidth() - mSrc.getWidth() * scale) * 0.5f;
        } else {
            scale = getWidth() / (float) mSrc.getWidth();
            dy = (getHeight() - mSrc.getHeight() * scale) * 0.5f;
        }
        if (mNinePatchDrawable != null) {
            mCanvas.scale(scale, scale);
            mCanvas.translate(dx, dy);
        } else {
            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postTranslate(dx, dy);
            mBitmapShader.setLocalMatrix(matrix);
        }
    }
    private int dp2px(float dipValue){
        final float scale = getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }

}
