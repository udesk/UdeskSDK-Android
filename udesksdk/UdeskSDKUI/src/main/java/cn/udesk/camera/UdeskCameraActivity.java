package cn.udesk.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.camera.callback.ErrorListener;
import cn.udesk.camera.callback.PermissionListener;
import cn.udesk.camera.callback.UdeskCameraListener;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/10.
 */

public class UdeskCameraActivity extends Activity {

    UdeskCameraView udeskCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_activity_small_camera);
        initView();
    }


    private void initView() {
        try {
            udeskCameraView = (UdeskCameraView) findViewById(R.id.udesk_cameraview);
            //设置视频保存路径
            udeskCameraView.setSaveVideoPath(UdeskUtil.getDirectoryPath(getApplicationContext(), UdeskConst.FileVideo));
            //设置只能录像或只能拍照或两种都可以（默认两种都可以）
            udeskCameraView.setFeatures(UdeskCameraView.BUTTON_STATE_BOTH);

            udeskCameraView.setErrorLisenter(new ErrorListener() {
                @Override
                public void onError() {
                    Log.i("udesksdk", "open camera error");

                    Intent mIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(UdeskConst.Camera_Error, true);
                    mIntent.putExtra(UdeskConst.SEND_BUNDLE, bundle);
                    UdeskCameraActivity.this.setResult(Activity.RESULT_OK, mIntent);
                    UdeskCameraActivity.this.finish();
                }

                @Override
                public void AudioPermissionError() {
                    Log.i("udesksdk", "AudioPermissionError");
                    Toast.makeText(getApplicationContext(), getString(R.string.udesk_audio_permission_error), Toast.LENGTH_SHORT).show();
                }
            });

            udeskCameraView.setCameraLisenter(new UdeskCameraListener() {
                @Override
                public void captureSuccess(Bitmap bitmap) {

                    if (bitmap != null) {
                        String path = UdeskUtil.saveBitmap(getApplicationContext(), bitmap);
                        finishActivity(null, path);
                    } else {
                        finish();
                    }

                }

                @Override
                public void recordSuccess(String url, Bitmap firstFrame) {
                    finishActivity(url, null);
                }
            });

            udeskCameraView.setCloseListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            udeskCameraView.setPermissionListener(new PermissionListener() {
                @Override
                public boolean onCheckAudioPermission() {
                    if (Build.VERSION.SDK_INT < 23) {
                        return true;
                    }else {
                        final boolean[] onPermissionGranted = {false};
                        XPermissionUtils.requestPermissions(UdeskCameraActivity.this, RequestCode.AUDIO,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                new XPermissionUtils.OnPermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        onPermissionGranted[0] = true;
                                    }

                                    @Override
                                    public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                        Toast.makeText(getApplicationContext(),
                                                getResources().getString(R.string.aduido_denied),
                                                Toast.LENGTH_SHORT).show();
                                        onPermissionGranted[0] = false;
                                    }
                                });
                        return onPermissionGranted[0];
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            udeskCameraView.onResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            udeskCameraView.onPause();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();

    }


    private void finishActivity(String url, String picturepath) {
        try {
            Intent mIntent = new Intent();
            Bundle bundle = new Bundle();
            if (!TextUtils.isEmpty(url)) {
                bundle.putString(UdeskConst.SEND_SMALL_VIDEO, UdeskConst.SMALL_VIDEO);
                bundle.putString(UdeskConst.PREVIEW_Video_Path, url);
            } else if (!TextUtils.isEmpty(picturepath)) {
                bundle.putString(UdeskConst.SEND_SMALL_VIDEO, UdeskConst.PICTURE);
                bundle.putString(UdeskConst.BitMapData, picturepath);
            }
            mIntent.putExtra(UdeskConst.SEND_BUNDLE, bundle);
            UdeskCameraActivity.this.setResult(Activity.RESULT_OK, mIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        try {
            udeskCameraView.ondestory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
