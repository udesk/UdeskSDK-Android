package udesk.tenxunmapdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.tencentmap.mapsdk.map.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.map.MapActivity;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import cn.udesk.config.UdeskConfig;


public class ShowSelectLocationActivity extends MapActivity implements View.OnClickListener {


    /**
     * 标题栏
     */
    private FrameLayout fl_back;

    private TencentLocationManager mLocationManager;
    private MapView mMapView;

    private TencentMap mMap;
    /**
     * 按钮：回到原地
     */
    private ImageView img_location_back_origin;
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
//        loadLocation(lat, longit);
    }

    /**
     * 初始化Ui
     */
    private void initUI() {


        fl_back = (FrameLayout) findViewById(R.id.fl_back);
        mMapView = (MapView) findViewById(R.id.bmapView);
        img_location_back_origin = (ImageView) findViewById(R.id.img_location_back_origin);

        // 地图初始化
        mMap = mMapView.getMap();
        fl_back.setOnClickListener(this);
        img_location_back_origin.setOnClickListener(this);
        mMap.setCenter(new LatLng(lat, longit));
        mMap.setZoom(16);
//        mLocationManager = TencentLocationManager.getInstance(this);
//        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.fl_back:  //返回
                ShowSelectLocationActivity.this.finish();
                break;
            case R.id.img_location_back_origin:
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longit), 18));
                break;

        }
    }

    private void loadLocation(double lat, double longit) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, longit), 18));
    }


}