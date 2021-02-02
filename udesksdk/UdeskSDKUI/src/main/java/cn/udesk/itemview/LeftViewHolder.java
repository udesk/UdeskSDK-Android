package cn.udesk.itemview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskWebViewUrlAcivity;
import cn.udesk.activity.WorkOrderWebViewActivity;
import cn.udesk.adapter.BrandAdapter;
import cn.udesk.adapter.BrandDivider;
import cn.udesk.adapter.StrucTableAdapter;
import cn.udesk.config.UdeskConfigUtil;
import cn.udesk.emotion.MoonUtils;
import cn.udesk.model.RobotJumpMessageModel;
import cn.udesk.model.SpanModel;
import cn.udesk.model.StructModel;
import cn.udesk.model.UdeskQueueItem;
import cn.udesk.photoselect.PictureVideoPlayActivity;
import cn.udesk.rich.XRichText;
import cn.udesk.widget.CircleProgressBar;
import cn.udesk.widget.HtmlTagHandler;
import cn.udesk.widget.RecycleViewDivider;
import cn.udesk.widget.UdeskImageView;
import cn.udesk.widget.UdeskRecycleView;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.InfoListBean;
import udesk.core.model.LinkBean;
import udesk.core.model.MessageInfo;
import udesk.core.model.OptionsListBean;
import udesk.core.model.ProductListBean;
import udesk.core.model.ShowProductBean;
import udesk.core.model.StrucTableBean;
import udesk.core.model.TemplateMsgBean;
import udesk.core.model.TopAskBean;
import udesk.core.model.WebConfigBean;
import udesk.core.model.WechatImageBean;
import udesk.core.utils.UdeskUtils;


public class LeftViewHolder extends BaseViewHolder implements XRichText.Callback {

    private UdeskImageView ivHeader;
    private TextView agentnickName;
    private TextView videoMsg;
    private LinearLayout itemAudio;
    private LinearLayout audioTop;
    private TextView tvDuration;
    private LinearLayout itemImg;
    private UdeskImageView imgView;
    private TextView fielTitle;
    private LinearLayout itemFile;
    private LinearLayout itemSmallVideo;
    private ImageView smallVideoTip;
    private CircleProgressBar circleProgressBar;
    private TextView redirectMsg;
    private LinearLayout itemStruct;
    private LinearLayout structImgView;
    private LinearLayout structTextView;
    private LinearLayout structBtnLineayLayout;
    private ImageView structImg;
    private TextView structTitle;
    private TextView structDes;
    private TextView events;
    private LinearLayout itemQueue;
    private TextView leaveingMsg;
    private TextView queueContext;
    private XRichText richMsg;
    private XRichText leaveMsg;
    public static final int[] RESIDS = {R.drawable.udesk_im_txt_left_default, R.drawable.udesk_im_txt_left_up, R.drawable.udesk_im_txt_left_down, R.drawable.udesk_im_txt_left_mid};
    private LinearLayout robotItemImgTxt;
    private ImageView robotImgTxtImg;
    private RelativeLayout robotImgTxtTop;
    private TextView robotImgTxtTitle;
    private TextView robotImgTxtDes;
    private LinearLayout robotItemTxt;
    private RelativeLayout robotRlFold;
    private TextView robotTxtFold;
    private ImageView robotImgfold;
    private LinearLayout robotLlUseful;
    private ImageView robotImgUseful;
    private ImageView robotImgUseless;
    private LinearLayout robotLlOk;
    private TextView robotTxtOK;
    private TextView robotTxtNo;
    private LinearLayout robotLlTxt;
    private LinearLayout robotItemBrand;
    private TextView robotTxtbrandTitle;
    private RecyclerView robotRvBrand;
    private LinearLayout robotItemQueClassify;
    private XRichText robotTxtQueTitle;
    private TextView tvTransferAgent;
    private LinearLayout itemStructTable;
    private TextView structTableTitle;
    private UdeskRecycleView structRv;
    private TextView structTableLine;
    private LinearLayout structTableChange;
    private LinearLayout itemReplyProduct;
    private ImageView replyProductImg;
    private TextView replyProductTitle;
    private RelativeLayout replyProductMid;
    private TextView replyProductInfoOne;
    private TextView replyProductInfoTwo;
    private TextView replyProductInfoThree;
    private StrucTableAdapter strucTableAdapter;
    private LinearLayout itemLink;
    private ImageView linkImg;
    private TextView linkTitle;
    private LinearLayout itemLeaveMsg;
    private LinearLayout itemRich;
    private LinearLayout itemVideo;
    private LinearLayout itemRedirect;
    private LinearLayout itemEvent;
    private LinearLayout llExpandContainer;
    private LinearLayout containerLink;
    private LinearLayout containerReplyProduct;
    private LinearLayout containerTable;
    private LinearLayout containerClassify;
    private LinearLayout containerImgTxt;
    private LinearLayout containerStruct;
    private LinearLayout containerSmallvideo;
    private LinearLayout containerFile;
    private LinearLayout containerImg;
    private LinearLayout containerAudio;
    private LinearLayout containerVideo;
    private LinearLayout containerRich;
    private LinearLayout containerLeavemsg;
    private LinearLayout containerTxt;
    private LinearLayout itemFlow;
    private XRichText flowMsg;
    private LinearLayout containerFlow;
    private LinearLayout itemTemplate;
    private TextView templateTitle;
    private XRichText templateContent;
    private LinearLayout templateContainer;
    private TextView templateLine;
    private LinearLayout itemProduct;
    private TextView productMsg;
    private TextView productName;
    private ImageView productIcon;


    @Override
    public void initView(Activity mContext, View convertView) {
        try {
            this.mContext = mContext;
            tvTime = convertView.findViewById(R.id.udesk_tv_time);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMTimeTextColorResId, tvTime);
            //头像
            llHead = convertView.findViewById(R.id.udesk_ll_head);
            ivHeader = convertView.findViewById(R.id.udesk_iv_head);
            agentnickName = convertView.findViewById(R.id.udesk_nick_name);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMAgentNickNameColorResId, agentnickName);
            //有用
            robotLlUseful = convertView.findViewById(R.id.udesk_robot_ll_useful);
            robotImgUseful = convertView.findViewById(R.id.udesk_useful);
            robotImgUseless = convertView.findViewById(R.id.udesk_useless);
            //是的
            robotLlOk = convertView.findViewById(R.id.udesk_im_ll_ok);
            robotTxtOK = convertView.findViewById(R.id.udesgk_im_ok);
            robotTxtNo = convertView.findViewById(R.id.udesgk_im_no);
            //转人工
            tvTransferAgent = convertView.findViewById(R.id.udesk_robot_transfer_agent);
            //机器人 人工 纯文本txt
            robotItemTxt = convertView.findViewById(R.id.udesk_robot_item_txt);
            tvMsg = convertView.findViewById(R.id.udesk_robot_tv_msg);
            robotRlFold = convertView.findViewById(R.id.udesl_robot_rl_fold);
            robotTxtFold = convertView.findViewById(R.id.udesk_tv_fold);
            robotImgfold = convertView.findViewById(R.id.udesk_img_fold);
            containerTxt = convertView.findViewById(R.id.udesk_container_txt);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMLeftTextColorResId, tvMsg);
            //消息 leavemsg
            itemLeaveMsg = convertView.findViewById(R.id.udesk_item_leave_msg);
            leaveMsg = convertView.findViewById(R.id.udesk_leave_msg);
//            leaveMsg.bind();
            containerLeavemsg = convertView.findViewById(R.id.udesk_container_leaveMsg);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMLeftTextColorResId, leaveMsg);
            //富文本rich
            itemRich = convertView.findViewById(R.id.udesk_item_rich);
            richMsg = convertView.findViewById(R.id.udesk_tv_rich_msg);
//            richMsg.bind();
            containerRich = convertView.findViewById(R.id.udesk_container_rich);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMLeftTextColorResId, richMsg);
            //流程
            itemFlow = convertView.findViewById(R.id.udesk_item_flow);
            flowMsg = convertView.findViewById(R.id.udesk_tv_flow);
//            flowMsg.bind();
            containerFlow = convertView.findViewById(R.id.udesk_container_flow);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMLeftTextColorResId, flowMsg);

            //video消息
            itemVideo = convertView.findViewById(R.id.udesk_item_video);
            videoMsg = convertView.findViewById(R.id.udesk_video_msg);
            containerVideo = convertView.findViewById(R.id.udesk_container_video);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMLeftTextColorResId, videoMsg);
            //audio消息
            itemAudio = convertView.findViewById(R.id.udesk_item_audio);
            audioTop = convertView.findViewById(R.id.udesk_audio_top);
            tvDuration = convertView.findViewById(R.id.udesk_im_item_record_duration);
            record_play = convertView.findViewById(R.id.udesk_im_item_record_play);
            containerAudio = convertView.findViewById(R.id.udesk_container_audio);

            //image消息
            itemImg = convertView.findViewById(R.id.udesk_item_img);
            imgView = convertView.findViewById(R.id.udesk_im_image);
            imagePercent = convertView.findViewById(R.id.udesk_precent);
            containerImg = convertView.findViewById(R.id.udesk_container_img);

            //file消息
            itemFile = convertView.findViewById(R.id.udesk_file_view);
            fielTitle = convertView.findViewById(R.id.udesk_file_name);
            fielSize = convertView.findViewById(R.id.udesk_file_size);
            operater = convertView.findViewById(R.id.udesk_file_operater);
            mProgress = convertView.findViewById(R.id.udesk_progress);
            containerFile = convertView.findViewById(R.id.udesk_container_file);
            //smallvideo
            itemSmallVideo = convertView.findViewById(R.id.udesk_item_smallvideo);
            smallVideoImgView = convertView.findViewById(R.id.udesk_im_smallvideo_image);
            smallVideoTip = convertView.findViewById(R.id.video_tip);
            circleProgressBar = convertView.findViewById(R.id.video_upload_bar);
            containerSmallvideo = convertView.findViewById(R.id.udesk_container_smallvideo);
            //redirect
            itemRedirect = convertView.findViewById(R.id.udesk_item_redirect);
            redirectMsg = convertView.findViewById(R.id.udesk_redirect_msg);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMTipTextColorResId, redirectMsg);
            //struct
            itemStruct = convertView.findViewById(R.id.udesk_item_struct);
            structImgView = convertView.findViewById(R.id.udesk_struct_img_container);
            structTextView = convertView.findViewById(R.id.udesk_struct_text_container);
            structBtnLineayLayout = convertView.findViewById(R.id.udesk_struct_btn_container);
            structImg = convertView.findViewById(R.id.udesk_struct_img);
            structTitle = convertView.findViewById(R.id.udesk_struct_title);
            structDes = convertView.findViewById(R.id.udesk_struct_des);
            containerStruct = convertView.findViewById(R.id.udesk_container_struct);
            //event
            itemEvent = convertView.findViewById(R.id.udesk_item_event);
            events = convertView.findViewById(R.id.udesk_event);
            //排队
            itemQueue = convertView.findViewById(R.id.udesk_item_queue);
            leaveingMsg = convertView.findViewById(R.id.udesk_leaveing_msg);
            queueContext = convertView.findViewById(R.id.udesk_queue_context);
            //图文
            robotItemImgTxt = convertView.findViewById(R.id.udesk_item_img_text);
            robotImgTxtImg = convertView.findViewById(R.id.udesk_im_img_txt_img);
            robotImgTxtTop = convertView.findViewById(R.id.udesk_img_text_top);
            robotImgTxtTitle = convertView.findViewById(R.id.udesk_im_img_txt_title);
            robotImgTxtDes = convertView.findViewById(R.id.udesk_im_img_txt_des);
            containerImgTxt = convertView.findViewById(R.id.udesk_container_img_txt);

            //机器人商品品牌列表
            robotItemBrand = convertView.findViewById(R.id.udesk_robot_item_struc_brand);
            robotTxtbrandTitle = convertView.findViewById(R.id.udesk_robot_txt_struc_brand_title);
            robotRvBrand = convertView.findViewById(R.id.udesk_robot_rv_struc_brand);
            LinearLayoutManager manager = new LinearLayoutManager(this.mContext);
            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
            robotRvBrand.setLayoutManager(manager);
            robotRvBrand.addItemDecoration(new BrandDivider(UdeskUtil.dip2px(this.mContext, 6)));
            //机器人常见问题有分类
            robotItemQueClassify = convertView.findViewById(R.id.udesk_robot_item_que_classify);
            robotTxtQueTitle = convertView.findViewById(R.id.udesk_robot_tv_que_title);
            containerClassify = convertView.findViewById(R.id.udesk_container_classify);
//            robotTxtQueTitle.bind();

            //结构化消息 表格 列表 商品 商品选择
            itemStructTable = convertView.findViewById(R.id.udesk_item_struct_table);
            structTableTitle = convertView.findViewById(R.id.udesk_struct_table_title);
            structTableLine = convertView.findViewById(R.id.udesk_struct_table_line);
            structRv = convertView.findViewById(R.id.udesk_struct_table_rv);
            structTableChange = convertView.findViewById(R.id.udesk_struct_table_change);
            containerTable = convertView.findViewById(R.id.udesk_container_table);

            //商品回复
            itemReplyProduct = convertView.findViewById(R.id.udesk_item_reply_product);
            replyProductImg = convertView.findViewById(R.id.udesk_product_img);
            replyProductTitle = convertView.findViewById(R.id.udesg_product_title);
            replyProductMid = convertView.findViewById(R.id.udesk_product_mid);
            replyProductInfoOne = convertView.findViewById(R.id.udesk_info_one);
            replyProductInfoTwo = convertView.findViewById(R.id.udesk_info_two);
            replyProductInfoThree = convertView.findViewById(R.id.udesk_info_three);
            containerReplyProduct = convertView.findViewById(R.id.udesk_container_reply_product);

            //链接消息
            itemLink = convertView.findViewById(R.id.udesk_item_link);
            linkImg = convertView.findViewById(R.id.udesk_link_img);
            linkTitle = convertView.findViewById(R.id.udesk_link_title);
            containerLink = convertView.findViewById(R.id.udesk_container_link);

            //工单模板消息
            itemTemplate = convertView.findViewById(R.id.udesk_item_template_msg);
            templateTitle = convertView.findViewById(R.id.udesk_template_title);
            templateContent = convertView.findViewById(R.id.udesk_template_content);
            templateContainer = convertView.findViewById(R.id.udesk_template_container);
            templateLine = convertView.findViewById(R.id.udesk_template_line);
            //product
            itemProduct = (LinearLayout) convertView.findViewById(R.id.udesk_item_product);
            UdeskConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskProductLeftBgResId, itemProduct);
            productMsg = (TextView) convertView.findViewById(R.id.udesk_product_msg);
            productName = (TextView) convertView.findViewById(R.id.product_name);
            productIcon = (ImageView) convertView.findViewById(R.id.udesk_product_icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 全部隐藏
     */
    @Override
    public void hideAllView() {
        try {
            robotLlUseful.setVisibility(View.GONE);
            robotLlOk.setVisibility(View.GONE);
            tvTransferAgent.setVisibility(View.GONE);
            robotItemTxt.setVisibility(View.GONE);
            itemLeaveMsg.setVisibility(View.GONE);
            itemRich.setVisibility(View.GONE);
            itemFlow.setVisibility(View.GONE);
            itemVideo.setVisibility(View.GONE);
            itemAudio.setVisibility(View.GONE);
            itemImg.setVisibility(View.GONE);
            itemFile.setVisibility(View.GONE);
            itemSmallVideo.setVisibility(View.GONE);
            itemRedirect.setVisibility(View.GONE);
            itemStruct.setVisibility(View.GONE);
            itemEvent.setVisibility(View.GONE);
            itemQueue.setVisibility(View.GONE);
            robotItemImgTxt.setVisibility(View.GONE);
            robotItemBrand.setVisibility(View.GONE);
            robotItemQueClassify.setVisibility(View.GONE);
            itemStructTable.setVisibility(View.GONE);
            itemReplyProduct.setVisibility(View.GONE);
            itemLink.setVisibility(View.GONE);
            itemTemplate.setVisibility(View.GONE);
            itemProduct.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bind() {
        try {
            hideAllView();
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = UdeskUtil.dip2px(mContext, 10);
            llHead.setLayoutParams(params);
            if (message instanceof UdeskQueueItem) {
                dealQueue();
                return;
            }
            if (!TextUtils.isEmpty(message.getFlowContent()) && message.getFlowId() != 0) {
                message.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_FLOW);
            }
            if (TextUtils.isEmpty(message.getMsgContent()) && (TextUtils.isEmpty(message.getFlowContent()) || message.getFlowId() == 0)) {
                message.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_ROBOT_CLASSIFY);
            }
            switch (UdeskConst.parseTypeForMessage(message.getMsgtype())) {
                case UdeskConst.ChatMsgTypeInt.TYPE_TEXT:
                    dealRobotTxtMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_LEAVEMSG:
                case UdeskConst.ChatMsgTypeInt.TYPE_LEAVEMSG_IM:
                    dealLeaveMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_RICH:
                    dealRichText();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_WECHAT_IMAGE:
                    dealImgTxt();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_LIVE_VIDEO:
                    dealVideoMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_VIDEO:
                case UdeskConst.ChatMsgTypeInt.TYPE_SHORT_VIDEO:
                    dealSmallVideo();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_AUDIO:
                    dealAudioMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_IMAGE:
                    dealImage();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_FILE:
                    dealFile();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_REDIRECT:
                    dealRedirect();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_STRUCT:
                    dealStruct();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_SURVEY:
                    message.setMsgContent(mContext.getResources().getString(R.string.udesk_survey_done));
                case UdeskConst.ChatMsgTypeInt.TYPE_EVENT:
                case UdeskConst.ChatMsgTypeInt.TYPE_ROBOT_TRANSFER:
                    dealEvent();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_ROBOT_CLASSIFY:
                    dealRobotQueClassifyMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_TABLE:
                    dealStrucTable();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_LIST:
                    dealStrucList();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_SHOW_PRODUCT:
                case UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_PRODUCT:
                    dealStrucProduct();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_REPLY_PRODUCT:
                    dealReplyProduct();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_LINK:
                    dealLink();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_FLOW:
                    dealFlow();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_TEMPLATE_MSG:
                    dealTemplateMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_PRODUCT:
                    dealProduct();
                    break;
                default:
                    dealRichText();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 商品消息处理
     */
    private void dealProduct() {
        try {
            showHead(true);
            itemProduct.setVisibility(View.VISIBLE);
            JSONObject jsonObject = new JSONObject(message.getMsgContent());
            if (!TextUtils.isEmpty(jsonObject.optString("imgUrl"))) {
                productIcon.setVisibility(View.VISIBLE);
                UdeskUtil.loadImage(mContext.getApplicationContext(), productIcon, jsonObject.optString("imgUrl"));
            } else {
                productIcon.setVisibility(View.GONE);
            }
            final String productUrl = jsonObject.optString("url");
            String name = jsonObject.optString("name");
            itemProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (UdeskSDKManager.getInstance().getUdeskConfig().productMessageClick != null) {
                        UdeskSDKManager.getInstance().getUdeskConfig().productMessageClick.txtMsgOnclick(productUrl);
                    } else {
                        if (!TextUtils.isEmpty(productUrl)) {
                            if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                return;
                            }
                            Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                            intent.putExtra(UdeskConst.WELCOME_URL, productUrl);
                            mContext.startActivity(intent);
                        }
                    }
                }
            });
            if (!TextUtils.isEmpty(name)) {
                productName.setVisibility(View.VISIBLE);
                if (UdeskSDKManager.getInstance().getUdeskConfig().udeskProductMaxLines > 0) {
                    productName.setMaxLines(UdeskSDKManager.getInstance().getUdeskConfig().udeskProductMaxLines);
                    productName.setEllipsize(TextUtils.TruncateAt.END);
                }
                productName.setText(name);
                productName.setTextColor(mContext.getResources().getColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskProductLeftNameLinkColorResId));
            } else {
                productName.setVisibility(View.GONE);
            }

            StringBuilder builder = new StringBuilder();
            builder.append("<font></font>");
            JSONArray jsonArray = jsonObject.getJSONArray("params");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.optJSONObject(i);
                    if (TextUtils.isEmpty(data.optString("text"))) {
                        continue;
                    }
                    String color = data.optString("color");
                    int size = data.optInt("size");
                    if (TextUtils.isEmpty(color)) {
                        color = "#000000";
                    }
                    if (size == 0) {
                        size = 12;
                    }
                    String textStr = "<font color=" + color +
                            "  size=" + UdeskUtil.dip2px(mContext, size) + ">" + data.optString("text") + "</font>";
                    if (data.optBoolean("fold")) {
                        textStr = "<b>" + textStr + "</b>";
                    }
                    if (data.optBoolean("break")) {
                        textStr = textStr + "<br>";
                    }
                    builder.append(textStr);
                }
            }
            String htmlString = builder.toString().replaceAll("font", HtmlTagHandler.TAG_FONT);
            Spanned fromHtml = Html.fromHtml(htmlString, null, new HtmlTagHandler());
            if (TextUtils.isEmpty(fromHtml)) {
                productMsg.setVisibility(View.GONE);
            } else {
                productMsg.setVisibility(View.VISIBLE);
                productMsg.setText(fromHtml);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理转人工
     */
    private boolean dealTransfer(ViewGroup itemView) {
        try {
            Map<String, Boolean> transferMap = ((UdeskChatActivity) mContext).getTransferMap();
            if (transferMap.containsKey(message.getMsgId())) {
                Boolean isShow = transferMap.get(message.getMsgId());
                dealUseful();
                tvTransferAgent.setVisibility(View.VISIBLE);
                tvTransferAgent.setText(message.getSwitchStaffTips());
                if (isShow) {
                    tvTransferAgent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                return;
                            }
                            InvokeEventContainer.getInstance().event_OnTransferClick.invoke(message);
                        }
                    });
                } else {
                    tvTransferAgent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_transfer_out_of_data));
                        }
                    });
                }

                if (itemView != null) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(UdeskUtil.dip2px(mContext, 310), RelativeLayout.LayoutParams.WRAP_CONTENT);
                    itemView.setLayoutParams(params);
                }
                return true;
            } else {
                tvTransferAgent.setVisibility(View.GONE);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 处理有用
     */
    private boolean dealUseful() {
        try {
            Map<String, Boolean> usefulMap = ((UdeskChatActivity) mContext).getUsefulMap();
            if (usefulMap.containsKey(message.getMsgId())) {
                robotLlUseful.setVisibility(View.VISIBLE);
                robotImgUseful.setVisibility(View.VISIBLE);
                robotImgUseless.setVisibility(View.VISIBLE);
                if (TextUtils.equals(message.getUsefulType(), UdeskConst.UsefulTpye.useful)) {
                    robotImgUseful.setImageResource(R.drawable.udesk_useful_clicked);
                    robotImgUseless.setImageResource(R.drawable.udesk_useless);
                    robotImgUseless.setVisibility(View.GONE);
                } else if (TextUtils.equals(message.getUsefulType(), UdeskConst.UsefulTpye.useless)) {
                    robotImgUseful.setImageResource(R.drawable.udesk_useful);
                    robotImgUseful.setVisibility(View.GONE);
                    robotImgUseless.setImageResource(R.drawable.udesk_useless_clicked);
                } else {
                    robotImgUseful.setImageResource(R.drawable.udesk_useful);
                    robotImgUseless.setImageResource(R.drawable.udesk_useless);
                }

                robotImgUseful.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.isUsefulClicked()) {
                            UdeskUtils.showToast(mContext, mContext.getResources().getString(R.string.udesk_answer_has_survey));
                        } else {
                            if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                return;
                            }
                            InvokeEventContainer.getInstance().event_OnAnswerClick.invoke(message.getLogId(), "1");
                            robotImgUseful.setImageResource(R.drawable.udesk_useful_clicked);
                            robotImgUseless.setImageResource(R.drawable.udesk_useless);
                            robotImgUseful.setVisibility(View.VISIBLE);
                            robotImgUseless.setVisibility(View.GONE);
                            message.setUsefulType(UdeskConst.UsefulTpye.useful);
                            message.setUsefulClicked(true);
                        }
                    }
                });
                robotImgUseless.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (message.isUsefulClicked()) {
                            UdeskUtils.showToast(mContext, mContext.getResources().getString(R.string.udesk_answer_has_survey));
                        } else {
                            if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                return;
                            }
                            InvokeEventContainer.getInstance().event_OnAnswerClick.invoke(message.getLogId(), "2");
                            robotImgUseful.setVisibility(View.GONE);
                            robotImgUseless.setVisibility(View.VISIBLE);
                            robotImgUseful.setImageResource(R.drawable.udesk_useful);
                            robotImgUseless.setImageResource(R.drawable.udesk_useless_clicked);
                            message.setUsefulType(UdeskConst.UsefulTpye.useless);
                            message.setUsefulClicked(true);
                        }
                    }
                });
                return true;
            } else {
                robotLlUseful.setVisibility(View.GONE);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 处理是的
     */
    private void dealOk() {
        robotLlOk.setVisibility(View.GONE);
    }

    /**
     * 处理问题带分类
     */
    private void dealRobotQueClassifyMsg() {
        try {
            showHead(true);
            robotItemQueClassify.setVisibility(View.VISIBLE);
            WebConfigBean webConfig = message.getWebConfig();
            if (webConfig != null && !TextUtils.isEmpty(webConfig.getLeadingWord())) {
                robotTxtQueTitle.callback(this).text(mContext, webConfig.getLeadingWord());
            } else {
                if (TextUtils.isEmpty(message.getRecommendationGuidance())){
                    robotTxtQueTitle.callback(this).text(mContext, mContext.getResources().getString(R.string.udesk_robot_recommendation_question));
                }else {
                    robotTxtQueTitle.setVisibility(View.GONE);
                }
            }
            dealTransfer(robotItemQueClassify);
            showRecommended(containerClassify);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //显示推荐问题
    private boolean showRecommended(final LinearLayout container) {
        try {
            ViewGroup.LayoutParams viewParams;
            List<TopAskBean> topAsk = message.getTopAsk();
            container.removeAllViews();
            if (topAsk != null && topAsk.size() > 0) {
                if (dealUseful()) {
                    viewParams = new LinearLayout.LayoutParams(UdeskUtil.dip2px(mContext, 310), ViewGroup.LayoutParams.WRAP_CONTENT);
                } else {
                    viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                }
                container.setLayoutParams(viewParams);
                if (!TextUtils.isEmpty(message.getRecommendationGuidance())){
                    View childView = LayoutInflater.from(mContext).inflate(R.layout.udesk_view_que_classify_recommend, container, false);
                    XRichText recommend = childView.findViewById(R.id.udesk_robot_tv_recommend);
                    recommend.callback(this).text(mContext, message.getRecommendationGuidance());
                    container.addView(childView);
                }

                if (topAsk.size() == 1 && topAsk.get(0).getOptionsList() != null && topAsk.get(0).getOptionsList().size() > 0) {
                    List<OptionsListBean> optionsList = topAsk.get(0).getOptionsList();
                    for (int j = 0; j < optionsList.size(); j++) {
                        final OptionsListBean optionsListBean = optionsList.get(j);
                        View childView = LayoutInflater.from(mContext).inflate(R.layout.udesk_view_que_classify_child, container, false);
                        TextView childTitle = childView.findViewById(R.id.udesk_robot_tv_que_child);
                        childTitle.setText(optionsListBean.getQuestion());
                        container.addView(childView);
                        childView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                    UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                    return;
                                }
                                if (message.isFAQ()){
                                    InvokeEventContainer.getInstance().event_OnQueClick.invoke(message.getMsgId(),message.getLogId(),
                                            optionsListBean.getQuestion(), optionsListBean.getQuestionId(),false,true);
                                }else {
                                    InvokeEventContainer.getInstance().event_OnQueClick.invoke(message.getMsgId(),message.getLogId(),
                                            optionsListBean.getQuestion(), optionsListBean.getQuestionId(),false,false);
                                }
                            }
                        });
                    }

                } else {
                    for (int i = 0; i < topAsk.size(); i++) {
                        TextView lineView = new TextView(mContext);
                        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, UdeskUtil.dip2px(mContext, 1));
                        lineView.setLayoutParams(lineParams);
                        lineView.setBackgroundResource(R.color.udesk_color_ffebedf0);
                        View groupView = LayoutInflater.from(mContext).inflate(R.layout.udesk_view_que_classify_group, container, false);
                        TextView groupTitle = groupView.findViewById(R.id.udesk_robot_que_group_title);
                        groupTitle.setText(topAsk.get(i).getQuestionType());
                        final ImageView arrow = groupView.findViewById(R.id.udesk_robot_que_group_arrow);
                        container.addView(lineView);
                        container.addView(groupView);
                        if (topAsk.get(i).getOptionsList() != null && topAsk.get(i).getOptionsList().size() > 0) {
                            final LinearLayout childContainer = new LinearLayout(mContext);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            childContainer.setLayoutParams(params);
                            childContainer.setOrientation(LinearLayout.VERTICAL);
                            List<OptionsListBean> optionsList = topAsk.get(i).getOptionsList();
                            for (int j = 0; j < optionsList.size(); j++) {
                                final OptionsListBean optionsListBean = optionsList.get(j);
                                View childView = LayoutInflater.from(mContext).inflate(R.layout.udesk_view_que_classify_child, container, false);
                                TextView childTitle = childView.findViewById(R.id.udesk_robot_tv_que_child);
                                childTitle.setText(optionsListBean.getQuestion());
                                childContainer.addView(childView);
                                childView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                            return;
                                        }
                                        if (message.isFAQ()){
                                            InvokeEventContainer.getInstance().event_OnQueClick.invoke(message.getMsgId(),message.getLogId(),
                                                    optionsListBean.getQuestion(), optionsListBean.getQuestionId(),false,true);
                                        }else {
                                            InvokeEventContainer.getInstance().event_OnQueClick.invoke(message.getMsgId(),message.getLogId(),
                                                    optionsListBean.getQuestion(), optionsListBean.getQuestionId(),false,false);
                                        }
                                    }
                                });
                            }
                            container.addView(childContainer);
                            if (i == 0) {
                                childContainer.setVisibility(View.VISIBLE);
                                arrow.setImageResource(R.drawable.udesk_fold);
                            } else {
                                childContainer.setVisibility(View.GONE);
                                arrow.setImageResource(R.drawable.udesk_expand);
                            }
                            final int groupPositon = i;
                            groupView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    for (int k = 3; k <= container.getChildCount(); k = k + 3) {
                                        if (k != (groupPositon * 3 + 3)) {
                                            container.getChildAt(k - 1).setVisibility(View.GONE);
                                            View childAt = container.getChildAt(k - 2);
                                            ImageView groupArrow = childAt.findViewById(R.id.udesk_robot_que_group_arrow);
                                            groupArrow.setImageResource(R.drawable.udesk_expand);
                                        }
                                    }
                                    if (childContainer.isShown()) {
                                        childContainer.setVisibility(View.GONE);
                                        arrow.setImageResource(R.drawable.udesk_expand);
                                    } else {
                                        childContainer.setVisibility(View.VISIBLE);
                                        arrow.setImageResource(R.drawable.udesk_fold);
                                    }
                                }
                            });

                        }

                    }
                }
                return true;
            } else {
                viewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                container.setLayoutParams(viewParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    /**
     * 处理商品品牌消息
     */
    private void dealRobotBrandMsg() {
        try {
            showHead(true);
            robotItemBrand.setVisibility(View.VISIBLE);
            List<MessageInfo> list = new ArrayList();
            BrandAdapter adapter = new BrandAdapter(mContext, list);
            robotRvBrand.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理链接
     */
    private void dealLink() {
        try {
            showHead(true);
            itemLink.setVisibility(View.VISIBLE);
            dealTransfer(itemLink);
            showRecommended(containerLink);
            final LinkBean linkBean = JsonUtils.parseLinkBean(message.getMsgContent());
            if (linkBean != null) {
                UdeskUtil.loadImage(mContext, linkImg, linkBean.getFaviconUrl());
                linkTitle.setText(linkBean.getTitle());
                itemLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                            return;
                        }
                        if (UdeskSDKManager.getInstance().getUdeskConfig().linkMessageWebonClick != null) {
                            UdeskSDKManager.getInstance().getUdeskConfig().linkMessageWebonClick.linkMsgOnclick(linkBean.getAnswerUrl());
                        }else {
                            Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                            intent.putExtra(UdeskConst.WELCOME_URL, linkBean.getAnswerUrl());
                            mContext.startActivity(intent);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理图文消息
     */
    private void dealImgTxt() {
        try {
            showHead(true);
            dealUseful();
            robotItemImgTxt.setVisibility(View.VISIBLE);
            final WechatImageBean wechatImageBean = JsonUtils.parseWechatImage(message.getMsgContent());
            if (wechatImageBean != null) {
                UdeskUtil.loadImage(mContext, robotImgTxtImg, wechatImageBean.getCoverUrl());
                robotImgTxtTitle.setText(wechatImageBean.getContent());
                robotImgTxtDes.setText(wechatImageBean.getDescription());
                if (!TextUtils.isEmpty(wechatImageBean.getContent())) {
                    robotImgTxtTitle.setVisibility(View.VISIBLE);
                    robotImgTxtTitle.setText(wechatImageBean.getContent());
                } else {
                    robotImgTxtTitle.setVisibility(View.GONE);
                }
                onWebClick(robotImgTxtTitle, wechatImageBean.getAnswerUrl());
                onWebClick(robotImgTxtDes, wechatImageBean.getAnswerUrl());
                onWebClick(robotImgTxtTop, wechatImageBean.getAnswerUrl());
            }
            dealTransfer(containerImgTxt);
            showRecommended(containerImgTxt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onWebClick(View view, final String url) {
        try {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                        UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                        return;
                    }
                    if (UdeskSDKManager.getInstance().getUdeskConfig().imgTxtMessageWebonClick != null) {
                        UdeskSDKManager.getInstance().getUdeskConfig().imgTxtMessageWebonClick.imgTxtMsgOnclick(url);
                    } else {
                        Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                        intent.putExtra(UdeskConst.WELCOME_URL, url);
                        mContext.startActivity(intent);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 机器人text消息处理
     */
    private void dealRobotTxtMsg() {
        try {
            showHead(true);
            robotItemTxt.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            robotItemTxt.setLayoutParams(layoutParams);
            if (!robotLlUseful.isShown()) {
                setTextBackgroud(robotItemTxt, RESIDS);
            }
            //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
            if (MoonUtils.isHasEmotions(message.getMsgContent())) {
                tvMsg.setText(MoonUtils.replaceEmoticons(mContext, message.getMsgContent(), (int) tvMsg.getTextSize()));
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
                        TxtURLSpan txtURLSpan = new TxtURLSpan(url.getURL(), mContext);
                        style.setSpan(txtURLSpan, sp.getSpanStart(url),
                                sp.getSpanEnd(url),
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    tvMsg.setText(style);
                }
            }
            tvMsg.post(new Runnable() {
                @Override
                public void run() {
                    if (tvMsg.getLineCount() > 5) {
                        robotRlFold.setVisibility(View.VISIBLE);
                        tvMsg.setMaxLines(5);
                        robotTxtFold.setText(mContext.getResources().getString(R.string.udesk_expand));
                        robotImgfold.setImageResource(R.drawable.udesk_text_expand);
                    } else {
                        robotRlFold.setVisibility(View.GONE);
                    }
                }
            });
            //设置消息长按事件  复制文本
            tvMsg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((UdeskChatActivity) mContext).handleText(message, v);
                    return false;
                }
            });

            dealTransfer(robotItemTxt);
            showRecommended(containerTxt);
            dealSingleLine(robotItemTxt);
            robotRlFold.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(View v) {
                    if (tvMsg.getMaxLines() > 5) {
                        robotTxtFold.setText(mContext.getResources().getString(R.string.udesk_expand));
                        robotImgfold.setImageResource(R.drawable.udesk_text_expand);
                        tvMsg.setMaxLines(5);
                    } else if (tvMsg.getMaxLines() == 5) {
                        robotTxtFold.setText(mContext.getResources().getString(R.string.udesk_collapse));
                        robotImgfold.setImageResource(R.drawable.udesk_text_fold);
                        tvMsg.setMaxLines(Integer.MAX_VALUE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dealSingleLine(final LinearLayout robotItemTxt) {
        try {
            if (dealUseful()) {
                int w = View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED);
                int h = View.MeasureSpec.makeMeasureSpec(0,
                        View.MeasureSpec.UNSPECIFIED);
                robotItemTxt.measure(w, h);
                int height = robotItemTxt.getMeasuredHeight();
                if (height < UdeskUtil.dip2px(mContext, 72)) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(UdeskUtil.dip2px(mContext, 310), UdeskUtil.dip2px(mContext, 72));
                    robotItemTxt.setLayoutParams(params);
                    robotItemTxt.setGravity(Gravity.CENTER_VERTICAL);
                }
            } else {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                robotItemTxt.setLayoutParams(layoutParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * leavemsg消息处理
     */
    private void dealLeaveMsg() {
        try {
            showHead(true);
            itemLeaveMsg.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            itemLeaveMsg.setLayoutParams(layoutParams);
            String msg = message.getMsgContent();
            //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
            if (MoonUtils.isHasEmotions(msg)) {
                msg = MoonUtils.replaceEmoticons(mContext, msg, (int) leaveMsg.getTextSize()).toString();
            }
            leaveMsg.callback(this).text(mContext, msg);
            dealTransfer(itemLeaveMsg);
            showRecommended(containerLeavemsg);
            dealSingleLine(itemLeaveMsg);
            //设置消息长按事件  复制文本
            leaveMsg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((UdeskChatActivity) mContext).handleText(message, v);
                    return false;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理工单模板消息
     */
    private void dealTemplateMsg() {
        try {
            showHead(true);
            itemTemplate.setVisibility(View.VISIBLE);
            final TemplateMsgBean templateMsgBean = JsonUtils.parseTemplateMsg(message.getMsgContent());
            templateTitle.setText(templateMsgBean.getTitle());
            templateContent.callback(this).text(mContext, templateMsgBean.getContent());
            List<TemplateMsgBean.BtnsBean> btns = templateMsgBean.getBtns();
            if (btns != null && btns.size() > 0) {
                templateLine.setVisibility(View.VISIBLE);
                templateContainer.setVisibility(View.VISIBLE);
                templateContainer.removeAllViews();
                for (int i = 0; i < btns.size(); i++) {
                    final TemplateMsgBean.BtnsBean btnsBean = btns.get(i);
                    if (btnsBean != null) {
                        if (i != 0) {
                            TextView line = new TextView(mContext);
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(UdeskUtil.dip2px(mContext, 1), LinearLayout.LayoutParams.MATCH_PARENT);
                            line.setLayoutParams(layoutParams);
                            line.setBackgroundColor(mContext.getResources().getColor(R.color.udesk_color_E8ECED));
                            templateContainer.addView(line);
                        }
                        TextView textView = new TextView(mContext);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
                        textView.setLayoutParams(layoutParams);
                        textView.setTextColor(mContext.getResources().getColor(R.color.udesk_color_307AE8));
                        textView.setTextSize(15);
                        textView.setGravity(Gravity.CENTER);
                        if (TextUtils.equals("link", btnsBean.getType()) && btnsBean.getData() != null && !TextUtils.isEmpty(btnsBean.getData().getUrl())) {
                            textView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                        UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                        return;
                                    }
                                    Intent intent = new Intent(mContext, WorkOrderWebViewActivity.class);
                                    intent.putExtra(UdeskConst.WORK_ORDER_URL, btnsBean.getData().getUrl());
                                    intent.putExtra(UdeskConst.WORK_ORDER_TITLE, templateMsgBean.getTitle());
                                    mContext.startActivity(intent);
                                }
                            });
                        }
                        if (!TextUtils.isEmpty(btnsBean.getName())) {
                            textView.setText(btnsBean.getName());
                        }
                        templateContainer.addView(textView);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 富文本消息处理
     */
    private void dealRichText() {
        try {
            showHead(true);
            itemRich.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            itemRich.setLayoutParams(layoutParams);
            String msg = message.getMsgContent();
            richMsg.callback(this).text(mContext, msg);
            dealTransfer(itemRich);
            showRecommended(containerRich);
            dealSingleLine(itemRich);

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 流程消息处理
     */
    private void dealFlow() {
        try {
            showHead(true);
            itemFlow.setVisibility(View.VISIBLE);
            String msg = message.getFlowContent();
            flowMsg.callback(this).text(mContext, msg);
            dealTransfer(itemFlow);
            showRecommended(containerFlow);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * video处理
     */
    private void dealVideoMsg() {
        try {
            showHead(true);
            videoMsg.setVisibility(View.VISIBLE);
            videoMsg.setText(message.getMsgContent());
            videoMsg.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((UdeskChatActivity) mContext).startVideo();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * audio处理
     */
    private void dealAudioMsg() {
        try {
            showHead(true);
            itemAudio.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            itemAudio.setLayoutParams(layoutParams);
            checkPlayBgWhenBind();
            if (message.getDuration() > 0) {
                char symbol = 34;
                tvDuration.setText(String.format("%d%s", message.getDuration(), String.valueOf(symbol)));
            } else {
                long audioDuration = getAudioDuration();
                if (audioDuration > 0) {
                    char symbol = 34;
                    tvDuration.setText(String.format("%d%s", audioDuration, String.valueOf(symbol)));
                }
            }
            dealTransfer(itemAudio);
            showRecommended(containerAudio);
            audioTop.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((UdeskChatActivity) mContext).clickRecordFile(message);
                }
            });
            long duration = message.getDuration();
            duration = duration == 0 ? 1 : duration;
            int min = UdeskUtils.getScreenWidth(mContext) / 6;
            int max = UdeskUtils.getScreenWidth(mContext) * 3 / 5;
            int step = (int) ((duration < 10) ? duration : (duration / 10 + 9));
            audioTop.getLayoutParams().width = (step == 0) ? min
                    : (min + (max - min) / 17 * step);//计算17份  2份是给背景图尖角预留位置
            dealSingleLine(itemAudio);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 获取语音时长
     *
     * @return
     */
    private long getAudioDuration() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        long duration = 0L;
        try {
            mediaPlayer.setDataSource(message.getMsgContent());
            mediaPlayer.prepare();
            return UdeskUtils.objectToLong(mediaPlayer.getDuration() / 1000);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaPlayer.release();
        }
        return duration;
    }

    /**
     * 检查播放状态
     */
    private void checkPlayBgWhenBind() {
        try {
            if (message.isPlaying) {
                resetAnimationAndStart();
            } else {
                record_play.setImageDrawable(mContext.getResources().getDrawable(
                        R.drawable.udesk_im_record_left_default));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 重置和开启动画
     */
    @Override
    public void resetAnimationAndStart() {
        try {
            record_play.setImageDrawable(mContext.getResources().getDrawable(R.drawable.udesk_im_record_play_left));
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

    @Override
    public void changeSmallvideoState(int percent) {

    }

    @Override
    protected void showTextHead(boolean b) {
        showHead(b);
    }

    /**
     * image处理
     */
    private void dealImage() {
        try {
            showHead(true);
            itemImg.setVisibility(View.VISIBLE);
            if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_SUCCESS
                    || message.getSendFlag() == UdeskConst.SendFlag.RESULT_FAIL) {
                imagePercent.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(mContext, message.getLocalPath())) {
                UdeskUtil.loadScaleImage(mContext, imgView, message.getLocalPath(), true);
            } else {
                UdeskUtil.loadScaleImage(mContext, imgView, UdeskUtils.uRLEncoder(message.getMsgContent()), true);
            }
            dealTransfer(itemImg);
            showRecommended(containerImg);
//            imgView.setTag(message.getTime());
            imgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (message == null) {
                        return;
                    }
                    Uri imgUri = null;
                    if (!TextUtils.isEmpty(message.getLocalPath())) {
                        UdeskUtil.getUriFromPath(mContext,message.getLocalPath());
                        UdeskUtil.previewPhoto(mContext, imgUri);
                    } else if (!TextUtils.isEmpty(message.getMsgContent())) {
                        try {
                            if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                return;
                            }
                            imgUri = Uri.parse(UdeskUtils.uRLEncoder(message.getMsgContent()));
                            UdeskUtil.previewPhoto(mContext, imgUri);
                        } catch (Exception e) {
                            if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                return;
                            }
                            imgUri = Uri.parse(message.getMsgContent());
                            UdeskUtil.previewPhoto(mContext, imgUri);
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * file处理
     */
    private void dealFile() {
        try {
            showHead(true);
            itemFile.setVisibility(View.VISIBLE);
            if (message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                if (TextUtils.isEmpty(message.getFilename())) {
                    fielTitle.setText(UdeskUtil.getFileName(mContext, message.getLocalPath()));
                } else {
                    fielTitle.setText(message.getFilename());
                }

                if (TextUtils.isEmpty(message.getFilesize())) {
                    fielSize.setText(UdeskUtil.getFileSizeByLoaclPath(mContext, message.getLocalPath()));
                } else {
                    fielSize.setText(message.getFilesize());
                }
                if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_SUCCESS) {
                    mProgress.setProgress(100);
                    operater.setText(mContext.getString(R.string.udesk_has_send));
                } else {
                    mProgress.setProgress(message.getPrecent());
                    operater.setText(String.format("%s%%", String.valueOf(message.getPrecent())));
                }
            } else {
                fielTitle.setText(message.getFilename());
                fielSize.setText(message.getFilesize());
                if (UdeskUtil.fileIsExitByUrl(mContext, UdeskConst.File_File, message.getMsgContent())
                        && UdeskUtil.getFileSize(UdeskUtil.getFileByUrl(mContext, UdeskConst.File_File, message.getMsgContent())) > 0) {
                    mProgress.setProgress(100);
                    operater.setText(mContext.getString(R.string.udesk_has_downed));
                } else {
                    mProgress.setProgress(0);
                    operater.setText(mContext.getString(R.string.udesk_has_download));
                }
                operater.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((UdeskChatActivity) mContext).downLoadMsg(message);
                    }
                });
            }
            dealTransfer(itemFile);
            showRecommended(containerFile);
            itemFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = null;
                        Uri contentUri;
                        String type;
                        if (message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                            if (UdeskUtil.isAndroidQ()) {
                                contentUri = Uri.parse(UdeskUtil.getFilePathQ(mContext, message.getLocalPath()));
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                file = new File(message.getLocalPath());
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    contentUri = UdeskUtil.getOutputMediaFileUri(mContext, file);
                                } else {
                                    contentUri = Uri.fromFile(file);
                                }
                            }
                        } else {
                            file = UdeskUtil.getFileByUrl(mContext, UdeskConst.File_File, message.getMsgContent());
                            if (file == null || UdeskUtil.getFileSizeQ(mContext.getApplicationContext(), file.getAbsolutePath()) <= 0) {
                                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_has_uncomplete_tip), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                contentUri = UdeskUtil.getOutputMediaFileUri(mContext, file);
                            } else {
                                contentUri = Uri.fromFile(file);
                            }
                        }
                        if (contentUri == null) {
                            return;
                        }
                        if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO)) {
                            intent.setDataAndType(contentUri, "video/mp4");
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                type = UdeskUtil.getMIMEType(mContext, contentUri);
                            }else {
                                type = UdeskUtil.getMIMEType(file);
                            }
                            intent.setDataAndType(contentUri, type);
                        }
                        mContext.startActivity(intent);
                    } catch (Exception e) {
                        if (!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("No Activity found to handle Intent")) {
                            Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_no_app_handle), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * smallvideo处理
     */
    private void dealSmallVideo() {
        try {
            showHead(true);
            itemSmallVideo.setVisibility(View.VISIBLE);
            showSuccessView();
            if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(mContext, message.getLocalPath())) {
                UdeskUtil.loadViewBySize(mContext, smallVideoImgView, message.getLocalPath(), UdeskUtil.dip2px(mContext, 130), UdeskUtil.dip2px(mContext, 200));
            } else if (UdeskUtil.fileIsExitByUrl(mContext, UdeskConst.FileImg, message.getMsgContent())) {
                String loaclpath = UdeskUtil.getPathByUrl(mContext, UdeskConst.FileImg, message.getMsgContent());
                UdeskUtil.loadViewBySize(mContext, smallVideoImgView, loaclpath, UdeskUtil.dip2px(mContext, 130), UdeskUtil.dip2px(mContext, 200));
            } else {
                if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                    UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                    return;
                }
                ((UdeskChatActivity) mContext).showVideoThumbnail(message);
            }
            dealTransfer(itemSmallVideo);
            showRecommended(containerSmallvideo);
//            smallVideoImgView.setTag(message.getTime());
            smallVideoImgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (message == null) {
                        return;
                    }
                    String path = "";
                    if (!TextUtils.isEmpty(message.getLocalPath()) && UdeskUtil.isExitFileByPath(mContext, message.getLocalPath())) {
                        path = message.getLocalPath();
                    } else if (!TextUtils.isEmpty(message.getMsgContent())) {
                        File file = UdeskUtil.getFileByUrl(mContext, UdeskConst.FileVideo, message.getMsgContent());
                        if (file != null && UdeskUtil.getFileSize(file) > 0) {
                            path = file.getPath();
                        } else {
                            if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                                UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                                return;
                            }
                            ((UdeskChatActivity) mContext).downLoadVideo(message);
                            path = message.getMsgContent();
                        }
                    }

                    Intent intent = new Intent();
                    intent.setClass(mContext, PictureVideoPlayActivity.class);
                    Bundle data = new Bundle();
                    data.putString(UdeskConst.PREVIEW_Video_Path, path);
                    intent.putExtras(data);
                    mContext.startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    private void showSuccessView() {
        try {
            circleProgressBar.setVisibility(View.GONE);
            smallVideoTip.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 转移消息处理
     */
    private void dealRedirect() {
        try {
            showHead(false);
            itemRedirect.setVisibility(View.VISIBLE);
            redirectMsg.setText(message.getMsgContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理商品表格
     */
    private void dealStrucTable() {
        try {
            showHead(true);
            itemStructTable.setVisibility(View.VISIBLE);
            structTableLine.setVisibility(View.VISIBLE);
            structTableChange.setVisibility(View.GONE);
            StrucTableBean strucTableBean = JsonUtils.parseStrucTable(message.getMsgContent());
            if (strucTableBean != null) {
                structTableTitle.setText(strucTableBean.getTitle());
                GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, strucTableBean.getColumnNumber());
                structRv.setLayoutManager(gridLayoutManager);
                structRv.setPadding(UdeskUtil.dip2px(mContext, 8), UdeskUtil.dip2px(mContext, 8), UdeskUtil.dip2px(mContext, 8), UdeskUtil.dip2px(mContext, 8));
                structRv.addItemDecoration(new RecycleViewDivider(mContext, GridLayoutManager.HORIZONTAL, 0, mContext.getResources().getColor(R.color.white), false));
                if (strucTableBean.getOptionList() != null && strucTableBean.getOptionList().size() > 0) {
                    StrucTableAdapter strucTableAdapter = new StrucTableAdapter(mContext, strucTableBean.getOptionList(), UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_TABLE);
                    structRv.setAdapter(strucTableAdapter);
                }
            }
            dealTransfer(containerTable);
            showRecommended(containerTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理商品列表
     */
    private void dealStrucList() {
        try {
            showHead(true);
            itemStructTable.setVisibility(View.VISIBLE);
            structTableLine.setVisibility(View.VISIBLE);
            structTableChange.setVisibility(View.GONE);
            StrucTableBean strucTableBean = JsonUtils.parseStrucTable(message.getMsgContent());
            if (strucTableBean != null) {
                structTableTitle.setText(strucTableBean.getTitle());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                structRv.setLayoutManager(linearLayoutManager);
                structRv.setPadding(0, 0, 0, 0);
                structRv.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.HORIZONTAL, UdeskUtil.dip2px(mContext, 1), mContext.getResources().getColor(R.color.udesk_color_E8ECED), false));
                if (strucTableBean.getOptionList() != null && strucTableBean.getOptionList().size() > 0) {
                    StrucTableAdapter strucTableAdapter = new StrucTableAdapter(mContext, strucTableBean.getOptionList(), UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_LIST);
                    structRv.setAdapter(strucTableAdapter);
                }
            }
            dealTransfer(containerTable);
            showRecommended(containerTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理商品消息  商品选择消息
     */
    private void dealStrucProduct() {
        try {
            showHead(true);
            itemStructTable.setVisibility(View.VISIBLE);
            structTableLine.setVisibility(View.VISIBLE);
            structTableChange.setVisibility(View.GONE);
            dealTransfer(containerTable);
            showRecommended(containerTable);
            final ShowProductBean showProductBean = JsonUtils.parseShowProduct(message.getMsgContent());
            if (showProductBean != null) {
                structTableTitle.setText(showProductBean.getTitle());
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                structRv.setLayoutManager(linearLayoutManager);
                structRv.setPadding(0, 0, 0, 0);
                structRv.addItemDecoration(new RecycleViewDivider(mContext, LinearLayoutManager.HORIZONTAL, UdeskUtil.dip2px(mContext, 1), mContext.getResources().getColor(R.color.udesk_color_E8ECED), false));
                if (showProductBean.getProductList() != null && showProductBean.getProductList().size() > 0) {
                    final List<ProductListBean> productList = showProductBean.getProductList();
                    if (productList.size() > showProductBean.getShowSize()) {
                        structTableChange.setVisibility(View.VISIBLE);
                        List<Integer> randomNum = UdeskUtil.getRandomNum(showProductBean.getShowSize(), productList.size());
                        if (((UdeskChatActivity) mContext).getRandomList().size() == 0) {
                            for (Integer i : randomNum) {
                                ((UdeskChatActivity) mContext).getRandomList().add(productList.get(i));
                            }
                        }
                        strucTableAdapter = new StrucTableAdapter(mContext, ((UdeskChatActivity) mContext).getRandomList(), UdeskConst.ChatMsgTypeInt.TYPE_SHOW_PRODUCT);
                        structTableChange.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                List<Integer> randomNum = UdeskUtil.getRandomNum(showProductBean.getShowSize(), productList.size());
                                ((UdeskChatActivity) mContext).getRandomList().clear();
                                for (Integer i : randomNum) {
                                    ((UdeskChatActivity) mContext).getRandomList().add(productList.get(i));
                                }
                                strucTableAdapter = new StrucTableAdapter(mContext, ((UdeskChatActivity) mContext).getRandomList(), UdeskConst.ChatMsgTypeInt.TYPE_SHOW_PRODUCT);
                                structRv.setAdapter(strucTableAdapter);
                            }
                        });
                    } else {
                        structTableChange.setVisibility(View.GONE);
                        strucTableAdapter = new StrucTableAdapter(mContext, productList, UdeskConst.ChatMsgTypeInt.TYPE_SHOW_PRODUCT);
                    }
                    structRv.setAdapter(strucTableAdapter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理商品回复消息
     */
    private void dealReplyProduct() {
        try {
            showHead(true);
            itemReplyProduct.setVisibility(View.VISIBLE);
            dealTransfer(containerReplyProduct);
            showRecommended(containerReplyProduct);
            final ProductListBean productListBean = JsonUtils.parseReplyProduct(message.getMsgContent());
            if (productListBean != null) {
                replyProductTitle.setText(productListBean.getName());
                if (!TextUtils.isEmpty(productListBean.getImage())) {
                    UdeskUtil.loadImage(mContext, replyProductImg, productListBean.getImage());
                }
                if (productListBean.getInfoList() != null && productListBean.getInfoList().size() > 0) {
                    List<InfoListBean> infoList = productListBean.getInfoList();
                    for (int j = 0; j < infoList.size(); j++) {
                        SpannableString spannableString = UdeskUtil.setSpan(infoList.get(j).getInfo(), UdeskUtils.objectToString(infoList.get(j).getColor()), infoList.get(j).getBoldFlag());
                        if (j == 0) {
                            replyProductMid.setVisibility(View.VISIBLE);
                            replyProductInfoOne.setText(spannableString);
                        } else if (j == 1) {
                            replyProductInfoTwo.setText(spannableString);
                        } else if (j == 2) {
                            replyProductInfoThree.setVisibility(View.VISIBLE);
                            replyProductInfoThree.setText(spannableString);
                        }
                    }
                } else {
                    replyProductMid.setVisibility(View.GONE);
                    replyProductInfoThree.setVisibility(View.GONE);
                }
                itemReplyProduct.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                            return;
                        }
                        if (UdeskSDKManager.getInstance().getUdeskConfig().replyProductMessageWebonClick != null) {
                            UdeskSDKManager.getInstance().getUdeskConfig().replyProductMessageWebonClick.replyProductMsgOnclick(productListBean.getUrl());
                        } else {
                            Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                            intent.putExtra(UdeskConst.WELCOME_URL, productListBean.getUrl());
                            mContext.startActivity(intent);
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 结构化消息处理
     */
    private void dealStruct() {
        try {
            showHead(true);
            itemStruct.setVisibility(View.VISIBLE);
            StructModel structModel = JsonUtils.parserStructMsg(message.getMsgContent());
            if (structModel != null) {
                //显示图片部分
                showStructImg(mContext, structModel, structImgView, structImg);
                //标题描述部分
                showStructText(structModel, structTextView, structTitle, structDes);
                //按钮部分
                showStructBtn(mContext, structModel, structBtnLineayLayout);
            }
            dealTransfer(itemStruct);
            showRecommended(containerStruct);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 结构化消息显示按钮
     *
     * @param mContext
     * @param structModel
     * @param structBtnLineayLayout
     */
    private void showStructBtn(Context mContext, StructModel structModel, LinearLayout structBtnLineayLayout) {
        try {
            if (structModel.getButtons() != null && structModel.getButtons().size() > 0) {
                structBtnLineayLayout.removeAllViews();
                structBtnLineayLayout.setVisibility(View.VISIBLE);
                List<StructModel.ButtonsBean> buttonsBeens = structModel.getButtons();
                for (int i = 0; i < buttonsBeens.size(); i++) {
                    StructModel.ButtonsBean buttonsBean = buttonsBeens.get(i);
                    TextView textView = new TextView(mContext);
                    textView.setText(buttonsBean.getText());
                    textView.setOnClickListener(new MyStructBtnOnClick(buttonsBean, mContext));
                    textView.setTextColor(mContext.getResources().getColor(R.color.udesk_custom_dialog_sure_btn_color));
                    textView.setGravity(Gravity.CENTER);
                    textView.setPadding(5, 5, 5, 5);
                    textView.setTextSize(18);
                    LinearLayout.LayoutParams textviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    View line = new View(mContext);
                    line.setBackgroundColor(mContext.getResources().getColor(R.color.udesk_struct_bg_line_color));
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

    @Override
    public void onImageClick(List<String> urlList, int position) {
        try {
            if (urlList.size() > 0) {
                UdeskUtil.previewPhoto(mContext, Uri.parse(urlList.get(position)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onLinkClick(String url) {
        try {
            if (UdeskSDKManager.getInstance().getUdeskConfig().richMessageWebonClick != null) {
                UdeskSDKManager.getInstance().getUdeskConfig().richMessageWebonClick.richMsgOnclick(url);
            } else {
                if (url.contains("tel:")) {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                    intent.putExtra(UdeskConst.WELCOME_URL, url);
                    mContext.startActivity(intent);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onFix(XRichText.ImageHolder holder) {
        try {
            holder.setStyle(XRichText.Style.LEFT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStepClick(SpanModel model) {
        try {
            if (TextUtils.equals(UdeskConst.ChatMsgTypeString.TYPE_FLOW,message.getMsgtype())&&model!=null&&UdeskUtils.objectToInt(model.getDataId())!=0){
                if (TextUtils.equals("1",model.getType())){
                    InvokeEventContainer.getInstance().event_OnQueClick.invoke(message.getMsgId(),message.getLogId(), model.getContent(), UdeskUtils.objectToInt(model.getDataId()),true,false);
                }else if (TextUtils.equals("2",model.getType())){
                    InvokeEventContainer.getInstance().event_OnFlowClick.invoke(message,UdeskUtils.objectToInt(model.getDataId()),model.getContent());
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRobotJumpMessage(RobotJumpMessageModel model) {
        try {
            if (TextUtils.equals(model.getMessageType(),"1")){
                InvokeEventContainer.getInstance().event_OnRobotJumpMessageClick.invoke(model.getContent());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 结构化消息按钮点击事件处理
     */
    private class MyStructBtnOnClick implements View.OnClickListener {
        final StructModel.ButtonsBean mStructBtn;
        private Context mContext;

        MyStructBtnOnClick(StructModel.ButtonsBean structBtn, Context context) {
            this.mStructBtn = structBtn;
            this.mContext = context;
        }

        @Override
        public void onClick(View view) {
            try {
                switch (mStructBtn.getType()) {
                    case UdeskConst.StructBtnTypeString.link:
                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                            return;
                        }
                        if (UdeskSDKManager.getInstance().getUdeskConfig().structMessageWebonClick != null) {
                            UdeskSDKManager.getInstance().getUdeskConfig().structMessageWebonClick.structMsgOnclick(mStructBtn.getValue());
                        } else {
                            Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                            intent.putExtra(UdeskConst.WELCOME_URL, mStructBtn.getValue());
                            mContext.startActivity(intent);
                        }
                        break;
                    case UdeskConst.StructBtnTypeString.phone:
                        Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mStructBtn.getValue()));
                        mContext.startActivity(dialIntent);
                        break;
                    case UdeskConst.StructBtnTypeString.sdkCallBack:
                        if (UdeskSDKManager.getInstance().getUdeskConfig().structMessageCallBack != null) {
                            UdeskSDKManager.getInstance().getUdeskConfig().structMessageCallBack.structMsgCallBack(mContext, mStructBtn.getValue());
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 结构化消息显示描述
     *
     * @param structModel
     * @param structTextView
     * @param structTitle
     * @param structDes
     */
    private void showStructText(StructModel structModel, LinearLayout structTextView, TextView structTitle, TextView structDes) {
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

    /**
     * 结构化消息显示图片
     *
     * @param mContext
     * @param structModel
     * @param structImgView
     * @param structImg
     */
    private void showStructImg(final Context mContext, StructModel structModel, LinearLayout structImgView, ImageView structImg) {
        try {
            final String imgUrl = structModel.getImg_url();
            if (!TextUtils.isEmpty(imgUrl)) {
                structImgView.setVisibility(View.VISIBLE);
                UdeskUtil.loadScaleImage(mContext, structImg, imgUrl, false);
                structImgView.setOnClickListener(new View.OnClickListener() {

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

    /**
     * 事件消息处理
     */
    private void dealEvent() {
        try {
            showHead(false);
            itemEvent.setVisibility(View.VISIBLE);
            tvTime.setVisibility(View.VISIBLE);
            if (!message.getCreatedTime().isEmpty()) {
                tvTime.setText(String.format("----%s----", UdeskUtil.parseEventTime(message.getCreatedTime())));
            } else {
                tvTime.setText(String.format("----%s----", UdeskUtil.parseEventTime(message.getTime())));
            }

            events.setText(message.getMsgContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 排队事件处理
     */
    private void dealQueue() {
        try {
            showHead(false);
            itemQueue.setVisibility(View.VISIBLE);
            itemQueue.setVisibility(View.VISIBLE);
            final UdeskQueueItem item = (UdeskQueueItem) message;
            queueContext.setText(item.getQueueContent());
            if (item.isEnableLeaveMsg()) {
                leaveingMsg.setVisibility(View.VISIBLE);
                leaveingMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!UdeskUtils.isNetworkConnected(mContext.getApplicationContext())) {
                            UdeskUtils.showToast(mContext.getApplicationContext(), mContext.getResources().getString(R.string.udesk_has_wrong_net));
                            return;
                        }
                        ((UdeskChatActivity) mContext).leaveMessage();
                    }
                });
            } else {
                leaveingMsg.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否显示客服头像和昵称
     *
     * @param isShow
     */
    private void showHead(boolean isShow) {
        try {
            if (isShow) {
                llHead.setVisibility(View.VISIBLE);
                ivHeader.setVisibility(View.VISIBLE);
                agentnickName.setVisibility(View.VISIBLE);
                ivHeader.setImageResource(R.drawable.udesk_im_default_agent_avatar);
                if (message.getUser_avatar() != null && !TextUtils.isEmpty(message.getUser_avatar().trim())) {
                    UdeskUtil.loadImage(mContext, ivHeader, message.getUser_avatar());
                }
                if (!TextUtils.isEmpty(message.getReplyUser())) {
                    agentnickName.setVisibility(View.VISIBLE);
                    agentnickName.setText(message.getReplyUser());
                } else {
                    agentnickName.setVisibility(View.GONE);
                }
            } else {
                llHead.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
