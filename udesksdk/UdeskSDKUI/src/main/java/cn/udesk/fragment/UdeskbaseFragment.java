package cn.udesk.fragment;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.udesk.aac.UdeskViewMode;
import cn.udesk.activity.UdeskChatActivity;

public abstract class UdeskbaseFragment extends Fragment {

    protected abstract void initView(View view, Bundle savedInstanceState);
    //获取布局文件ID
    protected abstract int getLayoutId();
    public abstract void initFunctionAdapter();
    public abstract void initfunctionItems();
    public abstract void hideMoreLayout();
    public abstract void setNavigationViewVis();
    public abstract void addNavigationFragment();
    public abstract CharSequence getInputContent();
    public abstract void clearInputContent();
    public abstract void onBackPressed();
    public abstract void cleanSource();
    public abstract void setUdeskImContainerVis(int vis);

    protected UdeskChatActivity udeskChatActivity;
    protected UdeskViewMode udeskViewMode;

    @Override
    public void onAttach(Context context) {
        udeskChatActivity=(UdeskChatActivity)context;
        udeskViewMode = ViewModelProviders.of(udeskChatActivity).get(UdeskViewMode.class);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), null, false);
        initView(view, savedInstanceState);
        return view;
    }


}
