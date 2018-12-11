package cn.udesk.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import cn.udesk.UdeskSDKManager;
import cn.udesk.db.UdeskDBManager;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.MessageInfo;

public class MessageCache {

    private volatile boolean isRunning = true;
    private BlockingQueue<MessageInfo> sendMsgQueue = null;
    private static MessageCache instance = new MessageCache();

    private MessageCache() {
        //声明一个容量为30的缓存队列(不需要太大了)，
        // 在客户离开界面后启动个发送，直到全部发送成功
        sendMsgQueue = new LinkedBlockingQueue<>(30);
    }

    public static MessageCache getInstance() {
        return instance;
    }

    //传入 getApplicationContext()
    public void putAll(Map<String, MessageInfo> cache, Context mContext) {
        try {
            if (sendMsgQueue != null && cache.size() > 0) {
                for (Map.Entry<String, MessageInfo> entry : cache.entrySet()) {
                    String key = entry.getKey();
                    sendMsgQueue.offer(cache.get(key));
                }
                isRunning = true;
                doSendMessage(mContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void doSendMessage(final Context mContext) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            //有数据时直接从队列的队首取走，无数据时阻塞，
                            // 在5s内有数据，取走，超过5s还没数据，返回null;
                            MessageInfo info = sendMsgQueue.poll(5, TimeUnit.SECONDS);
                            if (info != null) {
                                //处理发送消息流程
                                onSendMessageFail(info, mContext);
                                //保证发送顺序 避免请求密集导致服务端压力
                                Thread.sleep(500);
                            } else {
//                            没有数据，退出线程，等待系统销毁
                                isRunning = false;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void onSendMessageFail(final MessageInfo msg, Context mContext) {
//        xmpp发送失败后 再次调用messageSave  触发后端代发xmpp
        try {
            if (TextUtils.isEmpty(msg.getCustomerId())) {
                return;
            }
            UdeskHttpFacade.getInstance().messageSave(UdeskSDKManager.getInstance().getDomain(mContext),
                    UdeskSDKManager.getInstance().getAppkey(mContext),
                    UdeskSDKManager.getInstance().getSdkToken(mContext),
                    UdeskSDKManager.getInstance().getAppId(mContext),
                    msg.getCustomerId(), msg.getmAgentJid(),
                    msg.getSubsessionid(), UdeskConst.UdeskSendStatus.sending,
                    msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                    msg.getDuration(), msg.getSeqNum(), msg.getFilename(),
                    msg.getFilesize(), 0, "global_cache" + UdeskConst.sdk_page_status, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                msg.setCount();
//                       messagesave发送2次成功，有服务端代发通知客服,消息发送成功，更新db状态
                                if (msg.getCount() >= 2) {
                                    UdeskDBManager.getInstance().updateMsgSendFlag(msg.getMsgId(),
                                            UdeskConst.SendFlag.RESULT_SUCCESS);
                                } else {
                                    //继续加入队列
                                    sendMsgQueue.put(msg);

                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {

                            try {
                                //失败的次数会加1
                                msg.setFailureCount();
                                //messagesave 失败超过3次，计算服务异常，更新消息发送失败
                                //小于3次 继续加入队列
                                if (msg.getFailureCount() > 3) {
                                    UdeskDBManager.getInstance().updateMsgSendFlag(msg.getMsgId(),
                                            UdeskConst.SendFlag.RESULT_FAIL);
                                } else {
                                    //继续加入队列

                                    sendMsgQueue.put(msg);

                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void clear() {
        try {
            if (sendMsgQueue != null) {
                sendMsgQueue.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
