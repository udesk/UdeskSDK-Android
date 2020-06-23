package cn.udesk.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.adapter.SurvyAdapter;
import cn.udesk.adapter.TagAdapter;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.Tag;
import udesk.core.UdeskConst;

import static udesk.core.UdeskConst.REMARK_OPTION_HIDE;

/**
 * Created by user on 2018/3/28.
 */

public class UdeskSurvyPopwindow extends PopupWindow {

    private String text = "text";
    private String expression = "expression";
    private String star = "star";

    private View mMenuView, remarkView, remarkTips;
    private TextView title, starDes, summit;
    private LinearLayout cancle;
    private RecyclerView contentRecyclerView, optionTagRecycele;
    private EditText remarkEt;
    private SurvyAdapter contentAdapter;
    private TagAdapter tagAdapter;
    private List<Tag> choiceTags;
    private OptionsModel choiceOptionsModel = null;


    public interface SumbitSurvyCallBack {
        void sumbitSurvyCallBack(boolean isRobot,String optionId, String show_type, String survey_remark, String tags);
    }

    @SuppressLint("WrongConstant")
    public UdeskSurvyPopwindow(final Activity context, final SurveyOptionsModel surveyOptions, final SumbitSurvyCallBack callBack) {
        super(context);
        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            mMenuView = inflater.inflate(R.layout.udesk_survy_view, null);
            title = (TextView) mMenuView.findViewById(R.id.udesk_title);
            starDes = (TextView) mMenuView.findViewById(R.id.star_des);
            cancle = (LinearLayout) mMenuView.findViewById(R.id.udesk_survy_cancle);
            contentRecyclerView = (RecyclerView) mMenuView.findViewById(R.id.rv_text_list);
            optionTagRecycele = (RecyclerView) mMenuView.findViewById(R.id.rv_options_tags);
            remarkView = mMenuView.findViewById(R.id.udesk_remark_rl);
            remarkEt = (EditText) mMenuView.findViewById(R.id.udesk_remark_et);
            remarkEt.setHint(surveyOptions.getRemark());
            remarkTips = (TextView) mMenuView.findViewById(R.id.udesk_must_reamrk_tips);
            summit = (TextView) mMenuView.findViewById(R.id.submit_survy_tv);
            title.setText(surveyOptions.getTitle());

            cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            this.setContentView(mMenuView);
            this.setWidth(LayoutParams.FILL_PARENT);
            //设置SelectPicPopupWindow弹出窗体的高
            this.setHeight(LayoutParams.WRAP_CONTENT);
            //设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            setSoftInputMode(PopupWindow.INPUT_METHOD_NEEDED);
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.udesk_survy_anim);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0xb0000000);
            this.setBackgroundDrawable(dw);
            //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
            mMenuView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    int height = mMenuView.findViewById(R.id.udesk_root).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });

            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            int viewType;
            if (surveyOptions.getType().equals(expression)) {
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                viewType = UdeskConst.UdeskSurvyShowType.EXPRESSION;
            } else if (surveyOptions.getType().equals(star)) {
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                viewType = UdeskConst.UdeskSurvyShowType.STAR;
            } else {
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                viewType = UdeskConst.UdeskSurvyShowType.TEXT;
            }
            contentRecyclerView.setLayoutManager(layoutManager);
            contentRecyclerView.setItemAnimator(new DefaultItemAnimator());
            contentAdapter = new SurvyAdapter(context, surveyOptions.getOptions(), viewType, surveyOptions.getDefault_option_id());
            contentRecyclerView.setAdapter(contentAdapter);
            contentAdapter.setOnItemClickListener(new SurvyAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int postion, int viewtype, OptionsModel data) {
                    int checkId = contentAdapter.getCheckId();
                    if (checkId == data.getId()) {
                        contentAdapter.setCheckId(-1);
                        contentAdapter.notifyItemChanged(postion);
                        choiceOptionsModel = null;
                        //取消标记
                        setTags(new ArrayList<Tag>());
                        remarkView.setVisibility(View.GONE);
                        remarkTips.setVisibility(View.GONE);
                        if (choiceTags != null) {
                            choiceTags.clear();
                        }
                    } else {
                        choiceOptionsModel = data;
                        if (choiceTags != null) {
                            choiceTags.clear();
                        }
                        contentAdapter.setCheckId(data.getId());
                        contentAdapter.notifyDataSetChanged();
                        //显示标记
                        setTags(choiceOptionsModel.getTags());
                        checkRemarkOption(choiceOptionsModel);
                    }

                }
            });

            tagAdapter = new TagAdapter(context);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
            optionTagRecycele.setLayoutManager(gridLayoutManager);
            optionTagRecycele.setItemAnimator(new DefaultItemAnimator());
            optionTagRecycele.setAdapter(tagAdapter);
            tagAdapter.setOnItemClickListener(new TagAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, List<Tag> datas) {
                    choiceTags = datas;
                }
            });

            if (surveyOptions.getDefault_option_id() > 0) {
                for (OptionsModel model : surveyOptions.getOptions()) {
                    if (model.getId() == surveyOptions.getDefault_option_id()) {
                        choiceOptionsModel = model;
                        checkRemarkOption(choiceOptionsModel);
                        setTags(choiceOptionsModel.getTags());
                    }
                }
            }

            summit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (callBack != null && choiceOptionsModel != null) {
                        if (choiceOptionsModel.getRemark_option().equals(UdeskConst.REMARK_OPTION_REQUIRED) && TextUtils.isEmpty(remarkEt.getText().toString())) {
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.summit_must_remark), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(remarkEt.getText().toString().length() > 255) {
                            Toast.makeText(context.getApplicationContext(), context.getString(R.string.summit_out_of_range), Toast.LENGTH_LONG).show();
                            return;
                        }
                        callBack.sumbitSurvyCallBack(surveyOptions.isRobot(),String.valueOf(choiceOptionsModel.getId()), surveyOptions.getType(), remarkEt.getText().toString(), listToString(choiceTags));
                        dismiss();
                    } else {
                        Toast.makeText(context.getApplicationContext(), context.getString(R.string.summit_must_survey), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkRemarkOption(OptionsModel model) {

        try {
            if (model.getRemark_option().equals(REMARK_OPTION_HIDE)) {
                remarkView.setVisibility(View.GONE);
                remarkTips.setVisibility(View.GONE);
            } else {
                remarkView.setVisibility(View.VISIBLE);
                if (model.getRemark_option().equals(UdeskConst.REMARK_OPTION_REQUIRED)) {
                    remarkTips.setVisibility(View.VISIBLE);
                } else {
                    remarkTips.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setTags(List<Tag> tags) {
        try {
            for (Tag tag : tags) {
                tag.setCheck(false);
            }
            if (tags!= null && tags.size()>0){
                optionTagRecycele.setVisibility(View.VISIBLE);
            }else {
                optionTagRecycele.setVisibility(View.GONE);
            }
            tagAdapter.setDatas(tags);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String listToString(List<Tag> tags) {
        StringBuilder builder = new StringBuilder();
        try {
            if (tags == null || tags.isEmpty()) {
                return "";
            }
            for (int i = 0; i < tags.size(); i++) {
                if (i == 0) {
                    builder.append(tags.get(i).getText());
                } else {
                    builder.append(",").append(tags.get(i).getText());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

}
