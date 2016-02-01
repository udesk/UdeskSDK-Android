package cn.udesk.saas.demo;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import cn.udesk.UdeskSDKManager;
import cn.udesk.db.UdeskDBManager;
public class UdeskUseCaseActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_use_case_activity_view);
		//仅仅人工客服
		findViewById(R.id.btn_open_im).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						UdeskSDKManager.getInstance(UdeskUseCaseActivity.this).toLanuchChatAcitvity();
					}
				});
		
		//机器人客服
		findViewById(R.id.acess_html).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						UdeskSDKManager.getInstance(UdeskUseCaseActivity.this).showRobot();
					}
				});
		//智能选择客服
		findViewById(R.id.acess_intelligent_selection).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						UdeskSDKManager.getInstance(UdeskUseCaseActivity.this).showRobotOrConversation();
					}
				});
		
		findViewById(R.id.btn_open_helper).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						UdeskSDKManager.getInstance(UdeskUseCaseActivity.this).toLanuchHelperAcitivty();
					}
				});
	

		findViewById(R.id.delete_db).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						UdeskDBManager.getInstance().deleteAllMsg();
					}
				});
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
