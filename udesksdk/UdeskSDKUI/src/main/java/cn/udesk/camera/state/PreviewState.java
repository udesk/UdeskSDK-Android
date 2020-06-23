package cn.udesk.camera.state;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import cn.udesk.camera.CameraInterface;
import cn.udesk.camera.UdeskCameraView;
import cn.udesk.camera.callback.FocusCallback;
import cn.udesk.camera.callback.StopRecordCallback;
import cn.udesk.camera.callback.TakePictureCallback;


class PreviewState implements State {

    private CameraMachine machine;

    PreviewState(CameraMachine machine) {
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        try {
            CameraInterface.getInstance().doStartPreview(holder, screenProp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            CameraInterface.getInstance().doStopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void foucs(float x, float y, FocusCallback callback) {
        try {
            if (machine.getView().handlerFoucs(x, y)) {
                CameraInterface.getInstance().handleFocus(machine.getContext(), x, y, callback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {
        try {
            CameraInterface.getInstance().switchCamera(holder, screenProp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {
        try {
            CameraInterface.getInstance().takePicture(new TakePictureCallback() {
                @Override
                public void captureResult(Bitmap bitmap, boolean isVertical) {
                    machine.getView().showPicture(bitmap, isVertical);
                    machine.setState(machine.getBorrowPictureState());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void record(Context context,Surface surface, float screenProp) {
        try {
            CameraInterface.getInstance().startRecord(context,surface, screenProp, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopRecord(final boolean isShort, long time) {
        try {
            CameraInterface.getInstance().stopRecord(isShort, new StopRecordCallback() {
                @Override
                public void recordResult(String url, Bitmap firstFrame) {
                    if (isShort) {
                        machine.getView().resetState(UdeskCameraView.TYPE_SHORT);
                    } else {
                        machine.getView().playVideo(firstFrame, url);
                        machine.setState(machine.getBorrowVideoState());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {

    }

    @Override
    public void confirm() {

    }

    @Override
    public void zoom(float zoom, int type) {
        try {
            CameraInterface.getInstance().setZoom(zoom, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
