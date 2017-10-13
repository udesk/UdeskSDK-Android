package udesk.tenxunmapdemo;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.Location;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.lbssearch.object.result.SuggestionResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.mapsdk.raster.model.CameraPosition;
import com.tencent.mapsdk.raster.model.LatLng;
import com.tencent.tencentmap.mapsdk.map.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.map.MapView;
import com.tencent.tencentmap.mapsdk.map.TencentMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.udesk.config.UdeskConfig;


public class LocationActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener,
        TencentLocationListener, TencentMap.OnMapCameraChangeListener {


    private MapView mMapView;

    private TencentMap mMap;

    private TencentLocationManager mLocationManager;

    /**
     * 首次进入的当前位置信息
     */
    private TencentLocation mCurrentLocation;
    //    /**
//     * 当前的一对经、纬度值
//     */
    private LatLng mCurrentLatLonPoint;
    //
    private TencentSearch mSearch;

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
    private List<Geo2AddressResultObject.ReverseAddressResult.Poi> datas;

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
    private Geo2AddressResultObject.ReverseAddressResult.Poi mPoiItem;

    /**
     * 按钮：回到原地
     */
    private ImageView img_location_back_origin;

    //截图的缓存sd卡的路径
    private String bitmapdir = "";

    private boolean isfirst = true;


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
        img_location_back_origin = (ImageView) findViewById(R.id.img_location_back_origin);
        fl_back = (FrameLayout) findViewById(R.id.fl_back);
        fl_search = (FrameLayout) findViewById(R.id.fl_search);
        tv_send = (TextView) findViewById(R.id.tv_send);
        // 列表初始化
        datas = new ArrayList();
        locatorAdapter = new LocationAdapter(this, datas);
        lv_location_position.setAdapter(locatorAdapter);
        // 注册监听
        lv_location_position.setOnItemClickListener(this);
        fl_back.setOnClickListener(this);
        fl_search.setOnClickListener(this);
        tv_send.setOnClickListener(this);
        img_location_back_origin.setOnClickListener(this);

        mMapView = (MapView) findViewById(R.id.bmapView);
        // 地图初始化
        mMap = mMapView.getMap();
        mMap.setZoom(18);
        mLocationManager = TencentLocationManager.getInstance(this);
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);

        mMap.setOnMapCameraChangeListener(this);
        mSearch = new TencentSearch(this);
    }

    private void InitLocation() {
        TencentLocationRequest request = TencentLocationRequest.create();
        request.setInterval(2000);
        request.setAllowGPS(true);
        mLocationManager.requestLocationUpdates(request, this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_location_back_origin:  //回到原点
                if (mCurrentLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()), 16));
                    searchPoi();
                    mLocationManager.removeUpdates(this);

                }
                break;
            case R.id.fl_back:  //返回
                LocationActivity.this.finish();
                break;
            case R.id.fl_search:  //查找
//                Intent search_intent = new Intent(LocationActivity.this, SearchPositionActivity.class);
//                startActivityForResult(search_intent, REQUEST_CODE);
                break;
            case R.id.tv_send:  //发送
                if (mPoiItem == null) {
                    Toast.makeText(this, "请选择详细地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                mMap.getScreenShot(new TencentMap.OnScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {
                        saveBitmap(bitmap);
                        Intent intent = new Intent();
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Position, mPoiItem.title);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Latitude, (double) mPoiItem.location.lat);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Longitude, (double) mPoiItem.location.lng);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.BitmapDIR, bitmapdir);
                        setResult(RESULT_OK, intent);
                        LocationActivity.this.finish();
                    }
                });


                break;
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//
//        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
//            locatorAdapter.setSelectItemIndex(0);
//
//            // 获取经纬度
//            PoiItem poiItem = data.getParcelableExtra("PoiItem");
//            mCurrentLatLonPoint = poiItem.getLatLonPoint();
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLatLonPoint.getLatitude(), mCurrentLatLonPoint.getLongitude()), 16));
//            searchPoi();
//
//        }
//    }

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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

//        isTouch = false;
        // 设置选中项下标，并刷新
        locatorAdapter.setSelectItemIndex(position);
        locatorAdapter.notifyDataSetChanged();
        mPoiItem = (Geo2AddressResultObject.ReverseAddressResult.Poi) locatorAdapter.getItem(position);
        mCurrentLatLonPoint = new LatLng(mPoiItem.location.lat, mPoiItem.location.lng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLatLonPoint.getLatitude(), mCurrentLatLonPoint.getLongitude()), 16));
    }


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
            Log.i("xxx", "已经保存 =" + f.toString());
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
        mLocationManager.removeUpdates(this);
        mMapView.onDestroy();
    }


    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {

        if (isfirst){
            mCurrentLocation = tencentLocation;
            //定位成功
            mCurrentLatLonPoint = new LatLng(tencentLocation.getLatitude(), tencentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLonPoint, 16));
            searchPoi();
            if (mCurrentLocation != null) {
                mLocationManager.removeUpdates(this);
            }
            isfirst = false;
        }



    }

    /**
     * 显示列表，查找附近的地点
     */
    public void searchPoi() {
        if (mCurrentLatLonPoint == null) {
            return;
        }
        Location location = new Location((float) mCurrentLatLonPoint.getLatitude(), (float) mCurrentLatLonPoint.getLongitude());

        Geo2AddressParam geo2AddressParam = new Geo2AddressParam().location(location).get_poi(true);
        geo2AddressParam.get_poi(true);
        mSearch.geo2address(geo2AddressParam, new HttpResponseListener() {
            @Override
            public void onSuccess(int i, BaseObject baseObject) {
                datas.clear();
                Geo2AddressResultObject object = (Geo2AddressResultObject) baseObject;
                datas.addAll(object.result.pois);
                mPoiItem = datas.get(0);
                locatorAdapter.notifyDataSetChanged();
                pb_location_load_bar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int i, String s, Throwable throwable) {

                Throwable throwable1 = throwable;
            }
        });


        lv_location_position.setSelection(0);
        locatorAdapter.setSelectItemIndex(0);
        pb_location_load_bar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

        mCurrentLatLonPoint = cameraPosition.getTarget();
        searchPoi();

    }
}