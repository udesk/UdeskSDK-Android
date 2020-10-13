package cn.udesk.aac;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.udesk.JsonUtils;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.aac.livedata.APILiveData;
import cn.udesk.aac.livedata.DBLiveData;
import cn.udesk.aac.livedata.FileLiveData;
import cn.udesk.aac.livedata.ReceiveLivaData;
import cn.udesk.aac.livedata.RobotApiData;
import cn.udesk.aac.livedata.SendMessageLiveData;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.rich.LoaderTask;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.model.Product;

public class UdeskViewMode extends ViewModel {

    //收到客服 通过xmpp发送的消息
    private ReceiveLivaData<MergeMode> messageLivaData;

    //处理发送消息的逻辑
    private SendMessageLiveData<MergeMode> sendMessageLiveData;

    private DBLiveData<MergeMode> dbLiveData;

    //集中消息外的api处理
    private APILiveData<MergeMode> apiLiveData;

    private RobotApiData<MergeMode> robotApiData;

    //处理在UdeskViewMode中直接通知Activity和Fragment刷新的
    private MutableLiveData<MergeMode> mutableLiveData;

    private FileLiveData<MergeMode> upLoadFileLiveData;

    //缓存pre_session时发送的消息
    private List<MessageInfo> cachePreMsg = new ArrayList<>();

    MediatorLiveData<MergeMode> liveDataMerger = new MediatorLiveData();

    //标记是否增加了留言事件
    private boolean leavingMsg = false;


    public UdeskViewMode() {
        merger();
    }

    private void merger() {
        messageLivaData = new ReceiveLivaData<>();
        sendMessageLiveData = new SendMessageLiveData<>();
        apiLiveData = new APILiveData<>();
        dbLiveData = new DBLiveData<>();
        mutableLiveData = new MutableLiveData<>();
        upLoadFileLiveData = new FileLiveData<>();
        robotApiData = new RobotApiData<>();
        liveDataMerger.addSource(messageLivaData, new Observer<MergeMode>() {
            @Override
            public void onChanged(@Nullable MergeMode mergeMode) {
                liveDataMerger.postValue(mergeMode);
            }
        });
        liveDataMerger.addSource(sendMessageLiveData, new Observer<MergeMode>() {
            @Override
            public void onChanged(@Nullable MergeMode mergeMode) {
                liveDataMerger.postValue(mergeMode);
            }
        });
        liveDataMerger.addSource(apiLiveData, new Observer<MergeMode>() {
            @Override
            public void onChanged(@Nullable MergeMode mergeMode) {
                liveDataMerger.postValue(mergeMode);
            }
        });
        liveDataMerger.addSource(dbLiveData, new Observer<MergeMode>() {
            @Override
            public void onChanged(@Nullable MergeMode mergeMode) {
                liveDataMerger.postValue(mergeMode);
            }
        });
        liveDataMerger.addSource(mutableLiveData, new Observer<MergeMode>() {
            @Override
            public void onChanged(@Nullable MergeMode mergeMode) {
                liveDataMerger.postValue(mergeMode);
            }
        });

        liveDataMerger.addSource(upLoadFileLiveData, new Observer<MergeMode>() {
            @Override
            public void onChanged(@Nullable MergeMode mergeMode) {
                liveDataMerger.postValue(mergeMode);
            }
        });

        liveDataMerger.addSource(robotApiData, new Observer<MergeMode>() {
            @Override
            public void onChanged(@Nullable MergeMode mergeMode) {
                liveDataMerger.postValue(mergeMode);
            }
        });
    }

    public void setBaseValue(String domain, String secretKey, String sdktoken,
                             String appid) {
        apiLiveData.setBaseValue(domain, secretKey, sdktoken, appid);
        sendMessageLiveData.setBaseValue(domain, secretKey, sdktoken, appid);
        upLoadFileLiveData.setBaseValue(domain, secretKey, sdktoken, appid);
        robotApiData.setBaseValue(domain, secretKey, sdktoken, appid);
    }

    public void setHandler(UdeskChatActivity.MyHandler handler) {
        upLoadFileLiveData.setHandler(handler);
    }

    public void setCustomerId(String customerId) {
        apiLiveData.setCustomerId(customerId);
        sendMessageLiveData.setCustomerId(customerId);
        robotApiData.setCustomerId(customerId);
    }

    public void setSessionId(int sessionId) {
        robotApiData.setSessionId(sessionId);
    }

    public void setRobotUrl(String robotUrl) {
        robotApiData.setRobotUrl(robotUrl);
    }

    public void setAgentInfo(AgentInfo agentInfo) {
        apiLiveData.setAgentInfo(agentInfo);
        sendMessageLiveData.setAgentInfo(agentInfo);
    }

    public MediatorLiveData getLiveDataMerger() {
        return liveDataMerger;
    }

    //发送消息
    //封装发送文本消息
    public void sendTxtMessage(String msgString) {
        try {
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_TEXT,
                    System.currentTimeMillis(), msgString);

            postMessage(msg, UdeskConst.LiveDataType.AddMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendImLeaveMessage(MessageInfo msg) {
        try {
            postMessage(msg, UdeskConst.LiveDataType.AddMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送商品消息
    public void sendProductMessage(Product mProduct) {
        if (mProduct == null) {
            return;
        }
        try {
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_PRODUCT,
                    System.currentTimeMillis(), JsonUtils.getProduceJson(mProduct).toString());
            postMessage(msg, UdeskConst.LiveDataType.AddMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送地理位置信息
     *
     * @param lat
     * @param longitude
     * @param localvalue
     * @param bitmapDir
     */
    public void sendLocationMessage(double lat, double longitude, String localvalue, String bitmapDir) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(lat).append(";").append(longitude).append(";").append("16;").append(localvalue);
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_LOCATION,
                    System.currentTimeMillis(), builder.toString(), bitmapDir, "", "");
            postMessage(msg, UdeskConst.LiveDataType.AddMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送留言消息
    public void sendLeaveMessage(String message) {
        try {
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG,
                    System.currentTimeMillis(), message);
            postMessage(msg, UdeskConst.LiveDataType.AddLeaveMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送对话留言消息
    public void sendIMLeaveMessage(String message) {
        try {
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG_IM,
                    System.currentTimeMillis(), message);
            postMessage(msg, UdeskConst.LiveDataType.AddIMLeaveMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送原图图片消息
    public void sendBitmapMessage(Bitmap bitmap, Context context) {
        try {
            if (bitmap == null) {
                return;
            }
            File scaleImageFile = UdeskUtil.getScaleFile(bitmap, context);
            if (scaleImageFile != null) {
                MessageInfo msgInfo = UdeskUtil.buildSendMessage(
                        UdeskConst.ChatMsgTypeString.TYPE_IMAGE,
                        System.currentTimeMillis(), "", scaleImageFile.getPath(), "", "");
                postMessage(msgInfo, UdeskConst.LiveDataType.AddFileMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //发送文件类的消息( 包含视频 文件 图片)

    /**
     * @param filepath
     * @param msgType  图片:UdeskConst.ChatMsgTypeString.TYPE_IMAGE
     *                 文件:UdeskConst.ChatMsgTypeString.TYPE_File
     *                 MP4视频: UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO
     */
    public synchronized void sendFileMessage(Context context, String filepath, String msgType) {
        try {
            if (TextUtils.isEmpty(filepath)) {
                return;
            }
            String fileName = (UdeskUtil.getFileName(context, filepath, msgType));
            String fileSize = UdeskUtil.getFileSizeByLoaclPath(context, filepath);
            MessageInfo msgInfo = UdeskUtil.buildSendMessage(msgType,
                    System.currentTimeMillis(), "", filepath, fileName, fileSize);
            postMessage(msgInfo, UdeskConst.LiveDataType.AddFileMessage);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //发送图片消息
    public void scaleBitmap(final Context context,final String path,final int orientation) {
        try {
            if (!TextUtils.isEmpty(path)) {
                File scaleImageFile = UdeskUtil.getScaleFile(context,path, orientation);
                if (scaleImageFile != null) {
                    sendFileMessage(context, scaleImageFile.getPath(), UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                } else {
                    sendFileMessage(context, path, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 发送录音信息
    public void sendRecordAudioMsg(Context context, String audiopath, long duration) {
        try {
            String fileName = (UdeskUtil.getFileName(context, audiopath, UdeskConst.FileAudio));
            MessageInfo msgInfo = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_AUDIO,
                    System.currentTimeMillis(), "", audiopath, fileName, "");
            duration = duration / 1000 + 1;
            msgInfo.setDuration(duration);
            postMessage(msgInfo, UdeskConst.LiveDataType.AddFileMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //方式咨询对象
    public void sendCommodityMessage(UdeskCommodityItem commodity) {
        try {
            sendMessageLiveData.sendCommodityMessage(commodity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putLeavesMsg(MessageInfo info) {
        try {
            sendMessageLiveData.putLeavesMsg(info.getMsgContent(), info.getMsgId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void putIMLeavesMsg(MessageInfo info,String agentId,String groupId,String menuId) {
        try {
            sendMessageLiveData.putIMLeavesMsg(info.getMsgContent(), info.getMsgId(),"",agentId,groupId,menuId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void postMessage(MessageInfo msg, int type) {
        try {
            MergeMode mergeMode = new MergeMode(type, msg, UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode, mutableLiveData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postNextMessage(MergeMode mergeMode) {
        try {
            if (mergeMode != null) {
                MergeModeManager.getmInstance().dealMergeMode(mergeMode, mutableLiveData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addPressionMsg(MessageInfo info) {
        cachePreMsg.add(info);
    }

    public SendMessageLiveData<MergeMode> getSendMessageLiveData() {
        return sendMessageLiveData;
    }

    public DBLiveData<MergeMode> getDbLiveData() {
        return dbLiveData;
    }

    public APILiveData getApiLiveData() {
        return apiLiveData;
    }

    public FileLiveData getFileLiveData() {
        return upLoadFileLiveData;
    }

    public RobotApiData<MergeMode> getRobotApiData() {
        return robotApiData;
    }

    //发送对话过滤缓存消息
    public void sendPrefilterMsg(boolean isRetry) {
        try {
            if (cachePreMsg.size() > 0) {
                for (final MessageInfo messageInfo : cachePreMsg) {
                    if (isRetry) {
                        startRetryMsg(messageInfo);
                    } else {
                        LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                UdeskDBManager.getInstance().updateMsgSendFlagDB(messageInfo.getMsgId(), UdeskConst.SendFlag.RESULT_FAIL);
                                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.Send_Message_Failure, messageInfo.getMsgId(), UUID.randomUUID().toString());
                                MergeModeManager.getmInstance().putMergeMode(mergeMode, mutableLiveData);
                            }
                        });
                    }
                }
                cachePreMsg.clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击失败按钮 重试发送消息
    public void startRetryMsg(MessageInfo message) {
        try {
            if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_TEXT) || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_LOCATION)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_PRODUCT)) {
                postMessage(message, UdeskConst.LiveDataType.AddMessage);
            } else if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_IMAGE)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_AUDIO)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_FILE)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO)) {
                postMessage(message, UdeskConst.LiveDataType.AddFileMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isLeavingMsg() {
        return leavingMsg;
    }

    public void setLeavingMsg(boolean leavingMsg) {
        this.leavingMsg = leavingMsg;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (UdeskConst.isDebug) {
            Log.i("aac", "UdeskViewMode onCleared");
        }
        apiLiveData.quitQueue(UdeskSDKManager.getInstance().getUdeskConfig().UdeskQuenuMode);
        UdeskHttpFacade.getInstance().cancel();
        liveDataMerger.removeSource(messageLivaData);
        liveDataMerger.removeSource(sendMessageLiveData);
        liveDataMerger.removeSource(apiLiveData);
        liveDataMerger.removeSource(dbLiveData);
        liveDataMerger.removeSource(mutableLiveData);
        liveDataMerger.removeSource(upLoadFileLiveData);
        liveDataMerger.removeSource(robotApiData);

        messageLivaData = null;
        sendMessageLiveData = null;
        apiLiveData = null;
        dbLiveData = null;
        mutableLiveData = null;
        upLoadFileLiveData = null;
        liveDataMerger = null;
        robotApiData = null;
        cachePreMsg.clear();
    }
}
