package udesk.sdk.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

import cn.jpush.android.api.JPushInterface;
import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskConfig;
import udesk.core.UdeskConst;
import udesk.sdk.demo.R;

public class UdeskUseGuideActivity extends Activity {


    private String customer_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_use_guide_view);
        Intent intent = getIntent();
        customer_token = intent.getStringExtra("customer_token");
    }

    public  void onClick(View v){


        String rid = JPushInterface.getRegistrationID(getApplicationContext());
        UdeskSDKManager.getInstance().setRegisterId(UdeskUseGuideActivity.this, rid);

        if (v.getId() == R.id.udesk_group_help) {
            //帮助中心
            UdeskSDKManager.getInstance().toLanuchHelperAcitivty(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
        } else if (v.getId() == R.id.udesk_group_conversation) {
            String sdkToken = PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken");
            if (TextUtils.isEmpty(sdkToken)) {
                sdkToken = UUID.randomUUID().toString();
            }
            //咨询会话
            UdeskSDKManager.getInstance().entryChat(getApplicationContext(), UdeskConfig.createDefualt(), sdkToken);
        } else if (v.getId() == R.id.udesk_group_formtable) {
            //留言表单
            UdeskSDKManager.getInstance().goToForm(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());

        } else if (v.getId() == R.id.udesk_group_utils) {
            //开发者功能
            Intent funtionIntent = new Intent();
            funtionIntent.setClass(UdeskUseGuideActivity.this, UdeskFuncationExampleActivity.class);
            startActivity(funtionIntent);
        } else if (v.getId() == R.id.udesk_group_reset) {
            //重置域名和App Key
            UdeskSDKManager.getInstance().logoutUdesk();
            PreferenceHelper.write(UdeskUseGuideActivity.this, "init_base_name",
                    "sdktoken", "");
            PreferenceHelper.write(getApplicationContext(), "init_base_name",
                    "menu_id", "");
            Intent initIntent = new Intent();
            initIntent.setClass(UdeskUseGuideActivity.this, UdeskInitKeyActivity.class);
            startActivity(initIntent);
            finish();
        }

    }

}
