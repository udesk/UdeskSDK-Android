package udesk.udesksdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import cn.udesk.UdeskSDKManager;

/**
 * Created by sks on 2016/2/4.
 */
public class UdeskCaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_use_case_activity_view);

        findViewById(R.id.btn_open_im).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().toLanuchChatAcitvity(UdeskCaseActivity.this);
            }
        });

        findViewById(R.id.acess_html).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().showRobot(UdeskCaseActivity.this);
            }
        });

        findViewById(R.id.acess_intelligent_selection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().showRobotOrConversation(UdeskCaseActivity.this);
            }
        });

        findViewById(R.id.btn_open_helper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().toLanuchHelperAcitivty(UdeskCaseActivity.this);
            }
        });
    }
}
