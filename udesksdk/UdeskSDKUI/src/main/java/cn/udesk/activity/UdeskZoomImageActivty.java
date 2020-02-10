package cn.udesk.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.rich.LoaderTask;
import cn.udesk.xphotoview.IXphotoView;
import cn.udesk.xphotoview.XPhotoView;
import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.PhotoDraweeView;
import udesk.core.utils.UdeskUtils;

public class UdeskZoomImageActivty extends UdeskBaseActivity implements
        OnClickListener {

    private PhotoDraweeView zoomImageView;
    private View saveIdBtn, originaPhotosBtn;
    private Uri uri;
    private XPhotoView xPhotoView;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        try {
            UdeskUtil.setOrientation(this);
            if (!Fresco.hasBeenInitialized()) {
                UdeskUtil.frescoInit(this);
            }
            setContentView(R.layout.udesk_zoom_imageview);
            zoomImageView = (PhotoDraweeView) findViewById(R.id.udesk_zoom_imageview);
            zoomImageView.setOnPhotoTapListener(new OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    finish();
                }
            });
            Bundle bundle = getIntent().getExtras();
            uri = bundle.getParcelable("image_path");
            UdeskUtil.loadImage(getApplicationContext(), zoomImageView, uri);
            saveIdBtn = findViewById(R.id.udesk_zoom_save);
            originaPhotosBtn = findViewById(R.id.udesk_original_photos);
            saveIdBtn.setOnClickListener(this);
            originaPhotosBtn.setOnClickListener(this);
            xPhotoView = findViewById(R.id.udesk_xphoto_view);
            xPhotoView.setSingleTabListener(new IXphotoView.OnTabListener() {
                @Override
                public void onSingleTab() {
                    finish();
                }

                @Override
                public void onLongTab() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.udesk_zoom_save) {
                LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        saveImage();
                    }
                });
            } else if (v.getId() == R.id.udesk_original_photos) {
                File file = UdeskUtil.getFileFromDiskCache(UdeskZoomImageActivty.this.getApplicationContext(), uri);
                if (file == null) {
                    if (!UdeskUtils.isNetworkConnected(getApplicationContext())) {
                        UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_has_wrong_net));
                        return;
                    }
                    String oldPath = uri.getPath();
                    file = new File(oldPath);
                }

                if (file.exists()){
                    zoomImageView.setVisibility(View.GONE);
                    xPhotoView.recycleAll();
                    xPhotoView.setVisibility(View.VISIBLE);
                    xPhotoView.setImage(file);
                    originaPhotosBtn.setVisibility(View.GONE);
                }else {
                    UdeskUtils.showToast(getApplicationContext(),getString(R.string.wait_try_again));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveImage() {
        if (uri == null) {
            return;
        }
        try {
            File oldFile = UdeskUtil.getFileFromDiskCache(UdeskZoomImageActivty.this.getApplicationContext(), uri);
            if (oldFile == null) {
                String oldPath = uri.getPath();
                oldFile = new File(oldPath);
            }
            // 修改文件路径
            String newName = oldFile.getName();
            if (!newName.contains(".png")) {
                newName = newName + ".png";
            }
            final File folder = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            final File newFile = new File(folder, newName);
            // 拷贝，成功或者失败 都提示下
            if (copyFile(oldFile, newFile)) {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri uri = Uri.fromFile(newFile);
                        intent.setData(uri);
                        UdeskZoomImageActivty.this.sendBroadcast(intent);
                        UdeskUtils.showToast(getApplicationContext(),getString(R.string.udesk_success_save_image));
                        UdeskZoomImageActivty.this.finish();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        UdeskUtils.showToast(getApplicationContext(),getString(R.string.udesk_fail_save_image));
                        UdeskZoomImageActivty.this.finish();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean copyFile(File srcFile, File destFile) {
        boolean result;
        try {
            InputStream in = new FileInputStream(srcFile);
            try {
                result = copyToFile(in, destFile);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    private boolean copyToFile(InputStream inputStream, File destFile) {
        try {
            if (destFile.exists()) {
                destFile.delete();
            }
            FileOutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.flush();
                try {
                    out.getFD().sync();
                } catch (Exception e) {
                } finally {
                    out.close();
                    inputStream.close();
                }

            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
