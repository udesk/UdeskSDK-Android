package cn.udesk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

import cn.udesk.R;

public class HorVoiceView extends View {

	private Paint paint;
	private int color;
	private float lineHeight = 4;
	private float maxLineheight;
	private float lineWidth;
	private float textSize;
	private String text = "0:00";
	private int textColor;
	private int milliSeconds;
	private boolean isStart = false;

	
	LinkedList<Integer> list = new LinkedList<Integer>();
	
	public HorVoiceView(Context context) {
		super(context);
	}
	
	public HorVoiceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public HorVoiceView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		for(int i = 0 ; i < 10 ; i++ ){
			list.add(1);
		}
		paint = new Paint();
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.UdeskVoiceView);
		color = mTypedArray.getColor(R.styleable.UdeskVoiceView_udeskvoiceLineColor, Color.BLACK);
		lineWidth = mTypedArray.getDimension(R.styleable.UdeskVoiceView_udeskvoiceLineWidth, 35);
		lineHeight = mTypedArray.getDimension(R.styleable.UdeskVoiceView_udeskvoiceLineHeight, 4);
		maxLineheight = mTypedArray.getDimension(R.styleable.UdeskVoiceView_udeskvoiceLineHeight, 32);
		textSize = mTypedArray.getDimension(R.styleable.UdeskVoiceView_udeskvoiceTextSize, 45);
		textColor = mTypedArray.getColor(R.styleable.UdeskVoiceView_udeskvoiceTextColor, Color.BLACK);
		mTypedArray.recycle();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		try {
			int widthcentre = getWidth() / 2;
			int heightcentre = getHeight() / 2;

			paint.setStrokeWidth(0);
			paint.setColor(textColor);
			paint.setTextSize(textSize);
			paint.setTypeface(Typeface.DEFAULT);
			float textWidth = paint.measureText(text);
			canvas.drawText(text, widthcentre - textWidth / 2, heightcentre - (paint.ascent() + paint.descent())/2, paint);

			paint.setColor(color);
			paint.setStyle(Paint.Style.FILL);
			paint.setStrokeWidth(lineWidth);
			paint.setAntiAlias(true);
			for(int i = 0 ; i < 10 ; i++){
                RectF rect = new RectF(widthcentre + 2 * i * lineHeight + textWidth / 2 + lineHeight ,
                        heightcentre - list.get(i) * lineHeight / 2,
                        widthcentre  + 2 * i * lineHeight +2 * lineHeight + textWidth / 2,
                        heightcentre + list.get(i) * lineHeight /2);
                RectF rect2 = new RectF(widthcentre - (2 * i * lineHeight +2* lineHeight +  textWidth / 2),
                        heightcentre - list.get(i) * lineHeight / 2,
                        widthcentre  -( 2 * i * lineHeight + textWidth / 2 + lineHeight),
                        heightcentre + list.get(i) * lineHeight / 2);
                canvas.drawRect(rect, paint);
                canvas.drawRect(rect2, paint);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDraw(canvas);
	}
	
	public synchronized void addElement(Integer height){
		try {
			for (int i = 0; i <= height / 30 ; i ++) {
                list.remove(9 - i);
                list.add(i, (height / 20  - i) < 1 ? 1 : height / 10 - i);
            }
			postInvalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void setText(String text) {
		this.text = text;
		postInvalidate();
	}

	public synchronized void startRecording(UdeskTimeCallback callback){
		try {
			videoTime = 0;
			time=60;
			mCallback = callback;
			mTimeHandler.removeMessages(HandleTypeTimeOver);
			mTimeHandler.sendEmptyMessage(HandleTypeTimeOver);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void stopRecording(){
		mTimeHandler.removeMessages(HandleTypeTimeOver);
	}

	public interface UdeskTimeCallback{
		void onTimeOver();
	}

	private int videoTime = 0;
	private int time=60;
	private final int HandleTypeTimeOver = 0;
	private final int HandleTypeShowTooShort = 1;
	private UdeskTimeCallback mCallback;
	Handler mTimeHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
                    case HandleTypeTimeOver:
                        time--;
                        videoTime ++ ;
                        if(time<=0){
                            if(mCallback!=null){
                                mTimeHandler.removeMessages(HandleTypeTimeOver);
                                mCallback.onTimeOver();
                            }
                        }else{
                            this.sendEmptyMessageDelayed(HandleTypeTimeOver, 1000);//1秒更新一次
                            text = videoTime < 10 ? "0:0"+ videoTime: "0:"+videoTime + "";
                            setText(text);
                        }
                        break;

                    case HandleTypeShowTooShort:

                        break;
                    default:
                        break;
                }
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	};

}



































