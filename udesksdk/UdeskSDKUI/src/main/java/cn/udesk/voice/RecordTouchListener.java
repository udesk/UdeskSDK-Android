package cn.udesk.voice;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import cn.udesk.R;

public class RecordTouchListener implements View.OnTouchListener{
	RecordStateCallback mRecordStateCallback;
	private boolean mWillCancel = false;

	private View cancleView;
	private int cancleViewLeft;//消除控件距离左边的位置
	private int cancleViewTop,cancleViewBottmo;//消除控件所在屏幕y上的区间


//	public RecordTouchListener(RecordStateCallback callback , Context context) {
//		mRecordStateCallback = callback;
//
//	}

//	int[] location = new  int[2] ;
//	int height = audioCancle.getHeight();
//	audioCancle.getLocationInWindow(location); //获取在当前窗口内的绝对坐标
//	int x = location[0];
//	int y = location[1];
//	audioPop.setOnTouchListener(new RecordTouchListener(this,
//														UdeskChatActivity.this,x,y,y+height));

   private int getCancleViewLeft(){
	   if(cancleViewLeft == 0){
		   int[] location = new  int[2] ;
		   cancleView.getLocationInWindow(location);
		   cancleViewLeft = location[0];
	   }
	  return cancleViewLeft;
   }

	private  int getCancleViewTop(){
		if (cancleViewTop == 0){
			int[] location = new  int[2] ;
			cancleView.getLocationInWindow(location);
			cancleViewTop = location[1];
		}
		return cancleViewTop;
	}

	private int getCancleViewBottmo(){
		return cancleView.getHeight()  + getCancleViewTop();
	}

	public RecordTouchListener(RecordStateCallback callback , Context context,View audioCancle) {
		mRecordStateCallback = callback;
		cancleView = audioCancle;
//		this.cancleViewLeft = x;
//		this.cancleViewTop = y1;
//		this.cancleViewBottmo = y2;

	}

	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return true;
		case MotionEvent.ACTION_MOVE:
			int eventX = (int) event.getRawX();
			int eventY = (int) event.getRawY();
			boolean willCancle = ( eventX > getCancleViewLeft() && eventY > getCancleViewTop() && eventY < getCancleViewBottmo());
			if (willCancle != mWillCancel) {
				if (!willCancle) {
					if (mRecordStateCallback != null) {
						mRecordStateCallback.readyToContinue();
					}
				} else {
					if (mRecordStateCallback != null) {
						mRecordStateCallback.readyToCancelRecord();
					}
				}

				mWillCancel = willCancle;
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mWillCancel) {
				mRecordStateCallback.doCancelRecord();
			} else {
				mRecordStateCallback.endRecord();
			}
			break;

		}
		return false;
	}
}
