package cn.udesk.camera.state;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;

import cn.udesk.camera.callback.FocusCallback;


public interface State {

    void start(SurfaceHolder holder, float screenProp);

    void stop();

    void foucs(float x, float y, FocusCallback callback);

    void swtich(SurfaceHolder holder, float screenProp);

    void restart();

    void capture();

    void record(Context context,Surface surface, float screenProp);

    void stopRecord(boolean isShort, long time);

    void cancle(SurfaceHolder holder, float screenProp);

    void confirm();

    void zoom(float zoom, int type);

}
