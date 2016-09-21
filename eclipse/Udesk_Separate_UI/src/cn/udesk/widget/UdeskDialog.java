package cn.udesk.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;

import cn.udesk.R;

public class UdeskDialog extends Dialog{
	
	Context context;
	private ProgressBar progressBar;

	public UdeskDialog(Context context, int theme) {
		super(context, theme);
		this.context= context;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_dialog);
		progressBar= (ProgressBar) findViewById(R.id.udesk_progressbar);
	}

}
