package cn.udesk.aac.livedata;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import cn.udesk.aac.MergeMode;


/**
 * 收到消息的回执
 *
 * @param <M>
 */
public class ReceiveLivaData<M> extends MutableLiveData<MergeMode> {
//    /**
//     * 收到新消息
//     */
//    public void onNewMessage(MessageInfo msgInfo) {
//        try {
//            if (msgInfo != null) {
//                if (UdeskConst.isDebug) {
//                    Log.i("aac", " ReceiveLivaData onNewMessage content =" + msgInfo.getMsgContent());
//                }
//                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessage, msgInfo,UUID.randomUUID().toString());
//                MergeModeManager.getmInstance().putMergeMode(mergeMode,ReceiveLivaData.this);
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 收到发送消息的回执
//     */
//    public void onMessageReceived(String msgId) {
//        try {
//            if (!TextUtils.isEmpty(msgId)) {
//                if (UdeskConst.isDebug) {
//                    Log.i("aac", " ReceiveLivaData messageReceived msgId =" + msgId);
//                }
//                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessageReceived, msgId,UUID.randomUUID().toString());
//                MergeModeManager.getmInstance().putMergeMode(mergeMode,ReceiveLivaData.this);
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 收到客服在线下线的通知
//     */
//    public void onNewPresence(String jid, Integer onlineflag) {
//        try {
//            if (!TextUtils.isEmpty(jid)) {
//                Map<String, Object> hashMap = new HashMap<>();
//                hashMap.put("jid", jid);
//                hashMap.put("onlineflag", onlineflag);
//                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpPresence, hashMap,UUID.randomUUID().toString());
//                MergeModeManager.getmInstance().putMergeMode(mergeMode,ReceiveLivaData.this);
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

//    /**
//     * 收到满意度调查消息
//     *
//     * @param isSurvey
//     */
//    public void onReqsurveyMsg(Boolean isSurvey) {
//        try {
//            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpSurvey, isSurvey,UUID.randomUUID().toString());
//            MergeModeManager.getmInstance().putMergeMode(mergeMode,ReceiveLivaData.this);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }

//    //收到結束會話和工單回復的Action消息
//    public void onActionMsg(String type, String actionText, String agentJId) {
//        try {
//            if (TextUtils.isEmpty(actionText)) {
//                return;
//            }
//            if (type.equals("ticket_reply")) {
//                //调用获取工单离线消息
//                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpTicketReplay, true,UUID.randomUUID().toString());
//                MergeModeManager.getmInstance().putMergeMode(mergeMode,ReceiveLivaData.this);
//
//                return;
//            }
//            if (actionText.equals("overtest")) {
//                UdeskXmppManager.getInstance().sendActionMessage(agentJId);
//            } else if (actionText.equals("over")) {
//                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpOver, true,UUID.randomUUID().toString());
//                MergeModeManager.getmInstance().putMergeMode(mergeMode,ReceiveLivaData.this);
//
//                try {
//                    Thread.sleep(2000);
//                    UdeskXmppManager.getInstance().cancleXmpp();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    @Override
    protected void onActive() {
//        if (UdeskConst.isDebug) {
//            Log.i("aac", " ReceiveLivaData onActive");
//        }
//        InvokeEventContainer.getInstance().event_OnNewPresence.bind(this, "onNewPresence");
//        InvokeEventContainer.getInstance().eventui_OnNewMessage.bind(this, "onNewMessage");
//        InvokeEventContainer.getInstance().event_OnMessageReceived.bind(this, "onMessageReceived");
//        InvokeEventContainer.getInstance().event_OnReqsurveyMsg.bind(this, "onReqsurveyMsg");
//        InvokeEventContainer.getInstance().event_OnActionMsg.bind(this, "onActionMsg");


        super.onActive();
    }

    @Override
    protected void onInactive() {
//        if (UdeskConst.isDebug) {
//            Log.i("aac", " ReceiveLivaData onInactive");
//        }
//        InvokeEventContainer.getInstance().eventui_OnNewMessage.unBind(this);
//        InvokeEventContainer.getInstance().event_OnMessageReceived.unBind(this);
//        InvokeEventContainer.getInstance().event_OnNewPresence.unBind(this);
//        InvokeEventContainer.getInstance().event_OnReqsurveyMsg.unBind(this);
//        InvokeEventContainer.getInstance().event_OnActionMsg.unBind(this);

        super.onInactive();
    }
}
