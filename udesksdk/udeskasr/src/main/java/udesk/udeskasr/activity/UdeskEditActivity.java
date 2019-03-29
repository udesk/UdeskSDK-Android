package udesk.udeskasr.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import udesk.udeskasr.R;

public class UdeskEditActivity extends Activity {

    private EditText asrText;
    private String intentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udesk_edit);
        initView();
        initIntent();
    }

    private void initIntent() {
        try {
            Intent intent=getIntent();
            if (intent!=null){
                intentText = intent.getStringExtra(UdeskConstant.UDESK_ASR_TEXT);
                asrText.setText(intentText);
                asrText.setSelection(intentText.length());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initView() {
        findViewById(R.id.udesk_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra(UdeskConstant.UDESK_EDIT_TEXT,asrText.getText().toString());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        asrText = findViewById(R.id.udesk_edit);
    }
}
