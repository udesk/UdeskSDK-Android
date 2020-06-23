package cn.udesk.photoselect;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/8.
 */

public class PictureVideoPlayActivity extends AppCompatActivity implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private String path;
    private ImageView picture_left_back;
    private ImageView video_img;
    private ProgressBar progressBar;
    private MediaController mMediaController;
    private VideoView mVideoView;
    private int mPositionWhenPaused = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
            super.onCreate(savedInstanceState);
            UdeskUtil.setOrientation(this);
            setContentView(R.layout.udesk_activity_picture_videoplay);

            Bundle bundle = getIntent().getExtras();
            path = bundle.getString(UdeskConst.PREVIEW_Video_Path);
            picture_left_back = (ImageView) findViewById(R.id.picture_left_back);
            mVideoView = (VideoView) findViewById(R.id.udesk_video_view);
            video_img = findViewById(R.id.video_img);
            progressBar = (ProgressBar) findViewById(R.id.udesk_wait);
            mMediaController = new MediaController(this);
            mVideoView.setOnCompletionListener(this);
            mVideoView.setOnPreparedListener(this);
            mVideoView.setMediaController(mMediaController);
            picture_left_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            if (!TextUtils.isEmpty(path) && UdeskUtil.isExitFileByPath(this.getApplicationContext(), path)) {
                UdeskUtil.loadImage(getApplicationContext(), video_img, path);
            } else if (UdeskUtil.fileIsExitByUrl(getApplicationContext(), UdeskConst.FileImg, path)) {
                UdeskUtil.loadImage(getApplicationContext(), video_img, Uri.fromFile(
                        UdeskUtil.getFileByUrl(getApplicationContext(), UdeskConst.FileImg, path)
                ).toString());
            }
            mVideoView.setVideoPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    protected void onResume() {


        try {
            if (mPositionWhenPaused >= 0) {
                mVideoView.seekTo(mPositionWhenPaused);
                mPositionWhenPaused = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();

    }

    @Override
    protected void onPause() {
        try {
            mPositionWhenPaused = mVideoView.getCurrentPosition();
            mVideoView.stopPlayback();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        finish();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {


        try {
            mVideoView.start();
            mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        progressBar.setVisibility(View.GONE);
                        video_img.setVisibility(View.GONE);
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mMediaController = null;
            mVideoView = null;
            video_img = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
