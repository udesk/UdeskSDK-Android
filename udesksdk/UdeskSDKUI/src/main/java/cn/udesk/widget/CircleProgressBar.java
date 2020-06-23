package cn.udesk.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * Created by user on 2018/3/22.
 */

public class CircleProgressBar extends View {

    private float circleBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    /*内边距*/
    private float circlePadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
    /*字体大小*/
    private float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());
    /*绘制圆周的画笔*/
    private Paint backCirclePaint;
    /*绘制文字的画笔*/
    private Paint textPaint;
    /*百分比*/
    private float progress = 0;

    private Paint circlePaint;

    public CircleProgressBar(Context context) {
        super(context);
        init();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        try {
            backCirclePaint = new Paint();
            backCirclePaint.setStyle(Paint.Style.STROKE);
            backCirclePaint.setAntiAlias(true);
            backCirclePaint.setColor(0xFFFFFFFF);
            backCirclePaint.setStrokeWidth(circleBorderWidth);


            circlePaint = new Paint();
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(0x80333333);


            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(textSize);
            textPaint.setColor(0xFFFFFFFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
            int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(Math.min(measureWidth, measureHeight), Math.min(measureWidth, measureHeight));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        try {
            //半径
            float radius = (getMeasuredWidth() - circlePadding * 3) / 2;
            //X轴中点坐标
            int centerX = getMeasuredWidth() / 2;
            //画圆
            canvas.drawCircle(centerX, centerX, radius, circlePaint);

            //画圆弧
            canvas.drawArc(
                    new RectF(centerX - (radius - circleBorderWidth / 2), centerX - (radius - circleBorderWidth / 2),
                            centerX + (radius - circleBorderWidth / 2), centerX + (radius - circleBorderWidth / 2)),
                    -90, (float) (progress * 3.6), false, backCirclePaint);

            //4.绘制文字
            float textWidth = textPaint.measureText(progress + "%");
            int textHeight = (int) (Math.ceil(textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent) + 2);
            canvas.drawText(progress + "%", centerX - textWidth / 2, centerX + textHeight / 4, textPaint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置百分比
     *
     * @param percent
     */
    public void setPercent(float percent) {
        try {
            if (percent < 0) {
                percent = 0;
            } else if (percent > 100) {
                percent = 100;
            }
            this.progress = percent;
            invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public float getPercent(){
        return  this.progress;
    }

}
