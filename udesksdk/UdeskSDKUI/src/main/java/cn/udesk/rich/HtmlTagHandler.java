package cn.udesk.rich;

import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;

import java.lang.ref.SoftReference;
import java.util.Stack;

import udesk.core.event.InvokeEventContainer;

/**
 * Created by zhou on 16-10-20.
 * 自定义标签的处理
 */
public class HtmlTagHandler implements UdeskHtml.TagHandler {

    private static final int code_color = Color.parseColor("#F0F0F0");
    private static final int h1_color = Color.parseColor("#333333");


    private Stack<Integer> stack;
    private Stack<Boolean> list;
    private XRichText xRichText;
    private int index = 0;

    public HtmlTagHandler(XRichText xRichText) {
        stack = new Stack<>();
        list = new Stack<>();
        this.xRichText=xRichText;
    }

    @Override
    public void handleTag(boolean opening, String tag, Attributes attributes, Editable output, XMLReader xmlReader) {
        if (opening) {
            startTag(tag,output, xmlReader);
            stack.push(output.length());
        } else {
            int len;
            if (stack.isEmpty()) {
                len = 0;
            } else {
                len = stack.pop();
            }
            reallyHandler(len, output.length(), tag.toLowerCase(), attributes, output, xmlReader);
        }
    }

    @Override
    public void handleAttributes(String tag, Attributes attributes) {
        switch (tag.toLowerCase()){
            case "span":
//                InvokeEventContainer.getInstance().event_OnSpan.invoke(attributes);
                xRichText.onDealSpan(attributes);
                break;
        }
    }

    @Override
    public void handleClick(int start, int length, Editable output) {
//        InvokeEventContainer.getInstance().event_OnSpanClick.invoke(start,length,output);
        xRichText.onSpanClick(start,length,output);
    }

    @Override
    public void handleRobotJumpMessageClick(int start, int length, Editable output) {
        xRichText.onRobotJumpMessage(start,length,output);
    }

    @SuppressWarnings("unused")
    private void startTag(String tag, Editable out, XMLReader reader) {
        switch (tag.toLowerCase()) {
            case "ol":
                list.push(false);
                out.append('\n');
                break;
            case "dl":
                out.append('\n');
                break;
            case "dt":
                out.append('\n');
                break;
            case "pre":
                break;
        }
    }

    @SuppressWarnings("unused")
    private void reallyHandler(int start, int end, String tag,Attributes attributes, Editable out, XMLReader reader) {
        switch (tag.toLowerCase()) {
            case "ol":
                out.append('\n');
                index=0;
                if (!list.isEmpty()) {
                    list.pop();
                }
                break;
            case "li":
                int i;
                i = ++index;
                out.append('\n');
                out.append(String.valueOf(i));
                out.append(". ");
                break;
            case "dl":
            case "ut":
            case "dd":
                out.append('\n');
                break;
            default:
                return;
        }
        BulletSpan bulletSpan = new BulletSpan(40, h1_color);
        out.setSpan(bulletSpan, start, out.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

}
