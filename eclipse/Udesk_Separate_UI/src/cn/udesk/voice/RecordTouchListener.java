package cn.udesk.voice;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import cn.udesk.R;

public class RecordTouchListener implements View.OnTouchListener{
	RecordStateCallback mRecordStateCallback;
	private int OFFSET;
	private boolean mWillCancel;

	public RecordTouchListener(RecordStateCallback callback , Context context) {
		mRecordStateCallback = callback;
		OFFSET = context.getResources()
				.getDimensionPixelOffset(R.dimen.udesk_im_move_to_cancel_offset);
	}

	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			return true;
		case MotionEvent.ACTION_MOVE:
			int Y = (int) event.getY();
			boolean willCancle = (Y < 0 && Math.abs(Y) >= OFFSET);
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
		return true;
	}
}
