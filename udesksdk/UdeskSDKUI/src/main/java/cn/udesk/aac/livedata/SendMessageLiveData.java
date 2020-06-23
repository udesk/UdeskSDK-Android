package cn.udesk.aac.livedata;

import androidx.lifecycle.MutableLiveData;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import cn.udesk.aac.MergeMode;
import cn.udesk.aac.MergeModeManager;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.messagemanager.UdeskXmppManager;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.MessageCache;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;


/**
 * 发送消息的流程: 1调用http接口保存后  在调用xmpp发送消息；
 * 缓存发送的消息，收到xmpp回执后移除缓存，如果没有收到回执需要调用http接口保存。
 * 保存2次后  后端会代发。
 * <p>
 * 重发考虑：
 *
 * @param <M>
 */
public class SendMessageLiveData<M> extends MutableLiveData<MergeMode> {

    //Message调用http接口后，五秒没有收到回执后，执行第二次发送。如果收到则移除
    ConcurrentHashMap<String, Future> futureMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private Map<String, MessageInfo> sendingMsgCache = Collections.synchronizedMap(new LinkedHashMap<String, MessageInfo>());

    private String domain = "";
    private String secretKey = "";
    private String sdktoken = "";
    private String appid = "";
    private String customerId = "";
    private AgentInfo agentInfo;

    public void setBaseValue(String domain, String secretKey, String sdktoken,
                             String appid) {
        this.domain = domain;
        this.secretKey = secretKey;
        this.sdktoken = sdktoken;
        this.appid = appid;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
    }

    private void addScheduler(final MessageInfo msg) {
        try {
            //5秒后执行
            ScheduledFuture future = scheduler.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    secondSave(msg);
                }
            }, 5, 5, TimeUnit.SECONDS);
            futureMap.put(msg.getMsgId(), future);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //收到消息回执后，取消对应的future
    public void removeSendMsgCace(String msgId) {
        try {
            Future future = futureMap.get(msgId);
            if (future != null) {
                future.cancel(true);
            }
            sendingMsgCache.remove(msgId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized void sendMessage(final MessageInfo msg) {
        try {
            if (msg == null || TextUtils.isEmpty(customerId) || agentInfo == null) {
                return;
            }
            if (UdeskConst.isDebug) {
                Log.i("aac", " SendMessageLiveData messageSave");
            }
            sendingMsgCache.put(msg.getMsgId(), msg);
            UdeskHttpFacade.getInstance().messageSave(domain, secretKey, sdktoken,
                    appid, customerId, agentInfo.getAgent_id(), agentInfo.getIm_sub_session_id(),
                    UdeskConst.UdeskSendStatus.sending,
                    msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                    msg.getDuration(), msg.getSeqNum(), msg.getFilename(), msg.getFilesize(),
                    UdeskUtils.getSecondTimestamp(new Date()) - UdeskConst.active_time,
                    UdeskConst.sdk_page_status + UdeskConst.sdk_xmpp_statea,msg.getLocalPath(), new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                MessageInfo messageInfo = sendingMsgCache.get(msg.getMsgId());
                                if (messageInfo != null) {
                                    messageInfo.setCount();
                                }else {
                                    msg.setCount();
                                }
                                // 发给当前的客服
                                msg.setNoNeedSave(true);
                                UdeskXmppManager.getInstance().sendMessage(msg);
                                //加入延时执行的线程池中
                                addScheduler(msg);
                                JSONObject jsonObject = new JSONObject(message);
                                //返回客服消息序列  大于本地存储的, 有丢失消息, 需要拉取消息
                                if (jsonObject.has("agent_seq_num")) {
                                    int agent_seq_num = jsonObject.optInt("agent_seq_num");
                                    //检查是否跳序
                                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.Check_Agent_Seq_Num, agent_seq_num,UUID.randomUUID().toString());
                                    MergeModeManager.getmInstance().putMergeMode(mergeMode,SendMessageLiveData.this);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {

                            // 发给当前的客服
                            try {
                                if (TextUtils.equals("8002", message)) {
                                    removeSendMsgCace(msg.getMsgId());
                                    UdeskDBManager.getInstance().updateMsgSendFlagDB(msg.getMsgId(), UdeskConst.SendFlag.RESULT_FAIL);
                                    postMessage(msg.getMsgId(),UdeskConst.LiveDataType.Send_Message_Failure);
                                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RECREATE_CUSTOMER_INFO, msg.getMsgId(),UUID.randomUUID().toString());
                                    MergeModeManager.getmInstance().putMergeMode(mergeMode,SendMessageLiveData.this);

                                    return;
                                }
                                //加入延时执行的线程池中
                                addScheduler(msg);
                                msg.setNoNeedSave(false);
                                UdeskXmppManager.getInstance().sendMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void secondSave(final MessageInfo msg) {

        //第二次发送走的保存逻辑
        try {
            if (TextUtils.isEmpty(customerId) || msg == null || agentInfo == null) {
                return;
            }
            if (UdeskConst.isDebug) {
                Log.i("aac", " SendMessageLiveData secondSave");
            }
            UdeskHttpFacade.getInstance().messageSave(domain, secretKey, sdktoken, appid, customerId, agentInfo.getAgent_id(), agentInfo.getIm_sub_session_id(),
                    UdeskConst.UdeskSendStatus.sending, msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                    msg.getDuration(), msg.getSeqNum(), msg.getFilename(), msg.getFilesize(),
                    UdeskUtils.getSecondTimestamp(new Date()) - UdeskConst.active_time,
                    UdeskConst.sdk_page_status + UdeskConst.sdk_xmpp_statea, msg.getLocalPath(), new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                MessageInfo cacheMsg = sendingMsgCache.get(msg.getMsgId());
                                // 发送2次也算成功，有服务端代发通知客服,等同于收到xmpp回执
                                if (cacheMsg != null && (cacheMsg.getCount() + 1) >= 2) {
                                    postMessage(msg.getMsgId(),UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessageReceived);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            if (TextUtils.equals("8002", message)) {
                                removeSendMsgCace(msg.getMsgId());
                                UdeskDBManager.getInstance().updateMsgSendFlagDB(msg.getMsgId(), UdeskConst.SendFlag.RESULT_FAIL);
                                postMessage(msg.getMsgId(),UdeskConst.LiveDataType.Send_Message_Failure);
                                return;
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //htpp 方式保存消息到后台
    public void sendQueueMessage(final MessageInfo msg) {
        try {
            if (TextUtils.isEmpty(customerId)) {
                return;
            }
            UdeskHttpFacade.getInstance().queueMessageSave(domain,
                    secretKey, sdktoken, appid, customerId, msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                    msg.getDuration(), msg.getSeqNum(), msg.getFilename(), msg.getFilesize(), new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {

                            try {
                                JSONObject resultJson = new JSONObject(message);
                                if (resultJson.has("code")) {
                                    int status = resultJson.getInt("code");
                                    if (status == 1000) {
                                        //保存成功 等同于xmpp发送成功
                                        UdeskDBManager.getInstance().updateMsgSendFlagDB(msg.getMsgId(), UdeskConst.SendFlag.RESULT_SUCCESS);
                                        postMessage(msg.getMsgId(),UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessageReceived);
                                        return;
                                    }
                                    if (status == 9200) {
                                        String tipMsg = resultJson.getString("message");
                                        MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.QueueMessageSaveError, tipMsg,UUID.randomUUID().toString());
                                        MergeModeManager.getmInstance().putMergeMode(mergeMode,SendMessageLiveData.this);

                                    }
                                    postMessage(msg.getMsgId(),UdeskConst.LiveDataType.Send_Message_Failure);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onFail(String error) {
                            postMessage(msg.getMsgId(),UdeskConst.LiveDataType.Send_Message_Failure);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送输入预支消息
    public void sendPreMessage(String inputContent) {
        try {
            if (agentInfo == null) {
                return;
            }
            UdeskXmppManager.getInstance().sendPreMsg(UdeskConst.ChatMsgTypeString.TYPE_TEXT,
                    inputContent, agentInfo.getAgentJid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送商品链接广告
    public void sendCommodityMessage(UdeskCommodityItem commodityItem) {
        try {
            if (agentInfo == null) {
                return;
            }
            UdeskXmppManager.getInstance().sendComodityMessage(buildCommodityMessage(commodityItem),
                    agentInfo.getAgentJid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //构造广告消息的格式
    private String buildCommodityMessage(UdeskCommodityItem item) {
        JSONObject root = new JSONObject();
        try {
            JSONObject dataJson = new JSONObject();
            JSONObject paramsJson = new JSONObject();
            paramsJson.put("detail", item.getSubTitle());
            dataJson.put("url", item.getCommodityUrl());
            dataJson.put("image", item.getThumbHttpUrl());
            dataJson.put("title", item.getTitle());
            dataJson.put("product_params", paramsJson);
            root.put("type", "product");
            root.put("platform", "android");
            root.put("data", dataJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root.toString();
    }


    //提交留言消息
    public void putLeavesMsg(String msg, final String msgId) {
        try {
            UdeskHttpFacade.getInstance().putReplies(
                    domain, secretKey, sdktoken, appid, msg,msgId,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            //修改消息状态
                            try {
                                UdeskDBManager.getInstance().updateMsgSendFlagDB(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                                postMessage(msgId,UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessageReceived);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String msg) {
                            try {
                                UdeskDBManager.getInstance().updateMsgSendFlagDB(msgId, UdeskConst.SendFlag.RESULT_FAIL);
                                postMessage(msgId,UdeskConst.LiveDataType.Send_Message_Failure);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //提交留言消息
    public void putIMLeavesMsg(String msg, final String msgId,String msgType,String agentId,String groupId,String menuId) {
        try {
            if (TextUtils.isEmpty(customerId) || TextUtils.isEmpty(msg)) {
                return;
            }
            UdeskHttpFacade.getInstance().putIMReplies(
                    domain, secretKey, sdktoken, appid, msg,msgId,msgType,agentId,groupId,menuId,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            //修改消息状态
                            try {
                                UdeskDBManager.getInstance().updateMsgSendFlagDB(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                                postMessage(msgId, UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessageReceived);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String msg) {
                            try {
                                if (TextUtils.equals("6000", msg)) {
                                    postMessage(msgId, UdeskConst.LiveDataType.GetAgentInfo);
                                    return;
                                }
                                UdeskDBManager.getInstance().updateMsgSendFlagDB(msgId, UdeskConst.SendFlag.RESULT_FAIL);
                                postMessage(msgId, UdeskConst.LiveDataType.Send_Message_Failure);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postMessage(String msgId,int type){
        try {
            MergeMode mergeMode = new MergeMode(type, msgId,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,SendMessageLiveData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActive() {
        super.onActive();
        InvokeEventContainer.getInstance().event_OnSendMessageFail.bind(this, "secondSave");
        if (UdeskConst.isDebug) {
            Log.i("aac", " SendMessageLiveData onActive");
        }
    }

    @Override
    protected void onInactive() {
        if (UdeskConst.isDebug) {
            Log.i("aac", " SendMessageLiveData onInactive");
        }
        InvokeEventContainer.getInstance().event_OnSendMessageFail.unBind(this);
        //失去焦点 就启动
        MessageCache.getInstance().setBaseValue(domain, secretKey, sdktoken, appid, customerId);
        MessageCache.getInstance().putAll(sendingMsgCache);
        super.onInactive();
    }
}
