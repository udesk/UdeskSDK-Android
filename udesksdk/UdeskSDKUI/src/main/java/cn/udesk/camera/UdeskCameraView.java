package cn.udesk.camera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.IOException;

import cn.udesk.R;
import cn.udesk.camera.callback.CameraOpenOverCallback;
import cn.udesk.camera.callback.CameraView;
import cn.udesk.camera.callback.CaptureListener;
import cn.udesk.camera.callback.ErrorListener;
import cn.udesk.camera.callback.FocusCallback;
import cn.udesk.camera.callback.TypeListener;
import cn.udesk.camera.callback.UdeskCameraListener;
import cn.udesk.camera.state.CameraMachine;
import cn.udesk.rich.LoaderTask;
import udesk.core.utils.UdeskUtils;


public class UdeskCameraView extends FrameLayout implements CameraOpenOverCallback, SurfaceHolder
        .Callback, CameraView {

    private CameraMachine machine;

    //拍照浏览时候的类型
    public static final int TYPE_PICTURE = 0x001;
    public static final int TYPE_SHORT_VIDEO = 0x002;
    public static final int TYPE_SHORT = 0x003;
    public static final int TYPE_DEFAULT = 0x004;

    //录制视频比特率
    public static final int MEDIA_QUALITY_HIGH = 20 * 100000;
    public static final int MEDIA_QUALITY_MIDDLE = 16 * 100000;
    public static final int MEDIA_QUALITY_LOW = 12 * 100000;
    public static final int MEDIA_QUALITY_POOR = 8 * 100000;
    public static final int MEDIA_QUALITY_FUNNY = 4 * 100000;
    public static final int MEDIA_QUALITY_DESPAIR = 2 * 100000;
    public static final int MEDIA_QUALITY_SORRY = 1 * 80000;


    public static final int BUTTON_STATE_ONLY_CAPTURE = 0x101;      //只能拍照
    public static final int BUTTON_STATE_ONLY_RECORDER = 0x102;     //只能录像
    public static final int BUTTON_STATE_BOTH = 0x103;              //两者都可以


    //回调监听
    private UdeskCameraListener cameraLisenter;
    private ErrorListener errorLisenter;

    private Context mContext;
    private VideoView mVideoView;
    private ImageView mPhoto;
    private ImageView mSwitchCamera;
    private View mCloseView;
    private CaptureLayout mCaptureLayout;
    private FoucsView mFoucsView;
    private MediaPlayer mMediaPlayer;

    private int layout_width;
    private float screenProp = 0f;

    private Bitmap captureBitmap;   //捕获的图片
    private Bitmap firstFrame;      //第一帧图片
    private String videoUrl;        //视频URL


    //缩放梯度
    private int zoomGradient = 0;

    private boolean firstTouch = true;
    private float firstTouchLength = 0;

    public UdeskCameraView(Context context) {
        this(context, null);
    }

    public UdeskCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UdeskCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initData();
        initView();
    }

    private void initData() {
        try {
            layout_width = UdeskUtils.getScreenWidth(mContext);
            //缩放梯度
            zoomGradient = (int) (layout_width / 16f);
            Log.i("udesksdk","zoom = " + zoomGradient);
            machine = new CameraMachine(getContext(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        try {
            setWillNotDraw(false);
            View view = LayoutInflater.from(mContext).inflate(R.layout.udesk_camera_view, this);
            mVideoView = (VideoView) view.findViewById(R.id.video_preview);
            mPhoto = (ImageView) view.findViewById(R.id.image_photo);
            mSwitchCamera = (ImageView) view.findViewById(R.id.image_switch);
            mCloseView = view.findViewById(R.id.udesk_image_close);
            mCaptureLayout = (CaptureLayout) view.findViewById(R.id.capture_layout);
            mFoucsView = (FoucsView) view.findViewById(R.id.fouce_view);
            mVideoView.getHolder().addCallback(this);
            //切换摄像头
            mSwitchCamera.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    machine.swtich(mVideoView.getHolder(), screenProp);
                }
            });
            //拍照 录像
            mCaptureLayout.setCaptureLisenter(new CaptureListener() {
                @Override
                public void takePictures() {
                    mSwitchCamera.setVisibility(INVISIBLE);
                    mCloseView.setVisibility(INVISIBLE);
                    machine.capture();
                }

                @Override
                public void recordStart() {
                    mSwitchCamera.setVisibility(INVISIBLE);
                    mCloseView.setVisibility(INVISIBLE);
                    machine.record(mContext,mVideoView.getHolder().getSurface(), screenProp);
                }

                @Override
                public void recordShort(final long time) {
                    mCaptureLayout.setTooShortWithAnimation(getResources().getString(R.string.udesk_too_short));
                    mSwitchCamera.setVisibility(VISIBLE);
                    mCloseView.setVisibility(VISIBLE);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            machine.stopRecord(true, time);
                        }
                    }, 500);
                }

                @Override
                public void recordEnd(long time) {
                    machine.stopRecord(false, time);
                }

                @Override
                public void recordZoom(float zoom) {
                    machine.zoom(zoom, CameraInterface.TYPE_RECORDER);
                }

                @Override
                public void recordError() {
                    if (errorLisenter != null) {
                        errorLisenter.AudioPermissionError();
                    }
                }

                @Override
                public void recordTime(long time) {

                }
            });
            //确认 取消
            mCaptureLayout.setTypeLisenter(new TypeListener() {
                @Override
                public void cancel() {

                    mCaptureLayout.setTextWithAnimation(getResources().getString(R.string.camera_view_tips));
                    machine.cancle(mVideoView.getHolder(), screenProp);
                }

                @Override
                public void confirm() {
                    machine.confirm();
                }
            });
            resetState(TYPE_DEFAULT); //重置状态
            CameraInterface.getInstance().registerSensorManager(mContext);
            CameraInterface.getInstance().setSwitchView(mSwitchCamera);
            machine.start(mVideoView.getHolder(), screenProp);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            float widthSize = mVideoView.getMeasuredWidth();
            float heightSize = mVideoView.getMeasuredHeight();
            if (screenProp == 0) {
                screenProp = heightSize / widthSize;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cameraHasOpened() {
        try {
            CameraInterface.getInstance().doStartPreview(mVideoView.getHolder(), screenProp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //生命周期onResume
    public void onResume() {
//        try {
//            resetState(TYPE_DEFAULT); //重置状态
//            CameraInterface.getInstance().registerSensorManager(mContext);
//            CameraInterface.getInstance().setSwitchView(mSwitchCamera);
//            machine.start(mVideoView.getHolder(), screenProp);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    //生命周期onPause
    public void onPause() {
//        try {
//            stopVideo();
//            resetState(TYPE_PICTURE);
//            CameraInterface.getInstance().isPreview(false);
//            CameraInterface.getInstance().unregisterSensorManager(mContext);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public void ondestory() {
        try {
            stopVideo();
            resetState(TYPE_PICTURE);
            CameraInterface.getInstance().isPreview(false);
            CameraInterface.getInstance().unregisterSensorManager(mContext);
            CameraInterface.getInstance().doDestroyCamera();
            CameraInterface.getInstance().destroyCameraInterface();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //SurfaceView生命周期
    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        mMediaPlayer.setDisplay(holder);
                        return;
                    }
                    CameraInterface.getInstance().doOpenCamera(UdeskCameraView.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            CameraInterface.getInstance().doDestroyCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (event.getPointerCount() == 1) {
                        //显示对焦指示器
                        setFocusViewWidthAnimation(event.getX(), event.getY());
                    }
                    if (event.getPointerCount() == 2) {
                        Log.i("udesksdk", "ACTION_DOWN = " + 2);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerCount() == 1) {
                        firstTouch = true;
                    }
                    if (event.getPointerCount() == 2) {
                        //第一个点
                        float point_1_X = event.getX(0);
                        float point_1_Y = event.getY(0);
                        //第二个点
                        float point_2_X = event.getX(1);
                        float point_2_Y = event.getY(1);

                        float result = (float) Math.sqrt(Math.pow(point_1_X - point_2_X, 2) + Math.pow(point_1_Y -
                                point_2_Y, 2));

                        if (firstTouch) {
                            firstTouchLength = result;
                            firstTouch = false;
                        }
                        if ((int) (result - firstTouchLength) / zoomGradient != 0) {
                            firstTouch = true;
                            machine.zoom(result - firstTouchLength, CameraInterface.TYPE_CAPTURE);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    firstTouch = true;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //对焦框指示器动画
    private void setFocusViewWidthAnimation(float x, float y) {
        try {
            machine.foucs(x, y, new FocusCallback() {
                @Override
                public void focusSuccess() {
                    mFoucsView.setVisibility(INVISIBLE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateVideoViewSize(float videoWidth, float videoHeight) {
        try {
            if (videoWidth > videoHeight) {
                LayoutParams videoViewParam;
                int height = (int) ((videoHeight / videoWidth) * getWidth());
                videoViewParam = new LayoutParams(LayoutParams.MATCH_PARENT, height);
                videoViewParam.gravity = Gravity.CENTER;
                mVideoView.setLayoutParams(videoViewParam);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**************************************************
     * 对外提供的API                     *
     **************************************************/

    public void setSaveVideoPath(String path) {
        try {
            CameraInterface.getInstance().setSaveVideoPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setCameraLisenter(UdeskCameraListener cameraLisenter) {
        this.cameraLisenter = cameraLisenter;
    }


    //启动Camera错误回调
    public void setErrorLisenter(ErrorListener errorLisenter) {
        try {
            this.errorLisenter = errorLisenter;
            CameraInterface.getInstance().setErrorLinsenter(errorLisenter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置CaptureButton功能（拍照和录像）
    public void setFeatures(int state) {
        try {
            this.mCaptureLayout.setButtonFeatures(state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置录制质量
    public void setMediaQuality(int quality) {
        try {
            CameraInterface.getInstance().setMediaQuality(quality);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCloseListener(OnClickListener listener) {
        if (mCloseView != null) {
            mCloseView.setOnClickListener(listener);
        }
    }

    @Override
    public void resetState(int type) {
        try {
            switch (type) {
                case TYPE_SHORT_VIDEO:
                    stopVideo();    //停止播放
                    //初始化VideoView
                    UdeskUtils.deleteFile(videoUrl);
                    mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    machine.start(mVideoView.getHolder(), screenProp);
    //                mCaptureLayout.setTextWithAnimation(getResources().getString(R.string.camera_view_tips));
                    break;
                case TYPE_PICTURE:
                    mPhoto.setVisibility(INVISIBLE);
                    break;
                case TYPE_SHORT:
                    break;
                case TYPE_DEFAULT:
                    mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    break;
            }
            mSwitchCamera.setVisibility(VISIBLE);
            mCloseView.setVisibility(VISIBLE);
            mCaptureLayout.resetCaptureLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void confirmState(int type) {
        try {
            switch (type) {
                case TYPE_SHORT_VIDEO:
                    stopVideo();    //停止播放
                    mVideoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    machine.start(mVideoView.getHolder(), screenProp);
                    if (cameraLisenter != null) {
                        cameraLisenter.recordSuccess(videoUrl, firstFrame);
                    }
                    break;
                case TYPE_PICTURE:
                    mPhoto.setVisibility(INVISIBLE);
                    if (cameraLisenter != null) {
                        cameraLisenter.captureSuccess(captureBitmap);
                    }
                    break;
                case TYPE_SHORT:
                    break;
                case TYPE_DEFAULT:
                    break;
            }
            mCaptureLayout.resetCaptureLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showPicture(Bitmap bitmap, boolean isVertical) {
        try {
            if (isVertical) {
                mPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
            } else {
                mPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            captureBitmap = bitmap;
            mPhoto.setImageBitmap(bitmap);
            mPhoto.setVisibility(VISIBLE);
            mCaptureLayout.startAlphaAnimation();
            mCaptureLayout.startTypeBtnAnimator();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void playVideo(Bitmap firstFrame, final String url) {
        videoUrl = url;
        UdeskCameraView.this.firstFrame = firstFrame;
        LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.reset();
                    }
                    mMediaPlayer.setDataSource(url);
                    mMediaPlayer.setSurface(mVideoView.getHolder().getSurface());
                    mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer
                            .OnVideoSizeChangedListener() {
                        @Override
                        public void
                        onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            updateVideoViewSize(mMediaPlayer.getVideoWidth(), mMediaPlayer
                                    .getVideoHeight());
                        }
                    });
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                        }
                    });
                    mMediaPlayer.setLooping(true);
                    mMediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void stopVideo() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                CameraInterface.getInstance().doOpenCamera(UdeskCameraView.this);
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
        public void setTip(String tip) {
        try {
            mCaptureLayout.setTip(tip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPreviewCallback() {

        try {
            handlerFoucs(mFoucsView.getWidth() / 2, mFoucsView.getHeight() / 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handlerFoucs(float x, float y) {
        try {
            if (y > mCaptureLayout.getTop()) {
                return false;
            }
            mFoucsView.setVisibility(VISIBLE);
            if (x < mFoucsView.getWidth() / 2) {
                x = mFoucsView.getWidth() / 2;
            }
            if (x > layout_width - mFoucsView.getWidth() / 2) {
                x = layout_width - mFoucsView.getWidth() / 2;
            }
            if (y < mFoucsView.getWidth() / 2) {
                y = mFoucsView.getWidth() / 2;
            }
            if (y > mCaptureLayout.getTop() - mFoucsView.getWidth() / 2) {
                y = mCaptureLayout.getTop() - mFoucsView.getWidth() / 2;
            }
            mFoucsView.setX(x - mFoucsView.getWidth() / 2);
            mFoucsView.setY(y - mFoucsView.getHeight() / 2);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(mFoucsView, "scaleX", 1, 0.6f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(mFoucsView, "scaleY", 1, 0.6f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(mFoucsView, "alpha", 1f, 0.4f, 1f, 0.4f, 1f, 0.4f, 1f);
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(scaleX).with(scaleY).before(alpha);
            animSet.setDuration(400);
            animSet.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


}
