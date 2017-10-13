package udesk.gaodemapdemo;

import android.app.Activity;
import android.content.Context;
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
import cn.udesk.config.UdeskConfig;


public class MainActivity extends Activity {

    private String AppId = "d895db00f942fec9";

    private String UDESK_DOMAIN = "udesksdk.udesk.cn";
    //    替换成你们在后台生成的密钥
    private String UDESK_SECRETKEY = "413fd0555b55ccb58e642c2672b54a5f";

    private EditText mDomainEdit;
    private EditText mAppidEdit;

    private EditText mKeyEdit;

    private Button startBtn;


    String domain = "";
    String appkey = "";
    String appid = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_init_key_view);
        redDoaminAndKey();
        mDomainEdit = (EditText) findViewById(R.id.udesk_domain);
        mKeyEdit = (EditText) findViewById(R.id.udesk_appkey);
        mAppidEdit = (EditText) findViewById(R.id.appid);
        startBtn = (Button) findViewById(R.id.udesk_start);
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
                    UdeskSDKManager.getInstance().initApiKey(getApplicationContext(), mDomainEdit.getText().toString(), mKeyEdit.getText().toString(), mAppidEdit.getText().toString());
                    String sdkToken = PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken");
                    if (TextUtils.isEmpty(sdkToken)) {
                        sdkToken = UUID.randomUUID().toString();
                    }
                    final Map<String, String> info = new HashMap<String, String>();
                    info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdkToken);
                    info.put(UdeskConst.UdeskUserInfo.NICK_NAME, sdkToken);
                    UdeskSDKManager.getInstance().setUserInfo(
                            getApplicationContext(), sdkToken, info);
                    saveDoamiandKey();

                    UdeskConfig.isUseMap = true;
                    UdeskConfig.useMapType = UdeskConfig.UdeskMapType.GaoDe;


                    UdeskSDKManager.getInstance().setLocationMessageClickCallBack(new UdeskSDKManager.ILocationMessageClickCallBack() {
                        @Override
                        public void luanchMap(Context context, double latitude, double longitude, String selctLoactionValue) {
                            Intent intent = new Intent();
                            intent.putExtra(UdeskConfig.UdeskMapIntentName.Position, selctLoactionValue);
                            intent.putExtra(UdeskConfig.UdeskMapIntentName.Latitude, latitude);
                            intent.putExtra(UdeskConfig.UdeskMapIntentName.Longitude, longitude);
                            intent.setClass(context, ShowSelectLocationActivity.class);
                            context.startActivity(intent);
                        }
                    });

                    UdeskSDKManager.getInstance().setCls(LocationActivity.class);
                    PreferenceHelper.write(getApplicationContext(), "init_base_name", "sdktoken", sdkToken);
                    UdeskSDKManager.getInstance().entryChat(MainActivity.this);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter domian and key values", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void redDoaminAndKey() {
        domain = PreferenceHelper.readString(this, "init_base_name", "domain");
        appkey = PreferenceHelper.readString(this, "init_base_name", "appkey");
        appid = PreferenceHelper.readString(this, "init_base_name", "appid");
    }

    private void saveDoamiandKey() {
        PreferenceHelper.write(this, "init_base_name",
                "domain", mDomainEdit.getText().toString());
        PreferenceHelper.write(this, "init_base_name",
                "appkey", mKeyEdit.getText().toString());
        PreferenceHelper.write(this, "init_base_name",
                "appid", mAppidEdit.getText().toString());
    }

}
