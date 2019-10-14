package cn.udesk.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.RobotTipBean;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.ViewHolder> {
    private List<RobotTipBean.ListBean> list=new ArrayList<>();
    private String content="";
    private Context context;

    public TipAdapter(Context context) {
        this.context = context;
    }

    public void setList(List<RobotTipBean.ListBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setContent(String content) {
        this.content = content;
        notifyDataSetChanged();
    }

    public void setListAndContent(List<RobotTipBean.ListBean> list,String content){
        this.list.clear();
        this.content="";
        this.list=list;
        this.content=content;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.udesk_robot_view_tip,viewGroup,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        try{
            final RobotTipBean.ListBean listBean = list.get(i);
            if (listBean!=null){
                String question = (String) listBean.getQuestion();
                if (question.contains(content)){
                    int index=question.indexOf(content);
                    SpannableString spannableString=new SpannableString(question);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#FF7701")),index,index+content.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    viewHolder.tvTip.setText(spannableString);
                }else {
                    viewHolder.tvTip.setText(question);
                }
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InvokeEventContainer.getInstance().event_OnTipClick.invoke(listBean);
                    }
                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView tvTip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTip = itemView.findViewById(R.id.udesk_robot_tv);
        }
    }
}
