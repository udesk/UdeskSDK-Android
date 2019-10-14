package cn.udesk.aac.livedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import cn.udesk.UdeskSDKManager;
import cn.udesk.aac.MergeMode;
import cn.udesk.aac.MergeModeManager;
import cn.udesk.db.UdeskDBManager;
import udesk.core.UdeskConst;
import udesk.core.model.MessageInfo;

public class DBLiveData<M> extends MutableLiveData<MergeMode> {

    public void initDB(Context context){
        try {
            if (UdeskDBManager.getInstance().getSQLiteDatabase() == null) {
                UdeskDBManager.getInstance().init(context,
                        UdeskSDKManager.getInstance().getSdkToken(context));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //保存消息
    public void saveMessageDB(final MessageInfo msg) {
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().addMessageInfo(msg);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getHistoryMessage(final int offset, final int pageNum){
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    List<MessageInfo> list = UdeskDBManager.getInstance().getMessages(offset, pageNum);
                    if (list != null){
                        MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.LoadHistoryDBMsg,list,UUID.randomUUID().toString());
                        MergeModeManager.getmInstance().putMergeMode(mergeMode,DBLiveData.this);

                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateAllMsgRead(){
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().updateAllMsgRead();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateMessageFail(){
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().updateSendFlagToFail();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void addAllMessageInfo(final List<MessageInfo> messages){
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().addAllMessageInfo(messages);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void deleteAllMsg(){
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().deleteAllMsg();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onActive() {
        try {
            super.onActive();
            updateAllMsgRead();

            if (UdeskConst.isDebug) {
                Log.i("aac", " DBLiveData onActive");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onInactive() {
        try {
            if (UdeskConst.isDebug) {
                Log.i("aac", " DBLiveData onInactive");
            }
            updateAllMsgRead();
            updateMessageFail();
            super.onInactive();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
