package cn.udesk.itemview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Map;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskWebViewUrlAcivity;
import cn.udesk.config.UdeskConfigUtil;
import cn.udesk.emotion.MoonUtils;
import cn.udesk.fragment.UdeskResendDialog;
import cn.udesk.photoselect.PictureVideoPlayActivity;
import cn.udesk.widget.CircleProgressBar;
import cn.udesk.widget.HtmlTagHandler;
import cn.udesk.widget.UdeskImageView;
import udesk.core.UdeskConst;
import udesk.core.model.InfoListBean;
import udesk.core.model.ProductListBean;
import udesk.core.utils.UdeskUtils;

public class RightViewHolder extends BaseViewHolder {

    private ImageView ivStatus;
    private ProgressBar pbWait;
    private TextView videoMsg;
    private RelativeLayout itemAudio;
    private TextView tvDuration;
    private LinearLayout itemImg;
    private UdeskImageView imgView;
    private LinearLayout itemFile;
    private TextView fielTitle;
    private LinearLayout itemSmallVideo;
    private ImageView smallVideoTip;
    private CircleProgressBar circleProgressBar;
    private RelativeLayout itemLocation;
    private TextView locationValue;
    private ImageView cropBitMap;
    private LinearLayout itemProduct;
    private TextView productMsg;
    private TextView productName;
    private ImageView productIcon;
    private TextView leaveMsg;
    private ImageView cancleImg;
    private String productUrl;
    public static final int[] RESIDS = {R.drawable.udesk_im_txt_right_default, R.drawable.udesk_im_txt_right_up, R.drawable.udesk_im_txt_right_down, R.drawable.udesk_im_txt_right_mid};
    private LinearLayout itemReplyProduct;
    private ImageView replyProductImg;
    private TextView replyProductTitle;
    private RelativeLayout replyProductMid;
    private TextView replyProductInfoOne;
    private TextView replyProductInfoTwo;
    private TextView replyProductInfoThree;
    private LinearLayout itemText;
    private LinearLayout itemLeaveMsg;
    private ImageView ivHeader;
    private TextView customerNickName;
    private LinearLayout llBody;

    @Override
    public void initView(Activity mContext, View convertView) {
        try {
            this.mContext = mContext;
            tvTime = convertView.findViewById(R.id.udesk_tv_time);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMTimeTextColorResId, tvTime);
            //头像
            llHead = convertView.findViewById(R.id.udesk_ll_head);
            ivHeader = convertView.findViewById(R.id.udesk_iv_head);
            customerNickName = convertView.findViewById(R.id.udesk_nick_name);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMCustomerNickNameColorResId, customerNickName);
            llBody = convertView.findViewById(R.id.udesk_ll_body);
            ivStatus = convertView.findViewById(R.id.udesk_iv_status);
            cancleImg = convertView.findViewById(R.id.udesk_iv_cancle);
            cancleImg.setVisibility(View.GONE);
            pbWait = convertView.findViewById(R.id.udesk_im_wait);
            itemText = convertView.findViewById(R.id.udesk_item_txt);
            tvMsg = convertView.findViewById(R.id.udesk_tv_msg);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMRightTextColorResId, tvMsg);
            //消息 leavemsg
            itemLeaveMsg = convertView.findViewById(R.id.udesk_item_leave_msg);
            leaveMsg = (TextView) convertView.findViewById(R.id.udesk_leave_msg);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMRightTextColorResId, leaveMsg);
            //video消息
            videoMsg = (TextView) convertView.findViewById(R.id.udesk_video_msg);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskIMRightTextColorResId, videoMsg);
            //audio消息
            itemAudio = (RelativeLayout) convertView.findViewById(R.id.udesk_item_audio);
            tvDuration = (TextView) convertView.findViewById(R.id.udesk_im_item_record_duration);
            record_play = (ImageView) convertView.findViewById(R.id.udesk_im_item_record_play);
            //image消息
            itemImg = (LinearLayout) convertView.findViewById(R.id.udesk_item_img);
            imgView = convertView.findViewById(R.id.udesk_im_image);
            imagePercent = (TextView) convertView.findViewById(R.id.udesk_precent);
            //file消息
            itemFile = (LinearLayout) convertView.findViewById(R.id.udesk_file_view);
            fielTitle = (TextView) convertView.findViewById(R.id.udesk_file_name);
            fielSize = (TextView) convertView.findViewById(R.id.udesk_file_size);
            operater = (TextView) convertView.findViewById(R.id.udesk_file_operater);
            mProgress = (ProgressBar) convertView.findViewById(R.id.udesk_progress);
            //smallvideo
            itemSmallVideo = (LinearLayout) convertView.findViewById(R.id.udesk_item_smallvideo);
            smallVideoImgView = convertView.findViewById(R.id.udesk_im_smallvideo_image);
            smallVideoTip = (ImageView) convertView.findViewById(R.id.video_tip);
            circleProgressBar = (CircleProgressBar) convertView.findViewById(R.id.video_upload_bar);
            //location
            itemLocation = (RelativeLayout) convertView.findViewById(R.id.udesk_item_location);
            locationValue = (TextView) convertView.findViewById(R.id.postion_value);
            cropBitMap = (ImageView) convertView.findViewById(R.id.udesk_location_image);
            //product
            itemProduct = (LinearLayout) convertView.findViewById(R.id.udesk_item_product);
            UdeskConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskProductRightBgResId, itemProduct);
            productMsg = (TextView) convertView.findViewById(R.id.udesk_product_msg);
            productName = (TextView) convertView.findViewById(R.id.product_name);
            productIcon = (ImageView) convertView.findViewById(R.id.udesk_product_icon);

            //商品回复
            itemReplyProduct = convertView.findViewById(R.id.udesk_item_reply_product);
            replyProductImg = convertView.findViewById(R.id.udesk_product_img);
            replyProductTitle = convertView.findViewById(R.id.udesg_product_title);
            replyProductMid = convertView.findViewById(R.id.udesk_product_mid);
            replyProductInfoOne = convertView.findViewById(R.id.udesk_info_one);
            replyProductInfoTwo = convertView.findViewById(R.id.udesk_info_two);
            replyProductInfoThree = convertView.findViewById(R.id.udesk_info_three);
            itemReplyProduct.setBackgroundResource(R.drawable.udesk_bg_struct_new);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void hideAllView() {
        try {
            itemText.setVisibility(View.GONE);
            itemLeaveMsg.setVisibility(View.GONE);
            videoMsg.setVisibility(View.GONE);
            itemAudio.setVisibility(View.GONE);
            itemImg.setVisibility(View.GONE);
            itemFile.setVisibility(View.GONE);
            itemSmallVideo.setVisibility(View.GONE);
            itemLocation.setVisibility(View.GONE);
            itemProduct.setVisibility(View.GONE);
            itemReplyProduct.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bind() {
        try {
            hideAllView();
            showHead(true);
            changeUiState(message.getSendFlag());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = UdeskUtil.dip2px(mContext, 10);
            llHead.setLayoutParams(params);
            switch (UdeskConst.parseTypeForMessage(message.getMsgtype())) {
                case UdeskConst.ChatMsgTypeInt.TYPE_TEXT:
                    dealTextMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_LEAVEMSG:
                case UdeskConst.ChatMsgTypeInt.TYPE_LEAVEMSG_IM:
                    dealLeaveMsg();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_LIVE_VIDEO:
                    dealVideoMsg();
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
                case UdeskConst.ChatMsgTypeInt.TYPE_VIDEO:
                case UdeskConst.ChatMsgTypeInt.TYPE_SHORT_VIDEO:
                    dealSmallVideo();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_LOCATION:
                    dealLocation();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_PRODUCT:
                    dealProduct();
                    break;
                case UdeskConst.ChatMsgTypeInt.TYPE_REPLY_PRODUCT:
                    dealReplyProduct();
                    break;

                default:
                    break;
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
            itemReplyProduct.setVisibility(View.VISIBLE);
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
     * text消息处理
     */
    private void dealTextMsg() {
        try {
            itemText.setVisibility(View.VISIBLE);
            setTextBackgroud(itemText, RESIDS);
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

            //设置消息长按事件  复制文本
            tvMsg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((UdeskChatActivity) mContext).handleText(message, v);
                    return false;
                }
            });

            //重发按钮点击事件
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * leavemsg消息处理
     */
    private void dealLeaveMsg() {
        try {
            itemLeaveMsg.setVisibility(View.VISIBLE);
            //设置文本消息内容，表情符转换对应的表情,没表情的另外处理
            if (MoonUtils.isHasEmotions(message.getMsgContent())) {
                leaveMsg.setText(MoonUtils.replaceEmoticons(mContext, message.getMsgContent(), (int) tvMsg.getTextSize()));
            } else {
                leaveMsg.setText(message.getMsgContent());
                leaveMsg.setMovementMethod(LinkMovementMethod.getInstance());
                CharSequence text = leaveMsg.getText();
                if (text instanceof Spannable) {
                    int end = text.length();
                    Spannable sp = (Spannable) leaveMsg.getText();
                    URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);
                    SpannableStringBuilder style = new SpannableStringBuilder(text);
                    style.clearSpans();// should clear old spans
                    for (URLSpan url : urls) {
                        TxtURLSpan txtURLSpan = new TxtURLSpan(url.getURL(), mContext);
                        style.setSpan(txtURLSpan, sp.getSpanStart(url),
                                sp.getSpanEnd(url),
                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                    }
                    leaveMsg.setText(style);
                }
            }

            //设置消息长按事件  复制文本
            leaveMsg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((UdeskChatActivity) mContext).handleText(message, v);
                    return false;
                }
            });
            //设置重发按钮的点击事件
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * video处理
     */
    private void dealVideoMsg() {
        try {
            videoMsg.setVisibility(View.VISIBLE);
            videoMsg.setVisibility(View.VISIBLE);
            videoMsg.setText(message.getMsgContent());
            videoMsg.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).startVideo();
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
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
            itemAudio.setVisibility(View.VISIBLE);
            checkPlayBgWhenBind();
            if (message.getDuration() > 0) {
                char symbol = 34;
                tvDuration.setText(String.format("%d%s", message.getDuration(), String.valueOf(symbol)));
            }
            itemAudio.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ((UdeskChatActivity) mContext).clickRecordFile(message);
                }
            });
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
                }
            });
            long duration = message.getDuration();
            duration = duration == 0 ? 1 : duration;
            int min = UdeskUtils.getScreenWidth(mContext) / 6;
            int max = UdeskUtils.getScreenWidth(mContext) * 3 / 5;
            int step = (int) ((duration < 10) ? duration : (duration / 10 + 9));
            itemAudio.getLayoutParams().width = (step == 0) ? min
                    : (min + (max - min) / 17 * step);//计算17份  2份是给背景图尖角预留位置
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
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
                        R.drawable.udesk_im_record_right_default));
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
            record_play.setImageDrawable(mContext.getResources().getDrawable(R.drawable.udesk_im_record_play_right));
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

    private void dealImage() {
        try {
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

//            imgView.setTag(message.getTime());
            imgView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (message == null) {
                        return;
                    }
                    Uri imgUri = null;
                    if (!TextUtils.isEmpty(message.getLocalPath())) {
                        imgUri = UdeskUtil.getUriFromPath(mContext, message.getLocalPath());
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
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
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

            //设置重发按钮的点击事件
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
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
            itemSmallVideo.setVisibility(View.VISIBLE);

            if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_SUCCESS) {
                showSuccessView();
            } else {
                if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_RETRY || message.getSendFlag() == UdeskConst.SendFlag.RESULT_SEND) {
                    showSendView();
                } else if (message.getSendFlag() == UdeskConst.SendFlag.RESULT_FAIL) {
                    showFailureView();
                }
            }
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
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
                }
            });

            cancleImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    message.setSendFlag(UdeskConst.SendFlag.RESULT_FAIL);
                    showFailureView();
                    ((UdeskChatActivity) mContext).cancelSendVideoMsg(message);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * smallvideo failure
     */
    private void showFailureView() {
        try {
            ivStatus.setVisibility(View.VISIBLE);
            pbWait.setVisibility(View.GONE);
            cancleImg.setVisibility(View.GONE);
            circleProgressBar.setVisibility(View.GONE);
            smallVideoTip.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * smallvideo send
     */
    private void showSendView() {
        try {
            if (circleProgressBar.getPercent() == 100) {
                pbWait.setVisibility(View.VISIBLE);
                smallVideoTip.setVisibility(View.VISIBLE);
                cancleImg.setVisibility(View.GONE);
                circleProgressBar.setVisibility(View.GONE);
            } else {
                pbWait.setVisibility(View.GONE);
                cancleImg.setVisibility(View.VISIBLE);
                circleProgressBar.setVisibility(View.VISIBLE);
                circleProgressBar.setPercent(circleProgressBar.getPercent());
                smallVideoTip.setVisibility(View.GONE);
            }
            ivStatus.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * smallvideo success
     */
    private void showSuccessView() {
        try {
            cancleImg.setVisibility(View.GONE);
            circleProgressBar.setVisibility(View.GONE);
            ivStatus.setVisibility(View.GONE);
            pbWait.setVisibility(View.GONE);
            smallVideoTip.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 地理位置消息处理
     */
    private void dealLocation() {
        try {
            itemLocation.setVisibility(View.VISIBLE);
            final String[] locationMessage = message.getMsgContent().split(";");
            locationValue.setText(locationMessage[locationMessage.length - 1]);
            UdeskUtil.loadImage(mContext,cropBitMap,message.getLocalPath());
            cropBitMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (UdeskSDKManager.getInstance().getUdeskConfig().locationMessageClickCallBack != null) {
                        UdeskSDKManager.getInstance().getUdeskConfig().locationMessageClickCallBack.launchMap(mContext, Double.valueOf(locationMessage[0]),
                                Double.valueOf(locationMessage[1]), locationMessage[locationMessage.length - 1]);
                    }
                }
            });

            //设置重发按钮的点击事件
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 商品消息处理
     */
    private void dealProduct() {
        try {
            itemProduct.setVisibility(View.VISIBLE);
            JSONObject jsonObject = new JSONObject(message.getMsgContent());
            if (!TextUtils.isEmpty(jsonObject.optString("imgUrl"))) {
                productIcon.setVisibility(View.VISIBLE);
                UdeskUtil.loadImage(mContext.getApplicationContext(), productIcon, jsonObject.optString("imgUrl"));
            } else {
                productIcon.setVisibility(View.GONE);
            }
            productUrl = jsonObject.optString("url");
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
                productName.setTextColor(mContext.getResources().getColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskProductRightNameLinkColorResId));
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
            //重发按钮点击事件
            ivStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    UdeskResendDialog dialog = UdeskResendDialog.newInstance(mContext.getString(R.string.udesk_resend_msg), mContext.getString(R.string.udesk_cancel), message);
                    dialog.setRetryListner(new UdeskResendDialog.RetryListner() {
                        @Override
                        public void onRetry() {
                            ((UdeskChatActivity) mContext).retrySendMsg(message);
                        }
                    });
                    dialog.show(((UdeskChatActivity) mContext), "UdeskResendDialog");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeUiState(int state) {
        try {
            if (state == UdeskConst.SendFlag.RESULT_SUCCESS) {
                ivStatus.setVisibility(View.GONE);
                pbWait.setVisibility(View.GONE);
                cancleImg.setVisibility(View.GONE);
            } else {
                if (state == UdeskConst.SendFlag.RESULT_RETRY || state == UdeskConst.SendFlag.RESULT_SEND) {
                    ivStatus.setVisibility(View.GONE);
                    pbWait.setVisibility(View.VISIBLE);
                    cancleImg.setVisibility(View.GONE);
                } else if (state == UdeskConst.SendFlag.RESULT_FAIL) {
                    ivStatus.setVisibility(View.VISIBLE);
                    pbWait.setVisibility(View.GONE);
                    cancleImg.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * smallvideo状态改变
     *
     * @param percent
     */
    @Override
    public void changeSmallvideoState(int percent) {
        try {
            circleProgressBar.setPercent(percent);
            if (percent == 100) {
                cancleImg.setVisibility(View.GONE);
                circleProgressBar.setVisibility(View.GONE);
                smallVideoTip.setVisibility(View.VISIBLE);
                pbWait.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void showTextHead(boolean b) {
        if (b) {
            showHead(b);
        } else {
            llHead.setVisibility(View.GONE);
            if (!(UdeskSDKManager.getInstance().getUdeskConfig().isShowCustomerHead || isShowCustomerNickName())) {
                setBodyTopMargin(3);
            }
        }
    }

    /**
     * 是否显示客服头像和昵称
     *
     * @param isShow
     */
    private void showHead(boolean isShow) {
        try {
            if (isShow && (UdeskSDKManager.getInstance().getUdeskConfig().isShowCustomerHead || isShowCustomerNickName())) {
                llHead.setVisibility(View.VISIBLE);
                setBodyTopMargin(3);
                if (!UdeskSDKManager.getInstance().getUdeskConfig().isShowCustomerHead) {
                    ivHeader.setVisibility(View.GONE);
                } else {
                    ivHeader.setVisibility(View.VISIBLE);
                    if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getUdeskConfig().customerUrl)) {
                        UdeskUtil.loadImage(mContext, ivHeader, UdeskSDKManager.getInstance().getUdeskConfig().customerUrl);
                    }
                }
                if (!isShowCustomerNickName()) {
                    customerNickName.setVisibility(View.GONE);
                } else {
                    customerNickName.setVisibility(View.VISIBLE);
                    customerNickName.setText(getCustomerNickName());
                }

            } else {
                llHead.setVisibility(View.GONE);
                setBodyTopMargin(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBodyTopMargin(int i) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = UdeskUtils.dip2px(mContext, i);
        llBody.setLayoutParams(params);
    }

    private String getCustomerNickName() {
        Map<String, String> defaultUserInfo = UdeskSDKManager.getInstance().getUdeskConfig().defaultUserInfo;
        if (defaultUserInfo != null && defaultUserInfo.containsKey(UdeskConst.UdeskUserInfo.NICK_NAME)
                && !TextUtils.isEmpty(defaultUserInfo.get(UdeskConst.UdeskUserInfo.NICK_NAME))) {
            return defaultUserInfo.get(UdeskConst.UdeskUserInfo.NICK_NAME);
        }
        return "";
    }

    private boolean isShowCustomerNickName() {
        if (UdeskSDKManager.getInstance().getUdeskConfig().isShowCustomerNickname && !TextUtils.isEmpty(getCustomerNickName())) {
            return true;
        } else {
            return false;
        }
    }
}
