package udesk.sdk.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import udesk.sdk.demo.R;


public class UdeskInitKeyActivity extends Activity {

//    //替换成你们注册的域名
    private String UDESK_DOMAIN = "showshow.udesk.cn";
    //替换成你们在后台生成的密钥
    private String UDESK_SECRETKEY = "c18d023ff18902fdfdb6ce15a11ef47b";
    private EditText mDomainEdit;
    //输入描述账号的EditText
    private EditText mKeyEdit;

    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_init_key_view);

        mDomainEdit = (EditText) findViewById(R.id.udesk_domain);
        mKeyEdit = (EditText) findViewById(R.id.udesk_appkey);
        startBtn = (Button) findViewById(R.id.udesk_start);

        if (!TextUtils.isEmpty(UDESK_DOMAIN) && !TextUtils.isEmpty(UDESK_SECRETKEY)) {
            mDomainEdit.setText(UDESK_DOMAIN);
            mKeyEdit.setText(UDESK_SECRETKEY);
        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mDomainEdit.getText().toString()) && !TextUtils.isEmpty(mKeyEdit.getText().toString())) {
                    /*  使用前需要设置的信息:
                        1 保存domain和key
                        2创建客户*/
                    UdeskSDKManager.getInstance().initApiKey(getApplicationContext(), mDomainEdit.getText().toString(), mKeyEdit.getText().toString());

                    String sdkToken = PreferenceHelper.readString(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SdkToken);
                    if (TextUtils.isEmpty(sdkToken)) {
                        sdkToken = UUID.randomUUID().toString();
                    }
                    Map<String, String> info = new HashMap<String, String>();
                    info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdkToken);
                    info.put(UdeskConst.UdeskUserInfo.NICK_NAME, "Udesk good");
                    UdeskSDKManager.getInstance().setUserInfo(
                            getApplicationContext(), sdkToken, info);

                    Intent intent = new Intent();
                    intent.setClass(UdeskInitKeyActivity.this, UdeskUseGuideActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(UdeskInitKeyActivity.this, "Please enter domian and key values", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
