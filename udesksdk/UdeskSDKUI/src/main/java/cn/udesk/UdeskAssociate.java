package cn.udesk;

import android.text.TextUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UdeskAssociate {
    private static UdeskAssociate mInstance;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private String oldText = "";
    private Future future;

    private UdeskAssociate() {
    }

    public static UdeskAssociate getmInstance() {
        try {
            if (mInstance == null) {
                synchronized (UdeskAssociate.class) {
                    if (mInstance == null) {
                        mInstance = new UdeskAssociate();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return mInstance;
    }

    public String getOldText() {
        return oldText;
    }

    public boolean compareText(String newText) {
        try {
            if (TextUtils.isEmpty(newText)) {
                oldText = newText;
                return false;
            }
            if (TextUtils.isEmpty(oldText)) {
                oldText = newText;
                return true;
            }
            if (!TextUtils.equals(oldText, newText)) {
                oldText = newText;
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public Future scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        try {
            future = scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }catch (Exception e){
            e.printStackTrace();
        }
        return future;
    }

    public void cancel() {
        try {
            if (future != null) {
                future.cancel(true);
                future = null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Future getFuture() {
        return future;
    }

}
