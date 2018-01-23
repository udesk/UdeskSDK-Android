package cn.udesk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.StructModel;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.provider.UdeskFileProvider;
import udesk.core.model.MessageInfo;

import static android.util.Patterns.PHONE;
import static android.util.Patterns.WEB_URL;

public class MessageAdatper extends BaseAdapter {
    private static final int[] layoutRes = {
            R.layout.udesk_chat_msg_item_txt_l,//文本消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_txt_r,//文本消息右边的UI布局文件
            R.layout.udesk_chat_msg_item_audiot_l,//语音消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_audiot_r,//语音消息右边的UI布局文件
            R.layout.udesk_chat_msg_item_imgt_l,//图片消息左边的UI布局文件
            R.layout.udesk_chat_msg_item_imgt_r,//图片消息右边的UI布局文件
            R.layout.udesk_chat_msg_item_redirect,//转移消息提示信息UI布局文件
            R.layout.udesk_chat_rich_item_txt,//富文本消息UI布局文件
            R.layout.udesk_im_commodity_item,  //显示商品信息的UI布局文件
            R.layout.udesk_chat_msg_itemstruct_l, //显示结构化消息
            R.layout.udesk_chat_leavemsg_item_txt_l,//显示留言发送消息
            R.layout.udesk_chat_leavemsg_item_txt_r, // 显示收到留言消息的回复
            R.layout.udesk_chat_event_item, // 显示收到留言消息的回复
            R.layout.udesk_chat_msg_item_file_l,// 文件消息左
            R.layout.udesk_chat_msg_item_file_r, //文件消息右
            R.layout.udesk_chat_msg_item_location_r, //地理位置消息右
            R.layout.udesk_chat_msg_item_video_l, //视频消息左边
            R.layout.udesk_chat_msg_item_video_r //视频消息右边
    };

    /**
     * 非法消息类型
     */
    private static final int ILLEGAL = -1;
    /**
     * 收到的文本消息标识
     */
    private static final int MSG_TXT_L = 0;
    /**
     * 发送的文本消息标识
     */
    private static final int MSG_TXT_R = 1;
    /**
     * 收到的语音消息标识
     */
    private static final int MSG_AUDIO_L = 2;
    /**
     * 发送的语音消息标识
     */
    private static final int MSG_AUDIO_R = 3;
    /**
     * 收到图片消息标识
     */
    private static final int MSG_IMG_L = 4;
    /**
     * 发送图片消息标识
     */
    private static final int MSG_IMG_R = 5;
    /**
     * 收到转移客服消息标识
     */
    private static final int MSG_REDIRECT = 6;
    /**
     * 收到富文本消息标识
     */
    private static final int RICH_TEXT = 7;
    /**
     * 发送商品链接本消息标识
     */
    private static final int COMMODITY = 8;

    /**
     * 收到结构化消息
     */
    private static final int MSG_STRUCT = 9;

    //留言消息的 标识
    private static final int LEAVEMSG_TXT_L = 10;
    private static final int LEAVEMSG_TXT_R = 11;
    private static final int Udesk_Event = 12;
    private static final int MSG_FILE_L = 13;
    private static final int MSG_FILE_R = 14;
    private static final int MSG_LOCATION_R = 15;
    private static final int MSG_Video_Txt_l = 16;
    private static final int MSG_Video_Txt_R = 17;


    //2条消息之间 时间间隔超过SPACE_TIME， 会话界面会显示出消息的收发时间
    private static final long SPACE_TIME = 3 * 60 * 1000;

    private Activity mContext;
    private List<MessageInfo> list = new ArrayList<>();


    MessageAdatper(Activity context) {
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
            if (message instanceof UdeskCommodityItem) {
                return COMMODITY;
            }
            switch (UdeskConst.parseTypeForMessage(message.getMsgtype())) {
                case UdeskConst.ChatMsgTypeInt.TYPE_IMAGE:
                    if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                        return MSG_IMG_L;
                    } else {
                        return MSG_IMG_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_TEXT:
                    if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                        return MSG_TXT_L;
                    } else {
                        return MSG_TXT_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_RICH:
                    return RICH_TEXT;
                case UdeskConst.ChatMsgTypeInt.TYPE_AUDIO:
                    if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                        return MSG_AUDIO_L;
                    } else {
                        return MSG_AUDIO_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_REDIRECT:
                    return MSG_REDIRECT;
                case UdeskConst.ChatMsgTypeInt.TYPE_STRUCT:
                    return MSG_STRUCT;
                case UdeskConst.ChatMsgTypeInt.TYPE_LEAVEMSG:
                    if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                        return LEAVEMSG_TXT_L;
                    } else {
                        return LEAVEMSG_TXT_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_VIDEO:
                    if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                        return MSG_FILE_L;
                    } else {
                        return MSG_FILE_R;
                    }
                case UdeskConst.ChatMsgTypeInt.TYPE_EVENT:
                    return Udesk_Event;

                case UdeskConst.ChatMsgTypeInt.TYPE_LOCATION:
                    return MSG_LOCATION_R;
                case UdeskConst.ChatMsgTypeInt.TYPE_Video_Txt:
                    if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                        return MSG_Video_Txt_l;
                    } else {
                        return MSG_Video_Txt_R;
                    }

                default:
                    return ILLEGAL;
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
        if (layoutRes.length > 0) {
            return layoutRes.length;
        }
        return super.getViewTypeCount();
    }


    /**
     * 添加一条消息
     */
    void addItem(MessageInfo message) {
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
                        list.remove(info);
                        break;
                    }
                    return;

                }
            }
            if (message.getDirection() == UdeskConst.ChatMsgDirection.Recv && !message.getSend_status().equals("rollback")) {
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
        if (list.isEmpty()) {
            return;
        }
        for (int i = list.size() - 1; i > 0; i--) {
            MessageInfo messageUI = list.get(i);
            if (messageUI.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                if (messageUI.getSubsessionid().equals(message.getSubsessionid())) {
                    if (message.getSeqNum() - messageUI.getSeqNum() != 1) {
                        ((UdeskChatActivity) mContext).pullByJumpOrder(messageUI.getSeqNum(), messageUI.getSubsessionid());
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    void listAddItems(List<MessageInfo> messages, boolean isMore) {
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


    void listAddEventItems(List<MessageInfo> messages) {
        try {
            if (messages == null) {
                return;
            }
            list.addAll(messages);
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
                convertView = initView(convertView, itemType);
                BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
                tryShowTime(position, holder, msgInfo);
                holder.setMessage(msgInfo);
                holder.initHead(itemType);
                holder.showStatusOrProgressBar();
                holder.bind(mContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    /**
     * 根据传入的 itemType代表消息的类型和方向标识 初始相对应的UI控件
     */
    private View initView(View convertView, int itemType) {
        if (convertView == null) {
            try {
                convertView = LayoutInflater.from(mContext).inflate(
                        layoutRes[itemType], null);
                switch (itemType) {
                    case LEAVEMSG_TXT_L:
                        LeaveMsgViewHolder lleaveMsgViewHolder = new LeaveMsgViewHolder();
                        initItemNormalView(convertView, lleaveMsgViewHolder);
                        lleaveMsgViewHolder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, lleaveMsgViewHolder.tvMsg);
                        convertView.setTag(lleaveMsgViewHolder);
                        break;
                    case LEAVEMSG_TXT_R:
                        LeaveMsgViewHolder rleaveMsgViewHolder = new LeaveMsgViewHolder();
                        initItemNormalView(convertView, rleaveMsgViewHolder);
                        rleaveMsgViewHolder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMRightTextColorResId, rleaveMsgViewHolder.tvMsg);
                        convertView.setTag(rleaveMsgViewHolder);
                        break;
                    case MSG_TXT_L:
                        TxtViewHolder ltxtViewholder = new TxtViewHolder();
                        initItemNormalView(convertView, ltxtViewholder);
                        ltxtViewholder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, ltxtViewholder.tvMsg);
                        convertView.setTag(ltxtViewholder);
                        break;
                    case MSG_TXT_R:
                        TxtViewHolder rtxtViewholder = new TxtViewHolder();
                        initItemNormalView(convertView, rtxtViewholder);
                        rtxtViewholder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMRightTextColorResId, rtxtViewholder.tvMsg);
                        convertView.setTag(rtxtViewholder);
                        break;
                    case RICH_TEXT:
                        RichTextViewHolder richTextViewHolder = new RichTextViewHolder();
                        initItemNormalView(convertView, richTextViewHolder);
                        richTextViewHolder.rich_tvmsg = (TextView) convertView.findViewById(R.id.udesk_tv_rich_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, richTextViewHolder.rich_tvmsg);
                        convertView.setTag(richTextViewHolder);
                        break;
                    case MSG_AUDIO_L:
                    case MSG_AUDIO_R:
                        AudioViewHolder audioViewHolder = new AudioViewHolder();
                        initItemNormalView(convertView, audioViewHolder);
                        audioViewHolder.tvDuration = (TextView) convertView
                                .findViewById(R.id.udesk_im_item_record_duration);
                        audioViewHolder.record_item_content = convertView.findViewById(R.id.udesk_im_record_item_content);
                        audioViewHolder.record_play = (ImageView) convertView.findViewById(R.id.udesk_im_item_record_play);
                        convertView.setTag(audioViewHolder);
                        break;
                    case MSG_IMG_L:
                    case MSG_IMG_R:
                        ImgViewHolder imgViewHolder = new ImgViewHolder();
                        initItemNormalView(convertView, imgViewHolder);
                        imgViewHolder.imgView = (SimpleDraweeView) convertView.findViewById(R.id.udesk_im_image);
                        convertView.setTag(imgViewHolder);
                        break;
                    case MSG_FILE_L:
                    case MSG_FILE_R:
                        FileViewHolder fileViewHolder = new FileViewHolder();
                        initItemNormalView(convertView, fileViewHolder);
                        fileViewHolder.fielTitle = (TextView) convertView.findViewById(R.id.udesk_file_name);
                        fileViewHolder.udeskFileView = convertView.findViewById(R.id.udesk_file_view);
                        fileViewHolder.fielSize = (TextView) convertView.findViewById(R.id.udesk_file_size);
                        fileViewHolder.operater = (TextView) convertView.findViewById(R.id.udesk_file_operater);
                        fileViewHolder.mProgress = (ProgressBar) convertView.findViewById(R.id.udesk_progress);
                        convertView.setTag(fileViewHolder);
                        break;
                    case MSG_REDIRECT:
                        RedirectViewHolder redirectViewHolder = new RedirectViewHolder();
                        initItemNormalView(convertView, redirectViewHolder);
                        redirectViewHolder.redirectMsg = (TextView) convertView.findViewById(R.id.udesk_redirect_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMTipTextColorResId, redirectViewHolder.redirectMsg);
                        convertView.setTag(redirectViewHolder);
                        break;
                    case COMMODITY:
                        CommodityViewHolder commodityViewHolder = new CommodityViewHolder();
                        commodityViewHolder.rootView = convertView.findViewById(R.id.udesk_commit_root);
                        commodityViewHolder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
                        commodityViewHolder.thumbnail = (SimpleDraweeView) convertView
                                .findViewById(R.id.udesk_im_commondity_thumbnail);
                        commodityViewHolder.title = (TextView) convertView
                                .findViewById(R.id.udesk_im_commondity_title);
                        commodityViewHolder.subTitle = (TextView) convertView
                                .findViewById(R.id.udesk_im_commondity_subtitle);
                        commodityViewHolder.link = (TextView) convertView
                                .findViewById(R.id.udesk_im_commondity_link);
                        UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskCommityBgResId, commodityViewHolder.rootView);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommityTitleColorResId, commodityViewHolder.title);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommitysubtitleColorResId, commodityViewHolder.subTitle);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommityLinkColorResId, commodityViewHolder.link);
                        convertView.setTag(commodityViewHolder);
                        break;
                    case MSG_STRUCT:
                        StructViewHolder structViewHolder = new StructViewHolder();
                        initItemNormalView(convertView, structViewHolder);
                        structViewHolder.structImgView = convertView.findViewById(R.id.udesk_struct_img_container);
                        structViewHolder.structTextView = convertView.findViewById(R.id.udesk_struct_text_container);
                        structViewHolder.structBtnLineayLayout = (LinearLayout) convertView.findViewById(R.id.udesk_struct_btn_container);
                        structViewHolder.structImg = (SimpleDraweeView) convertView.findViewById(R.id.udesk_struct_img);
                        structViewHolder.structTitle = (TextView) convertView.findViewById(R.id.udesk_struct_title);
                        structViewHolder.structDes = (TextView) convertView.findViewById(R.id.udesk_struct_des);
                        convertView.setTag(structViewHolder);
                        break;
                    case Udesk_Event:
                        UdeskEventViewHolder eventViewHolder = new UdeskEventViewHolder();
                        initItemNormalView(convertView, eventViewHolder);
                        eventViewHolder.events = (TextView) convertView.findViewById(R.id.udesk_event);
                        convertView.setTag(eventViewHolder);
                        break;
                    case MSG_LOCATION_R:
                        MapViewHolder mapViewHolder = new MapViewHolder();
                        initItemNormalView(convertView, mapViewHolder);
                        mapViewHolder.locationValue = (TextView) convertView.findViewById(R.id.postion_value);
                        mapViewHolder.cropBitMap = (SimpleDraweeView) convertView.findViewById(R.id.udesk_location_image);
                        convertView.setTag(mapViewHolder);
                        break;
                    case MSG_Video_Txt_l:
                        VideoTxtViewHolder lvideoTxtViewHolder = new VideoTxtViewHolder();
                        initItemNormalView(convertView, lvideoTxtViewHolder);
                        lvideoTxtViewHolder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, lvideoTxtViewHolder.tvMsg);
                        convertView.setTag(lvideoTxtViewHolder);
                        break;
                    case MSG_Video_Txt_R:
                        VideoTxtViewHolder videoTxtViewHolder = new VideoTxtViewHolder();
                        initItemNormalView(convertView, videoTxtViewHolder);
                        videoTxtViewHolder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMRightTextColorResId, videoTxtViewHolder.tvMsg);
                        convertView.setTag(videoTxtViewHolder);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }


    abstract class BaseViewHolder {
        SimpleDraweeView ivHeader;
        ImageView ivStatus;
        TextView tvTime;
        ProgressBar pbWait;
        TextView agentnickName;
        MessageInfo message;
        int itemType;
        boolean isLeft = false;

        public void setMessage(MessageInfo message) {
            this.message = message;
        }

        public MessageInfo getMessage() {
            return message;
        }

        /**
         * 根据收发消息的标识，设置客服客户的头像
         */
        void initHead(int itemType) {
            try {
                this.itemType = itemType;
                switch (itemType) {
                    case MSG_TXT_R:
                    case MSG_AUDIO_R:
                    case MSG_IMG_R:
                    case LEAVEMSG_TXT_R:
                    case MSG_FILE_R:
                    case MSG_LOCATION_R:
                    case MSG_Video_Txt_R:
                        this.isLeft = false;
                        if (!TextUtils.isEmpty(UdeskBaseInfo.customerUrl)) {
                            UdeskUtil.loadHeadView(mContext, ivHeader, Uri.parse(UdeskBaseInfo.customerUrl));
                        }
                        break;

                    case MSG_TXT_L:
                    case MSG_AUDIO_L:
                    case RICH_TEXT:
                    case MSG_IMG_L:
                    case MSG_STRUCT:
                    case MSG_FILE_L:
                    case LEAVEMSG_TXT_L:
                    case MSG_Video_Txt_l:
                        this.isLeft = true;
                        if (message.getUser_avatar() != null && !TextUtils.isEmpty(message.getUser_avatar().trim())) {
                            ivHeader.setImageResource(R.drawable.udesk_im_default_agent_avatar);
                            UdeskUtil.loadHeadView(mContext, ivHeader, Uri.parse(message.getUser_avatar()));
                        }
                        agentnickName.setText(message.getReplyUser());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        /**
         * 设置消息发送状态  发送中，发送成功， 发送失败
         */
        void showStatusOrProgressBar() {
            try {
                if (itemType == COMMODITY || itemType == Udesk_Event) {
                    return;
                }
                if (itemType == MSG_TXT_L
                        || itemType == MSG_AUDIO_L
                        || itemType == MSG_IMG_L
                        || itemType == MSG_REDIRECT
                        || itemType == MSG_STRUCT
                        ) {
                    ivStatus.setVisibility(View.GONE);
                } else {
                    changeUiState(message.getSendFlag());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void changeUiState(int state) {
            try {
                if (state == UdeskConst.SendFlag.RESULT_SUCCESS) {
                    ivStatus.setVisibility(View.GONE);
                    pbWait.setVisibility(View.GONE);
                } else {
                    if (state == UdeskConst.SendFlag.RESULT_RETRY || state == UdeskConst.SendFlag.RESULT_SEND) {
                        ivStatus.setVisibility(View.GONE);
                        pbWait.setVisibility(View.VISIBLE);
                    } else if (state == UdeskConst.SendFlag.RESULT_FAIL) {
                        ivStatus.setVisibility(View.VISIBLE);
                        pbWait.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        abstract void bind(Context context);

    }

    /**
     * 展示富文本消息
     */
    class RichTextViewHolder extends BaseViewHolder {

        TextView rich_tvmsg;

        @Override
        void bind(Context context) {
            try {
                CharSequence charSequence = Html.fromHtml(message.getMsgContent());
                String msg = charSequence.toString();
                if (msg.endsWith("\n\n")) {
                    charSequence = charSequence.subSequence(0, charSequence.length() - 2);
                    rich_tvmsg.setText(charSequence);
                } else {
                    rich_tvmsg.setText(charSequence);
                }
                Linkify.addLinks(rich_tvmsg, WEB_URL, null);
                Linkify.addLinks(rich_tvmsg, PHONE, null);
                CharSequence text = rich_tvmsg.getText();
                if (text instanceof Spannable) {
                    int end = text.length();
                    Spannable sp = (Spannable) rich_tvmsg.getText();
                    URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                    SpannableStringBuilder style = new SpannableStringBuilder(text);
                    style.clearSpans();
                    for (URLSpan url : urls) {
                        MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                        style.setSpan(myURLSpan, sp.getSpanStart(url),
                                sp.getSpanEnd(url),
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    rich_tvmsg.setText(style);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }

        }

    }

    /**
     * 重写ClickableSpan 实现富文本点击事件跳转到UdeskWebViewUrlAcivity界面
     */
    private class MyURLSpan extends ClickableSpan {

        private String mUrl;

        MyURLSpan(String url) {
            mUrl = url;
        }

        @Override
        public void onClick(View widget) {
            try {
                if (WEB_URL.matcher(mUrl).find()) {
                    Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                    intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
                    mContext.startActivity(intent);
                } else if (PHONE.matcher(mUrl).find()) {
                    String phone = mUrl.toLowerCase();
                    if (!phone.startsWith("tel:")) {
                        phone = "tel:" + mUrl;
                    }
                    ((UdeskChatActivity) mContext).callphone(phone);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    //文本消息的url事件拦截处理。  客户设置了事件则走客户的事件，没走默认弹出界面
    private class TxtURLSpan extends ClickableSpan {

        private String mUrl;

        TxtURLSpan(String url) {
            mUrl = url;
        }


        @Override
        public void onClick(View widget) {
            try {
                if (UdeskSDKManager.getInstance().getTxtMessageClick() != null) {
                    UdeskSDKManager.getInstance().getTxtMessageClick().txtMsgOnclick(mUrl);
                } else {
                    Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                    intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
                    mContext.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 展示文本消息
     */
    class TxtViewHolder extends BaseViewHolder {
        TextView tvMsg;

        @Override
        void bind(Context context) {
            try {
                //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
                if (UDEmojiAdapter.replaceEmoji(context, message.getMsgContent(),
                        (int) tvMsg.getTextSize()) != null) {
                    tvMsg.setText(UDEmojiAdapter.replaceEmoji(context, message.getMsgContent(),
                            (int) tvMsg.getTextSize()));
                    Log.i("xxxxxxx","11111111111111111 = " + message.getMsgContent());
                } else {
                    tvMsg.setText(message.getMsgContent());
                    Log.i("xxxxxxx","2222222222222222222 = " + message.getMsgContent());
                    tvMsg.setMovementMethod(LinkMovementMethod.getInstance());
                    CharSequence text = tvMsg.getText();
                    if (text instanceof Spannable) {
                        int end = text.length();
                        Spannable sp = (Spannable) tvMsg.getText();
                        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                        SpannableStringBuilder style = new SpannableStringBuilder(text);
                        style.clearSpans();// should clear old spans
                        for (URLSpan url : urls) {
                            TxtURLSpan txtURLSpan = new TxtURLSpan(url.getURL());
                            style.setSpan(txtURLSpan, sp.getSpanStart(url),
                                    sp.getSpanEnd(url),
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                        tvMsg.setText(style);
                    }
                }
                //设置消息长按事件  复制文本
                tvMsg.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ((UdeskChatActivity) mContext).handleText(message, v);
                        return false;
                    }
                });

                //重发按钮点击事件
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 展示文件消息
     */
    class FileViewHolder extends BaseViewHolder {
        View udeskFileView;
        TextView fielTitle;
        TextView fielSize;
        TextView operater;
        ProgressBar mProgress;

        @Override
        void bind(Context context) {
            try {
                if (message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                    fielTitle.setText(UdeskUtil.getFileName(message.getLocalPath()));
                    fielSize.setText(UdeskUtil.getFileSizeByLoaclPath(message.getLocalPath()));
                    if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_SUCCESS) {
                        mProgress.setProgress(100);
                        operater.setText(mContext.getString(R.string.udesk_has_send));
                    } else {
                        mProgress.setProgress(message.getPrecent());
                        operater.setText(String.format("%s%%", String.valueOf(message.getPrecent())));
                    }
                } else {
                    fielTitle.setText(UdeskUtil.getFileName(message.getMsgContent()));
                    fielSize.setText(UdeskUtil.getFileSizeByMsgIdAndUrl(message.getMsgId(), message.getMsgContent()));
                    if (UdeskUtil.isExitFileByMsgIdAndUrl(message.getMsgId(), message.getMsgContent())
                            && UdeskUtil.getFileSize(UdeskUtil.getLoaclpathByMsgIdAndUrl(message.getMsgId(), message.getMsgContent())) > 0) {
                        mProgress.setProgress(100);
                        operater.setText(mContext.getString(R.string.udesk_has_downed));
                    } else {
                        mProgress.setProgress(0);
                        operater.setText(mContext.getString(R.string.udesk_has_download));
                    }
                    operater.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ((UdeskChatActivity) mContext).downLoadMsg(message);
                        }
                    });
                }

                udeskFileView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            File file;
                            if (message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                                file = new File(message.getLocalPath());
                            } else {
                                file = UdeskUtil.getLoaclpathByMsgIdAndUrl(message.getMsgId(), message.getMsgContent());
                                if (file == null || UdeskUtil.getFileSize(file) <= 0) {
                                    Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_has_uncomplete_tip), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            Uri contentUri;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                contentUri = UdeskFileProvider.getUriForFile(mContext, UdeskUtil.getFileProviderName(mContext), file);
                            } else {
                                contentUri = Uri.fromFile(file);
                            }
                            if (contentUri == null) {
                                return;
                            }
                            if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO)) {
                                intent.setDataAndType(contentUri, "video/mp4");
                            } else {
                                String type = UdeskUtil.getMIMEType(file);
                                intent.setDataAndType(contentUri, type);
                            }
                            mContext.startActivity(intent);
                        } catch (Exception e) {
                            if (!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("No Activity found to handle Intent")) {
                                Toast.makeText(mContext.getApplication(), mContext.getString(R.string.udesk_no_app_handle), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

                //设置重发按钮的点击事件
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

    }


    /**
     * 展示留言消息
     */
    class LeaveMsgViewHolder extends BaseViewHolder {
        TextView tvMsg;

        @Override
        void bind(Context context) {
            try {
                //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
                if (UDEmojiAdapter.replaceEmoji(context, message.getMsgContent(),
                        (int) tvMsg.getTextSize()) != null) {
                    tvMsg.setText(UDEmojiAdapter.replaceEmoji(context, message.getMsgContent(),
                            (int) tvMsg.getTextSize()));
                } else {
                    tvMsg.setText(message.getMsgContent());
                    tvMsg.setMovementMethod(LinkMovementMethod.getInstance());
                    CharSequence text = tvMsg.getText();
                    if (text instanceof Spannable) {
                        int end = text.length();
                        Spannable sp = (Spannable) tvMsg.getText();
                        URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                        SpannableStringBuilder style = new SpannableStringBuilder(text);
                        style.clearSpans();// should clear old spans
                        for (URLSpan url : urls) {
                            TxtURLSpan txtURLSpan = new TxtURLSpan(url.getURL());
                            style.setSpan(txtURLSpan, sp.getSpanStart(url),
                                    sp.getSpanEnd(url),
                                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                        }
                        tvMsg.setText(style);
                    }
                }

                //设置消息长按事件  复制文本
                tvMsg.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        ((UdeskChatActivity) mContext).handleText(message, v);
                        return false;
                    }
                });

                //设置重发按钮的点击事件
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }


    }

    /**
     * 展示语音消息
     */
    class AudioViewHolder extends BaseViewHolder {
        TextView tvDuration;
        View record_item_content;
        ImageView record_play;

        @Override
        void bind(Context context) {
            try {
                checkPlayBgWhenBind();
                if (message.getDuration() > 0) {
                    char symbol = 34;
                    tvDuration.setText(String.format("%d%s", message.getDuration(), String.valueOf(symbol)));
                }
                record_item_content.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).clickRecordFile(message);
                    }
                });
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
                long duration = message.getDuration();
                duration = duration == 0 ? 1 : duration;
                int min = UdeskUtil.getDisplayWidthPixels(mContext) / 6;
                int max = UdeskUtil.getDisplayWidthPixels(mContext) * 3 / 5;
                int step = (int) ((duration < 10) ? duration : (duration / 10 + 9));
                record_item_content.getLayoutParams().width = (step == 0) ? min
                        : (min + (max - min) / 15 * step);
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        private void checkPlayBgWhenBind() {
            try {
                if (message.isPlaying) {
                    resetAnimationAndStart();
                } else {
                    record_play.setImageDrawable(mContext.getResources().getDrawable(
                            isLeft ? R.drawable.udesk_im_record_left_default : R.drawable.udesk_im_record_right_default));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        private void resetAnimationAndStart() {
            try {
                record_play.setImageDrawable(mContext.getResources().getDrawable(
                        isLeft ? R.drawable.udesk_im_record_play_left : R.drawable.udesk_im_record_play_right));
                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable != null
                        && playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        // 判断开始播放

        void startAnimationDrawable() {
            try {
                message.isPlaying = true;
                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).start();
                } else {
                    resetAnimationAndStart();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }

        // 关闭播放
        void endAnimationDrawable() {
            try {
                message.isPlaying = false;

                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable != null
                        && playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).stop();
                    ((AnimationDrawable) playDrawable).selectDrawable(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    /**
     * 展示图片消息
     */
    public class ImgViewHolder extends BaseViewHolder {
        SimpleDraweeView imgView;

        @Override
        void bind(Context context) {
            try {
                if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(message.getLocalPath())) {
                    int[] wh = UdeskUtil.getImageWH(message.getLocalPath());
                    UdeskUtil.loadFileFromSdcard(context, imgView, Uri.fromFile(new File(message.getLocalPath())), wh[0], wh[1]);
                } else {
                    UdeskUtil.loadImageView(context, imgView, Uri.parse(message.getMsgContent()));
                }

                imgView.setTag(message.getTime());
                imgView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (message == null) {
                            return;
                        }
                        Uri imgUri = null;
                        if (!TextUtils.isEmpty(message.getLocalPath())) {
                            imgUri = Uri.fromFile(new File(message.getLocalPath()));
                        } else if (!TextUtils.isEmpty(message.getMsgContent())) {
                            imgUri = Uri.parse(message.getMsgContent());
                        }
                        UdeskUtil.previewPhoto(mContext, imgUri);
                    }
                });
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    /**
     * 展示客服转移消息提示
     */
    public class RedirectViewHolder extends BaseViewHolder {
        TextView redirectMsg;

        @Override
        void bind(Context context) {
            try {
                redirectMsg.setText(message.getMsgContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class UdeskEventViewHolder extends BaseViewHolder {

        TextView events;

        @Override
        void bind(Context context) {
            try {
                tvTime.setVisibility(View.VISIBLE);
                if (!message.getCreatedTime().isEmpty()) {
                    tvTime.setText(String.format("----%s----", UdeskUtil.parseEventTime(message.getCreatedTime())));
                } else
                    tvTime.setText(String.format("----%s----", UdeskUtil.parseEventTime(message.getTime())));
                events.setText(message.getMsgContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 展示商品链接消息
     */
    public class CommodityViewHolder extends BaseViewHolder {
        View rootView;
        SimpleDraweeView thumbnail;
        TextView title;
        TextView subTitle;
        TextView link;

        @Override
        void bind(Context context) {
            try {
                final UdeskCommodityItem item = (UdeskCommodityItem) message;
                title.setText(item.getTitle());
                subTitle.setText(item.getSubTitle());
                UdeskUtil.loadNoChangeView(context.getApplicationContext(), thumbnail, Uri.parse(item.getThumbHttpUrl()));
                link.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).sentLink(item.getCommodityUrl());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }

        }
    }

    /**
     * 处理结构化消息
     */
    public class StructViewHolder extends BaseViewHolder {
        View structImgView;
        View structTextView;
        LinearLayout structBtnLineayLayout;

        SimpleDraweeView structImg;
        TextView structTitle;
        TextView structDes;

        @Override
        void bind(Context context) {
            try {
                StructModel structModel = JsonUtils.parserStructMsg(message.getMsgContent());
                if (structModel != null) {
                    //显示图片部分
                    showStructImg(context, structModel, structImgView, structImg);
                    //标题描述部分
                    showStructText(structModel, structTextView, structTitle, structDes);
                    //按钮部分
                    showStructBtn(context, structModel, structBtnLineayLayout);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    public class MapViewHolder extends BaseViewHolder {
        TextView locationValue;
        SimpleDraweeView cropBitMap;

        @Override
        void bind(Context context) {

            try {
                final String[] locationMessage = message.getMsgContent().split(";");
                locationValue.setText(locationMessage[locationMessage.length - 1]);
                cropBitMap.setImageURI(Uri.fromFile(new File(message.getLocalPath())));
                cropBitMap.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (UdeskSDKManager.getInstance().getLocationMessageClickCallBack() != null) {
                            UdeskSDKManager.getInstance().getLocationMessageClickCallBack().luanchMap(mContext, Double.valueOf(locationMessage[0]),
                                    Double.valueOf(locationMessage[1]), locationMessage[locationMessage.length - 1]);
                        }
                    }
                });

                //设置重发按钮的点击事件
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 展示视频事件
     */
    class VideoTxtViewHolder extends BaseViewHolder {
        TextView tvMsg;

        @Override
        void bind(Context context) {
            try {
                tvMsg.setText(message.getMsgContent());
                tvMsg.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).startVideo();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showStructImg(Context context, StructModel structModel, View structImgView, SimpleDraweeView structImg) {
        try {
            final String imgUrl = structModel.getImg_url();
            if (!TextUtils.isEmpty(imgUrl)) {
                structImgView.setVisibility(View.VISIBLE);
                UdeskUtil.loadImageView(context, structImg, Uri.parse(imgUrl));
                structImgView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        UdeskUtil.previewPhoto(mContext, Uri.parse(imgUrl));
                    }
                });
            } else {
                structImgView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    private void showStructText(StructModel structModel, View structTextView, TextView structTitle, TextView structDes) {
        try {
            if (!TextUtils.isEmpty(structModel.getTitle()) || !TextUtils.isEmpty(structModel.getDescription())) {
                structTextView.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(structModel.getTitle())) {
                    structTitle.setVisibility(View.VISIBLE);
                    structTitle.setText(structModel.getTitle());
                } else {
                    structTitle.setVisibility(View.GONE);
                }
                if (!TextUtils.isEmpty(structModel.getDescription())) {
                    structDes.setVisibility(View.VISIBLE);
                    structDes.setText(structModel.getDescription());
                } else {
                    structDes.setVisibility(View.GONE);
                }
            } else {
                structTextView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStructBtn(Context context, StructModel structModel, LinearLayout structBtnLineayLayout) {
        try {
            if (structModel.getButtons() != null && structModel.getButtons().size() > 0) {
                structBtnLineayLayout.removeAllViews();
                structBtnLineayLayout.setVisibility(View.VISIBLE);
                List<StructModel.ButtonsBean> buttonsBeens = structModel.getButtons();
                for (int i = 0; i < buttonsBeens.size(); i++) {
                    StructModel.ButtonsBean buttonsBean = buttonsBeens.get(i);
                    TextView textView = new TextView(context);
                    textView.setText(buttonsBean.getText());
                    textView.setOnClickListener(new MyStructBtnOnClick(buttonsBean));
                    textView.setTextColor(context.getResources().getColor(R.color.udesk_custom_dialog_sure_btn_color));
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(5, 5, 5, 5);
                    textView.setTextSize(18);
                    LinearLayout.LayoutParams textviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    View line = new View(context);
                    line.setBackgroundColor(context.getResources().getColor(R.color.udesk_struct_bg_line_color));
                    LinearLayout.LayoutParams lineParas = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            1);
                    structBtnLineayLayout.addView(textView, textviewParams);
                    structBtnLineayLayout.addView(line, lineParas);
                }

            } else {
                structBtnLineayLayout.setVisibility(View.GONE);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private class MyStructBtnOnClick implements OnClickListener {
        StructModel.ButtonsBean mStructBtn;

        MyStructBtnOnClick(StructModel.ButtonsBean structBtn) {
            this.mStructBtn = structBtn;
        }

        @Override
        public void onClick(View view) {
            try {
                switch (mStructBtn.getType()) {
                    case UdeskConst.StructBtnTypeString.link:
                        Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                        intent.putExtra(UdeskConst.WELCOME_URL, mStructBtn.getValue());
                        mContext.startActivity(intent);
                        break;
                    case UdeskConst.StructBtnTypeString.phone:
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mStructBtn.getValue()));
                        mContext.startActivity(dialIntent);
                        break;
                    case UdeskConst.StructBtnTypeString.sdkCallBack:
                        if (UdeskSDKManager.getInstance().getStructMessageCallBack() != null) {
                            UdeskSDKManager.getInstance().getStructMessageCallBack().structMsgCallBack(mContext, mStructBtn.getValue());
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void initItemNormalView(View convertView, BaseViewHolder holder) {
        try {
            holder.ivHeader = (SimpleDraweeView) convertView.findViewById(R.id.udesk_iv_head);
            holder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
            holder.ivStatus = (ImageView) convertView.findViewById(R.id.udesk_iv_status);
            holder.pbWait = (ProgressBar) convertView.findViewById(R.id.udesk_im_wait);
            holder.agentnickName = (TextView) convertView.findViewById(R.id.udesk_nick_name);
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMTimeTextColorResId, holder.tvTime);
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMAgentNickNameColorResId, holder.agentnickName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算是否要显示当前位置消息的发送或接受时间
     */
    private void tryShowTime(int position, BaseViewHolder holder,
                             MessageInfo info) {
        try {
            if (info instanceof UdeskCommodityItem) {
                holder.tvTime.setVisibility(View.VISIBLE);
                holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, System.currentTimeMillis()));
            } else if (info.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_EVENT)) {
                holder.tvTime.setVisibility(View.GONE);
            } else if (needShowTime(position)) {
                holder.tvTime.setVisibility(View.VISIBLE);
                holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, info.getTime()));
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean needShowTime(int position) {
        try {
            if (position == 0) {
                return true;
            } else if (position > 0) {
                MessageInfo preItem = getItem(position - 1);
                if (preItem != null) {
                    try {
                        MessageInfo item = getItem(position);
                        long currTime = item.getTime();
                        long preTime = preItem.getTime();
                        return currTime - preTime > SPACE_TIME
                                || preTime - currTime > SPACE_TIME;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
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
    boolean changeImState(View convertView, String msgId, int state) {
        try {
            Object tag = convertView.getTag();
            if (tag != null && tag instanceof BaseViewHolder) {
                BaseViewHolder cache = (BaseViewHolder) tag;
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

    /**
     * 根据消息ID  修改对应消息的进度
     */
    boolean changeFileState(View convertView, String msgId, int precent, long fileSize, boolean isSuccess) {
        try {
            Object tag = convertView.getTag();
            if (tag != null && tag instanceof FileViewHolder) {
                FileViewHolder cache = (FileViewHolder) tag;
                if (cache.message != null && msgId.equals(cache.message.getMsgId())) {

                    cache.mProgress.setProgress(precent);
                    if (precent == 100) {
                        if (cache.message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                            cache.operater.setText(mContext.getString(R.string.udesk_has_send));
                        } else {
                            cache.operater.setText(mContext.getString(R.string.udesk_has_downed));
                        }

                    } else {
                        if (0 < precent && precent < 100)
                            cache.operater.setText(String.format("%d%%", precent));
                    }
                    if (fileSize > 0) {
                        cache.fielSize.setText(UdeskUtil.formetFileSize(fileSize));
                    }
                    if (!isSuccess) {
                        Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_download_failure), Toast.LENGTH_SHORT).show();
                        cache.operater.setText(mContext.getString(R.string.udesk_has_download));
                    }

                    return true;
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
    void updateStatus(String msgId, int state) {
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
    void updateProgress(String msgId, int present) {
        try {
            boolean isNeedRefresh = false;
            for (MessageInfo msg : list) {
                if (msg != null && msg.getMsgtype() != null && msg.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO) && msg.getMsgId() != null && msg.getMsgId().equals(msgId)) {
                    msg.setPrecent(present);
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
