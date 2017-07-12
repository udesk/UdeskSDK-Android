package cn.udesk.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import me.relex.photodraweeview.PhotoDraweeView;

public class UdeskZoomImageActivty extends Activity implements
        OnClickListener {

    private PhotoDraweeView zoomImageView;
    private View saveIdBtn;
    private Uri uri;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        try {
            if (!Fresco.hasBeenInitialized()) {
                UdeskSDKManager.getInstance().init(this);
            }
            setContentView(R.layout.udesk_zoom_imageview);
            zoomImageView = (PhotoDraweeView) findViewById(R.id.udesk_zoom_imageview);
            Bundle bundle = getIntent().getExtras();
            uri = bundle.getParcelable("image_path");
            UdeskUtil.loadImage(zoomImageView, uri);
            saveIdBtn = findViewById(R.id.udesk_zoom_save);
            saveIdBtn.setOnClickListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }catch (OutOfMemoryError error){
            error.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.udesk_zoom_save) {
                new Thread() {
                    public void run() {
                        saveImage();
                    }

                }.start();
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
            File oldFile = UdeskUtil.getFileFromDiskCache(uri);
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
                        Toast.makeText(
                                UdeskZoomImageActivty.this,
                                getResources().getString(
                                        R.string.udesk_success_save_image) + folder.getAbsolutePath(),
                                Toast.LENGTH_SHORT).show();
                        UdeskZoomImageActivty.this.finish();
                    }
                });

            } else {

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(
                                UdeskZoomImageActivty.this,
                                getResources().getString(
                                        R.string.udesk_fail_save_image),
                                Toast.LENGTH_SHORT).show();
                        UdeskZoomImageActivty.this.finish();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
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
