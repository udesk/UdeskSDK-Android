package udesk.sdk.demo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.UdeskTitleBar;
import udesk.sdk.demo.R;

/**
 * Created by user on 2016/8/20.
 */
public class UpdateUserInfoActivity extends Activity {

    private EditText nickName,email,phone,des;
    private UdeskTitleBar mTitlebar;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_update_userinfo_view);
        nickName = (EditText) findViewById(R.id.udesk_update_name);
        email = (EditText) findViewById(R.id.udesk_update_email);
        phone = (EditText) findViewById(R.id.udesk_update_tel);
        des = (EditText) findViewById(R.id.udesk_update_des);
        saveBtn = (Button) findViewById(R.id.udesk_store);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpdateInfo();
                finish();
            }
        });
        settingTitlebar();
    }

    private void settingTitlebar() {
        mTitlebar = (UdeskTitleBar) findViewById(cn.udesk.R.id.udesktitlebar);
        if (mTitlebar != null) {
            mTitlebar.setLeftTextSequence(getString(R.string.udesk_update_userinfo));
            mTitlebar.setLeftLinearVis(View.VISIBLE);
            mTitlebar.setLeftViewClick(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
//                    setUpdateInfo();
                    finish();
                }
            });
            if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
            }
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId,mTitlebar.getLeftTextView(),mTitlebar.getRightTextView());
            UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId ,mTitlebar.getRootView());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        setUpdateInfo();
    }


    private void setUpdateInfo(){
        //取editText 编辑框的值 缓存到内存，之后进入会话修改客户信息。
        Map info = new HashMap();
        if(!TextUtils.isEmpty(nickName.getText().toString().trim())){
            info.put(UdeskConst.UdeskUserInfo.NICK_NAME, nickName.getText().toString().trim());
        }
        if(!TextUtils.isEmpty(email.getText().toString().trim())){
            info.put(UdeskConst.UdeskUserInfo.EMAIL, email.getText().toString().trim());
        }
        if(!TextUtils.isEmpty(phone.getText().toString().trim())){
            info.put(UdeskConst.UdeskUserInfo.CELLPHONE, phone.getText().toString().trim());
        }
        if(!TextUtils.isEmpty(des.getText().toString().trim())){
            info.put(UdeskConst.UdeskUserInfo.DESCRIPTION, des.getText().toString().trim());
        }
        if (!info.isEmpty()){
            UdeskSDKManager.getInstance().setUpdateUserinfo(info);
        }

    }
}
