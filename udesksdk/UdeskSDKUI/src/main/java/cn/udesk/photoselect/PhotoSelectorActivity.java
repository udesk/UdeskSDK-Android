package cn.udesk.photoselect;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.photoselect.adapter.FolderAdapter;
import cn.udesk.photoselect.adapter.PhotosAdapter;
import cn.udesk.photoselect.decoration.GridSpacingItemDecoration;
import cn.udesk.photoselect.entity.LocalMedia;
import cn.udesk.photoselect.entity.LocalMediaFolder;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/6.
 */

public class PhotoSelectorActivity extends FragmentActivity implements View.OnClickListener,
        PhotosAdapter.OnPhotoSelectChangedListener, FolderAdapter.OnFolderClickListener {

    private View mBack, orginView, udesk_select_folder;
    private RecyclerView picture_recycler;
    private TextView folderTitile, sendto, pre;
    private CheckBox checkBox;
    LocalMedialLoader localMedialLoader;
    PhotosAdapter photosAdapter;
    private List<LocalMediaFolder> foldersList = new ArrayList<>();
    private List<LocalMedia> localMedias = new ArrayList<>();
    private boolean hasLoaded = false;
    private View udesk_rl_bottom;


    //展示文件夹
    private RelativeLayout rootViewAllItems;
    private RecyclerView allfolderRc;
    FolderAdapter folderAdapter;
    private AnimatorSet setHide;
    private AnimatorSet setShow;
    private boolean isDestroyed = false;

    //预览activity请求码
    public final int REQUEST_PREVIEW_ACTIVITY = 1;

    int disPlayWidth;
    int disPlayHeghit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            WindowManager wm1 = getWindowManager();
            UdeskUtil.setOrientation(this);
            disPlayWidth = wm1.getDefaultDisplay().getWidth();
            disPlayHeghit = wm1.getDefaultDisplay().getHeight();
            setContentView(R.layout.udesk_activity_select);
            if (!Fresco.hasBeenInitialized()) {
                  UdeskUtil.frescoInit(this);
            }
            initView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        try {
            mBack = findViewById(R.id.udesk_back_linear);
            mBack.setOnClickListener(this);
            folderTitile = (TextView) findViewById(R.id.picture_holder_name);
            udesk_select_folder = findViewById(R.id.udesk_select_folder);
            udesk_select_folder.setOnClickListener(this);
            sendto = (TextView) findViewById(R.id.udesk_titlebar_right);
            pre = (TextView) findViewById(R.id.udesk_pre);
            sendto.setOnClickListener(this);
            pre.setOnClickListener(this);
            checkBox = (CheckBox) findViewById(R.id.udesk_checkbox);
            orginView = findViewById(R.id.original_select_view);
            orginView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox.setChecked(!checkBox.isChecked());
                }
            });

            udesk_rl_bottom = findViewById(R.id.udesk_rl_bottom);


            picture_recycler = (RecyclerView) findViewById(R.id.udesk_picture_recycler);
            picture_recycler.setHasFixedSize(true);
            picture_recycler.addItemDecoration(new GridSpacingItemDecoration(4,
                    UdeskUtil.dip2px(this, 2), false));
            picture_recycler.setLayoutManager(new GridLayoutManager(this, 4));
            // 解决调用 notifyItemChanged 闪烁问题,取消默认动画
            ((SimpleItemAnimator) picture_recycler.getItemAnimator()).setSupportsChangeAnimations(false);
            photosAdapter = new PhotosAdapter(PhotoSelectorActivity.this, this, disPlayWidth, disPlayHeghit);
            picture_recycler.setAdapter(photosAdapter);

            rootViewAllItems = (RelativeLayout) findViewById(R.id.udesk_root_view_album_items);
            allfolderRc = (RecyclerView) findViewById(R.id.udesk_album_items);
            allfolderRc.setLayoutManager(new LinearLayoutManager(this));
            folderAdapter = new FolderAdapter(getApplicationContext(), this);
            allfolderRc.setAdapter(folderAdapter);

            localMedialLoader = new LocalMedialLoader();
            if (Build.VERSION.SDK_INT < 23) {
                readLocalMedia();
            } else {
                XPermissionUtils.requestPermissions(PhotoSelectorActivity.this, RequestCode.EXTERNAL,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                readLocalMedia();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                Toast.makeText(getApplicationContext(),
                                        getResources().getString(R.string.photo_denied),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            setViewEneable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        try {
            int id = view.getId();
            if (id == R.id.udesk_back_linear) {
                finish();
            } else if (id == R.id.udesk_select_folder) {
                showFoloderItems(View.GONE == rootViewAllItems.getVisibility());
            } else if (id == R.id.udesk_pre) {
                SelectResult.selectLocalMedia.addAll(SelectResult.photos);
                SelectResult.allLocalMedia.clear();
                Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
                intent.putExtra(UdeskConst.PREVIEW_PHOTO_IS_ALL, false);
                startActivityForResult(intent, REQUEST_PREVIEW_ACTIVITY);
            } else if (id == R.id.udesk_titlebar_right) {
                sendPhotos();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPhotos() {
        try {
            Intent mIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(UdeskConst.SEND_PHOTOS, SelectResult.photos);
            bundle.putBoolean(UdeskConst.SEND_PHOTOS_IS_ORIGIN, checkBox.isChecked());
            mIntent.putExtra(UdeskConst.SEND_BUNDLE, bundle);
            PhotoSelectorActivity.this.setResult(Activity.RESULT_OK, mIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    protected void readLocalMedia() {

        try {
            if (localMedialLoader != null) {
                localMedialLoader.loadAllMedia(PhotoSelectorActivity.this, new LocalMedialLoader.LocalMediaLoadListener() {
                    @Override
                    public void loadComplete(List<LocalMediaFolder> folders) {

                        if (folders.size() > 0 && !hasLoaded) {
                            hasLoaded = true;
                            foldersList = folders;
                            LocalMediaFolder folder = folders.get(0);
                            setFolderTitile(folder.getName());
                            localMedias = folder.getMedia();
                            if (photosAdapter != null) {
                                photosAdapter.bindImagesData(localMedias);
                            }
                            if (folderAdapter != null) {
                                folderAdapter.bindFildersData(folders);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSelectorChanged() {
        setViewEneable();
    }

    @Override
    public void onPictureClick(LocalMedia media, int position) {
        try {
            SelectResult.allLocalMedia.addAll(localMedias);
            SelectResult.selectLocalMedia.clear();
            Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
            intent.putExtra(UdeskConst.PREVIEW_PHOTO_INDEX, position);
            intent.putExtra(UdeskConst.PREVIEW_PHOTO_IS_ALL, true);
            startActivityForResult(intent, REQUEST_PREVIEW_ACTIVITY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //选择文件夹
    @Override
    public void onFolderItemClick(int position) {
        try {
            showFoloderItems(false);
            LocalMediaFolder localMediaFolder = foldersList.get(position);
            if (localMediaFolder != null) {
                setFolderTitile(localMediaFolder.getName());
                localMedias = localMediaFolder.getMedia();
                if (photosAdapter != null) {
                    photosAdapter.bindImagesData(localMedias);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSelectorOutOfMax() {
        Toast.makeText(getApplicationContext(), getString(R.string.udesk_max_tips), Toast.LENGTH_SHORT).show();
    }

    private void setFolderTitile(String titile) {
        if (folderTitile != null) {
            folderTitile.setText(titile);
        }
    }

    private void setViewEneable() {
        try {
            if (SelectResult.isEmpty()) {
                sendto.setText(R.string.udesk_send_message);
                pre.setText(R.string.udesk_photo_pre);
                sendto.setEnabled(false);
                pre.setEnabled(false);
                pre.setTextColor(getResources().getColor(R.color.udesk_color_747578));
                sendto.setBackgroundColor(getResources().getColor(R.color.udesk_color_8045c01a));
            } else {
                sendto.setEnabled(true);
                pre.setEnabled(true);
                sendto.setText(getString(R.string.udesk_selector_action_done_photos, SelectResult.count(), UdeskConst.count));
                pre.setText(getString(R.string.udesk_selector_action_done_photo_pre, SelectResult.count()));
                sendto.setBackgroundColor(getResources().getColor(R.color.udesk_color_45c01a));
                pre.setTextColor(getResources().getColor(R.color.udesk_color_bg_white));
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showFoloderItems(boolean isShow) {
        try {
            if (null == setShow) {
                newAnimators();
            }
            if (isShow) {
                rootViewAllItems.setVisibility(View.VISIBLE);
                setShow.start();
            } else {
                setHide.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void newAnimators() {
        newHideAnim();
        newShowAnim();
    }

    private void newShowAnim() {
        try {
            ObjectAnimator translationShow = ObjectAnimator.ofFloat(allfolderRc, "translationY", udesk_rl_bottom.getTop(), 0);
            ObjectAnimator alphaShow = ObjectAnimator.ofFloat(rootViewAllItems, "alpha", 0.0f, 1.0f);
            translationShow.setDuration(300);
            setShow = new AnimatorSet();
            setShow.setInterpolator(new AccelerateDecelerateInterpolator());
            setShow.play(translationShow).with(alphaShow);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newHideAnim() {
        try {
            ObjectAnimator translationHide = ObjectAnimator.ofFloat(allfolderRc, "translationY", 0, udesk_rl_bottom.getTop());
            ObjectAnimator alphaHide = ObjectAnimator.ofFloat(rootViewAllItems, "alpha", 1.0f, 0.0f);
            translationHide.setDuration(200);
            setHide = new AnimatorSet();
            setHide.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    rootViewAllItems.setVisibility(View.GONE);
                }
            });
            setHide.setInterpolator(new AccelerateInterpolator());
            setHide.play(translationHide).with(alphaHide);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode != Activity.RESULT_OK || data == null) {
                return;
            }
            if (REQUEST_PREVIEW_ACTIVITY == requestCode) {
                Bundle bundle = data.getBundleExtra(UdeskConst.SEND_BUNDLE);
                if (bundle != null) {
                    boolean isOrgin = bundle.getBoolean(UdeskConst.SEND_PHOTOS_IS_ORIGIN, false);
                    boolean isSend = bundle.getBoolean(UdeskConst.IS_SEND, false);
                    checkBox.setChecked(isOrgin);
                    if (isSend) {
                        sendPhotos();
                    } else {
                        if (photosAdapter != null) {
                            photosAdapter.notifyDataSetChanged();
                            setViewEneable();
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (null != rootViewAllItems && rootViewAllItems.getVisibility() == View.VISIBLE) {
                showFoloderItems(false);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onBackPressed();
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
            picture_recycler.removeAllViews();
            allfolderRc.removeAllViews();
            foldersList.clear();
            localMedias.clear();
            SelectResult.clear();
            XPermissionUtils.destory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
