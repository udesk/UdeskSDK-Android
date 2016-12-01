package udesk.sdk.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import cn.jpush.android.api.JPushInterface;
import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskConfig;
import udesk.core.UdeskHttpFacade;
import udesk.sdk.demo.R;
import udesk.sdk.demo.jpush.ExampleApplication;

public class UdeskUseGuideActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_use_guide_view);


    }

    public  void onClick(View v){

        String rid = JPushInterface.getRegistrationID(getApplicationContext());
        UdeskSDKManager.getInstance().setRegisterId(UdeskUseGuideActivity.this,rid);
        if (v.getId() == R.id.udesk_group_help){
            //帮助中心
            UdeskSDKManager.getInstance().toLanuchHelperAcitivty(UdeskUseGuideActivity.this);
        }else if (v.getId() == R.id.udesk_group_conversation){
            //咨询会话
//            UdeskSDKManager.getInstance().showRobotOrConversation(UdeskUseGuideActivity.this);
            UdeskSDKManager.getInstance().showRobotOrConversationByImGroup(UdeskUseGuideActivity.this);
        }else if (v.getId() == R.id.udesk_group_formtable){
            //留言表单
            UdeskSDKManager.getInstance().goToForm(UdeskUseGuideActivity.this);
        }else  if(v.getId() == R.id.udesk_group_utils){
            //开发者功能
            Intent funtionIntent = new Intent();
            funtionIntent.setClass(UdeskUseGuideActivity.this, UdeskFuncationExampleActivity.class);
            startActivity(funtionIntent);
        }else if(v.getId() == R.id.udesk_group_reset){
            //重置域名和App Key
            PreferenceHelper.write(UdeskUseGuideActivity.this, "init_base_name",
                   "sdktoken", "");
            Intent initIntent = new Intent();
            initIntent.setClass(UdeskUseGuideActivity.this, UdeskInitKeyActivity.class);
            startActivity(initIntent);
            finish();
        }

    }

}
