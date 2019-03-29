package cn.udesk.xphotoview;

import android.graphics.Rect;

/**
 * Created by zhenghui on 2017/5/22.
 */

public interface IXphotoView {

    enum DoubleTabScale {
        CENTER_CROP(1),

        CENTER_INSIDE(2);

        public int value = 1;

        DoubleTabScale(int v) {
            value = v;
        }

        public static DoubleTabScale valueOf(int value) {
            if(value == CENTER_CROP.value) {
                return CENTER_CROP;
            }

            if(value == CENTER_INSIDE.value) {
                return CENTER_INSIDE;
            }

            return CENTER_CROP;
        }
    }

    interface OnTabListener {
        void onSingleTab();
        void onLongTab();
    }

    void onSingleTab();

    void onLongTab();

    DoubleTabScale getDoubleTabScale();

    String getCachedDir();

    void onImageSetFinished(boolean finished);

    void callPostInvalidate();

    void onSetImageFinished(IViewAttacher bm, boolean success, Rect image);

    void interceptParentTouchEvent(boolean intercept);

    void recycleAll();
}

