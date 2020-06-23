package cn.udesk.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.udesk.R;


public class UdeskExpandableLayout extends LinearLayout {

	private Context mContext;
	private RelativeLayout mContentView;
	int mContentHeight = 0;
	private DropDownAnim animationDown;
	private DropDownAnim animationUp;
	private LayoutInflater mInflater;
	private View mExpandView;
	private TextView txt;
	
	
	public UdeskExpandableLayout(Context context) {
		super(context);
		this.mContext = context;
		init(mContext);
	}

	public UdeskExpandableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init(mContext);
	}

	private void init(Context mContext) {
		try {
			mInflater = LayoutInflater.from(mContext);
			mExpandView= mInflater.inflate(R.layout.udesk_expandlayout_xml, this);
			this.mContentView = (RelativeLayout) mExpandView.findViewById(R.id.expand_value);
			mContentView.setVisibility(View.GONE);
			txt = (TextView) mExpandView.findViewById(R.id.text_context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		try {
			if (this.mContentHeight == 0) {
				this.mContentView.measure(widthMeasureSpec, 0);
				this.mContentHeight = this.mContentView.getMeasuredHeight();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void startAnimation(final boolean isLine){
		try {
			clearAnimation();
			if (animationDown == null) {
				animationDown = new DropDownAnim(mContentView,txt,
						mContentHeight, true);
				animationDown.setDuration(1000);
			}
			animationDown.setLine(isLine);
			startAnimation(animationDown);
			postDelayed(new Runnable() {

				@Override
				public void run() {
					stopAnimation();
				}
			}, 1500);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void startNetAnimation(){
		try {
			clearAnimation();
			if (animationDown == null) {
				animationDown = new DropDownAnim(mContentView,txt,
						mContentHeight, true);
				animationDown.setDuration(1000);
			}
			animationDown.setNetLine();
			startAnimation(animationDown);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public void stopAnimation(){

		try {
			clearAnimation();
			if (animationUp == null) {
				animationUp = new DropDownAnim(mContentView,txt,
						mContentHeight, false);
				animationUp.setDuration(200);
			}
			startAnimation(animationUp);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	class DropDownAnim extends Animation {
		private int targetHeight;
		private View view;
		private boolean down;
		private TextView txtView;

		public DropDownAnim(View targetview, TextView txtView,int vieweight,
				boolean isdown) {
			this.view = targetview;
			this.txtView = txtView;
			this.targetHeight = vieweight;
			this.down = isdown;
			
		}
		
		public void setLine(boolean isLine) {
			try {
				txtView.setTextColor(mContext.getResources().getColor(R.color.udesk_color_bg_white));
				if(isLine){
                    this.view.setBackgroundColor(Color.rgb(65, 207, 124));
                    txtView.setText(mContext.getString(R.string.udesk_service_line));
                }else{
                    this.view.setBackgroundColor(Color.rgb(233, 93, 79));
                    txtView.setText(mContext.getString(R.string.udesk_service_offline));
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void setNetLine() {
			try {
				this.view.setBackgroundColor(mContext.getResources().getColor(R.color.udesk_color_FFDFDF));
				txtView.setText(mContext.getString(R.string.udesk_no_network));
				txtView.setTextColor(mContext.getResources().getColor(R.color.udesk_color_eb212121));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			try {
				int newHeight;
				if (down) {
                    newHeight = (int) (targetHeight * interpolatedTime);
                } else {
                    newHeight = (int) (targetHeight * (1 - interpolatedTime));
                }
				view.getLayoutParams().height = newHeight;
				view.requestLayout();
				if (view.getVisibility() == View.GONE) {
                    view.setVisibility(View.VISIBLE);
                }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void initialize(int width, int height, int parentWidth,
				int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}
}
