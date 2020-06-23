package cn.udesk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.adapter.OptionsAgentGroupAdapter;
import cn.udesk.config.UdeskConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.AgentGroupNode;
import cn.udesk.widget.UdeskDialog;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskConst;


/**
 * 选择客服组
 */
public class UdeskOptionsAgentGroupActivity extends UdeskBaseActivity implements AdapterView.OnItemClickListener {

    private UdeskTitleBar mTitlebar;
    private TextView title;
    private ListView listView;
    private UdeskDialog dialog;
    private List<AgentGroupNode> groups;
    private List<AgentGroupNode> adapterData = new ArrayList<AgentGroupNode>();
    private OptionsAgentGroupAdapter adapter;
    private String rootId = "item_0";
    private AgentGroupNode backMode = null;

    private boolean startActivityForResult = false;

    public static void start(Activity context, int requestCode) {
        Intent intent = new Intent(context, UdeskOptionsAgentGroupActivity.class);
        intent.putExtra("forResult", true);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            UdeskUtil.setOrientation(this);
            setContentView(R.layout.udesk_options_agentgroup_view);
            initView();
            if (getIntent() != null) {
                startActivityForResult = getIntent().getBooleanExtra("forResult", false);
            }
            groups = UdeskSDKManager.getInstance().getInitCustomerBean().getIm_group();
            settingTitlebar();
            drawView(rootId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        try {
            title = (TextView) findViewById(R.id.udesk_title);
            listView = (ListView) findViewById(R.id.udesk_options_listview);
            mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
            adapter = new OptionsAgentGroupAdapter(this);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    backParentView();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //进入会话界面
    private void luanchChat() {
        try {
            Intent intent = new Intent(getApplicationContext(), UdeskChatActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置标题栏的title和返回事件
     */
    private void settingTitlebar() {

        try {
            if (mTitlebar != null) {
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarMiddleTextResId, mTitlebar.getUdeskTopText(), mTitlebar.getUdeskBottomText());
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarRightTextResId, mTitlebar.getRightTextView());
                if (mTitlebar.getRootView() != null) {
                    UdeskConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarBgResId, mTitlebar.getRootView());
                }
                if (UdeskConfig.DEFAULT != UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId);
                }
                mTitlebar
                        .setTopTextSequence(getString(R.string.udesk_options_agentgroup));
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


    /**
     * 返回到上一级
     */
    private void backParentView() {
        try {
            if (backMode == null) {
                finish();
            } else {
                drawView(backMode.getParentId());
                backMode = filterModel(backMode.getParentId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 客服组下还有客服组则进入下一级客服组，是客服，值获取到客服的ID，进行会话连接
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        try {
            AgentGroupNode groupNode = adapter.getItem(i);
            backMode = groupNode;
            if (groupNode != null) {
                if (groupNode.getHas_next()) {
                    drawView(groupNode.getId());
                } else {
                    if (startActivityForResult) {
                        finshActivity(groupNode.getId());
                    } else {
                        Intent intent = new Intent(getApplicationContext(), UdeskChatActivity.class);
                        intent.putExtra(UdeskConst.UDESKMENUID, groupNode.getId());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }


                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void finshActivity(String groupId) {
        Intent intent = new Intent();
        intent.putExtra(UdeskConst.UDESKMENUID, groupId);
        setResult(RESULT_OK, intent);
        UdeskOptionsAgentGroupActivity.this.finish();
    }


    //根据id 画出相应的UI显示
    private void drawView(String currentId) {
        try {
            adapterData.clear();
            if (currentId.equals("item_0")) {
                title.setVisibility(View.GONE);
            } else {
                String currentTempId = currentId;
                List<AgentGroupNode> temps = new ArrayList<AgentGroupNode>();
                boolean isHasParent = true;
                while (isHasParent) {
                    AgentGroupNode model = filterModel(currentTempId);
                    if (model == null) {
                        isHasParent = false;
                    } else {
                        currentTempId = model.getParentId();
                        temps.add(model);
                    }
                }
                title.setVisibility(View.VISIBLE);
                title.setText(buildTitleName(temps));
            }
            for (AgentGroupNode model : groups) {
                if (model.getParentId().equals(currentId)) {
                    adapterData.add(model);
                }
            }
            adapter.setList(adapterData);
        } catch (Exception e) {
            luanchChat();
        }
    }

    /**
     * 获取相应ID的客服组model
     */
    private AgentGroupNode filterModel(String currentId) {
        try {
            for (AgentGroupNode model : groups) {
                if (model.getId().equals(currentId)) {
                    return model;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 构造界面显示的组名 > 的字符串
     */
    private String buildTitleName(List<AgentGroupNode> temps) {
        try {
            StringBuilder builder = new StringBuilder();
            for (int i = temps.size() - 1; i >= 0; i--) {
                builder.append(temps.get(i).getItem_name()).append(" > ");
            }
            String temp = builder.toString();
            return temp.substring(0, temp.length() - 2);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
