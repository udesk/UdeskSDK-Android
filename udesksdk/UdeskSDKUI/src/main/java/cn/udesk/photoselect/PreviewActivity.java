package cn.udesk.photoselect;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.photoselect.adapter.PreviewPhotosAdapter;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/8.
 */

public class PreviewActivity extends AppCompatActivity implements PreviewPhotosAdapter.OnClickListener,
        View.OnClickListener, PreviewFragment.OnPreviewFragmentClickListener {


    private static final int UI_ANIMATION_DELAY = 300;

    View back, orginView, selector_select_view;
    private CheckBox orginCheckBox, selectorCheckBox;
    private RelativeLayout mBottomBar;
    private FrameLayout mToolBar;
    //底部选中的列表
    private FrameLayout flFragment;
    TextView tv_number, sendto;
    private RecyclerView rvPhotos;
    PreviewPhotosAdapter adapter;
    private PagerSnapHelper snapHelper;
    private LinearLayoutManager linearLayoutManager;
    private PreviewFragment previewFragment;
    int disPlayWidth;
    int disPlayHeghit;
    private int index;//在所有文件中 选中的位置
    private int lastPosition = 0;//记录recyclerView最后一次角标位置

    private boolean isPreviewAll = true; //标记预览是全部还是选中开关
    private boolean isDestroyed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            UdeskUtil.setOrientation(this);
            WindowManager wm1 = getWindowManager();
            disPlayWidth = wm1.getDefaultDisplay().getWidth();
            disPlayHeghit = wm1.getDefaultDisplay().getHeight();
            setContentView(R.layout.udesk_activity_preview);
            hideActionBar();
            initData();
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private void initData() {
        Intent intent = getIntent();
        index = intent.getIntExtra(UdeskConst.PREVIEW_PHOTO_INDEX, 0);
        isPreviewAll = intent.getBooleanExtra(UdeskConst.PREVIEW_PHOTO_IS_ALL, true);
        lastPosition = index;
    }

    private void initView() {
        try {
            mToolBar = (FrameLayout) findViewById(R.id.m_top_bar_layout);
            flFragment = (FrameLayout) findViewById(R.id.fl_fragment);
            mBottomBar = (RelativeLayout) findViewById(R.id.m_bottom_bar);
            back = findViewById(R.id.udesk_back_linear);
            back.setOnClickListener(this);
            tv_number = (TextView) findViewById(R.id.tv_number);
            sendto = (TextView) findViewById(R.id.udesk_titlebar_right);
            sendto.setOnClickListener(this);
            orginCheckBox = (CheckBox) findViewById(R.id.udesk_checkbox);
            orginView = findViewById(R.id.original_select_view);
            orginView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    orginCheckBox.setChecked(!orginCheckBox.isChecked());
                }
            });

            selector_select_view = findViewById(R.id.selector_select_view);
            selectorCheckBox = (CheckBox) findViewById(R.id.udesk_select_checkbox);
            selector_select_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    boolean isChecked = selectorCheckBox.isChecked();
                    if (isPreviewAll) {
                        if (isChecked) {
                            SelectResult.allLocalMedia.get(lastPosition).setSelected(false);
                            SelectResult.removePhoto(SelectResult.allLocalMedia.get(lastPosition));
                        } else {
                            if (SelectResult.count() >= UdeskConst.count){
                                Toast.makeText(getApplicationContext(), getString(R.string.udesk_max_tips), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SelectResult.allLocalMedia.get(lastPosition).setSelected(true);
                            SelectResult.addPhoto(SelectResult.allLocalMedia.get(lastPosition));
                        }
                    } else {
                        if (isChecked) {
                            SelectResult.selectLocalMedia.get(lastPosition).setSelected(false);
                            SelectResult.removePhoto(SelectResult.selectLocalMedia.get(lastPosition));
                        } else {
                            if (SelectResult.count() >= UdeskConst.count){
                                Toast.makeText(getApplicationContext(), getString(R.string.udesk_max_tips), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SelectResult.selectLocalMedia.get(lastPosition).setSelected(true);
                            SelectResult.addPhoto(SelectResult.selectLocalMedia.get(lastPosition));
                        }
                    }
                    previewFragment.setSelectedPosition(-1);
                    toggleSelector();
                    selectorCheckBox.setChecked(!isChecked);
                }
            });
            if (isPreviewAll) {
                adapter = new PreviewPhotosAdapter(this, SelectResult.allLocalMedia, this, disPlayWidth, disPlayHeghit - UdeskUtil.dip2px(getApplicationContext(), 100));
            } else {
                adapter = new PreviewPhotosAdapter(this, SelectResult.selectLocalMedia, this, disPlayWidth, disPlayHeghit - UdeskUtil.dip2px(getApplicationContext(), 100));
            }
            rvPhotos = (RecyclerView) findViewById(R.id.udesk_rv_photos);
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            rvPhotos.setLayoutManager(linearLayoutManager);
            rvPhotos.setAdapter(adapter);
            rvPhotos.scrollToPosition(index);
            snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(rvPhotos);
            rvPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    int leftViewPosition = snapHelper.findTargetSnapPosition(linearLayoutManager, 1, rvPhotos.getHeight() / 2);
                    int rightViewPosition = snapHelper.findTargetSnapPosition(linearLayoutManager, rvPhotos.getWidth() - 1, rvPhotos.getHeight() / 2);
                    if (leftViewPosition == rightViewPosition) {
                        if (lastPosition == leftViewPosition - 1) {
                            return;
                        }
                        lastPosition = leftViewPosition - 1;
                        setIndexNum(leftViewPosition);
                        previewFragment.setSelectedPosition(-1);
                        toggleSelector();
                    }
                }
            });
            setViewEneable();
            setIndexNum(index + 1);
            addFragment();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (previewFragment != null){
            toggleSelector();
        }
    }

    @Override
    public void onPhotoClick() {

        try {
            toggle(View.VISIBLE == mToolBar.getVisibility());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPhotoScaleChanged() {
        try {
            if (View.VISIBLE == mToolBar.getVisibility()) {
                hide();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            int id = view.getId();
            if (id == R.id.udesk_back_linear) {
                finishActivity(false);
            } else if (id == R.id.udesk_titlebar_right) {
                finishActivity(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setViewEneable() {
        try {
            if (SelectResult.isEmpty()) {
                sendto.setText(R.string.udesk_send_message);
                flFragment.setVisibility(View.GONE);
                sendto.setEnabled(false);
                sendto.setBackgroundColor(getResources().getColor(R.color.udesk_color_8045c01a));
            } else {
                flFragment.setVisibility(View.VISIBLE);
                sendto.setEnabled(true);
                sendto.setText(getString(R.string.udesk_selector_action_done_photos, SelectResult.count(), UdeskConst.count));
                sendto.setBackgroundColor(getResources().getColor(R.color.udesk_color_45c01a));
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setIndexNum(int postion) {
        try {
            if (isPreviewAll) {
                tv_number.setText(getString(R.string.udesk_selector_photo_index_num, postion, SelectResult.allLocalMedia.size()));
            } else {
                tv_number.setText(getString(R.string.udesk_selector_photo_index_num, postion, SelectResult.selectLocalMedia.size()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void toggle(boolean isVisible) {
        if (isVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        try {
            AlphaAnimation hideAnimation = new AlphaAnimation(1.0f, 0.0f);
            hideAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mBottomBar.setVisibility(View.GONE);
                    mToolBar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            hideAnimation.setDuration(UI_ANIMATION_DELAY);
            mBottomBar.startAnimation(hideAnimation);
            mToolBar.startAnimation(hideAnimation);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void toggleSelector() {
        try {
            if (isPreviewAll) {
                if (SelectResult.allLocalMedia.get(lastPosition).isSelected()) {
                    selectorCheckBox.setChecked(true);
                    if (!SelectResult.photos.isEmpty()) {
                        for (int i = 0; i < SelectResult.count(); i++) {
                            if (SelectResult.allLocalMedia.get(lastPosition).getPath().equals(SelectResult.getPhotoPath(i))) {
                                previewFragment.setSelectedPosition(i);
                                break;
                            }
                        }
                    }
                } else {
                    selectorCheckBox.setChecked(false);
                }
            } else {
                if (SelectResult.selectLocalMedia.get(lastPosition).isSelected()) {
                    selectorCheckBox.setChecked(true);
                    if (!SelectResult.photos.isEmpty()) {
                        for (int i = 0; i < SelectResult.count(); i++) {
                            if (SelectResult.selectLocalMedia.get(lastPosition).getPath().equals(SelectResult.getPhotoPath(i))) {
                                previewFragment.setSelectedPosition(i);
                                break;
                            }
                        }
                    }
                } else {
                    selectorCheckBox.setChecked(false);
                }
            }

            previewFragment.notifyDataSetChanged();
            setViewEneable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void show() {
        try {
            mBottomBar.setVisibility(View.VISIBLE);
            mToolBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFragment() {
        try {
            removeFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            previewFragment = new PreviewFragment();
            transaction.replace(R.id.fl_fragment, previewFragment);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeFragment() {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fl_fragment);
            if (fragment != null) {
                transaction.remove(fragment);
            }
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPreviewPhotoClick(int position) {
        try {
            String path = SelectResult.getPhotoPath(position);
            if (isPreviewAll) {
                for (int i = 0; i < SelectResult.allLocalMedia.size(); i++) {
                    if (TextUtils.equals(path, SelectResult.allLocalMedia.get(i).getPath())) {
                        rvPhotos.scrollToPosition(i);
                        lastPosition = i;
                        setIndexNum(lastPosition + 1);
                        previewFragment.setSelectedPosition(position);
                        previewFragment.notifyDataSetChanged();
                        selectorCheckBox.setChecked(true);
                        return;
                    }
                }
            } else {
                for (int i = 0; i < SelectResult.selectLocalMedia.size(); i++) {
                    if (TextUtils.equals(path, SelectResult.selectLocalMedia.get(i).getPath())) {
                        rvPhotos.scrollToPosition(i);
                        lastPosition = i;
                        setIndexNum(lastPosition + 1);
                        previewFragment.setSelectedPosition(position);
                        previewFragment.notifyDataSetChanged();
                        selectorCheckBox.setChecked(true);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        finishActivity(false);
    }

    private void finishActivity(boolean isSend) {
        try {
            Intent mIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putBoolean(UdeskConst.SEND_PHOTOS_IS_ORIGIN, orginCheckBox.isChecked());
            bundle.putBoolean(UdeskConst.IS_SEND, isSend);
            mIntent.putExtra(UdeskConst.SEND_BUNDLE, bundle);
            PreviewActivity.this.setResult(Activity.RESULT_OK, mIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            cleanSource();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cleanSource();
        super.onDestroy();
    }

    private void cleanSource() {
        try {
            if (isDestroyed) {
                return;
            }
            // 回收资源
            isDestroyed = true;
            SelectResult.selectLocalMedia.clear();
            SelectResult.allLocalMedia.clear();
            flFragment.removeAllViews();
            rvPhotos.removeAllViews();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
