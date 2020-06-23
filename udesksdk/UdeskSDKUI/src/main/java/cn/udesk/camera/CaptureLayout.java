package cn.udesk.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.udesk.R;
import cn.udesk.camera.callback.CaptureListener;
import cn.udesk.camera.callback.TypeListener;


public class CaptureLayout extends FrameLayout {

    private CaptureListener captureLisenter;    //拍照按钮监听
    private TypeListener typeLisenter;          //拍照或录制后接结果按钮监听


    public void setTypeLisenter(TypeListener typeLisenter) {
        this.typeLisenter = typeLisenter;
    }

    public void setCaptureLisenter(CaptureListener captureLisenter) {
        this.captureLisenter = captureLisenter;
    }

    private CaptureButton btn_capture;      //拍照按钮
    private TypeButton btn_confirm;         //确认按钮
    private TypeButton btn_cancel;          //取消按钮
    private TextView txt_tip;               //提示文本

    private int layout_width;
    private int layout_height;
    private int button_size;


//    private boolean isFirst = true;

    public CaptureLayout(Context context) {
        this(context, null);
    }

    public CaptureLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CaptureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        try {
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(outMetrics);

            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                layout_width = outMetrics.widthPixels;
            } else {
                layout_width = outMetrics.widthPixels / 2;
            }
            button_size = (int) (layout_width / 4.5f);
            layout_height = button_size + (button_size / 5) * 2 + 200;

            initView();
            initEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(layout_width, layout_height);
    }

    public void initEvent() {
        try {
            btn_cancel.setVisibility(GONE);
            btn_confirm.setVisibility(GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTypeBtnAnimator() {
        try {
            //拍照录制结果后的动画
            btn_capture.setVisibility(GONE);
            btn_cancel.setVisibility(VISIBLE);
            btn_confirm.setVisibility(VISIBLE);
            btn_cancel.setClickable(false);
            btn_confirm.setClickable(false);
            ObjectAnimator animator_cancel = ObjectAnimator.ofFloat(btn_cancel, "translationX", layout_width / 4, 0);
            ObjectAnimator animator_confirm = ObjectAnimator.ofFloat(btn_confirm, "translationX", -layout_width / 4, 0);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animator_cancel, animator_confirm);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    btn_cancel.setClickable(true);
                    btn_confirm.setClickable(true);
                }
            });
            set.setDuration(200);
            set.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initView() {
        try {
            setWillNotDraw(false);
            //拍照按钮
            btn_capture = new CaptureButton(getContext(), button_size);
            LayoutParams btn_capture_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            btn_capture_param.gravity = Gravity.CENTER;
            btn_capture.setLayoutParams(btn_capture_param);
            btn_capture.setCaptureLisenter(new CaptureListener() {
                @Override
                public void takePictures() {
                    if (captureLisenter != null) {
                        captureLisenter.takePictures();
                    }
                }

                @Override
                public void recordShort(long time) {
                    if (captureLisenter != null) {
                        captureLisenter.recordShort(time);
                    }
    //                startAlphaAnimation();
                }

                @Override
                public void recordStart() {
                    if (captureLisenter != null) {
                        captureLisenter.recordStart();
                    }
                    startAlphaAnimation();
                }

                @Override
                public void recordEnd(long time) {
                    if (captureLisenter != null) {
                        captureLisenter.recordEnd(time);
                    }
                    setTip(time/1000+"s");
    //                startAlphaAnimation();
                    startTypeBtnAnimator();
                }

                @Override
                public void recordZoom(float zoom) {
                    if (captureLisenter != null) {
                        captureLisenter.recordZoom(zoom);
                    }
                }

                @Override
                public void recordError() {
                    if (captureLisenter != null) {
                        captureLisenter.recordError();
                    }
                }

                @Override
                public void recordTime(long time) {
                    setTip(time/1000+"s / 15s");
                }
            });

            //取消按钮
            btn_cancel = new TypeButton(getContext(), TypeButton.TYPE_CANCEL, button_size);
            final LayoutParams btn_cancel_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            btn_cancel_param.gravity = Gravity.CENTER_VERTICAL;
            btn_cancel_param.setMargins((layout_width / 4) - button_size / 2, 0, 0, 0);
            btn_cancel.setLayoutParams(btn_cancel_param);
            btn_cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (typeLisenter != null) {
                        typeLisenter.cancel();
                    }
    //                startAlphaAnimation();
                }
            });

            //确认按钮
            btn_confirm = new TypeButton(getContext(), TypeButton.TYPE_CONFIRM, button_size);
            LayoutParams btn_confirm_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            btn_confirm_param.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
            btn_confirm_param.setMargins(0, 0, (layout_width / 4) - button_size / 2, 0);
            btn_confirm.setLayoutParams(btn_confirm_param);
            btn_confirm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (typeLisenter != null) {
                        typeLisenter.confirm();
                    }
    //                startAlphaAnimation();
                }
            });


            txt_tip = new TextView(getContext());
            LayoutParams txt_param = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            txt_param.gravity = Gravity.CENTER_HORIZONTAL;
            txt_param.setMargins(0, 0, 0, 0);
            txt_tip.setText(getResources().getString(R.string.camera_view_tips));
            txt_tip.setTextColor(0xFFFFFFFF);
            txt_tip.setGravity(Gravity.CENTER);
            txt_tip.setLayoutParams(txt_param);

            this.addView(btn_capture);
            this.addView(btn_cancel);
            this.addView(btn_confirm);
            this.addView(txt_tip);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    /**************************************************
     * 对外提供的API                      *
     **************************************************/
    public void resetCaptureLayout() {
        try {
            btn_capture.resetState();
            btn_cancel.setVisibility(GONE);
            btn_confirm.setVisibility(GONE);
            btn_capture.setVisibility(VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startAlphaAnimation() {
//        if (isFirst) {
//        ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 1f, 0f);
//        animator_txt_tip.setDuration(500);
//        animator_txt_tip.start();
//            isFirst = false;
//        }
    }

    public void setTextWithAnimation(String tip) {
        try {
            txt_tip.setText(tip);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 0f, 1f);
//        animator_txt_tip.setDuration(500);
//        animator_txt_tip.start();
    }

    public void setTooShortWithAnimation(String tip) {
        try {
            txt_tip.setText(tip);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        ObjectAnimator animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 0f, 1f, 1f, 0);
//        animator_txt_tip.setDuration(2500);
//        animator_txt_tip.start();
    }

    public void setButtonFeatures(int state) {
        try {
            btn_capture.setButtonFeatures(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTip(String tip) {
        try {
            txt_tip.setText(tip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTip() {
        try {
            txt_tip.setVisibility(VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
