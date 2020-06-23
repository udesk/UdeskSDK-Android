package cn.udesk.emotion;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.udesk.R;

public class EmojiAdapter extends BaseAdapter {

    private Context mContext;
    private int mStartIndex;
    private int mEmotionLayoutWidth;
    private int mEmotionLayoutHeight;
    private final float mPerWidth;
    private final float mPerHeight;
    private final float mIvSize;

    public EmojiAdapter(Context context, int emotionLayoutWidth, int emotionLayoutHeight, int startIndex) {
        mContext = context;
        mStartIndex = startIndex;
        mEmotionLayoutWidth = emotionLayoutWidth;
        mEmotionLayoutHeight = emotionLayoutHeight - LQREmotionKit.dip2px(35 + 26 + 20);//减去底部的tab高度、小圆点的高度才是viewpager的高度，再减少30dp是让表情整体的顶部和底部有个外间距

        mPerWidth = mEmotionLayoutWidth * 1f / EmotionLayout.EMOJI_COLUMNS;
        mPerHeight = mEmotionLayoutHeight * 1f / EmotionLayout.EMOJI_ROWS;
        float ivWidth = mPerWidth * .8f;
        float ivHeight = mPerHeight * .8f;
        mIvSize = Math.min(ivWidth, ivHeight);
    }

    @Override
    public int getCount() {
        int count = EmojiManager.getDisplayCount() - mStartIndex + 1;
        count = Math.min(count, EmotionLayout.EMOJI_PER_PAGE + 1);
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return mStartIndex + position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout rl = new RelativeLayout(mContext);
        try {
            rl.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, (int) mPerHeight));


            ImageView emojiThumb = new ImageView(mContext);
            int count = EmojiManager.getDisplayCount();
            int index = mStartIndex + position;
            if (position == EmotionLayout.EMOJI_PER_PAGE || index == count) {
                emojiThumb.setBackgroundResource(R.drawable.udesk_ic_emoji_del);
            } else if (index < count) {
                emojiThumb.setBackgroundDrawable(EmojiManager.getDisplayDrawable(mContext, index));
            }

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.width = (int) mIvSize;
            layoutParams.height = (int) mIvSize;
            emojiThumb.setLayoutParams(layoutParams);

            rl.setGravity(Gravity.CENTER);
            rl.addView(emojiThumb);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rl;
    }
}
