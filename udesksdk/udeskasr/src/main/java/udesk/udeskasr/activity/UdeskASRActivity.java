package udesk.udeskasr.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.udesk.PreferenceHelper;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.photoselect.PhotoSelectorActivity;
import cn.udesk.widget.UdeskAppMarketDialog;
import udesk.core.event.InvokeEventContainer;
import udesk.udeskasr.AutoCheck;
import udesk.udeskasr.R;
import udesk.udeskasr.RecogResult;
import udesk.udeskasr.widget.WaveCircleView;

public class UdeskASRActivity extends Activity implements EventListener, View.OnClickListener, WaveCircleView.ASRListener {

    private TextView mASRText;
    private TextView pressSpeak;
    private TextView clear;
    private TextView send;
    private ImageView close;
    private WaveCircleView mic;
    private EventManager asr;
    private boolean logTime = true;
    private static final String TAG = "UdeskASRActivity";
    private boolean isAudioStart = false;
    private StringBuffer audioText;

    protected UdeskAppMarketDialog marketDialog;

    public void disMarketDialog() {
        if (marketDialog != null) {
            marketDialog.dismiss();
        }
    }

    public void showMarketDialog(final String content) {

        if (UdeskASRActivity.this.isFinishing()) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (marketDialog != null && marketDialog.isShowing()) {
                        return;
                    }
                    marketDialog = new UdeskAppMarketDialog(UdeskASRActivity.this);
                    marketDialog.setContentTxtVis(View.VISIBLE);
                    marketDialog.setContent(content);
                    marketDialog.setCancleTextViewVis(View.GONE);
                    marketDialog.setOkTxtTextViewVis(View.GONE);
                    marketDialog.setudeskBottomViewVis(View.GONE);
                    marketDialog.setCanceledOnTouchOutside(true);
                    marketDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udesk_asr);
        initView();
        initASR();
    }

    /**
     * 初始化语音识别
     */
    private void initASR() {
        try {
            // 基于sdk集成1.1 初始化EventManager对象
            asr = EventManagerFactory.create(this, "asr");
            // 基于sdk集成1.3 注册自己的输出事件类
            asr.registerListener(this); //  EventListener 中 onEvent方法
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        mASRText = findViewById(R.id.udesk_asr_txt);
        pressSpeak = findViewById(R.id.udesk_press_speak);
        clear = findViewById(R.id.udesk_clear);
        send = findViewById(R.id.udesk_send);
        close = findViewById(R.id.udesk_close);
        mic = findViewById(R.id.udesk_mic);
        setWaveCircleView();
        mASRText.setOnClickListener(this);
        clear.setOnClickListener(this);
        send.setOnClickListener(this);
        close.setOnClickListener(this);
        mic.setmASRListener(this);
        changVis(true, true, false, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(mASRText.getText())) {
            changVis(true, true, false, false);
        } else {
            changVis(false, false, true, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
            if (mic.isStart()) {
                mic.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // 基于SDK集成4.2 发送取消事件
            asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
            // 基于SDK集成5.2 退出事件管理器
            // 必须与registerListener成对出现，否则可能造成内存泄露
            asr.unregisterListener(this);
            if (mic.isStart()) {
                mic.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置动画参数
     */
    private void setWaveCircleView() {
        try {
            mic.setColor(getResources().getColor(R.color.udesk_color_307AE8));
            mic.setDuration(1000);
            mic.setWaveCreatedSpeed(500);
            mic.setmCenterBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.udesk_mic));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            int i = v.getId();
            if (i == R.id.udesk_asr_txt) {
                Intent intent = new Intent(this, UdeskEditActivity.class);
                intent.putExtra(UdeskConstant.UDESK_ASR_TEXT, mASRText.getText());
                startActivityForResult(intent, UdeskConstant.UDESK_REQUEST_CODE);
            } else if (i == R.id.udesk_clear) {
                mASRText.setText("");
                audioText.setLength(0);
                changVis(true, true, false, false);
            } else if (i == R.id.udesk_send) {
                if (mASRText!=null&&mASRText.length()>0){
                    InvokeEventContainer.getInstance().event_OnAudioResult.invoke(mASRText.getText().toString());
                }
                finish();
                overridePendingTransition(0,R.anim.udesk_audio_exit_anim);
            } else if (i == R.id.udesk_close) {
                finish();
                overridePendingTransition(0,R.anim.udesk_audio_exit_anim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 识别编辑回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == UdeskConstant.UDESK_REQUEST_CODE && data != null) {
                String text=data.getStringExtra(UdeskConstant.UDESK_EDIT_TEXT);
                if (!TextUtils.isEmpty(text)){
                    mASRText.setText(text);
                    mASRText.setTextColor(getResources().getColor(R.color.udesk_color_212121));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * mic 事件回调 开启
     */
    @Override
    public void start() {
        try {
            final boolean[] onPermissionGranted = {false};
            if (Build.VERSION.SDK_INT < 23) {
                changVis(false, false, false, false);
                audioStart();
                mic.start();
            } else {

//                String clickRecording = PreferenceHelper.readString(getApplicationContext(), "udeks_permission", "Recording");
                boolean isNeedShowAppMarkDialog = XPermissionUtils.isNeedShowAppMarkDialog(UdeskASRActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.INTERNET,
                                Manifest.permission.READ_PHONE_STATE});
                if (isNeedShowAppMarkDialog){
                    showMarketDialog(getString(cn.udesk.R.string.udesk_voice_permission));
                }
                requestAudioPermission(onPermissionGranted);
                if (onPermissionGranted[0]){
                    changVis(false, false, false, false);
                    audioStart();
                    mic.start();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(getString(R.string.udesk_asr_fail));
            changVis(true, true, false, false);
        }
    }

    private void requestAudioPermission(final boolean[] onPermissionGranted) {
        XPermissionUtils.requestPermissions(UdeskASRActivity.this, RequestCode.ASR,
                new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET,
                        Manifest.permission.READ_PHONE_STATE},


                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        disMarketDialog();
//                        PreferenceHelper.write(getApplicationContext(), "udeks_permission", "Recording", "true");
                        onPermissionGranted[0] = true;
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                        disMarketDialog();
//                        PreferenceHelper.write(getApplicationContext(), "udeks_permission", "Recording", "true");
                        showToast(getResources().getString(R.string.audio_denied));
                        onPermissionGranted[0] = false;
                    }
                });
    }

    /**
     * mic 事件回调 停止
     */
    @Override
    public void stop() {
        try {
            audioStop();
            if (TextUtils.isEmpty(audioText)){
                mASRText.setText("");
            }
            if (TextUtils.isEmpty(mASRText.getText())) {
                changVis(true, true, false, false);
            } else {
                changVis(false, false, true, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(getString(R.string.udesk_asr_fail));
            changVis(true, true, false, false);
        }
    }

    /**
     * 语音识别开始
     */
    private void audioStart() {
        mASRText.setText(getString(R.string.udesk_please_talk));
        mASRText.setTextColor(getResources().getColor(R.color.udesk_color_66212121));
        audioText = new StringBuffer();
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        // 替换成测试的event
        event = SpeechConstant.ASR_START;
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // 长语音
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0);
        // 中文输入法模型，有逗号  1737英语
        params.put(SpeechConstant.PID, 1537);
        // 复制此段可以自动检测错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage();
                        Log.d(TAG, message);
                    }
                }
            }
        }, false)).checkAsr(params);
        // 可以替换成自己的json
        String json = null;
        // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        asr.send(event, json, null, 0, 0);
        isAudioStart = true;

    }

    /**
     * 停止语音识别
     */
    private void audioStop() {
        if (isAudioStart) {
            asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0);
            isAudioStart = false;
        }
    }

    /**
     * 基于DEMO集成3.1 开始回调事件
     *
     * @param name
     * @param params
     * @param data
     * @param offset
     * @param length
     */
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        try {
            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                // 临时识别结果, 长语音模式需要从此消息中取出结果
                RecogResult recogResult = RecogResult.parseJson(params);
                String[] results = recogResult.getResultsRecognition();
                if (recogResult.isFinalResult()) {
                    for (String result : results) {
                        audioText.append(result);
                    }
                    mASRText.setText(audioText.toString());
                    mASRText.setTextColor(getResources().getColor(R.color.udesk_color_212121));
                    if (TextUtils.isEmpty(mASRText.getText())) {
                        changVis(true, true, false, false);
                    } else {
                        changVis(false, false, true, true);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(getString(R.string.udesk_asr_fail));
            changVis(true, true, false, false);
        }
    }

    private void changVis(boolean isSpeakShow, boolean isCloseShow, boolean isClearShow, boolean isSendShow) {
        if (isSpeakShow) {
            pressSpeak.setVisibility(View.VISIBLE);
        } else {
            pressSpeak.setVisibility(View.GONE);
        }
        if (isCloseShow) {
            close.setVisibility(View.VISIBLE);
        } else {
            close.setVisibility(View.GONE);
        }
        if (isClearShow) {
            clear.setVisibility(View.VISIBLE);
        } else {
            clear.setVisibility(View.GONE);
        }
        if (isSendShow) {
            send.setVisibility(View.VISIBLE);
        } else {
            send.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        try {
            XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
