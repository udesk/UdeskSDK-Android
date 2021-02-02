package cn.udesk.adapter;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.udesk.R;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.itemview.BaseViewHolder;
import cn.udesk.itemview.LeftViewHolder;
import cn.udesk.itemview.RightViewHolder;
import cn.udesk.model.UdeskQueueItem;
import udesk.core.UdeskConst;
import udesk.core.model.MessageInfo;

public class MessageAdatper extends BaseAdapter {

    /**
     * 左侧消息类型
     */
    public static final int LEFT = 0;
    /**
     * 右侧消息类型
     */
    public static final int RIGHT = 1;
    /**
     * 非法消息类型
     */
    public static final int ILLEGAL = -1;

    //2条消息之间 时间间隔超过SPACE_TIME， 会话界面会显示出消息的收发时间
    public static final long SPACE_TIME = 3 * 60 * 1000;

    private final Activity mContext;
    private List<MessageInfo> list = new ArrayList<>();
    private BaseViewHolder holder;


    public MessageAdatper(Activity context) {
        mContext = context;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    public List<MessageInfo> getList() {
        return list;
    }

    /**
     * @return 返回当前位置消息的类型和方向标识
     */
    @Override
    public int getItemViewType(int position) {
        try {
            MessageInfo message = getItem(position);
            if (message == null) {
                return ILLEGAL;
            }
            if (message instanceof UdeskQueueItem) {
                return LEFT;
            }
            if (UdeskConst.parseTypeForMessage(message.getMsgtype()) == UdeskConst.ChatMsgTypeInt.TYPE_SURVEY){
                return LEFT;
            }
            if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                return LEFT;
            } else {
                return RIGHT;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ILLEGAL;
        }

    }

    /**
     * @return 返回有多少种UI布局样式
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public void removeQueueMessage(MessageInfo message) {
        if (message == null) {
            return;
        }
        try {
            list.contains(message);
            list.remove(message);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加一条消息
     */
    public void addItem(MessageInfo message) {
        if (message == null) {
            return;
        }
        //不是撤回消息则过滤含有相同msgID的消息，如果是撤回消息则替换掉
        try {
            for (MessageInfo info : list) {
                if (!TextUtils.isEmpty(message.getMsgId()) &&
                        !TextUtils.isEmpty(info.getMsgId()) &&
                        message.getMsgId().equals(info.getMsgId())) {

                    if (message.getSend_status().equals("rollback")) {
                        String content = String.format(mContext.getString(R.string.udesk_rollback_tips), message.getMsgContent());
                        message.setMsgContent(content);
                        UdeskDBManager.getInstance().addMessageDB(message);
                        list.remove(info);
                        break;
                    }
                    return;

                }
            }
            if (!TextUtils.isEmpty(message.getSubsessionid()) && message.getDirection() == UdeskConst.ChatMsgDirection.Recv&& TextUtils.equals(UdeskConst.Sender.agent, message.getSender()) && !message.getSend_status().equals("rollback")) {
                isNeedLoadMessage(message);
            }
            list.add(message);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //判断是否有跳序
    private void isNeedLoadMessage(MessageInfo message) {
        try {
            if (list.isEmpty()) {
                return;
            }
            for (int i = list.size() - 1; i > 0; i--) {
                MessageInfo messageUI = list.get(i);
                if (!TextUtils.isEmpty(messageUI.getSubsessionid())
                        && messageUI.getSubsessionid().equals(message.getSubsessionid())
                        && messageUI.getDirection() == UdeskConst.ChatMsgDirection.Recv
                        && message.getSeqNum() - messageUI.getSeqNum() != 1) {
                    ((UdeskChatActivity) mContext).pullByJumpOrder();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listAddItems(List<MessageInfo> messages, boolean isMore) {
        try {
            if (messages == null) {
                return;
            }
            if (isMore) {
                messages.addAll(list);
                list.clear();
                list = messages;
            } else {
                list.clear();
                list.addAll(messages);
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void listAddEventItems(List<MessageInfo> messages) {
        try {
            if (messages == null) {
                return;
            }
            if (list.size()>0){
                List<MessageInfo> newMessages=new ArrayList<>();
                for (MessageInfo info:list){
                    for (MessageInfo newInfo:messages){
                        if (TextUtils.equals(info.getMsgId(),newInfo.getMsgId())){
                            newMessages.add(info);
                        }
                        if (info.getSwitchStaffType() > 0
                                && info.getSwitchStaffType() == newInfo.getSwitchStaffType()
                                && info.getLogId() == newInfo.getLogId()
                                && TextUtils.equals(info.getMsgContent(), newInfo.getMsgContent())
                                && info.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                            newMessages.add(info);
                        }
                    }
                }
                list.removeAll(newMessages);
            }
            list.addAll(messages);
            Collections.sort(list, new Comparator<MessageInfo>() {
                @Override
                public int compare(MessageInfo o1, MessageInfo o2) {
                    if (o1.getTime()>o2.getTime()){
                        return 1;
                    }
                    if (o1.getTime()==o2.getTime()){
                        return 0;
                    }
                    return -1;
                }
            });
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public MessageInfo getItem(int position) {
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            MessageInfo msgInfo = getItem(position);
            if (msgInfo != null) {
                int itemType = getItemViewType(position);
                if (convertView == null) {
                    if (itemType == LEFT) {
                        convertView = LayoutInflater.from(mContext).inflate(
                                R.layout.udesk_item_left, null);
                        holder = new LeftViewHolder();
                    } else if (itemType == RIGHT) {
                        convertView = LayoutInflater.from(mContext).inflate(
                                R.layout.udesk_item_right, null);
                        holder = new RightViewHolder();
                    }
                    holder.initView(mContext,convertView);
                    convertView.setTag(holder);
                }
                holder = (BaseViewHolder) convertView.getTag();
                holder.setData(mContext,list,position);
                holder.tryShowTime(msgInfo,position);
                holder.bind();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }


    /**
     * 根据消息ID  修改对应消息的状态
     */
    public boolean changeImState(View convertView, String msgId, int state) {
        try {
            Object tag = convertView.getTag();
            if (tag != null && tag instanceof RightViewHolder) {
                RightViewHolder cache = (RightViewHolder) tag;
                if (cache.message != null && msgId.equals(cache.message.getMsgId())) {
                    cache.changeUiState(state);
                    cache.message.setSendFlag(state);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean changeVideoThumbnail(View convertView, String msgId) {
        try {
            Object tag = convertView.getTag();
            if (tag != null) {
                BaseViewHolder cache = (BaseViewHolder) tag;
                cache.changeVideoThumbnail(msgId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 根据消息ID  修改对应消息的进度
     */
    public boolean changeFileState(View convertView, String msgId, int percent, long fileSize, boolean isSuccess) {
        try {
            Log.i("xxxxxx","percent = " + percent);
            Object tag = convertView.getTag();
            if (tag != null) {
                BaseViewHolder holder = (BaseViewHolder) tag;
                if (holder.message != null && msgId.contains(holder.message.getMsgId())) {
                    switch (UdeskConst.parseTypeForMessage(holder.message.getMsgtype())) {
                        case UdeskConst.ChatMsgTypeInt.TYPE_FILE:
                            holder.changeFileState(percent, fileSize, isSuccess);
                            return true;

                        case UdeskConst.ChatMsgTypeInt.TYPE_VIDEO:
                        case UdeskConst.ChatMsgTypeInt.TYPE_SHORT_VIDEO:
                            holder.changeSmallvideoState(percent);
                            return true;

                        case UdeskConst.ChatMsgTypeInt.TYPE_IMAGE:
                            holder.changeImageState(percent);
                            return true;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 根据消息ID  修改对应消息的状态
     */
    public void updateStatus(String msgId, int state) {
        try {
            for (MessageInfo msg : list) {
                if (msg.getMsgId() != null && msg.getMsgId().equals(msgId)) {
                    msg.setSendFlag(state);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据消息ID  修改对应文件上传的进度
     */
    public void updateProgress(String msgId, int precent) {
        try {
            boolean isNeedRefresh = false;
            for (MessageInfo msg : list) {
                if (msg != null && msg.getMsgtype() != null && msg.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO) && msg.getMsgId() != null && msg.getMsgId().equals(msgId)) {
                    msg.setPrecent(precent);
                    isNeedRefresh = true;
                }
            }
            if (isNeedRefresh) {
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
