package cn.udesk.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

import cn.udesk.R;
import cn.udesk.fragment.UdeskHelperFragment;
import cn.udesk.widget.UdeskTitleBar;

public class UdeskHelperActivity extends FragmentActivity {
	
	private UdeskTitleBar  mTitlebar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_activity_base);
		Fragment helpFragment = Fragment.instantiate(this,
				UdeskHelperFragment.class.getName());
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.udesk_demo_fragment_view, helpFragment)
				.commitAllowingStateLoss();
		settingTitlebar();
	}
	
	
	/**
	 * titlebar 的设置
	 */
	private void settingTitlebar(){
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		if(mTitlebar != null){
			mTitlebar.setTitleTextSequence(getString(R.string.udesk_navi_helper_title_main));
			mTitlebar.setLeftTextVis(View.VISIBLE);
			mTitlebar.setLeftViewClick(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
	}

}
