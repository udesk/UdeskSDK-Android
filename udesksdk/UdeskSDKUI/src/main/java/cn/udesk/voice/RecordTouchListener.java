package cn.udesk.voice;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;


public class RecordTouchListener implements View.OnTouchListener{
	RecordStateCallback mRecordStateCallback;
	private boolean mWillCancel = false;

	private View cancleView;
	private int cancleViewLeft;//消除控件距离左边的位置
	private int cancleViewTop;//消除控件所在屏幕y上的区间

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
	}

	public boolean onTouch(View view, MotionEvent event) {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
