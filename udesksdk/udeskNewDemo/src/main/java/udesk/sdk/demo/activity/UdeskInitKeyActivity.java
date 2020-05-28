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
//    private String UDESK_DOMAIN = "brazil0326.udesk.cn";
//    //替换成你们生成应用产生的appid
//    private String AppId = "717b563a93e83c26";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "12cec1ded15716158f7d50fefd1d42a7";

//    //替换成你们注册生成的域名
//    private String UDESK_DOMAIN = "brazil0326.udesk.cn";
//    //替换成你们生成应用产生的appid
//    private String AppId = "e925309ab51f0725";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "8c63951173fbd6ac81a6c6146f940e7b";

    //替换成你们注册生成的域名
    private String UDESK_DOMAIN = "udesk-rd-bj-10.udesk.cn";
    //替换成你们生成应用产生的appid
    private String AppId = "cc57963d6284bfe7";
    // 替换成你们在后台生成的密钥
    private String UDESK_SECRETKEY = "8bcc3f04490559068bd4a894272dc588";

//    //替换成你们注册生成的域名
//    private String UDESK_DOMAIN = "reocar.udeskmonkey.com";
//    //替换成你们生成应用产生的appid
//    private String AppId = "8376c6ddcba14df3";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "0175ba741a912c516cea06b0fbe66992";

    //替换成你们注册生成的域名
//    private String UDESK_DOMAIN = "linapp.udeskt1.com";
//    //替换成你们生成应用产生的appid
//    private String AppId = "842f71cc75e47e06";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "fe4cd5d012d633657e1c5c501b125f9b";


//    替换成你们注册生成的域名
//    private String UDESK_DOMAIN = "linapp.udeskt4.com";
//    //替换成你们生成应用产生的appid
//    private String AppId = "87da63e10d6aff57";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "d6be1a0be4913724d6c514277139c7ee";

//    替换成你们注册生成的域名
//    private String UDESK_DOMAIN = "reocar.udeskb2.com";
//    //替换成你们生成应用产生的appid
//    private String AppId = "17c3e02398a5b62a";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "3dfcd552c34cb78fe55e06a8239d4fce";

//    替换成你们注册生成的域名
//    private String UDESK_DOMAIN = "xianghuanji.s2.udesk.cn";
//    //替换成你们生成应用产生的appid
//    private String AppId = "957f1680f4a7ee45";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "bfdf5d8b5d1000ff3114068743e1b5bb";

//    替换成你们注册生成的域名
//    private String UDESK_DOMAIN = "reocar.udeskb1.com";
//    //替换成你们生成应用产生的appid
//    private String AppId = "e53830a1359ad788";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "ef63d5d0bef9f7d3edbb22c8e358bf7f";

//    private String UDESK_DOMAIN = "linapp.udeskt2.com";
//    //替换成你们生成应用产生的appid
//    private String AppId = "58bf39a766187b03";
//    // 替换成你们在后台生成的密钥
//    private String UDESK_SECRETKEY = "41824663ec849ec266f9a41844180777";

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
        sdkToken = PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken");
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
                    Toast.makeText(UdeskInitKeyActivity.this, "Please enter domian and key values", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void readDoaminAndKey() {
        domain = PreferenceHelper.readString(this, "init_base_name", "domain");
        appkey = PreferenceHelper.readString(this, "init_base_name", "appkey");
        appid = PreferenceHelper.readString(this, "init_base_name", "appid");
    }

}
