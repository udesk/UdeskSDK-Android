package cn.udesk.itemview;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.model.UdeskQueueItem;
import cn.udesk.widget.UdeskImageView;
import udesk.core.UdeskConst;
import udesk.core.model.MessageInfo;


public abstract class BaseViewHolder {

    public TextView tvTime;
    public MessageInfo message;
    public MessageInfo preMessage;
    public MessageInfo nextMessage;
    public List<MessageInfo> list = new ArrayList<>();
    public int position;
    public Context mContext;
    //audio
    public ImageView record_play;
    //smallvideo
    public UdeskImageView smallVideoImgView;
    //file
    public TextView fielSize;
    public TextView operater;
    public ProgressBar mProgress;
    //image
    public TextView imagePercent;
    //text
    public TextView tvMsg;
    public LinearLayout llHead;

    //2条消息之间 时间间隔超过SPACE_TIME， 会话界面会显示出消息的收发时间
    public static final long SPACE_TIME = 3 * 60 * 1000;
    //两条消息间隔 用于文本背景设置
    public static final long TEXT_SPACE_TIME = 20 * 1000;

    public void setData(Context context, List<MessageInfo> list, int position) {
        try {
            this.list = list;
            this.position = position;
            this.message = getItem(position);
            preMessage = getItem(position - 1);
            nextMessage = getItem(position + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 计算是否要显示当前位置消息的发送或接受时间
     */
    public boolean tryShowTime(MessageInfo msgInfo, int position) {
        try {
            if (msgInfo != null) {
                if (msgInfo instanceof UdeskQueueItem) {
                    tvTime.setVisibility(View.VISIBLE);
                    tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, System.currentTimeMillis()));
                    return true;
                } else if (msgInfo.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_EVENT)) {
                    tvTime.setVisibility(View.GONE);
                } else if (needShowTime(position, SPACE_TIME)) {
                    tvTime.setVisibility(View.VISIBLE);
                    tvTime.setText(UdeskUtil.formatLongTypeTimeToString(mContext, msgInfo.getTime()));
                    return true;
                } else {
                    tvTime.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean needShowTime(int position, long time) {
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
                        return currTime - preTime > time
                                || preTime - currTime > time;
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

    public MessageInfo getItem(int position) {
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    /**
     * smallvideo状态改变
     *
     * @param percent
     */
    public abstract void changeSmallvideoState(int percent);

    /**
     * audio开启播放
     */
    public void startAnimationDrawable() {
        try {
            if (message != null) {
                message.isPlaying = true;
                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).start();
                } else {
                    resetAnimationAndStart();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * audio关闭播放
     */
    public void endAnimationDrawable() {
        try {
            if (message != null) {
                message.isPlaying = false;
                Drawable playDrawable = record_play.getDrawable();
                if (playDrawable != null
                        && playDrawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) playDrawable).stop();
                    ((AnimationDrawable) playDrawable).selectDrawable(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 文件状态改变
     *
     * @param percent
     * @param fileSize
     * @param isSuccess
     */
    public void changeFileState(int percent, long fileSize, boolean isSuccess) {
        try {
            mProgress.setProgress(percent);
            if (percent == 100) {
                if (message.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                    operater.setText(mContext.getString(R.string.udesk_has_send));
                } else {
                    operater.setText(mContext.getString(R.string.udesk_has_downed));
                }
            } else {
                if (0 < percent && percent < 100) {
                    operater.setText(String.format("%d%%", percent));
                }
            }
            if (fileSize > 0) {
                fielSize.setText(UdeskUtil.formetFileSize(fileSize));
            }
            if (!isSuccess) {
                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.udesk_download_failure), Toast.LENGTH_SHORT).show();
                operater.setText(mContext.getString(R.string.udesk_has_download));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * image状态改变
     *
     * @param percent
     */
    public void changeImageState(int percent) {
        try {
            imagePercent.setVisibility(View.VISIBLE);
            imagePercent.setText(percent + "%");
            if (percent == 100) {
                imagePercent.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 简文本设置文字背景
     *
     * @param resIds text的四个背景
     */
    public void setTextBackgroud(View view, int[] resIds) {
        try {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin = UdeskUtil.dip2px(mContext, 10);
            llHead.setLayoutParams(params);
            if (tryShowTime(message, position)) {
                showTextHead(true);
                setTextBackgroudByNext(view, resIds[0], resIds[1]);
            } else {
                if (preMessage == null) {
                    showTextHead(true);
                    setTextBackgroudByNext(view, resIds[0], resIds[1]);
                } else {
                    if (UdeskConst.parseTypeForMessage(preMessage.getMsgtype()) == UdeskConst.ChatMsgTypeInt.TYPE_TEXT) {
                        if (needShowTime(position, TEXT_SPACE_TIME)) {
                            showTextHead(true);
                            setTextBackgroudByNext(view, resIds[0], resIds[1]);
                        } else {
                            if (isSameSide(message, preMessage)) {
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.topMargin = UdeskUtil.dip2px(mContext, 3);
                                llHead.setLayoutParams(layoutParams);
                                showTextHead(false);
                                setTextBackgroudByNext(view, resIds[2], resIds[3]);
                            } else {
                                showTextHead(true);
                                setTextBackgroudByNext(view, resIds[0], resIds[1]);
                            }
                        }
                    } else {
                        showTextHead(true);
                        setTextBackgroudByNext(view, resIds[0], resIds[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据后面是否有数据设置文字背景
     *
     * @param view
     * @param resId1
     * @param resId2
     */
    private void setTextBackgroudByNext(View view, int resId1, int resId2) {
        try {
            if (nextMessage == null) {
                view.setBackgroundResource(resId1);
            } else {
                if (UdeskConst.parseTypeForMessage(nextMessage.getMsgtype()) == UdeskConst.ChatMsgTypeInt.TYPE_TEXT) {
                    if (isSameSide(message, nextMessage)) {
                        if (needShowTime(position + 1, TEXT_SPACE_TIME)) {
                            view.setBackgroundResource(resId1);
                        } else {
                            view.setBackgroundResource(resId2);
                        }
                    } else {
                        view.setBackgroundResource(resId1);
                    }
                } else {
                    view.setBackgroundResource(resId1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSameSide(MessageInfo message, MessageInfo nextMessage) {
        return message.getDirection() == nextMessage.getDirection() && TextUtils.equals(message.getmAgentJid(), nextMessage.getmAgentJid());
    }

    protected abstract void showTextHead(boolean b);

    public abstract void bind();

    public abstract void initView(Activity mContext, View convertView);

    public abstract void hideAllView();

    public abstract void resetAnimationAndStart();

    public boolean changeVideoThumbnail(String msgId) {
        if (message != null && msgId.equals(message.getMsgId())) {
            if (UdeskUtil.fileIsExitByUrl(mContext, UdeskConst.FileImg, message.getMsgContent())) {
                String loaclpath = UdeskUtil.getPathByUrl(mContext, UdeskConst.FileImg, message.getMsgContent());
                UdeskUtil.loadViewBySize(mContext, smallVideoImgView, loaclpath, UdeskUtil.dip2px(mContext, 130), UdeskUtil.dip2px(mContext, 200));
            }
            return true;
        }
        return false;
    }
}
