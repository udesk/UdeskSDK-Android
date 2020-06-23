package cn.udesk.emotion;

import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;

import java.util.List;

/**
 * 表情控件的ViewPager适配器(emoji + 贴图)
 */

public class EmotionViewPagerAdapter extends PagerAdapter {

    int mPageCount = 0;
    int mTabPosi = 0;

    private int mEmotionLayoutWidth;
    private int mEmotionLayoutHeight;

    private IEmotionSelectedListener listener;
    EditText mMessageEditText;

    public void attachEditText(EditText messageEditText) {
        mMessageEditText = messageEditText;
    }

    public EmotionViewPagerAdapter(int emotionLayoutWidth, int emotionLayoutHeight, int tabPosi, IEmotionSelectedListener listener) {
        mEmotionLayoutWidth = emotionLayoutWidth;
        mEmotionLayoutHeight = emotionLayoutHeight;
        mTabPosi = tabPosi;

        if (mTabPosi == 0) {
            mPageCount = (int) Math.ceil(EmojiManager.getDisplayCount() / (float) EmotionLayout.EMOJI_PER_PAGE);
        } else {
            mPageCount = (int) Math.ceil(StickerManager.getInstance().getStickerCategories().get(mTabPosi - 1).getStickers().size() / (float) EmotionLayout.STICKER_PER_PAGE);
        }

        this.listener = listener;
    }

    @Override
    public int getCount() {
        return mPageCount == 0 ? 1 : mPageCount;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        Context context = container.getContext();
        RelativeLayout rl = new RelativeLayout(context);
        rl.setGravity(Gravity.CENTER);

        try {
            GridView gridView = new GridView(context);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            gridView.setLayoutParams(params);
            gridView.setGravity(Gravity.CENTER);

            gridView.setTag(position);//标记自己是第几页
            if (mTabPosi == 0) {
                gridView.setOnItemClickListener(emojiListener);
                gridView.setAdapter(new EmojiAdapter(context, mEmotionLayoutWidth, mEmotionLayoutHeight, position * EmotionLayout.EMOJI_PER_PAGE));
                gridView.setNumColumns(EmotionLayout.EMOJI_COLUMNS);
            } else {
                StickerCategory category = StickerManager.getInstance().getCategory(StickerManager.getInstance().getStickerCategories().get(mTabPosi - 1).getName());
                gridView.setOnItemClickListener(stickerListener);
                gridView.setAdapter(new StickerAdapter(context, category, mEmotionLayoutWidth, mEmotionLayoutHeight, position * EmotionLayout.STICKER_PER_PAGE));
                gridView.setNumColumns(EmotionLayout.STICKER_COLUMNS);
            }

            rl.addView(gridView);
            container.addView(rl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rl;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public AdapterView.OnItemClickListener emojiListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            try {
                int index = position + (Integer) parent.getTag() * EmotionLayout.EMOJI_PER_PAGE;
                int count = EmojiManager.getDisplayCount();
                if (position == EmotionLayout.EMOJI_PER_PAGE || index >= count) {
                    if (listener != null) {
                        listener.onEmojiSelected("/DEL");
                    }
                    onEmojiSelected("/DEL");
                } else {
                    String text = EmojiManager.getDisplayText((int) id);
                    if (!TextUtils.isEmpty(text)) {
                        if (listener != null) {
                            listener.onEmojiSelected(text);
                        }
                        onEmojiSelected(text);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    public AdapterView.OnItemClickListener stickerListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            try {
                StickerCategory category = StickerManager.getInstance().getStickerCategories().get(mTabPosi - 1);
                List<StickerItem> stickers = category.getStickers();
                int index = position + (Integer) parent.getTag() * EmotionLayout.STICKER_PER_PAGE;

                if (index >= stickers.size()) {
                    Log.i("CSDN_LQR", "index " + index + " larger than size " + stickers.size());
                    return;
                }

                if (listener != null) {
                    StickerItem sticker = stickers.get(index);
                    StickerCategory real = StickerManager.getInstance().getCategory(sticker.getCategory());

                    if (real == null) {
                        return;
                    }

                    listener.onStickerSelected(sticker.getCategory(), sticker.getName(), StickerManager.getInstance().getStickerBitmapPath(sticker.getCategory(), sticker.getName()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void onEmojiSelected(String key) {
        try {
            if (mMessageEditText == null) {
                return;
            }
            Editable editable = mMessageEditText.getText();
            if (key.equals("/DEL")) {
                mMessageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            } else {
                int start = mMessageEditText.getSelectionStart();
                int end = mMessageEditText.getSelectionEnd();
                start = (start < 0 ? 0 : start);
                end = (start < 0 ? 0 : end);
                editable.replace(start, end, key);

                int editEnd = mMessageEditText.getSelectionEnd();
                MoonUtils.replaceEmoticons(LQREmotionKit.getContext(), editable, 0, editable.toString().length());
                mMessageEditText.setSelection(editEnd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
