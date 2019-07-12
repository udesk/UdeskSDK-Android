package cn.udesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.config.UdeskConfigUtil;
import udesk.core.UdeskConst;
import udesk.core.utils.UdeskUtils;

public class WorkOrderWebViewActivity extends UdeskBaseWebViewActivity {

    private String title;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            UdeskUtil.setOrientation(this);
            Intent intent = getIntent();
            title = intent.getStringExtra(UdeskConst.WORK_ORDER_TITLE);
            url = intent.getStringExtra(UdeskConst.WORK_ORDER_URL);
            loadingView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadingView() {
        try {
            settingTitlebar();
            String secretParams = convertPostParams();
            if (url.contains("?")){
                url=url+"&"+secretParams;
            }else {
                url=url+"?"+secretParams;
            }
            mwebView.loadUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void settingTitlebar() {
        try {
            if (mTitlebar != null) {
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarMiddleTextResId, mTitlebar.getUdeskTopText(), mTitlebar.getUdeskBottomText());
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarRightTextResId, mTitlebar.getRightTextView());
                if (mTitlebar.getRootView() != null){
                    UdeskConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarBgResId ,mTitlebar.getRootView());
                }
                if (UdeskConfig.DEFAULT != UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId);
                }
                mTitlebar.setTopTextSequence(title);
                mTitlebar.setUdeskBottomTextVis(View.GONE);
                mTitlebar.setLeftLinearVis(View.VISIBLE);
                mTitlebar.setLeftViewClick(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();

                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String convertPostParams() {
        HashMap<String, String> baseParams = UdeskUtil.buildGetParams(UdeskSDKManager.getInstance().getSdkToken(this),
                UdeskSDKManager.getInstance().getAppkey(this),
                UdeskSDKManager.getInstance().getAppId(this));
        final StringBuilder signBuilder = new StringBuilder();
        if (baseParams != null && !baseParams.isEmpty()) {
            // 将Key 提取出来，组成Array
            final String[] keyArray = new String[baseParams.size()];
            Set<String> keySet = baseParams.keySet();
            int index = 0;
            for (String key : keySet) {
                keyArray[index++] = key;
            }

            // 排序
            Arrays.sort(keyArray, new Comparator<String>() {
                @Override
                public int compare(String arg0, String arg1) {
                    return TextUtils.isEmpty(arg0) ? 0 : arg0.compareTo(arg1);
                }
            });

            // 对params进行编码
            if (keyArray.length > 0) {
                try {
                    for (int i = 0; i < keyArray.length; i++) {
                        if (!TextUtils.isEmpty(baseParams.get(keyArray[i]))) {
                            signBuilder
                                    .append(keyArray[i])
                                    .append("=")
                                    .append(URLEncoder.encode(
                                            baseParams.get(keyArray[i]), "UTF-8"))
                                    .append("&");
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        // 编码后进行Md5
        String signParams = signBuilder.toString() + UdeskSDKManager.getInstance().getAppkey(this);
        String s = signBuilder.append("sign=")
                .append(UdeskUtils.MD5(signParams)).toString();
        return s;

    }
}
