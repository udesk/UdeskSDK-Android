package cn.udesk.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import cn.udesk.R;
import cn.udesk.widget.EmojiconSpan;

public class UDEmojiAdapter extends BaseAdapter {

    public final static String EMOJI_PREFIX = "[emoji";

    public final static String[] EMOJI_ARRAY = {"001", "002", "003", "004",
            "005", "006", "007", "008", "009", "010", "011", "012", "013",
            "014", "015", "016", "017", "018", "019", "020", "021", "022",
            "023", "024", "025", "026", "027", "028",};

    public static int[] EMOJI_RESOURCE_ID_ARRAY = new int[]{
            R.drawable.udesk_001,
            R.drawable.udesk_002,
            R.drawable.udesk_003,
            R.drawable.udesk_004,
            R.drawable.udesk_005,
            R.drawable.udesk_006,
            R.drawable.udesk_007,
            R.drawable.udesk_008,
            R.drawable.udesk_009,
            R.drawable.udesk_010,
            R.drawable.udesk_011,
            R.drawable.udesk_012,
            R.drawable.udesk_013,
            R.drawable.udesk_014,
            R.drawable.udesk_015,
            R.drawable.udesk_016,
            R.drawable.udesk_017,
            R.drawable.udesk_018,
            R.drawable.udesk_019,
            R.drawable.udesk_020,
            R.drawable.udesk_021,
            R.drawable.udesk_022,
            R.drawable.udesk_023,
            R.drawable.udesk_024,
            R.drawable.udesk_025,
            R.drawable.udesk_026,
            R.drawable.udesk_027,
            R.drawable.udesk_028,
    };

    private Context mContext;

    public UDEmojiAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return EMOJI_ARRAY.length;
    }

    @Override
    public String getItem(int position) {
        try {
            if (position < 0 || position >= EMOJI_ARRAY.length) {
                return "";
            }
            return "[emoji" + EMOJI_ARRAY[position] + "]";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

//	public int getItemResourceId(int position) {
//		if (position < 0 || position >= EMOJI_ARRAY.length) {
//			return 0;
//		}
//		return EMOJI_RESOURCE_ID_ARRAY[position];
//	}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.udesk_layout_emoji_item, null);
            }

            ((ImageView) convertView)
                    .setImageResource(EMOJI_RESOURCE_ID_ARRAY[position]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    public static SpannableString replaceEmoji(Context mContext, String text, int textSize) {
        try {
            final int emojiWidth = (int) (mContext.getResources()
                    .getDisplayMetrics().density * 40 / 2.0f);
            SpannableString spannable = new SpannableString(text);
            final int prefixLength = EMOJI_PREFIX.length();
            int index = 0, start = 0;
            index = text.indexOf(EMOJI_PREFIX, index);
            if (index <= -1) {
                return null;
            }
            while (index > -1) {
                start = index + prefixLength;
                String emojiNumber = text
                        .substring(start, text.indexOf("]", start));
                for (int j = 0; j < EMOJI_ARRAY.length; j++) {
                    if (EMOJI_ARRAY[j].equals(emojiNumber)) {
                        Drawable drawable = mContext.getResources().getDrawable(
                                EMOJI_RESOURCE_ID_ARRAY[j]);
                        if (emojiNumber.equals("028")) {  //sdk 端最后一个表情替换成了删除，收到最后一个表情，做特殊处理
                            drawable = mContext.getResources().getDrawable(R.drawable.udesk_029);
                        }
                        if (drawable != null) {
                            drawable.setBounds(0, 0, emojiWidth, emojiWidth);
                            EmojiconSpan span = new EmojiconSpan(mContext, drawable, textSize);
                            spannable.setSpan(span, index,
                                    start + emojiNumber.length() + 1,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            break;
                        }
                    }
                }

                index = text.indexOf(EMOJI_PREFIX, index + 7);
            }
            return spannable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
