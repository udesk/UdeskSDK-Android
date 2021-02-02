package cn.udesk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.LinearLayout;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;

public class UdeskMaxHeightView extends LinearLayout {

    private static final float DEFAULT_MAX_RATIO_WITHOUT_ARGU = 0.6f;

    private float mMaxRatio = UdeskSDKManager.getInstance().getUdeskConfig().maxHeightViewRatio;
    private float mMaxDimen = UdeskSDKManager.getInstance().getUdeskConfig().maxHeightViewDimen;
    private float mMaxHeight = 0;

    public UdeskMaxHeightView(Context context) {
        super(context);
        init();
    }

    public UdeskMaxHeightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        init();
    }

    public UdeskMaxHeightView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(context, attrs);
        init();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.UdeskMaxHeightView);

        final int count = a.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.UdeskMaxHeightView_udesk_HeightRatio) {
                mMaxRatio = a.getFloat(attr, 0f);
            } else if (attr == R.styleable.UdeskMaxHeightView_udesk_HeightDimen) {
                mMaxDimen = a.getDimension(attr, 0f);
            }
        }
        a.recycle();
    }

    private void init() {
        if (mMaxDimen <= 0 && mMaxRatio <= 0) {
            mMaxHeight = DEFAULT_MAX_RATIO_WITHOUT_ARGU * (float) getScreenHeight(getContext());
        } else if (mMaxDimen <= 0 && mMaxRatio > 0) {
            mMaxHeight = mMaxRatio * (float) getScreenHeight(getContext());
        } else if (mMaxDimen > 0 && mMaxRatio <= 0) {
            mMaxHeight = mMaxDimen;
        } else {
            mMaxHeight = Math.min(mMaxDimen, mMaxRatio * (float) getScreenHeight(getContext()));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = heightSize <= mMaxHeight ? heightSize
                    : (int) mMaxHeight;
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            heightSize = heightSize <= mMaxHeight ? heightSize
                    : (int) mMaxHeight;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            heightSize = heightSize <= mMaxHeight ? heightSize
                    : (int) mMaxHeight;
        }
        int maxHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize,
                heightMode);
        super.onMeasure(widthMeasureSpec, maxHeightMeasureSpec);
    }

    private int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getHeight();
    }

}