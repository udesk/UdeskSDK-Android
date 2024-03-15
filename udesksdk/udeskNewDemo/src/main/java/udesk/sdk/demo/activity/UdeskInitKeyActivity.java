package udesk.sdk.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskSDKManager;
import udesk.core.UdeskConst;
import udesk.sdk.demo.R;


public class UdeskInitKeyActivity extends Activity {

//    //替换成你们注册生成的域名
    private String UDESK_DOMAIN = "udesk-rd-bj-12.s4.udesk.cn";
    //替换成你们生成应用产生的appid
    private String AppId = "9950cd46516a2b16";
    // 替换成你们在后台生成的密钥
    private String UDESK_SECRETKEY = "ba94865ae12b7fa42b396d22be528b0b";
    

    private EditText mDomainEdit;

    private EditText mAppidEdit;

    private EditText mKeyEdit;
    private EditText stoken;
    private EditText customerToken;

    private Button startBtn;

    String domain = "";
    String appkey = "";
    String appid = "";
    String sdkToken = "";
    private CheckBox use_http;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_init_key_view);
        //传入注册的域名和密钥
        readDoaminAndKey();
        mDomainEdit = (EditText) findViewById(R.id.udesk_domain);
        mKeyEdit = (EditText) findViewById(R.id.udesk_appkey);
        mAppidEdit = (EditText) findViewById(R.id.appid);
        stoken = (EditText) findViewById(R.id.stoken);
        customerToken = (EditText) findViewById(R.id.customer_token);
        use_http = findViewById(R.id.use_http);
        startBtn = (Button) findViewById(R.id.udesk_start);
//        sdkToken = PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken");
        if (TextUtils.isEmpty(sdkToken)) {
            sdkToken = UUID.randomUUID().toString();
        }
        stoken.setText(sdkToken);
        if (TextUtils.isEmpty(domain) || TextUtils.isEmpty(appkey) || TextUtils.isEmpty(appid)) {
            mDomainEdit.setText(UDESK_DOMAIN);
            mKeyEdit.setText(UDESK_SECRETKEY);
            mAppidEdit.setText(AppId);
        } else {
            mDomainEdit.setText(domain);
            mKeyEdit.setText(appkey);
            mAppidEdit.setText(appid);
        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mDomainEdit.getText().toString()) && !TextUtils.isEmpty(mKeyEdit.getText().toString())) {
                    //  使用前需要设置的信息:
                    UdeskSDKManager.getInstance().initApiKey(getApplicationContext(), mDomainEdit.getText().toString(),
                            mKeyEdit.getText().toString(), mAppidEdit.getText().toString());

                    UdeskConst.HTTP = use_http.isChecked() ? "http://" : "https://";
                    sdkToken = stoken.getText().toString();
                    PreferenceHelper.write(getApplicationContext(), "init_base_name", "sdktoken", sdkToken);
                    PreferenceHelper.write(getApplicationContext(), "init_base_name", "domain", mDomainEdit.getText().toString());
                    PreferenceHelper.write(getApplicationContext(), "init_base_name", "appkey", mKeyEdit.getText().toString());
                    PreferenceHelper.write(getApplicationContext(), "init_base_name", "appid", mAppidEdit.getText().toString());
                    Intent intent = new Intent();
                    intent.setClass(UdeskInitKeyActivity.this, UdeskUseGuideActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter domian and key values", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void readDoaminAndKey() {
        domain = PreferenceHelper.readString(getApplicationContext(), "init_base_name", "domain");
        appkey = PreferenceHelper.readString(getApplicationContext(), "init_base_name", "appkey");
        appid = PreferenceHelper.readString(getApplicationContext(), "init_base_name", "appid");
    }

}
