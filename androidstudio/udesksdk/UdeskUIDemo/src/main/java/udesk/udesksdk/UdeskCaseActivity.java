package udesk.udesksdk;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.udesk.UdeskSDKManager;
import cn.udesk.model.MsgNotice;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.xmpp.UdeskMessageManager;


public class UdeskCaseActivity extends Activity {
    //数字提醒控件
    private BadgeView mUnReadTips = null;
    private TextView msg_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_use_case_activity_view);

        //进入人工客服会话界面
        findViewById(R.id.btn_open_im).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().toLanuchChatAcitvity(UdeskCaseActivity.this);
            }
        });


        //进入机器人会话界面
        findViewById(R.id.acess_html).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().showRobot(UdeskCaseActivity.this);
            }
        });

        //后台设置了开通机器人，则进入机器人界面，如果没开通机器人界面，则进入人工客服会话界面
        findViewById(R.id.acess_intelligent_selection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().showRobotOrConversation(UdeskCaseActivity.this);
            }
        });

        //进入帮助中心界面
        findViewById(R.id.btn_open_helper).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().toLanuchHelperAcitivty(UdeskCaseActivity.this);
            }
        });

        //进入客服组指引界面，指定客服组中的客服分配会话
        findViewById(R.id.im_agentgroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UdeskSDKManager.getInstance().showConversationByImGroup(UdeskCaseActivity.this);
            }
        });

        /**
         * 弹出对话框，输入客服组的ID,进行会话分配
         */
        findViewById(R.id.im_define_groupId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildDialog("指定客服组 id 进行分配", "请输入客服组的ID", 1);
            }
        });
        /**
         * 弹出对话框，输入客服的ID,指定客服进行会话分配
         */
        findViewById(R.id.im_define_agentId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildDialog("指定客服id 进行分配", "请输入客服的ID", 2);
            }
        });

        /**
         * 如需要发送商品链接广告信息，创建广告消息，保存在UdeskSDKManager中，进入会话界面首先会发送商品链接广告信息
         */
        findViewById(R.id.im_create_commity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCommodity();
            }
        });

        //初始化数字提醒控件
        mUnReadTips = (BadgeView) findViewById(R.id.id_unread_tips);
        mUnReadTips.setVisibility(View.GONE);

        //显示未读消息条数
        findViewById(R.id.unread_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int unreadMsg = UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount();
                mUnReadTips.setVisibility(View.VISIBLE);
                mUnReadTips.setText(unreadMsg + "");
            }
        });


    }

    @Override
    protected void onResume() {

        super.onResume();

        /**
         * 注册接收消息提醒事件
         */
        UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");
        Log.i("xxx","UdeskCaseActivity 中bind OnNewMsgNotice");
    }

    /**
     * 处理不在会话界面 收到消息的通知事例  方法名OnNewMsgNotice  对应于绑定事件
     * UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this,"OnNewMsgNotice")中参数的字符串
     *
     * @param msgNotice
     */
    public void OnNewMsgNotice(MsgNotice msgNotice) {
        if (msgNotice != null) {
            Log.i("xxx","UdeskCaseActivity 中收到msgNotice");
            NotificationUtils.getInstance().notifyMsg(UdeskCaseActivity.this, msgNotice.getContent());
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mUnReadTips.setVisibility(View.GONE);
        UdeskMessageManager.getInstance().event_OnNewMsgNotice.unBind(this);
        Log.i("xxx","UdeskCaseActivity 中unbind OnNewMsgNotice");
    }

    /**
     * 创建可输入客服组或客服ID的对话框
     *
     * @param title
     * @param hint
     * @param flag  1 指定客服组id， 2 指定客服id
     */
    private void buildDialog(String title, String hint, final int flag) {
        final CustomDialog dialog = new CustomDialog(UdeskCaseActivity.this);
        dialog.setDialogTitle(title);
        final EditText editText = (EditText) dialog.getEditText();//方法在CustomDialog中实现
        editText.setHint(hint);
        dialog.setOnPositiveListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             dialog.dismiss();
                                             String input = editText.getText().toString();
                                             if (TextUtils.isEmpty(input.trim())) {
                                                 Toast.makeText(getApplicationContext(), "客服ID不能为空！", Toast.LENGTH_LONG).show();
                                             } else {
                                                 if (flag == 1) {
                                                     UdeskSDKManager.getInstance().lanuchChatByGroupId(UdeskCaseActivity.this, input.trim());
                                                 } else if (flag == 2) {
                                                     UdeskSDKManager.getInstance().lanuchChatByAgentId(UdeskCaseActivity.this, input.trim());
                                                 }
                                             }

                                         }
                                     }

        );
        dialog.setOnNegativeListener(new View.OnClickListener()

                                     {
                                         @Override
                                         public void onClick(View v) {
                                             dialog.dismiss();
                                         }
                                     }

        );
        dialog.show();
    }


    /**
     * 创建广告商品链接的例子
     */
    private void createCommodity() {
        UdeskCommodityItem item = new UdeskCommodityItem();
        item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");// 商品主标题
        item.setSubTitle("¥ 99.00");//商品副标题
        item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");// 左侧图片
        item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");// 商品网络链接
        UdeskSDKManager.getInstance().setCommodity(item);
    }


}
