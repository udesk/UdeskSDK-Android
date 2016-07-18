package udesk.udesksdk;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class CustomDialog extends Dialog {

    /**
     * Dialog的内容输入编辑器
     */
    private EditText editText;
    /**
     * 确定按钮
     */
    private Button positiveButton;
    /**
     * 取消按钮
     */
    private Button  negativeButton;

    /**
     * 对话框的标题
     */
    private TextView title;

    public CustomDialog(Context context) {
        super(context, R.style.add_dialog);
        initView();
    }

    /**
     * 控件初始化
     */
    private void initView() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_normal_layout, null);
        title = (TextView) mView.findViewById(R.id.title);
        editText = (EditText) mView.findViewById(R.id.udesk_id);
        positiveButton = (Button) mView.findViewById(R.id.positiveButton);
        negativeButton = (Button) mView.findViewById(R.id.negativeButton);
        super.setContentView(mView);
    }

    /**
     * 返回编辑的EditText
     * @return
     */
    public View getEditText() {
        return editText;
    }

    /**
     * s设置对话框的标题
     * @param name
     */
    public void setDialogTitle(String name) {
        if (title != null) {
            title.setText(name);
        }
    }


    /**
     * 确定键监听器
     *
     * @param listener
     */
    public void setOnPositiveListener(View.OnClickListener listener) {
        positiveButton.setOnClickListener(listener);
    }

    /**
     * 取消键监听器
     *
     * @param listener
     */
    public void setOnNegativeListener(View.OnClickListener listener) {
        negativeButton.setOnClickListener(listener);
    }

}
