package cn.udesk.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.adapter.NavigationAdapter;
import cn.udesk.model.NavigationMode;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/28.
 */

public class NavigationFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private NavigationAdapter navigationAdapter;

    UdeskChatActivity activity;
    private String currentView= UdeskConst.CurrentFragment.agent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = null;
        try {
            activity = (UdeskChatActivity) NavigationFragment.this.getActivity();
            rootView = inflater.inflate(R.layout.udesknavigatiion_fragment,
                    container, false);
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_navigation_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            navigationAdapter = new NavigationAdapter(getContext(),currentView);
            mRecyclerView.setAdapter(navigationAdapter);
            navigationAdapter.setOnItemClickListener(new NavigationAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, NavigationMode data) {
                    if (TextUtils.equals(currentView,UdeskConst.CurrentFragment.agent)&&UdeskSDKManager.getInstance().getUdeskConfig().navigationItemClickCallBack != null && activity != null) {
                        UdeskSDKManager.getInstance().getUdeskConfig().navigationItemClickCallBack.callBack(activity.getApplicationContext(), activity.udeskViewMode, data,currentView);
                    }else if (TextUtils.equals(currentView,UdeskConst.CurrentFragment.robot)&&UdeskSDKManager.getInstance().getUdeskConfig().robotNavigationItemClickCallBack != null && activity != null){
                        UdeskSDKManager.getInstance().getUdeskConfig().robotNavigationItemClickCallBack.callBack(activity.getApplicationContext(), activity.udeskViewMode, data,currentView);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }
    public void setCurrentView(String currentView){
        this.currentView=currentView;
    }
}
