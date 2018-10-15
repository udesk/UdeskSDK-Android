package udesk.sdk.demo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskSDKManager;
import cn.udesk.callback.IFunctionItemClickCallBack;
import cn.udesk.callback.ILocationMessageClickCallBack;
import cn.udesk.callback.INavigationItemClickCallBack;
import cn.udesk.callback.ITxtMessageWebonCliclk;
import cn.udesk.callback.IUdeskFormCallBack;
import cn.udesk.callback.IUdeskStructMessageCallBack;
import cn.udesk.config.UdeskConfig;
import cn.udesk.model.FunctionMode;
import cn.udesk.model.NavigationMode;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.presenter.ChatActivityPresenter;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskConst;
import udesk.core.model.MessageInfo;
import udesk.core.model.Product;
import udesk.sdk.demo.R;
import udesk.core.LocalManageUtil;
import udesk.sdk.demo.maps.LocationActivity;
import udesk.sdk.demo.maps.ShowSelectLocationActivity;


public class UdeskFuncationExampleActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    private UdeskTitleBar mTitlebar;
    private CheckBox set_sdkpush,
            set_usevoice,
            set_usephoto,
            set_usecamera,
            set_usemap,
            set_usefile,
            set_useemotion,
            set_usemore,
            set_use_navigation_view,
            set_use_navigation_survy,
            set_use_onlyrobot,
            set_use_smallvideo,
            set_use_commodity,
            set_use_prouct,
            set_use_isscaleimg,
            set_en,
            set_ch,
            mark,
            force_quit,
            portrait,
            landscape,
            user;

    private EditText nick_name, cellphone, email, description,
            textfiledkey, textfiledvalue,
            update_nick_name, update_description,
            updatetextfiledkey, updatetextfiledvalue,
            firstMessage, customerUrl, robot_modelKey, robpt_customer_info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_funcation_example_view);
        initview();
    }

    private void initview() {
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
        }
        set_sdkpush = (CheckBox) findViewById(R.id.set_sdkpush);
        set_usevoice = (CheckBox) findViewById(R.id.set_usevoice);
        set_usephoto = (CheckBox) findViewById(R.id.set_usephoto);
        set_usecamera = (CheckBox) findViewById(R.id.set_usecamera);
        set_usemap = (CheckBox) findViewById(R.id.set_usemap);
        set_usefile = (CheckBox) findViewById(R.id.set_usefile);
        set_useemotion = (CheckBox) findViewById(R.id.set_useemotion);
        set_usemore = (CheckBox) findViewById(R.id.set_usemore);
        set_use_navigation_view = (CheckBox) findViewById(R.id.set_use_navigation_view);
        set_use_navigation_survy = (CheckBox) findViewById(R.id.set_use_navigation_survy);
        set_use_onlyrobot = (CheckBox) findViewById(R.id.set_use_onlyrobot);
        set_use_smallvideo = (CheckBox) findViewById(R.id.set_use_smallvideo);
        set_use_commodity = (CheckBox) findViewById(R.id.set_use_commodity);
        set_use_prouct = (CheckBox) findViewById(R.id.set_use_prouct);
        set_use_isscaleimg = (CheckBox) findViewById(R.id.set_use_isscaleimg);
        set_en = (CheckBox) findViewById(R.id.set_en);
        set_ch = (CheckBox) findViewById(R.id.set_ch);
        mark = (CheckBox) findViewById(R.id.mark);
        force_quit = (CheckBox) findViewById(R.id.force_quit);
        mark.setOnCheckedChangeListener(this);
        force_quit.setOnCheckedChangeListener(this);
        portrait = (CheckBox) findViewById(R.id.portrait);
        landscape = (CheckBox) findViewById(R.id.landscape);
        user = (CheckBox) findViewById(R.id.user);
        portrait.setOnCheckedChangeListener(this);
        landscape.setOnCheckedChangeListener(this);
        user.setOnCheckedChangeListener(this);
        set_en.setOnCheckedChangeListener(this);
        set_ch.setOnCheckedChangeListener(this);
        firstMessage = (EditText) findViewById(R.id.firstMessage);
        customerUrl = (EditText) findViewById(R.id.customerUrl);
        robot_modelKey = (EditText) findViewById(R.id.robot_modelKey);
        nick_name = (EditText) findViewById(R.id.nick_name);
        cellphone = (EditText) findViewById(R.id.cellphone);
        email = (EditText) findViewById(R.id.email);
        description = (EditText) findViewById(R.id.description);
        textfiledkey = (EditText) findViewById(R.id.textfiledkey);
        textfiledvalue = (EditText) findViewById(R.id.textfiledvalue);
        update_nick_name = (EditText) findViewById(R.id.update_nick_name);
        update_description = (EditText) findViewById(R.id.update_description);
        updatetextfiledkey = (EditText) findViewById(R.id.updatetextfiledkey);
        updatetextfiledvalue = (EditText) findViewById(R.id.updatetextfiledvalue);
        robpt_customer_info = (EditText) findViewById(R.id.robpt_customer_info);


    }


    private UdeskConfig.Builder makeBuilder() {
        UdeskConfig.Builder builder = new UdeskConfig.Builder();
        builder.setUdeskTitlebarBgResId(R.color.udesk_titlebar_bg1) //设置标题栏TitleBar的背景色
                .setUdeskTitlebarTextLeftRightResId(R.color.udesk_color_navi_text1) //设置标题栏TitleBar，左右两侧文字的颜色
                .setUdeskIMLeftTextColorResId(R.color.udesk_color_im_text_left1) //设置IM界面，左侧文字的字体颜色
                .setUdeskIMRightTextColorResId(R.color.udesk_color_im_text_right1) // 设置IM界面，右侧文字的字体颜色
                .setUdeskIMAgentNickNameColorResId(R.color.udesk_color_im_left_nickname1) //设置IM界面，左侧客服昵称文字的字体颜色
                .setUdeskIMTimeTextColorResId(R.color.udesk_color_im_time_text1) // 设置IM界面，时间文字的字体颜色
                .setUdeskIMTipTextColorResId(R.color.udesk_color_im_tip_text1) //设置IM界面，提示语文字的字体颜色，比如客服转移
                .setUdeskbackArrowIconResId(R.drawable.udesk_titlebar_back) // 设置返回箭头图标资源id
                .setUdeskCommityBgResId(R.color.udesk_color_im_commondity_bg1) //咨询商品item的背景颜色
                .setUdeskCommityTitleColorResId(R.color.udesk_color_im_commondity_title1) // 商品介绍Title的字样颜色
                .setUdeskCommitysubtitleColorResId(R.color.udesk_color_im_commondity_subtitle1)// 商品咨询页面中，商品介绍子Title的字样颜色
                .setUdeskCommityLinkColorResId(R.color.udesk_color_im_commondity_link1) //商品咨询页面中，发送链接的字样颜色
                .setUserSDkPush(set_sdkpush.isChecked()) // 配置 是否使用推送服务  true 表示使用  false表示不使用
                .setOnlyUseRobot(set_use_onlyrobot.isChecked())//配置是否只使用机器人功能 只使用机器人功能,只使用机器人功能;  其它功能不使用。
                .setUdeskQuenuMode(force_quit.isChecked() ? UdeskConfig.UdeskQuenuFlag.FORCE_QUIT : UdeskConfig.UdeskQuenuFlag.Mark)  //  配置放弃排队的策略
                .setUseVoice(set_usevoice.isChecked()) // 是否使用录音功能  true表示使用 false表示不使用
                .setUsephoto(set_usephoto.isChecked()) //是否使用发送图片的功能  true表示使用 false表示不使用
                .setUsecamera(set_usecamera.isChecked()) //是否使用拍照的功能  true表示使用 false表示不使用
                .setUsefile(set_usefile.isChecked()) //是否使用上传文件功能  true表示使用 false表示不使用
                .setUseMap(set_usemap.isChecked()) //是否使用发送位置功能  true表示使用 false表示不使用
                .setUseMapSetting(UdeskConfig.UdeskMapType.GaoDe, LocationActivity.class, new ILocationMessageClickCallBack() {
                    @Override
                    public void luanchMap(Context context, double latitude, double longitude, String selctLoactionValue) {
                        Intent intent = new Intent();
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Position, selctLoactionValue);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Latitude, latitude);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Longitude, longitude);
                        intent.setClass(context, ShowSelectLocationActivity.class);
                        context.startActivity(intent);
                    }
                })
                .setUseEmotion(set_useemotion.isChecked()) //是否使用表情 true表示使用 false表示不使用
                .setUseMore(set_usemore.isChecked()) // 是否使用更多控件 展示出更多功能选项 true表示使用 false表示不使用
                .setUseNavigationSurvy(set_use_navigation_survy.isChecked()) //设置是否使用导航UI中的满意度评价UI rue表示使用 false表示不使用
                .setUseSmallVideo(set_use_smallvideo.isChecked())  //设置是否需要小视频的功能 rue表示使用 false表示不使用
                .setScaleImg(set_use_isscaleimg.isChecked()) //上传图片是否使用原图 还是缩率图
                .setScaleMax(1024) // 缩放图 设置最大值，如果超出则压缩，否则不压缩
                .setOrientation(landscape.isChecked() ? UdeskConfig.OrientationValue.landscape :
                        (user.isChecked() ? UdeskConfig.OrientationValue.user : UdeskConfig.OrientationValue.portrait)) //设置默认屏幕显示习惯
                .setUserForm(true) //在没有请求到管理员在后端对sdk使用配置下，在默认的情况下，是否需要表单留言，true需要， false 不需要
                .setDefualtUserInfo(getDefualtUserInfo()) // 创建用户基本信息
                .setDefinedUserTextField(getDefinedUserTextField()) //创建用户自定义的文本信息
                .setDefinedUserRoplist(getDefinedUserRoplist()) //创建用户自定义的列表信息
                .setUpdateDefualtUserInfo(getUpdateDefualtUserInfo()) // 设置更新用户的基本信息
                .setUpdatedefinedUserTextField(getUpdateDefinedTextField()) //设置用户更新自定义字段文本信息
                .setUpdatedefinedUserRoplist(getUpdateDefinedRoplist()) //设置用户更新自定义列表字段信息
                .setFirstMessage(firstMessage.getText().toString()) //设置带入一条消息  会话分配就发送给客服
                .setCustomerUrl(customerUrl.getText().toString()) //设置客户的头像地址
                .setRobot_modelKey(robot_modelKey.getText().toString()) // udesk 机器人配置插件 对应的Id值
                .setConcatRobotUrlWithCustomerInfo(robpt_customer_info.getText().toString())
                .setCommodity(set_use_commodity.isChecked() ? createCommodity() : null)//配置发送商品链接的mode
                .setProduct(set_use_prouct.isChecked() ? createProduct() : null)//配置发送商品链接的mode
                .setExtreFunctions(getExtraFunctions(), new IFunctionItemClickCallBack() {
                    @Override
                    public void callBack(Context context, ChatActivityPresenter mPresenter, int id, String name) {

                        if (id == 21) {
                            UdeskSDKManager.getInstance().toLanuchHelperAcitivty(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
                            mPresenter.sendTxtMessage("打开帮助中心");
                        } else if (id == 22) {
                            mPresenter.sendTxtMessage("打开表单留言");
                            UdeskSDKManager.getInstance().goToForm(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
                        } else if (id == 23) {
                            Toast.makeText(getApplicationContext(), "将要断开xmpp链接！", Toast.LENGTH_LONG).show();
                            UdeskSDKManager.getInstance().disConnectXmpp();
                        }
                    }
                })//在more 展开面板中设置额外的功能按钮
                .setNavigations(set_use_navigation_view.isChecked(), getNavigations(), new INavigationItemClickCallBack() {
                    @Override
                    public void callBack(Context context, ChatActivityPresenter mPresenter, NavigationMode navigationMode) {
                        if (navigationMode.getId() == 1) {
                            mPresenter.sendProductMessage(createProduct());
                        } else if (navigationMode.getId() == 2) {
                            mPresenter.sendTxtMessage(UUID.randomUUID().toString());
                            mPresenter.sendTxtMessage("www.baidu.com");
                        }
                    }
                })//设置是否使用导航UI rue表示使用 false表示不使用
                .setTxtMessageClick(new ITxtMessageWebonCliclk() {
                    @Override
                    public void txtMsgOnclick(String url) {
                        Toast.makeText(getApplicationContext(), "对文本消息中的链接消息处理设置回调", Toast.LENGTH_SHORT).show();
                    }
                })   //如果需要对文本消息中的链接消息处理可以设置该回调，点击事件的拦截回调。 包含表情的不会拦截回调。
                .setFormCallBack(new IUdeskFormCallBack() {
                    @Override
                    public void toLuachForm(Context context) {
                        Toast.makeText(getApplicationContext(), "不用udesk系统提供的留言功能", Toast.LENGTH_SHORT).show();
                    }
                })//离线留言表单的回调接口：  如果不用udesk系统提供的留言功能，可以设置该接口  回调使用自己的处理流程
                .setStructMessageCallBack(new IUdeskStructMessageCallBack() {

                    @Override
                    public void structMsgCallBack(Context context, String josnValue) {
                        Toast.makeText(getApplicationContext(), "结构化消息控件点击事件回调", Toast.LENGTH_SHORT).show();
                    }
                })//设置结构化消息控件点击事件回调接口.
        ;

        return builder;
    }

    private List<FunctionMode> getExtraFunctions() {
        List<FunctionMode> modes = new ArrayList<>();
        FunctionMode functionMode1 = new FunctionMode("帮助中心", 21, R.mipmap.udesk_help_tip);
        FunctionMode functionMode2 = new FunctionMode("表单留言", 22, R.mipmap.udesk_form_table);
        FunctionMode functionMode3 = new FunctionMode("断开xmpp连接", 23, R.mipmap.udesk_form_table);
        modes.add(functionMode1);
        modes.add(functionMode2);
        modes.add(functionMode3);
        return modes;
    }

    private List<NavigationMode> getNavigations() {
        List<NavigationMode> modes = new ArrayList<>();
        NavigationMode navigationMode1 = new NavigationMode("发送商品消", 1);
        NavigationMode navigationMode2 = new NavigationMode("发送文本", 2);
        modes.add(navigationMode1);
        modes.add(navigationMode2);
        return modes;
    }

    private Map<String, String> getDefualtUserInfo() {
        Map<String, String> defualtInfos = new HashMap<>();
        if (!TextUtils.isEmpty(nick_name.getText().toString())) {
            defualtInfos.put(UdeskConst.UdeskUserInfo.NICK_NAME, nick_name.getText().toString());
        }
        if (!TextUtils.isEmpty(cellphone.getText().toString())) {
            defualtInfos.put(UdeskConst.UdeskUserInfo.CELLPHONE, cellphone.getText().toString());
        }
        if (!TextUtils.isEmpty(email.getText().toString())) {
            defualtInfos.put(UdeskConst.UdeskUserInfo.EMAIL, email.getText().toString());
        }
        if (!TextUtils.isEmpty(description.getText().toString())) {
            defualtInfos.put(UdeskConst.UdeskUserInfo.DESCRIPTION, description.getText().toString());
        }
        return defualtInfos;
    }

    private Map<String, String> getDefinedUserTextField() {
        Map<String, String> definedInfos = new HashMap<>();
        if (!TextUtils.isEmpty(textfiledkey.getText().toString())
                && !TextUtils.isEmpty(textfiledvalue.getText().toString())) {
            definedInfos.put(textfiledkey.getText().toString(), textfiledvalue.getText().toString());
        }
        return definedInfos;
    }

    private Map<String, String> getDefinedUserRoplist() {
        Map<String, String> definedRoplistInfos = new HashMap<>();
        return definedRoplistInfos;
    }

    private Map<String, String> getUpdateDefualtUserInfo() {
        Map<String, String> defualtInfos = new HashMap<>();
        if (!TextUtils.isEmpty(update_nick_name.getText().toString())) {
            defualtInfos.put(UdeskConst.UdeskUserInfo.NICK_NAME, update_nick_name.getText().toString());
        }

        if (!TextUtils.isEmpty(update_description.getText().toString())) {
            defualtInfos.put(UdeskConst.UdeskUserInfo.DESCRIPTION, update_description.getText().toString());
        }
        return defualtInfos;
    }

    private Map<String, String> getUpdateDefinedTextField() {
        Map<String, String> definedInfos = new HashMap<>();
        if (!TextUtils.isEmpty(updatetextfiledkey.getText().toString())
                && !TextUtils.isEmpty(updatetextfiledvalue.getText().toString())) {
            definedInfos.put(updatetextfiledkey.getText().toString(), updatetextfiledvalue.getText().toString());
        }
        return definedInfos;
    }

    private Map<String, String> getUpdateDefinedRoplist() {
        Map<String, String> definedRoplistInfos = new HashMap<>();
        return definedRoplistInfos;
    }

    private UdeskCommodityItem createCommodity() {
        UdeskCommodityItem item = new UdeskCommodityItem();
        item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");// 商品主标题
        item.setSubTitle("¥ 99.00");//商品副标题
        item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");// 左侧图片
        item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");// 商品网络链接
        return item;
    }

    private Product createProduct() {
        Product product = new Product();
        product.setImgUrl("http://img12.360buyimg.com/n1/s450x450_jfs/t10675/253/1344769770/66891/92d54ca4/59df2e7fN86c99a27.jpg");
        product.setName(" Apple iPhone X (A1903) 64GB 深空灰色 移动联通4G手机");
        product.setUrl("https://item.jd.com/6748052.html");

        List<Product.ParamsBean> paramsBeans = new ArrayList<>();

        Product.ParamsBean paramsBean0 = new Product.ParamsBean();
        paramsBean0.setText("京 东 价  ");
        paramsBean0.setColor("#C1B6B6");
        paramsBean0.setFold(false);
        paramsBean0.setBreakX(false);
        paramsBean0.setSize(12);

        Product.ParamsBean paramsBean1 = new Product.ParamsBean();
        paramsBean1.setText("￥6999.00");
        paramsBean1.setColor("#E6321A");
        paramsBean1.setFold(true);
        paramsBean1.setBreakX(true);
        paramsBean1.setSize(16);

        Product.ParamsBean paramsBean2 = new Product.ParamsBean();
        paramsBean2.setText("促　销  ");
        paramsBean2.setColor("#C1B6B6");
        paramsBean2.setFold(false);
        paramsBean2.setBreakX(false);
        paramsBean2.setSize(12);

        Product.ParamsBean paramsBean3 = new Product.ParamsBean();
        paramsBean3.setText("满1999元另加30元，或满2999元另加50元，即可在购物车换购热销商品 ");
        paramsBean3.setColor("#E6321A");
        paramsBean3.setFold(true);
        paramsBean3.setBreakX(false);
        paramsBean3.setSize(16);
        paramsBeans.add(paramsBean0);
        paramsBeans.add(paramsBean1);
        paramsBeans.add(paramsBean2);
        paramsBeans.add(paramsBean3);

        product.setParams(paramsBeans);

        return product;
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if (compoundButton.getId() == R.id.mark) {
            mark.setChecked(b);
            force_quit.setChecked(false);
        } else if (compoundButton.getId() == R.id.force_quit) {
            mark.setChecked(false);
            force_quit.setChecked(b);
        } else if (compoundButton.getId() == R.id.portrait) {
            portrait.setChecked(b);
            landscape.setChecked(false);
            user.setChecked(false);
        } else if (compoundButton.getId() == R.id.landscape) {
            portrait.setChecked(false);
            landscape.setChecked(b);
            user.setChecked(false);
        } else if (compoundButton.getId() == R.id.user) {
            portrait.setChecked(false);
            landscape.setChecked(false);
            user.setChecked(b);
        } else if (compoundButton.getId() == R.id.set_ch) {
            set_ch.setChecked(b);
            set_en.setChecked(false);
            LocalManageUtil.saveSelectLanguage(this, Locale.CHINA);
        } else if (compoundButton.getId() == R.id.set_en) {
            set_ch.setChecked(false);
            set_en.setChecked(b);
            LocalManageUtil.saveSelectLanguage(this, Locale.ENGLISH);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View v) {
        if (v.getId() == R.id.udesk_by_agentid) {
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
                    UdeskSDKManager.getInstance().entryChat(getApplicationContext(),
                            makeBuilder().setAgentId(editText.getText().toString().trim(), true).build(),
                            PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken")
                    );
                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else if (v.getId() == R.id.udesk_by_groupid) {
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
                    UdeskSDKManager.getInstance().entryChat(getApplicationContext(),
                            makeBuilder().setGroupId(editText.getText().toString().trim(), true).build(),
                            PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken")
                    );
                }
            });
            dialog.setCancleTextViewOnclick(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else if (v.getId() == R.id.udesk_unread_msg) {
            //获取未读消息
            List<MessageInfo> unReadMsgs = UdeskSDKManager.getInstance().getUnReadMessages(getApplicationContext(), PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken"));
            if (unReadMsgs == null || unReadMsgs.isEmpty()) {
                Toast.makeText(UdeskFuncationExampleActivity.this, "没有未读消息", Toast.LENGTH_SHORT).show();
                return;
            }
            final UdeskCustomDialog dialog = new UdeskCustomDialog(UdeskFuncationExampleActivity.this);
            dialog.setDialogTitle("未读消息");
            ListView mListview = dialog.getListView();
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
        } else if (v.getId() == R.id.udesk_unread_msgcount) {
            //获取未读消息数量
            int unreadMsg = UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount(getApplicationContext(), PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken"));
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
        }
//        else if (v.getId() == R.id.udesk_en) {
////            UdeskUtil.changeAppLanguage(getApplicationContext(), Locale.ENGLISH);
//            UdeskSDKManager.getInstance().entryChat(getApplicationContext(), makeBuilder().setLocale(Locale.ENGLISH).build(),
//                    PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken")
//            );
//        }
        else if (v.getId() == R.id.udesk_conversion_bysetting_menu) {
            UdeskSDKManager.getInstance().entryChat(getApplicationContext(), makeBuilder().build(),
                    PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken")
            );
        }

    }


}
