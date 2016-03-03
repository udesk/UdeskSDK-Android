package cn.udesk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.adapter.SurvyOptionAdapter;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SurveyOptionsModel;

public class SurvyDialogActivity extends Activity implements OnItemClickListener , OnClickListener{
	SurveyOptionsModel surveyOptions = null;
	ListView optionsListView;
	SurvyOptionAdapter adapter ;
	TextView titleView;
	TextView desc;
	TextView cancle;
	TextView ok;
	OptionsModel checkOptions;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_survy_view);
		Intent intent  = getIntent();
		if(intent != null){
			surveyOptions = (SurveyOptionsModel) intent.getSerializableExtra(UdeskConst.SurvyDialogKey);
		}
		
		if(surveyOptions != null){
			optionsListView = (ListView) findViewById(R.id.udesk_list_choice);
			titleView = (TextView)findViewById(R.id.udesk_title);
			desc = (TextView) findViewById(R.id.udesk_desc);
			cancle = (TextView)findViewById(R.id.udesk_cancle);
			ok = (TextView)findViewById(R.id.udesk_ok);
			cancle.setOnClickListener(this);
			ok.setOnClickListener(this);
			adapter = new SurvyOptionAdapter(this,surveyOptions);
			titleView.setText(surveyOptions.getTitle());
			desc.setText(surveyOptions.getDesc());
			optionsListView.setAdapter(adapter);
			optionsListView.setOnItemClickListener(this);
			if(surveyOptions.getOptions().get(0) != null){
				checkOptions = surveyOptions.getOptions().get(0);
			}
		}
		
	
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		checkOptions = adapter.getItem(position);
		adapter.updateCheckOptions(position);
	}
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.udesk_cancle){
			finish();
		}else if(v.getId() == R.id.udesk_ok){
			Intent mIntent = new Intent();  
	        mIntent.putExtra(UdeskConst.SurvyOptionIDKey, checkOptions.getId());  
	        SurvyDialogActivity.this.setResult(Activity.RESULT_OK, mIntent);
			finish();
		}
		
	}

}
