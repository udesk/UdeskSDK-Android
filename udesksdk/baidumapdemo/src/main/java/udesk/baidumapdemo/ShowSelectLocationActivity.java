package udesk.baidumapdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.udesk.config.UdeskConfig;

import static android.content.ContentValues.TAG;


/**
 * 显示百度地图页面
 *
 * @author chenjunxu
 * @date 16/12/23
 */
public class ShowSelectLocationActivity extends Activity implements View.OnClickListener {

    /**
     * 显示的地图
     */
    protected MapView bmapView;
    /**
     * 百度地图对象
     */
    private BaiduMap mBaiduMap;

    /**
     * 定位
     */
    private LocationClient mLocClient;
    // MapView 中央对于的屏幕坐标
    private android.graphics.Point mCenterPoint = null;

    /**
     * 按钮：回到原地
     */
    private ImageView img_location_back_origin;

    /**
     * 标题栏
     */
    private FrameLayout fl_back;


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
        loadLocation(lat, longit);
    }

    /**
     * 初始化Ui
     */
    private void initUI() {

        img_location_back_origin = (ImageView) findViewById(R.id.img_location_back_origin);
        fl_back = (FrameLayout) findViewById(R.id.fl_back);
        bmapView = (MapView) findViewById(R.id.bmapView);

        // 地图初始化
        mBaiduMap = bmapView.getMap();
        // 设置为普通矢量图地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        bmapView.setPadding(10, 0, 0, 10);
        bmapView.showZoomControls(false);
        // 设置缩放比例(500米)
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(16.0f);
        mBaiduMap.setMapStatus(msu);

        // 初始化当前 MapView 中心屏幕坐标
        mCenterPoint = mBaiduMap.getMapStatus().targetScreen;

        // 定位初始化
        mLocClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        img_location_back_origin.setOnClickListener(this);
        fl_back.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_location_back_origin:  //回到原点
                // 实现动画跳转
                img_location_back_origin.setImageResource(R.drawable.back_origin_select);
                loadLocation(lat, longit);
                break;
            case R.id.fl_back:  //返回
                ShowSelectLocationActivity.this.finish();
                break;

        }
    }

    private void loadLocation(double lat, double longit) {
        LatLng currentLatLng = new LatLng(lat, longit);
        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(currentLatLng);
        mBaiduMap.animateMapStatus(u);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        bmapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        bmapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        bmapView.onDestroy();
        bmapView = null;
    }


}