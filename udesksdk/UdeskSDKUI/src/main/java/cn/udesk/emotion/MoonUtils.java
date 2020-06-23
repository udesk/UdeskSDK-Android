package cn.udesk.emotion;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图文混排工具
 */
public class MoonUtils {

    private static final float SMALL_SCALE = 0.5F;

    /**
     * EditText用来转换表情文字的方法，如果没有使用EmoticonPickerView的attachEditText方法，则需要开发人员手动调用方法来又识别EditText中的表情
     */
    public static void replaceEmoticons(Context context, Editable editable, int start, int count) {
        if (count <= 0 || editable.length() < start + count) {
            return;
        }

        CharSequence s = editable.subSequence(start, start + count);
        Matcher matcher = EmojiManager.getPattern().matcher(s);
        while (matcher.find()) {
            int from = start + matcher.start();
            int to = start + matcher.end();
            String emot = editable.subSequence(from, to).toString();
            Drawable d = getEmotDrawable(context, emot, SMALL_SCALE);
            if (d != null) {
                MyImageSpan span = new MyImageSpan(d);
                editable.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public static boolean isHasEmotions(String string){
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        Matcher matcher = EmojiManager.getPattern().matcher(string);
        return matcher.find();
    }

    public static SpannableString replaceEmoticons(Context context, String string, int textSize) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        SpannableString spannable = new SpannableString(string);
        Matcher matcher = EmojiManager.getPattern().matcher(string);
        while (matcher.find()) {
            int from = matcher.start();
            int to =  matcher.end();
            String emot = string.subSequence(from, to).toString();
            Drawable d = getEmotDrawable(context, emot, textSize);
            if (d != null) {
                MyImageSpan span = new MyImageSpan(d);
                spannable.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannable;
    }

    private static Drawable getEmotDrawable(Context context, String text, int textSize) {
        Drawable drawable = EmojiManager.getDrawable(context, text);

        if (drawable != null) {
            drawable.setBounds(0, 0, 2 * textSize, 2 * textSize);
        }

        return drawable;
    }

    private static Drawable getEmotDrawable(Context context, String text, float scale) {
        Drawable drawable = EmojiManager.getDrawable(context, text);

        // scale
        if (drawable != null) {
            int width = (int) (drawable.getIntrinsicWidth() * scale);
            int height = (int) (drawable.getIntrinsicHeight() * scale);
            drawable.setBounds(0, 0, width, height);
        }

        return drawable;
    }
}
