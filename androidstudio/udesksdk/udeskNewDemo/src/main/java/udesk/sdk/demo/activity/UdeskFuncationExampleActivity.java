package udesk.sdk.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.jpush.android.api.JPushInterface;
import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskCallBack;
import udesk.core.model.MessageInfo;
import udesk.sdk.demo.R;

public class UdeskFuncationExampleActivity extends Activity {

    private UdeskTitleBar mTitlebar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_funcation_example_view);
        settingTitlebar();
    }

    private void settingTitlebar() {
        mTitlebar = (UdeskTitleBar) findViewById(cn.udesk.R.id.udesktitlebar);
        if (mTitlebar != null) {
            mTitlebar.setLeftTextSequence(getString(R.string.udesk_utils_tips));
            mTitlebar.setLeftLinearVis(View.VISIBLE);
            mTitlebar.setLeftViewClick(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
            }
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId,mTitlebar.getLeftTextView(),mTitlebar.getRightTextView());
            UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId ,mTitlebar.getRootView());
        }
    }

    public  void onClick(View v){
        if (v.getId() == R.id.udesk_by_agentid){
            //指定分配客服
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskFuncationExampleActivity.this);
            dialog.setDialogTitle("指定分配客服");
            final EditText editText = (EditText) dialog.getEditText();
            editText.setHint("客服ID");
            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "客服ID不能为空！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    UdeskSDKManager.getInstance().lanuchChatByAgentId(UdeskFuncationExampleActivity.this, editText.getText().toString().trim());

                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else if (v.getId() == R.id.udesk_by_groupid){
            //指定分配客服组
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskFuncationExampleActivity.this);
            dialog.setDialogTitle("指定分配客服组");
            final EditText editText = (EditText) dialog.getEditText();
            editText.setHint("客服组ID");
            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "客服组ID不能为空！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    UdeskSDKManager.getInstance().lanuchChatByGroupId(UdeskFuncationExampleActivity.this, editText.getText().toString().trim());

                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else if (v.getId() == R.id.udesk_unread_msg){
            //获取未读消息
            List<MessageInfo> unReadMsgs =  UdeskSDKManager.getInstance().getUnReadMessages();
            if (unReadMsgs == null || unReadMsgs.isEmpty()){
                Toast.makeText(UdeskFuncationExampleActivity.this,"没有未读消息",Toast.LENGTH_SHORT).show();
                return;
            }
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskFuncationExampleActivity.this);
            dialog.setDialogTitle("未读消息");
            ListView mListview =  dialog.getListView();
            UnRedMsgAdapter msgAdapter = new UnRedMsgAdapter(UdeskFuncationExampleActivity.this);
            mListview.setAdapter(msgAdapter);
            msgAdapter.setList(unReadMsgs);
            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else  if(v.getId() == R.id.udesk_unread_msgcount){
            //获取未读消息数量
            int unreadMsg = UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount();
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskFuncationExampleActivity.this);
            dialog.setDialogTitle("获取未读消息数量");
            final TextView text = (TextView) dialog.getcontentText();
            text.setText(String.valueOf(unreadMsg));
            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else if(v.getId() == R.id.udesk_update_userinfo){
            //更新客户信息
            Intent intent = new Intent();
            intent.setClass(UdeskFuncationExampleActivity.this,UpdateUserInfoActivity.class);
            startActivity(intent);
        }else if(v.getId() == R.id.udesk_update_ui){
            // 更换UI模板
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskFuncationExampleActivity.this);
            dialog.setDialogTitle("更换UI模版");
            View view1 = dialog.getViewStyle1();
            View view2 = dialog.getViewStyle2();
            dialog.setStyle1Text("原生");
            dialog.setStyle2Text("经典");
            final CheckBox checkBox1 = dialog.getStyle1Checkbox();
            final CheckBox checkBox2 = dialog.getStyle2Checkbox();
            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox2.setChecked(false);
                    if (checkBox1.isChecked()){
                        checkBox1.setChecked(false);
                    }else {
                        checkBox1.setChecked(true);
                    }
                }
            });

            view2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox1.setChecked(false);
                    if (checkBox2.isChecked()){
                        checkBox2.setChecked(false);
                    }else {
                        checkBox2.setChecked(true);
                    }
                }
            });

            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkBox1.isChecked()){
                        UIStyle1();
                    }else if(checkBox2.isChecked()){
                        UIStyle2();
                    }
                    dialog.dismiss();
                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }else if(v.getId() == R.id.udesk_conversion_bysetting_menu){
            //通过后台配置自定义菜单选项进入会话
            UdeskSDKManager.getInstance().showConversationByImGroup(UdeskFuncationExampleActivity.this);
        }else if(v.getId() == R.id.udesk_send_commodity_link){
            //进入会话后， 首先发送商品链接
            createCommodity();
        }else if(v.getId() == R.id.udesk_sdkpush){
            // 更换UI模板
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskFuncationExampleActivity.this);
            dialog.setDialogTitle("推送设置");
            View view1 = dialog.getViewStyle1();
            View view2 = dialog.getViewStyle2();
            dialog.setStyle1Text("开启推送");
            dialog.setStyle2Text("关闭推送");
            final CheckBox checkBox1 = dialog.getStyle1Checkbox();
            final CheckBox checkBox2 = dialog.getStyle2Checkbox();
            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox2.setChecked(false);
                    if (checkBox1.isChecked()){
                        checkBox1.setChecked(false);
                    }else {
                        checkBox1.setChecked(true);
                    }
                }
            });

            view2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox1.setChecked(false);
                    if (checkBox2.isChecked()){
                        checkBox2.setChecked(false);
                    }else {
                        checkBox2.setChecked(true);
                    }
                }
            });

            dialog.setOkTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String rid = JPushInterface.getRegistrationID(getApplicationContext());
                    if(checkBox1.isChecked()){
                         //开启推送
                        UdeskConfig.isUserSDkPush = true;
                        setSdkPush("on",rid);
                    }else if(checkBox2.isChecked()){
                       //关闭推送
                        UdeskConfig.isUserSDkPush = false;
                        setSdkPush("off",rid);
                    }
                    dialog.dismiss();
                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

    }

    /**
     *
     * @param status       sdk推送状态 ["on" | "off"]
     * @param registerId  机关推送注册的 Registration Id。  如果你用其它推送方案  请
     */
    private  void setSdkPush(String status,String registerId){
        //设置推送状态关闭
        UdeskSDKManager.getInstance().setSdkPushStatus(
                UdeskSDKManager.getInstance().getDomain(this),
                UdeskSDKManager.getInstance().getSecretKey(this),
                UdeskSDKManager.getInstance().getSdkToken(this), status,
                registerId, UdeskSDKManager.getInstance().getAppid(), new UdeskCallBack() {
                    @Override
                    public void onSuccess(String message) {

                        try {
                            JSONObject object = new JSONObject(message);
                            if (object.has("code") && object.getString("code").equals("1000")){
                                UdeskFuncationExampleActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText( UdeskFuncationExampleActivity.this,"设置成功",Toast.LENGTH_SHORT);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFail(String message) {

                    }
                }
        );
    }

    private void createCommodity() {
        UdeskCommodityItem item = new UdeskCommodityItem();
        item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");// 商品主标题
        item.setSubTitle("¥ 99.00");//商品副标题
        item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");// 左侧图片
        item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");// 商品网络链接
        UdeskSDKManager.getInstance().setCommodity(item);
        UdeskSDKManager.getInstance().toLanuchChatAcitvity(UdeskFuncationExampleActivity.this);
    }

    private void UIStyle1(){
        UdeskConfig.udeskTitlebarBgResId = R.color.udesk_titlebar_bg1;
        UdeskConfig.udeskTitlebarTextLeftRightResId = R.color.udesk_color_navi_text1;
        UdeskConfig.udeskIMRightTextColorResId = R.color.udesk_color_im_text_right1;
        UdeskConfig.udeskIMLeftTextColorResId = R.color.udesk_color_im_text_left1;
        UdeskConfig.udeskIMAgentNickNameColorResId = R.color.udesk_color_im_left_nickname1;
        UdeskConfig.udeskIMTimeTextColorResId = R.color.udesk_color_im_time_text1;
        UdeskConfig.udeskIMTipTextColorResId = R.color.udesk_color_im_tip_text1;
        UdeskConfig.udeskbackArrowIconResId = R.drawable.udesk_titlebar_back;

        UdeskConfig.udeskCommityBgResId = R.color.udesk_color_im_commondity_bg1;
        UdeskConfig.udeskCommityTitleColorResId = R.color.udesk_color_im_commondity_title1;
        UdeskConfig.udeskCommitysubtitleColorResId = R.color.udesk_color_im_commondity_subtitle1;
        UdeskConfig.udeskCommityLinkColorResId = R.color.udesk_color_im_commondity_title1;
    }

    private void UIStyle2(){
        UdeskConfig.udeskTitlebarBgResId = R.color.udesk_titlebar_bg2;
        UdeskConfig.udeskTitlebarTextLeftRightResId = R.color.udesk_color_navi_text2;
        UdeskConfig.udeskIMRightTextColorResId = R.color.udesk_color_im_text_right2;
        UdeskConfig.udeskIMLeftTextColorResId = R.color.udesk_color_im_text_left2;
        UdeskConfig.udeskIMAgentNickNameColorResId = R.color.udesk_color_im_left_nickname2;
        UdeskConfig.udeskIMTimeTextColorResId = R.color.udesk_color_im_time_text2;
        UdeskConfig.udeskIMTipTextColorResId = R.color.udesk_color_im_tip_text2;
        UdeskConfig.udeskbackArrowIconResId = R.drawable.udesk_titlebar_back;

        UdeskConfig.udeskCommityBgResId = R.color.udesk_color_im_commondity_bg2;
        UdeskConfig.udeskCommityTitleColorResId = R.color.udesk_color_im_commondity_title2;
        UdeskConfig.udeskCommitysubtitleColorResId = R.color.udesk_color_im_commondity_subtitle2;
        UdeskConfig.udeskCommityLinkColorResId = R.color.udesk_color_im_commondity_title2;
    }
}
