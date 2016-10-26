package udesk.sdk.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.MsgNotice;
import udesk.sdk.demo.R;

public class UdeskUseGuideActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_use_guide_view);
//         注册接收消息提醒事件
        UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");

    }

    public  void onClick(View v){
        if (v.getId() == R.id.udesk_group_help){
            //帮助中心
            UdeskSDKManager.getInstance().toLanuchHelperAcitivty(UdeskUseGuideActivity.this);
        }else if (v.getId() == R.id.udesk_group_conversation){
            //咨询会话
            UdeskSDKManager.getInstance().showRobotOrConversation(UdeskUseGuideActivity.this);
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
            PreferenceHelper.write(UdeskUseGuideActivity.this, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_SdkToken, "");
            finish();
        }

    }

        public void OnNewMsgNotice(MsgNotice msgNotice) {
        if (msgNotice != null) {
            NotificationUtils.getInstance().notifyMsg(this.getApplicationContext(), msgNotice.getContent());
        }

    }

}
