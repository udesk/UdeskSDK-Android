package cn.udesk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.adapter.OptionsAgentGroupAdapter;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.AgentGroupNode;
import cn.udesk.widget.UdeskDialog;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskHttpFacade;


/**
 * 选择客服组
 */
public class UdeskOptionsAgentGroupActivity extends Activity implements AdapterView.OnItemClickListener {

    private UdeskTitleBar mTitlebar;
    private TextView title;
    private ListView listView;
    private UdeskDialog dialog;
    private List<AgentGroupNode> groups;
    private List<AgentGroupNode> adapterData = new ArrayList<AgentGroupNode>();
    private OptionsAgentGroupAdapter adapter;
    private String rootId = "item_0";
    private AgentGroupNode backMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_options_agentgroup_view);
        initView();
        getImGroupInfo();
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

    /**
     * 请求客服组信息，没有客服组则直接进入会话界面
     */
    private void getImGroupInfo() {
        try {
            showLoading();
            UdeskHttpFacade.getInstance().getImGroupApi(
                    UdeskSDKManager.getInstance().getDomain(this),
                    UdeskSDKManager.getInstance().getAppkey(this),
                    UdeskSDKManager.getInstance().getSdkToken(this),
                    UdeskSDKManager.getInstance().getAppId(this),
                    new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            dismiss();
                            try {
                                JSONObject resultJson = new JSONObject(message);
                                if (resultJson.optInt("code") == 1000) {
                                    groups = JsonUtils.parseIMGroup(message);
                                    if (groups == null || groups.isEmpty()) {
                                        luanchChat();
                                        return;
                                    }
                                    settingTitlebar();
                                    drawView(rootId);
                                } else {
                                    Toast.makeText(UdeskOptionsAgentGroupActivity.this, UdeskOptionsAgentGroupActivity.this.getString(R.string.udesk_error), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            } catch (Exception e) {
                                Toast.makeText(UdeskOptionsAgentGroupActivity.this, UdeskOptionsAgentGroupActivity.this.getString(R.string.udesk_error), Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        }

                        @Override
                        public void onFail(String message) {
                            dismiss();
                            Toast.makeText(UdeskOptionsAgentGroupActivity.this, UdeskOptionsAgentGroupActivity.this.getString(R.string.udesk_has_bad_net), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //进入会话界面
    private void luanchChat() {
        try {
//            Intent intent = new Intent(UdeskOptionsAgentGroupActivity.this, UdeskChatActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            UdeskOptionsAgentGroupActivity.this.startActivity(intent);
            UdeskSDKManager.getInstance().toLanuchChatAcitvity(UdeskOptionsAgentGroupActivity.this);
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
                UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
                UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
                if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
                }
                mTitlebar
                        .setLeftTextSequence(getString(R.string.udesk_options_agentgroup));
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
                if (TextUtils.isEmpty(groupNode.getGroup_id())) {
                    drawView(groupNode.getId());
                } else {
                    UdeskSDKManager.getInstance().lanuchChatByGroupId(UdeskOptionsAgentGroupActivity.this, groupNode.getGroup_id());
                    finish();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showLoading() {
        try {
            dialog = new UdeskDialog(UdeskOptionsAgentGroupActivity.this, R.style.udesk_dialog);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
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
