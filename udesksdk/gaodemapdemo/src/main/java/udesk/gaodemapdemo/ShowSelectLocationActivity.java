package udesk.gaodemapdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;


import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.LatLng;


import cn.udesk.config.UdeskConfig;



public class ShowSelectLocationActivity extends Activity implements View.OnClickListener {



    /**
     * 标题栏
     */
    private FrameLayout fl_back;
    private MapView mMapView;

    private AMap mMap;

    private double lat;
    private double longit;
    private String value = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        value = getIntent().getStringExtra(UdeskConfig.UdeskMapIntentName.Position);
        lat = getIntent().getDoubleExtra(UdeskConfig.UdeskMapIntentName.Latitude, 0.0);
        longit = getIntent().getDoubleExtra(UdeskConfig.UdeskMapIntentName.Longitude, 0.0);
        initUI();
        mMapView.onCreate(savedInstanceState);
        loadLocation(lat, longit);
    }

    /**
     * 初始化Ui
     */
    private void initUI() {


        fl_back = (FrameLayout) findViewById(R.id.fl_back);
        mMapView = (MapView) findViewById(R.id.bmapView);

        // 地图初始化
        mMap = mMapView.getMap();

        fl_back.setOnClickListener(this);
        // 设置为普通矢量图地图
        mMap.setMapType(AMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        // 设置缩放比例(500米)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16f));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        mMap.setMyLocationEnabled(true);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.fl_back:  //返回
                ShowSelectLocationActivity.this.finish();
                break;

        }
    }

    private void loadLocation(double lat, double longit) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longit), 18));
    }



    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }


}