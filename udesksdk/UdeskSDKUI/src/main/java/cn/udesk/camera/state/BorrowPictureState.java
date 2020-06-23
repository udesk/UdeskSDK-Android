package cn.udesk.camera.state;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import cn.udesk.camera.CameraInterface;
import cn.udesk.camera.UdeskCameraView;
import cn.udesk.camera.callback.FocusCallback;


public class BorrowPictureState implements State {
    private CameraMachine machine;

    public BorrowPictureState(CameraMachine machine) {
        this.machine = machine;
    }

    @Override
    public void start(SurfaceHolder holder, float screenProp) {
        try {
            CameraInterface.getInstance().doStartPreview(holder, screenProp);
            machine.setState(machine.getPreviewState());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {

    }


    @Override
    public void foucs(float x, float y, FocusCallback callback) {
    }

    @Override
    public void swtich(SurfaceHolder holder, float screenProp) {

    }

    @Override
    public void restart() {

    }

    @Override
    public void capture() {

    }

    @Override
    public void record(Context context,Surface surface, float screenProp) {

    }

    @Override
    public void stopRecord(boolean isShort, long time) {
    }

    @Override
    public void cancle(SurfaceHolder holder, float screenProp) {
        try {
            CameraInterface.getInstance().doStartPreview(holder, screenProp);
            machine.getView().resetState(UdeskCameraView.TYPE_PICTURE);
            machine.setState(machine.getPreviewState());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void confirm() {
        try {
            machine.getView().confirmState(UdeskCameraView.TYPE_PICTURE);
            machine.setState(machine.getPreviewState());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void zoom(float zoom, int type) {

    }


}
