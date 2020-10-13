package cn.udesk.rich;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import org.xml.sax.Attributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.model.RobotJumpMessageModel;
import cn.udesk.model.SpanModel;
import udesk.core.UdeskConst;


public class XRichText extends AppCompatTextView implements ViewTreeObserver.OnGlobalLayoutListener {
    private static Pattern PATTERN_IMG_TAG = Pattern.compile("<(img|IMG)(.*?)>");
    private static Pattern PATTERN_IMG_WIDTH = Pattern.compile("(width|WIDTH)=\"(.*?)\"");
    private static Pattern PATTERN_IMG_HEIGHT = Pattern.compile("(height|HEIGHT)=\"(.*?)\"");
    private static Pattern PATTERN_IMG_SRC = Pattern.compile("(src|SRC)=\"(.*?)\"");

    private HashMap<String, ImageHolder> imageHolderMap = new HashMap<String, ImageHolder>();

    Callback callback;
    ImageLoader downLoader;
    //控件的宽高
    private int richWidth;
    private boolean isInit = true;
    LocalImageGetter imgGetter;

    private static Object lock = new Object();
    private Attributes attributes;
    private UrlDrawable urlDrawable;
    //图片的最大宽度
    private int imageMaxWidth=310;
    private Context mContext;
    public XRichText(Context context) {
        super(context);
    }

    public XRichText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XRichText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

//    public void bind(){
//        InvokeEventContainer.getInstance().event_OnSpan.bind(this,"onDealSpan");
//        InvokeEventContainer.getInstance().event_OnSpanClick.bind(this,"onSpanClick");
//    }
//    public void unBind(){
//        InvokeEventContainer.getInstance().event_OnSpan.unBind(this);
//        InvokeEventContainer.getInstance().event_OnSpanClick.unBind(this);
//    }

    public void onSpanClick(Integer start, Integer length, Editable out) {
        attributes=getAttributes();
        final SpanModel spanModel= new SpanModel();
        spanModel.setContent(out.toString().substring(start,length));
        if (attributes != null) {
            String data = attributes.getValue("", "data-type");
            spanModel.setType(data);
            data = attributes.getValue("", "class");
            spanModel.setClassName(data);
            data = attributes.getValue("", "data-id");
            spanModel.setDataId(data);
            data = attributes.getValue("", "data-robotid");
            spanModel.setRobotId(data);
        }
        out.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                if (((XRichText)widget).callback != null && spanModel != null) {
                    callback(((XRichText)widget).callback);
                    callback.onStepClick(spanModel);
                }
            }
        }, start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    public void onRobotJumpMessage(Integer start, Integer length, Editable out) {
        attributes=getAttributes();
        final RobotJumpMessageModel robotJumpMessageModel= new RobotJumpMessageModel();
        robotJumpMessageModel.setContent(out.toString().substring(start,length));
        if (attributes != null) {
            String data = attributes.getValue("", "data-message-type");
            robotJumpMessageModel.setMessageType(data);
            data = attributes.getValue("", "data-content");
            robotJumpMessageModel.setContent(data);
            data = attributes.getValue("", "data-robotid");
            robotJumpMessageModel.setRobotId(data);
            data = attributes.getValue("", "data-replace-type");
            robotJumpMessageModel.setReplaceType(data);
        }
        out.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {

                if (((XRichText)widget).callback != null && robotJumpMessageModel != null) {
                    callback(((XRichText)widget).callback);
                    callback.onRobotJumpMessage(robotJumpMessageModel);
                }
            }
        }, start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void onDealSpan(Attributes attributes) {
        this.attributes=attributes;
    }
    private Attributes getAttributes(){
        return attributes;
    }

    /**
     * 可以外面动态控制图片的宽度最大值
     * @param width
     */
    public void setImageMaxWidth(int width){
        this.imageMaxWidth=width;
    }
    public void text(Context context, String text) {
        mContext = context;
        richWidth=UdeskUtil.dip2px(context,imageMaxWidth)-getPaddingLeft()-getPaddingRight();
        urlDrawable = new UrlDrawable();
        queryImgs(text);
        if (imgGetter == null) {
            imgGetter = new LocalImageGetter(this);
        }
        CharSequence charSequence = UdeskHtml.fromHtml(context, text, imgGetter, new HtmlTagHandler(this));
        if (charSequence.toString().endsWith("\n\n")){
            charSequence=charSequence.subSequence(0,charSequence.length()-2);
        }
        SpannableStringBuilder builder;
        if (charSequence instanceof SpannableStringBuilder) {
            builder = (SpannableStringBuilder) charSequence;
        } else {
            builder = new SpannableStringBuilder(charSequence);
        }
        ImageSpan[] imgSpans = builder.getSpans(0, builder.length(), ImageSpan.class);
        final List<String> imgUrls = new ArrayList<String>();

        for (int i = 0, size = imgSpans.length; i < size; i++) {

            ImageSpan span = imgSpans[i];
            String path = span.getSource();
            int start = builder.getSpanStart(span);
            int end = builder.getSpanEnd(span);
            imgUrls.add(path);

            final int position = i;
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (((XRichText)widget).callback != null) {
                        callback(((XRichText)widget).callback);
                        callback.onImageClick((ArrayList<String>) imgUrls, position);
                    }
                }
            };
            ClickableSpan[] clickableSpans = builder.getSpans(start, end, ClickableSpan.class);
            if (clickableSpans != null && clickableSpans.length != 0) {
                for (ClickableSpan cs : clickableSpans) {
                    builder.removeSpan(cs);
                }
            }
            builder.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        URLSpan[] urls = builder.getSpans(0, builder.length(), URLSpan.class);
        for (URLSpan url : urls) {
            MyURLSpan myURLSpan = new MyURLSpan(url.getURL(), callback);
            int statr = builder.getSpanStart(url);
            int end = builder.getSpanEnd(url);
            builder.setSpan(myURLSpan, statr,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        Linkify.addLinks(builder,Linkify.PHONE_NUMBERS);
        setText(builder);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * 查询图片
     *
     * @param text
     */
    private void queryImgs(String text) {
        ImageHolder holder = null;
        Matcher imgMatcher, srcMatcher, wMatcher, hMatcher;
        int position = 0;

        imgMatcher = PATTERN_IMG_TAG.matcher(text);

        while (imgMatcher.find()) {
            String img = imgMatcher.group().trim();
            srcMatcher = PATTERN_IMG_SRC.matcher(img);

            String src = null;
            if (srcMatcher.find()) {
                src = getTextBetweenQuotation(srcMatcher.group().trim().substring(4));
            }
            if (TextUtils.isEmpty(src)) {
                continue;
            }

            holder = new ImageHolder(src, position);

            wMatcher = PATTERN_IMG_WIDTH.matcher(img);
            if (wMatcher.find()) {
                holder.setWidth(str2Int(getTextBetweenQuotation(wMatcher.group().trim().substring(6))));
            }

            hMatcher = PATTERN_IMG_HEIGHT.matcher(img);
            if (hMatcher.find()) {
                holder.setHeight(str2Int(getTextBetweenQuotation(hMatcher.group().trim().substring(6))));
            }

            imageHolderMap.put(holder.src, holder);
            position++;
            setWidthHeight(urlDrawable,holder);
        }
    }


    /**
     * 当获取到bitmap后调用此方法
     *
     * @param drawable
     * @param holder
     * @param rawBmp
     */
    public boolean fillBmp(UrlDrawable drawable, ImageHolder holder, Bitmap rawBmp) {
        Log.i("xxxx", "fillBmp begin 1");
        if (drawable == null || holder == null || rawBmp == null || richWidth <= 0) {
            return false;
        }
        Log.i("xxxx", "fillBmp begin 2");
        if (callback != null) {
            callback.onFix(holder);
        }
        Bitmap destBmp = holder.valid(rawBmp, richWidth);
        Log.i("xxxx", "fillBmp begin 3");
        if (destBmp == null) {
            return false;
        }
        Log.i("xxxx", "fillBmp end");
        wrapDrawable(drawable, holder, destBmp);
        return true;
    }

    /**
     * 获取图片的宽高 预设置图片的宽高
     *
     * @param drawable
     * @param holder
     */
    public boolean setWidthHeight(UrlDrawable drawable, ImageHolder holder) {
        if (holder.getWidth()>0&&holder.getHeight()>0){
            int[] imageWidthHeight = UdeskUtil.getImageWidthHeight(mContext,new int[]{holder.getWidth(),holder.getHeight()},richWidth);
            Bitmap rawBmp=Bitmap.createBitmap(imageWidthHeight[0],imageWidthHeight[1],Bitmap.Config.RGB_565);
            rawBmp.eraseColor(getResources().getColor(R.color.transparent));
            return fillBmp(drawable,holder,rawBmp);
        }else if (richWidth > 0 && UdeskUtil.fileIsExitByUrl(getContext(), UdeskConst.FileImg, holder.getSrc())) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(UdeskUtil.getPathByUrl(getContext(), UdeskConst.FileImg,
                    holder.getSrc()),options);
            if (options.outWidth>0&&options.outHeight>0){
                int[] imageWidthHeight = UdeskUtil.getImageWidthHeight(mContext,new int[]{options.outWidth,options.outHeight},richWidth);
                Bitmap rawBmp=Bitmap.createBitmap(imageWidthHeight[0],imageWidthHeight[1],Bitmap.Config.RGB_565);
                rawBmp.eraseColor(getResources().getColor(R.color.transparent));
                return fillBmp(drawable,holder,rawBmp);
            }
        }
        return false;
    }


    private void wrapDrawable(UrlDrawable drawable, ImageHolder holder, Bitmap destBmp) {
        if (destBmp.getWidth() > richWidth) {
            return;
        }

        Rect rect = null;
        int left = 0;
        switch (holder.style) {
            case LEFT:
                rect = new Rect(left, 0, destBmp.getWidth(), destBmp.getHeight());
                break;

            case CENTER:
                left = (richWidth - destBmp.getWidth()) / 2;
                if (left < 0) {
                    left = 0;
                }
                rect = new Rect(left, 0, left + destBmp.getWidth(), destBmp.getHeight());
                break;

            case RIGHT:
                left = richWidth - destBmp.getWidth();
                if (left < 0) {
                    left = 0;
                }
                rect = new Rect(left, 0, richWidth, destBmp.getHeight());
                break;
        }
        drawable.setBounds(0, 0, destBmp.getWidth(), destBmp.getHeight());
        drawable.setBitmap(destBmp, rect);
        setText(getText());
    }

    private static Pattern TextBetweenQuotationPattern = Pattern.compile("\"(.*?)\"");
    /**
     * 从双引号之间取出字符串
     */
    private static String getTextBetweenQuotation(String text) {
        Matcher matcher = TextBetweenQuotationPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 将String转化成Int
     *
     * @param text
     * @return
     */
    private int str2Int(String text) {
        int result = -1;
        try {
            result = Integer.valueOf(text);
        } catch (Exception e) {
        }
        return result;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    @TargetApi(16)
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    /**
     * 设置回调
     *
     * @param callback
     * @return
     */
    public XRichText callback(Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 设置自定义图片下载器
     *
     * @param loader
     * @return
     */
    public XRichText imageDownloader(ImageLoader loader) {
        this.downLoader = loader;
        return this;
    }

    @Override
    public void onGlobalLayout() {
//        synchronized (lock) {
//            if (isInit) {
//                Log.i("xxxx", "getWidth=" + getWidth());
//                richWidth = getWidth() - getPaddingLeft() - getPaddingRight();
//                if (richWidth > 0) {
//                    isInit = false;
//                    lock.notifyAll();
//                }
//            }
//        }

    }

    private class LocalImageGetter implements UdeskHtml.ImageGetter {

        public LocalImageGetter(View widget) {
            if (((XRichText)widget).callback != null) {
                callback(((XRichText)widget).callback);
            }
        }

        @Override
        public Drawable getDrawable(String source) {
            final ImageHolder holder = imageHolderMap.get(source);
            if (holder == null) {
                return null;
            }

            if (downLoader == null) {
                downLoader = new BaseImageLoader(getContext());
            }

            Runnable loadRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap rawBmp = null;
                        Log.i("xxxx", "richWidth = " + richWidth);
                        if (richWidth <= 0) {
                            try {
                                synchronized (lock) {
                                    lock.wait(2 * 1000);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i("xxxx", "richWidth = " + richWidth);
                        if (richWidth <= 0 ){
                            richWidth=UdeskUtil.dip2px(mContext,imageMaxWidth)-getPaddingLeft()-getPaddingRight();
                        }
                        if (richWidth > 0 && UdeskUtil.fileIsExitByUrl(getContext(), UdeskConst.FileImg, holder.getSrc())) {

                            Bitmap localBitMap = UdeskUtil.compressRatio(mContext,UdeskUtil.getPathByUrl(getContext(), UdeskConst.FileImg,
                                    holder.getSrc()),richWidth);
                            if (localBitMap != null ) {
                                int bitmapSize = UdeskUtil.getBitmapSize(localBitMap);
                                Log.i("xxxx", "bitmapsize = " + bitmapSize);
                                if (bitmapSize > 0){
                                    rawBmp = localBitMap;
                                }
                            }
                        }
                        if (rawBmp == null) {
                            rawBmp = downLoader.getBitmap(holder.getSrc(),richWidth);
                        }
                        final Bitmap successBmp = rawBmp;
                        if (rawBmp != null) {
                            ILoad loadImpl = new ILoad() {
                                @Override
                                public void afterLoad() {
                                    fillBmp(urlDrawable, holder, successBmp);
                                }
                            };
                            LoaderTask.getMainHandler().obtainMessage(LoaderTask.MSG_POST_RESULT, loadImpl).sendToTarget();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            };
            LoaderTask.getThreadPoolExecutor().execute(loadRunnable);

            return urlDrawable;
        }
    }


    public interface ILoad {
        void afterLoad();
    }


    public static class ImageHolder {
        private String src;
        private int position;
        private int width = -1;
        private int height = -1;
        private Style style = Style.CENTER;

        public ImageHolder(String src, int position) {
            this.src = src;
            this.position = position;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public Style getStyle() {
            return style;
        }

        public void setStyle(Style style) {
            this.style = style;
        }

        /**
         * 修正参数
         *
         * @param rawBmp
         * @return
         */
        public Bitmap valid(Bitmap rawBmp, int maxWidth) {
            if (rawBmp == null) {
                return null;
            }

            int reqWidth = width;
            int reqHeight = height;

            if (reqWidth == -1 || reqHeight == -1) {
                reqWidth = rawBmp.getWidth();
                reqHeight = rawBmp.getHeight();
            }

            if (reqWidth >= maxWidth) {
                float ratio = maxWidth * 1.0f / reqWidth;
                reqWidth = maxWidth;
                reqHeight = (int) (reqHeight * ratio);
            }

            width = reqWidth;
            height = reqHeight;
            return Kit.scaleImageTo(rawBmp, reqWidth, reqHeight, false);
        }
    }

    public enum Style {
        LEFT,       //左对齐
        CENTER,     //居中
        RIGHT       //右对齐
    }

    private static class Kit {

        private static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight, boolean needRecycle) {
            return scaleImage(org, (float) newWidth / org.getWidth(), (float) newHeight / org.getHeight(), needRecycle);
        }

        private static Bitmap scaleImage(Bitmap org, float scaleWidth, float scaleHeight, boolean needRecycle) {
            if (org == null) {
                return null;
            }

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            Bitmap bitmap = Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), matrix, true);

            if (needRecycle && !org.isRecycled()) {
                org.recycle();
            }
            return bitmap;
        }
    }

    public static class UrlDrawable extends BitmapDrawable {
        private Bitmap bitmap;
        private Rect rect;
        private Paint paint;

        public UrlDrawable() {
            paint = new Paint();
        }

        @Override
        public void draw(Canvas canvas) {
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, rect.left, rect.top, paint);
            }
        }

        public void setBitmap(Bitmap bitmap, Rect rect) {
            this.bitmap = bitmap;
            this.rect = rect;
        }
    }


    private class MyURLSpan extends ClickableSpan {
        private Callback callback;
        private String mUrl;

        MyURLSpan(String url, Callback callback) {
            mUrl = url;
            this.callback = callback;
        }

        @Override
        public void onClick(View widget) {
            if (((XRichText)widget).callback != null) {
                callback(((XRichText)widget).callback);
                this.callback=((XRichText)widget).callback;
                callback.onLinkClick(mUrl);
            }
        }
    }

    public interface Callback {
        void onImageClick(List<String> urlList, int position);

        boolean onLinkClick(String url);

        void onFix(ImageHolder holder);

        void onStepClick(SpanModel model);
        void onRobotJumpMessage(RobotJumpMessageModel model);

    }

}
