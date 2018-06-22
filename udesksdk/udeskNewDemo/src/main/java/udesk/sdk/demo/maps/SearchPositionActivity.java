package udesk.sdk.demo.maps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import udesk.sdk.demo.R;

/**
 * Created by user on 2017/8/18.
 */

public class SearchPositionActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener, PoiSearch.OnPoiSearchListener {

    /**
     * 附近地点列表
     */
    private ListView lv_locator_search_position;

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
    private ProgressBar pb_location_search_load_bar;

    /**
     * 输入框
     */
    private EditText et_search;
    /**
     * 返回
     */
    private FrameLayout fl_search_back;

    /**
     * 发送
     */
    private TextView tv_search_send;

    private String mKeyWord;

    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_position);
        initUI();
    }

    /**
     * 初始化Ui
     */
    private void initUI() {

        lv_locator_search_position = (ListView) findViewById(R.id.lv_locator_search_position);
        fl_search_back = (FrameLayout) findViewById(R.id.fl_search_back);
        tv_search_send = (TextView) findViewById(R.id.tv_search_send);
        pb_location_search_load_bar = (ProgressBar) findViewById(R.id.pb_location_search_load_bar);
        et_search = (EditText) findViewById(R.id.et_search);


        // 列表初始化
        datas = new ArrayList();
        locatorAdapter = new LocationAdapter(this, datas);
        lv_locator_search_position.setAdapter(locatorAdapter);

        // 注册监听
        lv_locator_search_position.setOnItemClickListener(this);
        fl_search_back.setOnClickListener(this);
        tv_search_send.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_search_back:
                SearchPositionActivity.this.finish();
                break;
            case R.id.tv_search_send:
                if (!TextUtils.isEmpty(et_search.getText().toString())) {
                    pb_location_search_load_bar.setVisibility(View.VISIBLE);
                    // 根据输入框的内容，进行搜索
//                    mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().keyword(et_search.getText().toString()).city(""));
                    mKeyWord = et_search.getText().toString();
                    datas.clear();
                    doSearchQuery(mKeyWord);
                } else {
                    Toast.makeText(SearchPositionActivity.this, "请输入地点", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    protected void doSearchQuery(String keyWord) {

        query = new PoiSearch.Query(keyWord, "", "");// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(1);// 设置查第一页
        query.setCityLimit(true);
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = new Intent();
        // 设置坐标
        intent.putExtra("PoiItem", datas.get(position));
        setResult(RESULT_OK, intent);
        SearchPositionActivity.this.finish();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        pb_location_search_load_bar.setVisibility(View.GONE);
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                datas.clear();
                List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                datas.addAll(poiItems);
                locatorAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "没有收到结果：" + rCode, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "搜索失败：" + rCode, Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
