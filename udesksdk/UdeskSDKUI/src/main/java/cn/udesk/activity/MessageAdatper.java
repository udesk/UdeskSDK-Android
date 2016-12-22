package cn.udesk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.widget.ChatImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import udesk.com.nostra13.universalimageloader.core.DisplayImageOptions;
import udesk.com.nostra13.universalimageloader.core.ImageLoader;
import udesk.com.nostra13.universalimageloader.core.assist.ImageScaleType;
import udesk.core.model.MessageInfo;

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
            R.layout.udesk_im_commodity_item  //显示商品信息的UI布局文件
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
    //2条消息之间 时间间隔超过SPACE_TIME， 会话界面会显示出消息的收发时间
    private static final long SPACE_TIME = 3 * 60 * 1000;

    private Context mContext;
    private List<MessageInfo> list = new ArrayList<MessageInfo>();
    private DisplayImageOptions options;
    private DisplayImageOptions agentHeadOptions;
    private ImageLoader mImageLoader;

    public MessageAdatper(Context context) {
        mContext = context;
        initDisplayOptions();
        getImageLoader(context);
    }

    /**
     * 初始化universalimageloader开源库的DisplayImageOptions的设置
     */
    private void initDisplayOptions() {
        try {
            options = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.udesk_defualt_failure)
                    .showImageOnLoading(R.drawable.udesk_defalut_image_loading)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .build();

            agentHeadOptions = new DisplayImageOptions.Builder()
                    .showImageOnFail(R.drawable.udesk_im_default_agent_avatar)
                    .showImageOnLoading(R.drawable.udesk_im_default_agent_avatar)
                    .showImageForEmptyUri(R.drawable.udesk_im_default_agent_avatar)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ImageLoader getImageLoader(Context context) {
        if (mImageLoader == null) {
            mImageLoader = ImageLoader.getInstance();
        }
        if(!mImageLoader.isInited()){
            mImageLoader.init(UdeskUtil.initImageLoaderConfig(context));
        }
        return mImageLoader;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public List<MessageInfo> getList() {
        return list;
    }

    /**
     * @param position
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
     *
     * @param message
     */
    public void addItem(MessageInfo message) {
        if (message == null) {
            return;
        }
        //加上过滤含有相同msgID的消息
        try {
            for (MessageInfo info : list){
                    if (!TextUtils.isEmpty(message.getMsgId()) && !TextUtils.isEmpty(info.getMsgId()) && message.getMsgId().equals(info.getMsgId())){
                        return;
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        list.add(message);
    }

    public void addItems(List<MessageInfo> messages) {
        if (messages == null) {
            return;
        }
        list.clear();
        list = messages;
        notifyDataSetChanged();
    }

    public void listAddItems(List<MessageInfo> messages) {
        if (messages == null) {
            return;
        }
        List<MessageInfo> tempMsgs = messages;
        tempMsgs.addAll(list);
        list.clear();
        list = tempMsgs;
        notifyDataSetChanged();
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
        MessageInfo msgInfo = getItem(position);
        if (msgInfo != null) {
            int itemType = getItemViewType(position);
            convertView = initView(convertView, itemType, position, msgInfo);
            BaseViewHolder holder = (BaseViewHolder) convertView.getTag();
            tryShowTime(position, holder, msgInfo);
            holder.setMessage(msgInfo);
            holder.initHead(itemType);
            holder.showStatusOrProgressBar();
            holder.bind(mContext);
        }
        return convertView;
    }

    /**
     * 根据传入的 itemType代表消息的类型和方向标识 初始相对应的UI控件
     */
    private View initView(View convertView, int itemType, int position, final MessageInfo msgInfo) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    layoutRes[itemType], null);

            switch (itemType) {
                case MSG_TXT_L:
                case MSG_TXT_R: {
                    TxtViewHolder holder = new TxtViewHolder();
                    initItemNormalView(convertView, holder, itemType, position);
                    holder.tvMsg = (TextView) convertView.findViewById(R.id.udesk_tv_msg);
                    if (itemType == MSG_TXT_L){
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, holder.tvMsg);
                    }else if(itemType == MSG_TXT_R) {
                        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMRightTextColorResId, holder.tvMsg);
                    }
                    convertView.setTag(holder);
                    break;
                }
                case RICH_TEXT: {
                    RichTextViewHolder holder = new RichTextViewHolder();
                    initItemNormalView(convertView, holder, itemType, position);
                    holder.rich_tvmsg = (TextView) convertView.findViewById(R.id.udesk_tv_rich_msg);
                    UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMLeftTextColorResId, holder.rich_tvmsg);
                    convertView.setTag(holder);
                    break;
                }
                case MSG_AUDIO_L:
                case MSG_AUDIO_R: {
                    AudioViewHolder holder = new AudioViewHolder();
                    initItemNormalView(convertView, holder, itemType, position);
                    holder.tvDuration = (TextView) convertView
                            .findViewById(R.id.udesk_im_item_record_duration);
                    holder.record_item_content = convertView.findViewById(R.id.udesk_im_record_item_content);
                    holder.record_play = (ImageView) convertView.findViewById(R.id.udesk_im_item_record_play);
                    convertView.setTag(holder);
                    break;
                }
                case MSG_IMG_L:
                case MSG_IMG_R: {
                    ImgViewHolder holder = new ImgViewHolder();
                    initItemNormalView(convertView, holder, itemType, position);
                    holder.imgView = (ChatImageView) convertView.findViewById(R.id.udesk_im_image);
                    convertView.setTag(holder);
                    break;
                }
                case MSG_REDIRECT: {
                    RedirectViewHolder holder = new RedirectViewHolder();
                    initItemNormalView(convertView, holder, itemType, position);
                    holder.redirectMsg = (TextView) convertView.findViewById(R.id.udesk_redirect_msg);
                    UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMTipTextColorResId,  holder.redirectMsg);
                    convertView.setTag(holder);
                    break;
                }
                case COMMODITY: {
                    CommodityViewHolder holder = new CommodityViewHolder();
                    holder.rootView = convertView.findViewById(R.id.udesk_commit_root);
                    holder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
                    holder.thumbnail = (ImageView) convertView
                            .findViewById(R.id.udesk_im_commondity_thumbnail);
                    holder.title = (TextView) convertView
                            .findViewById(R.id.udesk_im_commondity_title);
                    holder.subTitle = (TextView) convertView
                            .findViewById(R.id.udesk_im_commondity_subtitle);
                    holder.link = (TextView) convertView
                            .findViewById(R.id.udesk_im_commondity_link);
                    UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskCommityBgResId , holder.rootView);
                    UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommityTitleColorResId ,holder.title);
                    UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommitysubtitleColorResId , holder.subTitle);
                    UdekConfigUtil.setUITextColor(UdeskConfig.udeskCommityLinkColorResId , holder.link);
                    convertView.setTag(holder);
                    break;
                }
            }
        }
        return convertView;
    }


    abstract class BaseViewHolder {
        public CircleImageView ivHeader;
        public ImageView ivStatus;
        public TextView tvTime;
        public ProgressBar pbWait;
        public TextView agentnickName;
        public MessageInfo message;
        public int itemType;
        public boolean isLeft = false;

        public void setMessage(MessageInfo message) {
            this.message = message;
        }

        public int getItemType() {
            return itemType;
        }

        public MessageInfo getMessage() {
            return message;
        }

        /**
         * 根据收发消息的标识，设置客服客户的头像
         *
         * @param itemType
         */
        void initHead(int itemType) {
            this.itemType = itemType;
            switch (itemType) {
                case MSG_TXT_R:
                case MSG_AUDIO_R:
                case MSG_IMG_R:
                    this.isLeft = false;
                    if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getCustomerUrl())){
                        getImageLoader(mContext).displayImage(UdeskSDKManager.getInstance().getCustomerUrl(),ivHeader,
                                new DisplayImageOptions.Builder()
                                        .showImageOnFail(R.drawable.udesk_im_default_user_avatar)
                                        .showImageOnLoading(R.drawable.udesk_im_default_user_avatar)
                                        .showImageForEmptyUri(R.drawable.udesk_im_default_user_avatar)
                                        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                                        .build() );
                    }else {
                        ivHeader.setImageResource(R.drawable.udesk_im_default_user_avatar);
                    }
                    break;
                case MSG_TXT_L:
                case MSG_AUDIO_L:
                case RICH_TEXT:
                case MSG_IMG_L:
                    this.isLeft = true;
                    if (message.getAgentUrl() == null || TextUtils.isEmpty(message.getAgentUrl().trim())){
                        ivHeader.setImageResource(R.drawable.udesk_im_default_agent_avatar);
                    }else{
                        getImageLoader(mContext).displayImage(message.getAgentUrl(), ivHeader, agentHeadOptions);
                    }
                    agentnickName.setText(message.getNickName());
//                    ivHeader.setImageResource(R.drawable.udesk_im_default_agent_avatar);
                    break;
                default:
                    break;
            }

        }

        /**
         * 设置消息发送状态  发送中，发送成功， 发送失败
         */
        public void showStatusOrProgressBar() {
            if (itemType == COMMODITY) {
                return;
            }
            if (itemType == MSG_TXT_L
                    || itemType == MSG_AUDIO_L
                    || itemType == MSG_IMG_L
                    || itemType == MSG_REDIRECT
                    ) {
                ivStatus.setVisibility(View.GONE);
            } else {
                changeUiState(message.getSendFlag());
            }
        }

        public void changeUiState(int state) {
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
        }

        abstract void bind(Context context);

    }

    /**
     * 展示富文本消息
     */
    class RichTextViewHolder extends BaseViewHolder {

        public TextView rich_tvmsg;

        @Override
        void bind(Context context) {
            try{
                CharSequence charSequence = Html.fromHtml(message.getMsgContent().replaceAll("(<p>||</p>)", ""));
                rich_tvmsg.setText(charSequence);
                rich_tvmsg.setMovementMethod(LinkMovementMethod.getInstance());
                CharSequence text = rich_tvmsg.getText();
                if (text instanceof Spannable) {
                    int end = text.length();
                    Spannable sp = (Spannable) rich_tvmsg.getText();
                    URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                    SpannableStringBuilder style = new SpannableStringBuilder(text);
//                    SpannableStringBuilder builder = new SpannableStringBuilder(charSequence);
                    style.clearSpans();// should clear old spans
                    for (URLSpan url : urls) {
//                        int start = builder.getSpanStart(url);
//                        int ends = builder.getSpanEnd(url);
//                        String texttitle = builder.toString().substring(start, ends);
                        MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
                        style.setSpan(myURLSpan, sp.getSpanStart(url),
                                sp.getSpanEnd(url),
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    rich_tvmsg.setText(style);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }

    /**
     * 重写ClickableSpan 实现富文本点击事件跳转到UdeskWebViewUrlAcivity界面
     */
    private class MyURLSpan extends ClickableSpan {

        private String mUrl;
//        private String textTitle;

        MyURLSpan(String url) {
            mUrl = url;
//            textTitle = mtextTilte;
        }


        @Override
        public void onClick(View widget) {
            Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
            intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
//            intent.putExtra(UdeskConst.WELCOME_URL_TITLE, textTitle);
            mContext.startActivity(intent);
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
            if (UdeskSDKManager.getInstance().getTxtMessageClick() != null){
                UdeskSDKManager.getInstance().getTxtMessageClick().txtMsgOnclick(mUrl);
            }else {
                Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
                mContext.startActivity(intent);
            }

        }
    }

    /**
     * 展示文本消息
     */
    class TxtViewHolder extends BaseViewHolder {
        public TextView tvMsg;

        @Override
        void bind(Context context) {
            //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
            if(UDEmojiAdapter.replaceEmoji(context, message.getMsgContent(),
                    (int) tvMsg.getTextSize()) != null){
                tvMsg.setText(UDEmojiAdapter.replaceEmoji(context, message.getMsgContent(),
                        (int) tvMsg.getTextSize()));
            }else{
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

            /**
             * 设置重发按钮的点击事件
             */
            ivStatus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((UdeskChatActivity) mContext).retrySendMsg(message);
                }
            });
        }

        View getSubjectContent() {
            return tvMsg;
        }

    }

    /**
     * 展示语音消息
     */
    class AudioViewHolder extends BaseViewHolder {
        public TextView tvDuration;
        public View record_item_content;
        public ImageView record_play;

        public TextView getDurationView() {
            return tvDuration;
        }

        @Override
        void bind(Context context) {
            checkPlayBgWhenBind();
            if (message.getDuration() > 0) {
                char symbol = 34;
                tvDuration.setText(message.getDuration() + "" + String.valueOf(symbol));
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
            int min = UdeskUtil.getDisplayWidthPixels((Activity) mContext) / 6;
            int max = UdeskUtil.getDisplayWidthPixels((Activity) mContext) * 3 / 5;
            int step = (int) ((duration < 10) ? duration : (duration / 10 + 9));
            record_item_content.getLayoutParams().width = (step == 0) ? min
                    : (min + (max - min) / 15 * step);
        }

        private void checkPlayBgWhenBind() {
            if (message.isPlaying) {
                resetAnimationAndStart();
            } else {
                record_play.setImageDrawable(mContext.getResources().getDrawable(
                        isLeft ? R.drawable.udesk_im_record_left_default : R.drawable.udesk_im_record_right_default));
            }
        }

        private void resetAnimationAndStart() {
            record_play.setImageDrawable(mContext.getResources().getDrawable(
                    isLeft ? R.drawable.udesk_im_record_play_left : R.drawable.udesk_im_record_play_right));
            Drawable playDrawable = record_play.getDrawable();
            if (playDrawable != null
                    && playDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) playDrawable).start();
            }
        }

        // 判断开始播放

        public void startAnimationDrawable() {
            message.isPlaying = true;

            Drawable playDrawable = record_play.getDrawable();
            if (playDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) playDrawable).start();
            } else {
                resetAnimationAndStart();
            }
        }

        // 关闭播放
        protected void endAnimationDrawable() {
            message.isPlaying = false;

            Drawable playDrawable = record_play.getDrawable();
            if (playDrawable != null
                    && playDrawable instanceof AnimationDrawable) {
                ((AnimationDrawable) playDrawable).stop();
                ((AnimationDrawable) playDrawable).selectDrawable(0);
            }
        }
    }

    /**
     * 展示图片消息
     */
    public class ImgViewHolder extends BaseViewHolder {
        public ChatImageView imgView;

        @Override
        void bind(Context context) {
            try {
                if (options == null) {
                    initDisplayOptions();
                }
                getImageLoader(context).displayImage(UdeskUtil.buildImageLoaderImgUrl(message), imgView, options);
                imgView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).previewPhoto(message);

                    }
                });
                ivStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).retrySendMsg(message);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }catch (OutOfMemoryError error){
                error.printStackTrace();
            }
        }
    }

    /**
     * 展示客服转移消息提示
     */
    public class RedirectViewHolder extends BaseViewHolder {
        public TextView redirectMsg;

        @Override
        void bind(Context context) {
            redirectMsg.setText(message.getMsgContent());
        }

    }

    /**
     * 展示商品链接消息
     */
    public class CommodityViewHolder extends BaseViewHolder {
        public View rootView;
        public ImageView thumbnail;
        public TextView title;
        public TextView subTitle;
        public TextView link;

        @Override
        void bind(Context context) {
            try{
                final UdeskCommodityItem item = (UdeskCommodityItem) message;
                title.setText(item.getTitle());
                subTitle.setText(item.getSubTitle());
                getImageLoader(context).displayImage(item.getThumbHttpUrl(), thumbnail, options);
                link.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((UdeskChatActivity) mContext).sentLink(item.getCommodityUrl());
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }catch (OutOfMemoryError error){
                error.printStackTrace();
            }

        }
    }


    private void initItemNormalView(View convertView, BaseViewHolder holder,
                                    int itemType, final int position) {
        holder.ivHeader = (CircleImageView) convertView.findViewById(R.id.udesk_iv_head);
        holder.tvTime = (TextView) convertView.findViewById(R.id.udesk_tv_time);
        holder.ivStatus = (ImageView) convertView.findViewById(R.id.udesk_iv_status);
        holder.pbWait = (ProgressBar) convertView.findViewById(R.id.udesk_im_wait);
        holder.agentnickName = (TextView) convertView.findViewById(R.id.udesk_nick_name);
        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMTimeTextColorResId,holder.tvTime);
        UdekConfigUtil.setUITextColor(UdeskConfig.udeskIMAgentNickNameColorResId,holder.agentnickName);
    }

    /**
     * 计算是否要显示当前位置消息的发送或接受时间
     */
    private void tryShowTime(int position, BaseViewHolder holder,
                             MessageInfo info) {
        if (info instanceof UdeskCommodityItem) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext,System.currentTimeMillis()));
        } else if (needShowTime(position)) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext,info.getTime()));
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }
    }

    private boolean needShowTime(int position) {
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
        return false;
    }

    /**
     * 根据消息ID  修改对应消息的状态
     */
    public boolean changeImState(View convertView, String msgId, int state) {
        Object tag = convertView.getTag();
        if (tag != null && tag instanceof BaseViewHolder) {
            BaseViewHolder cache = (BaseViewHolder) tag;
            if (cache.message != null && msgId.equals(cache.message.getMsgId())) {
                cache.changeUiState(state);
                cache.message.setSendFlag(state);
                return true;
            }
        }

        return false;
    }
    /**
     * 根据消息ID  修改对应消息的状态
     */
    public void updateStatus(String msgId, int state){
        try {
            for (MessageInfo msg : list){
                if (msg.getMsgId() != null && msg.getMsgId().equals(msgId)){
                    msg.setSendFlag(state);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean changeVideoTime(View convertView, String msgId, int duration) {
        Object tag = convertView.getTag();
        if (tag != null && tag instanceof AudioViewHolder) {
            AudioViewHolder cache = (AudioViewHolder) tag;
            if (cache.message != null && msgId.equals(cache.message.getMsgId())) {
                char symbol = 34;
                cache.getDurationView().setText(duration + String.valueOf(symbol));
                cache.message.setDuration(duration);
                return true;
            }
        }

        return false;
    }


    public View getTextViewForContentItem(View contentView) {
        Object tag = contentView.getTag();
        if (tag != null && tag instanceof TxtViewHolder) {
            TxtViewHolder cache = (TxtViewHolder) tag;
            if (UdeskConst.parseTypeForMessage(cache.message.getMsgtype()) != UdeskConst.ChatMsgTypeInt.TYPE_TEXT) {
                throw new RuntimeException(" we need text type ");
            }
            return cache.getSubjectContent();
        }
        return null;
    }

    public void dispose() {
        if (mContext != null) {
            mContext = null;
        }
        if (list != null) {
            list.clear();
            list = null;
        }
    }

}
