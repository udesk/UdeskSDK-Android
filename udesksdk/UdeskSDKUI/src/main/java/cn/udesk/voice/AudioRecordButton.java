package cn.udesk.voice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;

import cn.udesk.R;
import cn.udesk.rich.LoaderTask;


/**
 * Created by wudeng on 2017/9/6.
 */

public class AudioRecordButton extends AppCompatButton implements AudioRecordManager.OnAudioStateListener {

    private static final int STATE_NORMAL = 100001;
    private static final int STATE_RECORDING = 100002;
    private static final int STATE_WANT_CANCEL = 100003;
    private static final int CANCEL_HEIGHT = 50;

    private static final int MSG_AUDIO_PREPARED = 100004;
    private static final int MSG_VOICE_CHANGE = 100005;
    private static final int MSG_DIALOG_DISMISS = 100006;
    private static final int MSG_VOICE_FINISHED = 100007;
    private static final int MSG_VOICE_ERROR = 100008;
    private static final int MSG_AUDIO_ERROR = 100009;


    // 当前状态，默认为正常
    private int mCurrentState = STATE_NORMAL;
    private boolean isReady = false;
    private volatile boolean isRecording = false;
    private RecordDialogManager mDialogManager;
    private AudioRecordManager mAudioRecordManager;
    private AudioManager mAudioManager;
    private String mAudioSaveDir;
    private long mRecordTime;
    private OnRecordingListener mRecordingListener;
    private String mAudioFilePath;
    private boolean hasInit = false;
    private volatile boolean isActionUp = false;

    public AudioRecordButton(Context context) {
        this(context, null);
    }

    public AudioRecordButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioRecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 初始化按钮样式
        try {
            setBackgroundResource(R.drawable.udesk_chat_edit_bg);
            setText(getResources().getString(R.string.press_record));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置录音回调
     *
     * @param listener 回调监听
     */
    public void setRecordingListener(OnRecordingListener listener) {
        this.mRecordingListener = listener;
    }

    /**
     * 按钮初始化
     *
     * @param audioSaveDir 录音文件保存路径
     */
    public void init(String audioSaveDir) {
        try {
            mAudioSaveDir = audioSaveDir;
            // 初始化 dialog 管理器
            mDialogManager = new RecordDialogManager(getContext());
            // 获取音频管理，以申请音频焦点
            mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            // 初始化录音管理器
            mAudioRecordManager = AudioRecordManager.getInstance(mAudioSaveDir);

            mAudioRecordManager.setAudioStateListener(this);

            // 设置按钮长按事件监听，只有触发长按才开始准备录音
            setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isActionUp = false;
                    if (mRecordingListener != null) {
                        mRecordingListener.recordStart();
                    }
                    // 获取焦点
                    int focus = mAudioManager.requestAudioFocus(null,
                            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                    if (focus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        if (mAudioRecordManager != null) {
                            isReady = true;
                            mAudioRecordManager.prepareAudio();
                        }
                    } else if (focus == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                        if (mRecordingListener != null) {
                            mRecordingListener.recordError("AUDIO_FOCUS_REQUEST_FAILED");
                        }
                    }
                    return true;
                }
            });
            hasInit = true;
        } catch (Exception e) {
            e.printStackTrace();
            mDialogManager.dismissDialog();
        }
    }

    // 子线程 runnable，每隔0.1秒获取音量大小，并记录录音时间
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mRecordTime += 100;
                    if (mRecordTime >= 59 * 1000) {
                        isRecording = false;
                        mHandler.sendEmptyMessage(MSG_VOICE_FINISHED);
                    } else {
                        if (mRecordTime >= 200 && mRecordingListener != null && getRecordAudioLength() < 20) {
                            mRecordingListener.recordError("");
                            isRecording = false;
                            mHandler.sendEmptyMessage(MSG_VOICE_ERROR);
                        } else {
                            if (isActionUp){
                                isRecording = false;
                                if (mRecordTime<1000){
                                    mDialogManager.dismissDialog();
                                    mAudioRecordManager.releaseAudio();
                                }else {
                                    mHandler.sendEmptyMessage(MSG_VOICE_FINISHED);
                                }
                            }else {
                                mHandler.sendEmptyMessage(MSG_VOICE_CHANGE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case MSG_AUDIO_PREPARED:
                        // 录音管理器 prepare 成功，开始录音并显示dialog
                        // 启动线程记录时间并获取音量变化
                        isRecording = true;
                        mDialogManager.showDialogRecord();
                        // 启动线程，每隔0.1秒获取音量大小
                        LoaderTask.getThreadPoolExecutor().execute(mGetVoiceLevelRunnable);
                        break;
                    case MSG_VOICE_CHANGE:
                        mDialogManager.updateVoiceLevel(mAudioRecordManager.getVoiceLevel(7));
                        break;
                    case MSG_DIALOG_DISMISS:
                        mDialogManager.dismissDialog();
                        mHandler.removeCallbacksAndMessages(null);
                        break;
                    case MSG_VOICE_FINISHED:
                        mDialogManager.dismissDialog();
                        mAudioRecordManager.releaseAudio();
                        // 将录音文件路径和录音时长回调
                        if (mRecordingListener != null) {
                            mRecordingListener.recordFinish(mAudioFilePath, mRecordTime);
                        }
                        reset();
                    case MSG_VOICE_ERROR:
                        mDialogManager.dismissDialog();
                        mAudioRecordManager.releaseAudio();
                        reset();
                        break;
                    case MSG_AUDIO_ERROR:
                        if (mRecordingListener != null) {
                            String message = (String) msg.obj;
                            if (!TextUtils.isEmpty(message)) {
                                mRecordingListener.recordError(message);
                            }
                            mDialogManager.dismissDialog();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // 录音准备出错时回调
    @Override
    public void prepareError(String message) {
        try {
            Message handlerMsg = mHandler.obtainMessage(
                    MSG_AUDIO_ERROR);
            handlerMsg.obj = message;
            mHandler.sendMessage(handlerMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 录音准备完成后回调
    @Override
    public void prepareFinish(String audioFilePath) {
        try {
            mAudioFilePath = audioFilePath;
            mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDiaogMiss() {
        mHandler.sendEmptyMessage(MSG_DIALOG_DISMISS);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            if (!hasInit) {
                return true;
            }
            int x = (int) event.getX();
            int y = (int) event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    changeState(STATE_RECORDING);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isRecording) {
                        if (isWantToCancel(x, y)) {
                            changeState(STATE_WANT_CANCEL);
                        } else {
                            changeState(STATE_RECORDING);
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    // 未触发 longClick,直接重置
                    if (!isReady) {
                        isActionUp = true;
                        reset();
                        return super.onTouchEvent(event);
                    }
                    // 触发了longClick，开始初始化录音，但是为初始化完成,或者录音时间太短
                    if (!isRecording || mRecordTime <= 1000) {
                        mDialogManager.showDialogToShort();
                        sendDiaogMiss();
                        mAudioRecordManager.cancelAudio();
                        isReady = false;
                        isRecording = false;
                        mRecordTime = 0;
                        changeState(STATE_NORMAL);
                        // 释放焦点
                        if (mAudioManager != null) {
                            mAudioManager.abandonAudioFocus(null);
                        }
                        isActionUp = true;
                        break;
                    } else if (mCurrentState == STATE_RECORDING) {
                        mDialogManager.dismissDialog();
                        mAudioRecordManager.releaseAudio();
                        // 将录音文件路径和录音时长回调
                        if (mRecordingListener != null) {
                            mRecordingListener.recordFinish(mAudioFilePath, mRecordTime);
                        }
                    } else if (mCurrentState == STATE_WANT_CANCEL) {
                        mDialogManager.dismissDialog();
                        mAudioRecordManager.cancelAudio();
                    }
                    isActionUp = true;
                    reset();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onTouchEvent(event);
    }

    public long getRecordAudioLength() {
        if (TextUtils.isEmpty(mAudioFilePath)) {
            return 0;
        }
        try {
            File file = new File(mAudioFilePath);
            long length = file.length();
            return length;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void changeState(int state) {
        try {
            if (mCurrentState != state) {
                mCurrentState = state;
                if (state == STATE_NORMAL) {
                    setText(getResources().getString(R.string.press_record));
                    setBackgroundResource(R.drawable.udesk_chat_edit_bg);
                } else if (state == STATE_RECORDING) {
                    setText(getResources().getString(R.string.release_end));
                    setBackgroundResource(R.drawable.udesk_chat_record_button_recording);
                    if (isRecording) {
                        mDialogManager.showRecording();
                    }
                } else if (state == STATE_WANT_CANCEL) {
                    setText(getResources().getString(R.string.release_cancel));
                    setBackgroundResource(R.drawable.udesk_chat_record_button_recording);
                    if (isRecording) {
                        mDialogManager.showDialogWantCancel();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否是要取消
     *
     * @param x 手指当前位置 x 坐标
     * @param y 手指当前位置 y 坐标
     */
    private boolean isWantToCancel(int x, int y) {
        return x < 0 || x > getWidth()
                || y < -CANCEL_HEIGHT || y > getHeight() + CANCEL_HEIGHT;
    }

    /**
     * 释放资源，释放音频焦点
     */
    private void reset() {
        try {
            isReady = false;
            isRecording = false;
            mRecordTime = 0;
            changeState(STATE_NORMAL);
            if (mDialogManager != null){
                mDialogManager.dismissDialog();
            }
            // 释放焦点
            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(null);
            }
            mHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前录音文件保存路径
     *
     * @return 当前录音文件保存路径
     */
    public String getAudioSaveDir() {
        return mAudioSaveDir;
    }


    public interface OnRecordingListener {

        void recordStart();

        /**
         * 录音正常结束
         *
         * @param audioFilePath 录音文件绝对路径
         * @param recordTime    录音时长,ms
         */
        void recordFinish(String audioFilePath, long recordTime);

        /**
         * 录音发生错误
         *
         * @param message 错误提示
         */
        void recordError(String message);
    }

    public void destoryRelease(){
        try {
            reset();
            if (mAudioRecordManager != null){
                mAudioRecordManager.setAudioStateListener(null);
                mAudioRecordManager = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
