package udesk.sdk.demo.maps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.udesk.config.UdeskConfig;
import udesk.sdk.demo.R;


public class LocationActivity extends Activity implements View.OnClickListener, GeocodeSearch.OnGeocodeSearchListener,
        AMap.OnCameraChangeListener, AdapterView.OnItemClickListener {


    private MapView mMapView;

    private AMap mMap;

    private AMapLocationClient locationClient = null;//定位类

    /**
     * 首次进入的当前位置信息
     */
    private AMapLocation mCurrentLocation;
    /**
     * 当前的一对经、纬度值
     */
    private LatLonPoint mCurrentLatLonPoint;

    private GeocodeSearch mSearch;

    /**
     * 附近地点列表
     */
    private ListView lv_location_position;
    /**
     * 列表适配器
     */
    private LocationAdapter locatorAdapter;
    /**
     * 列表数据
     */
    private List<PoiItem> datas;

    /**
     * 进度条
     */
    private ProgressBar pb_location_load_bar;

    /**
     * 标题栏
     */
    private FrameLayout fl_back;
    private FrameLayout fl_search;
    private TextView tv_send;

    /**
     * 获取的位置
     */
    private PoiItem mPoiItem;

    //截图的缓存sd卡的路径
    private String bitmapdir = "";

    private boolean isTouch = true;

    /**
     * 请求码
     */
    private final static int REQUEST_CODE = 0x123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        initUI();
        mMapView.onCreate(savedInstanceState);
        InitLocation();
    }

    private void initUI() {
        lv_location_position = (ListView) findViewById(R.id.lv_location_position);
        pb_location_load_bar = (ProgressBar) findViewById(R.id.pb_location_load_bar);
        fl_back = (FrameLayout) findViewById(R.id.fl_back);
        fl_search = (FrameLayout) findViewById(R.id.fl_search);
        tv_send = (TextView) findViewById(R.id.tv_send);
        // 列表初始化
        datas = new ArrayList();
        locatorAdapter = new LocationAdapter(getApplicationContext(), datas);
        lv_location_position.setAdapter(locatorAdapter);
        // 注册监听
        lv_location_position.setOnItemClickListener(this);
        fl_back.setOnClickListener(this);
        fl_search.setOnClickListener(this);
        tv_send.setOnClickListener(this);
        mMapView = (MapView) findViewById(R.id.bmapView);
        // 地图初始化
        mMap = mMapView.getMap();
        // 设置为普通矢量图地图
        mMap.setMapType(AMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        // 设置缩放比例(500米)
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16f));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        //设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.5f));
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapTouchListener(touchListener);

        mSearch = new GeocodeSearch(this);
        mSearch.setOnGeocodeSearchListener(this);
    }

    private void InitLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        //设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
        locationClient.startLocation();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_back:  //返回
                finish();
                break;
            case R.id.fl_search:  //查找
                Intent search_intent = new Intent(LocationActivity.this, SearchPositionActivity.class);
                startActivityForResult(search_intent, REQUEST_CODE);
                break;
            case R.id.tv_send:  //发送
                if (mPoiItem == null) {
                    Toast.makeText(getApplicationContext(), "请选择详细地址", Toast.LENGTH_SHORT).show();
                    return;
                }

                mMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {
                        saveBitmap(bitmap);
                        Intent intent = new Intent();
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Position, mPoiItem.getTitle());
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Latitude, mPoiItem.getLatLonPoint().getLatitude());
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Longitude, mPoiItem.getLatLonPoint().getLongitude());
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.BitmapDIR, bitmapdir);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            locatorAdapter.setSelectItemIndex(0);

            // 获取经纬度
            PoiItem poiItem = data.getParcelableExtra("PoiItem");
            mCurrentLatLonPoint = poiItem.getLatLonPoint();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLatLonPoint.getLatitude(), mCurrentLatLonPoint.getLongitude()), 16));
            searchPoi();

        }
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


    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        isTouch = false;
        // 设置选中项下标，并刷新
        locatorAdapter.setSelectItemIndex(position);
        locatorAdapter.notifyDataSetChanged();
        mMap.clear();
        PoiItem info = (PoiItem) locatorAdapter.getItem(position);
        mPoiItem = info;
        mCurrentLatLonPoint = info.getLatLonPoint();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLatLonPoint.getLatitude(), mCurrentLatLonPoint.getLongitude()), 16));
    }


    /**
     * 定位监听
     */
    final AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                mCurrentLocation = loc;
                mCurrentLatLonPoint = new LatLonPoint(loc.getLatitude(), loc.getLongitude());
                Log.e("xxx", "定位详细信息：" + loc.toString());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 16));
                //查询周边
                searchPoi();
            } else {
                Toast.makeText(getApplicationContext(), "定位失败，请打开位置权限", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /**
     * 显示列表，查找附近的地点
     */
    public void searchPoi() {
        if (mCurrentLatLonPoint == null) {
            return;
        }

        datas.clear();
        RegeocodeQuery query = new RegeocodeQuery(mCurrentLatLonPoint, 200, GeocodeSearch.AMAP);
        mSearch.getFromLocationAsyn(query);
        lv_location_position.setSelection(0);
        locatorAdapter.setSelectItemIndex(0);
        pb_location_load_bar.setVisibility(View.VISIBLE);
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        // 逆地址编码回调：坐标->地址
        if (rCode == 1000 && regeocodeResult != null &&
                regeocodeResult.getRegeocodeAddress() != null
                && regeocodeResult.getRegeocodeQuery() != null) {

            regeocodeResult.getRegeocodeAddress().getPois();
            datas.clear();
            if (regeocodeResult.getRegeocodeAddress().getPois() != null && regeocodeResult.getRegeocodeAddress().getPois().size() > 0) {
                datas.addAll(regeocodeResult.getRegeocodeAddress().getPois());
                mPoiItem = datas.get(0);
            }
            locatorAdapter.notifyDataSetChanged();
            pb_location_load_bar.setVisibility(View.GONE);
        }
    }


    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

        if (isTouch) {
            mCurrentLatLonPoint = new LatLonPoint(cameraPosition.target.
                    latitude, cameraPosition.target.longitude);

            searchPoi();
        }

    }


    // 地图触摸事件监听器
    final AMap.OnMapTouchListener touchListener = new AMap.OnMapTouchListener() {
        @Override
        public void onTouch(MotionEvent event) {
            isTouch = true;
        }
    };


    /**
     * 保存方法
     */
    public void saveBitmap(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File f = getOutputMediaFile(this, "IMG_" + timeStamp + ".jpg");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            bitmapdir = f.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static File getOutputMediaFile(Context context, String mediaName) {
        File mediaFile = null;
        try {
            File mediaStorageDir = null;
            try {
                mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "UDeskMap");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mediaName);
        } catch (Exception e) {
            return null;
        }
        return mediaFile;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }


}