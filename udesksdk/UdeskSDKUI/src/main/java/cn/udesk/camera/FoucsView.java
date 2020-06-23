package cn.udesk.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import udesk.core.utils.UdeskUtils;


public class FoucsView extends View {
    private int size;
    private int center_x;
    private int center_y;
    private int length;
    private Paint mPaint;

    public FoucsView(Context context) {
        this(context, null);
    }

    public FoucsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoucsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        try {
            this.size = UdeskUtils.getScreenWidth(context) / 3;
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(0xEE16AE16);
            mPaint.setStrokeWidth(4);
            mPaint.setStyle(Paint.Style.STROKE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            center_x = (int) (size / 2.0);
            center_y = (int) (size / 2.0);
            length = (int) (size / 2.0) - 2;
            setMeasuredDimension(size, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            canvas.drawRect(center_x - length, center_y - length, center_x + length, center_y + length, mPaint);
            canvas.drawLine(2, getHeight() / 2, size / 10, getHeight() / 2, mPaint);
            canvas.drawLine(getWidth() - 2, getHeight() / 2, getWidth() - size / 10, getHeight() / 2, mPaint);
            canvas.drawLine(getWidth() / 2, 2, getWidth() / 2, size / 10, mPaint);
            canvas.drawLine(getWidth() / 2, getHeight() - 2, getWidth() / 2, getHeight() - size / 10, mPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
