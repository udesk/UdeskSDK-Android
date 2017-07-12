package cn.udesk.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.udesk.R;
import cn.udesk.UdeskUtil;


public class UDPullGetMoreListView extends ListView implements OnScrollListener {

    private final static int RELEASE_To_REFRESH = 0;
    private final static int PULL_To_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;

    // 实际的padding的距离与界面上偏移距离的比例
    private final static int RATIO = 3;

    private LayoutInflater inflater;

    private LinearLayout llheader;

    private TextView tvTips;
    private ProgressBar pbLoading;
    
    // 用于保证startY的值在一个完整的touch事件中只被记录一次
    private boolean isRecored;

    private int headContentHeight;

    private int startY;
    private int firstItemIndex;
    private int state;
    private boolean isBack;
    private OnRefreshListener refreshListener;

    private boolean isRefreshable;
    private boolean isPush;


    public UDPullGetMoreListView(Context context) {
        super(context);
        init(context);
    }

    public UDPullGetMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        try {
            inflater = LayoutInflater.from(context);
            llheader = (LinearLayout) inflater.inflate(R.layout.udesk_layout_get_more, null);
            pbLoading = (ProgressBar) llheader.findViewById(R.id.udesk_get_more_progress);
            tvTips = (TextView) llheader.findViewById(R.id.udesk_get_more_tips);

            measureView(llheader);
            headContentHeight = llheader.getMeasuredHeight();

            llheader.setPadding(0, -1 * headContentHeight, 0, 0);
            llheader.invalidate();

            addHeaderView(llheader, null, false);

            setOnScrollListener(this);

            state = DONE;
            isRefreshable = false;
            isPush = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3) {
        firstItemIndex = firstVisiableItem;
        if(firstItemIndex == 1 && !isPush) {
            setSelection(0);
        }
    }


    public void onScrollStateChanged(AbsListView arg0, int arg1) {

        switch(arg1){
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE://空闲状态
                UdeskUtil.imageResume();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING://滚动状态
                UdeskUtil.imagePause();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL://触摸后滚动
                break;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        try {
            if (isRefreshable) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (firstItemIndex == 0 && !isRecored) {
                        isRecored = true;
                        isPush = true;
                        startY = (int) event.getY();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (state != REFRESHING && state != LOADING) {
                        if (state == DONE) {
                            // 什么都不做
                        }
                        if (state == PULL_To_REFRESH) {
                            state = DONE;
                            changeHeaderViewByState();

                        }
                        if (state == RELEASE_To_REFRESH) {
                            state = REFRESHING;
                            changeHeaderViewByState();
                            onRefresh();

                        }
                    }

                    isRecored = false;
                    isBack = false;

                    break;

                case MotionEvent.ACTION_MOVE:
                    int tempY = (int) event.getY();

                    if (!isRecored && firstItemIndex == 0) {
                        isRecored = true;
                        startY = tempY;
                    }

                    if (state != REFRESHING && isRecored && state != LOADING) {

                        // 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

                        // 可以松手去刷新了
                        if (state == RELEASE_To_REFRESH) {

                            setSelection(0);

                            // 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                            if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0) {
                                state = PULL_To_REFRESH;
                                changeHeaderViewByState();

                            }
                            // 一下子推到顶了
                            else if (tempY - startY <= 0) {
                                state = DONE;
                                changeHeaderViewByState();

                            }
                            // 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
                            else {
                                // 不用进行特别的操作，只用更新paddingTop的值就行了
                            }
                        }
                        // 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
                        if (state == PULL_To_REFRESH) {

                            setSelection(0);

                            // 下拉到可以进入RELEASE_TO_REFRESH的状态
                            if ((tempY - startY) / RATIO >= headContentHeight) {
                                state = RELEASE_To_REFRESH;
                                isBack = true;
                                changeHeaderViewByState();
                            }
                            // 上推到顶了
                            else if (tempY - startY <= 0) {
                                state = DONE;
                                changeHeaderViewByState();
                                isPush = false;
                            }
                        }

                        // done状态下
                        if (state == DONE) {
                            if (tempY - startY > 0) {
                                state = PULL_To_REFRESH;
                                changeHeaderViewByState();
                            }
                        }

                        // 更新headView的size
                        if (state == PULL_To_REFRESH) {
                            llheader.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);

                        }

                        // 更新headView的paddingTop
                        if (state == RELEASE_To_REFRESH) {
                            llheader.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
                        }

                    }

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onTouchEvent(event);
    }

    // 当状态改变时候，调用该方法，以更新界面
    private void changeHeaderViewByState() {
        try {
            switch (state) {
            case RELEASE_To_REFRESH:
                pbLoading.setVisibility(View.GONE);
                tvTips.setVisibility(View.VISIBLE);
                tvTips.setText(getResources().getString(R.string.udesk_release_to_get_more));

                break;

            case PULL_To_REFRESH:
                pbLoading.setVisibility(View.GONE);
                tvTips.setVisibility(View.VISIBLE);
                // 是由RELEASE_To_REFRESH状态转变来的
                if (isBack) {
                    isBack = false;
                }
                tvTips.setText(getResources().getString(R.string.udesk_get_more_history));
                break;

            case REFRESHING:

                pbLoading.setVisibility(View.VISIBLE);
                tvTips.setText(getResources().getString(R.string.udesk_loading_more));
                llheader.setPadding(0, 0, 0, 0);

                break;

            case DONE:

                pbLoading.setVisibility(View.GONE);
                tvTips.setText(getResources().getString(R.string.udesk_get_more_history));
                llheader.setPadding(0, -1 * headContentHeight, 0, 0);

                break;
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        try {
            this.refreshListener = refreshListener;
            isRefreshable = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public interface OnRefreshListener {
        void onRefresh();
    }


    public void onRefreshComplete() {
        state = DONE;
        changeHeaderViewByState();
        invalidateViews();
    }


    private void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }


    public void clickToRefresh() {
        state = REFRESHING;
        changeHeaderViewByState();
    }


    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
	public int pointToPosition(int x, int y) {
    	mX =x;
    	mY =y;
		return super.pointToPosition(x, y);
	}
    private int mX,mY;
    
    public int[] getRealPosition(){
    	return new int[]{mX,mY};
    }

}