package com.zig.slope;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.zig.slope.bean.Placemark;
import com.zig.slope.view.TakePhotoPopWin;
import com.zig.slope.callback.AllInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 此demo用来展示如何结合定位SDK实现定位，并使用MyLocationOverlay绘制定位位置 同时展示如何使用自定义图标绘制并点击时弹出泡泡
 */
//@Route(path = "/map/index")
public class LocationActivity extends FragmentActivity implements SensorEventListener, AllInterface.OnMenuSlideListener{
    private String userName;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
//    LeftDrawerLayout mLeftDrawerLayout;
//    LeftMenuFragment mMenuFragment;
    MapView mMapView;
    BaiduMap mBaiduMap;
//    View shadowView;
//    ImageView menu_icon;

    // UI相关
    OnCheckedChangeListener radioButtonListener;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==100){
                List<Placemark> points = (List<Placemark>) msg.obj;
                Log.i("zxy", "handleMessage: points="+points.size());
                for (int i = 0;i<points.size();i++){
                    if(points.get(i).getLatLng()!=null) {
                        setMark(points.get(i));
                    }
                }
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        Log.i(TAG, "onCreate: userName==="+userName);
        setContentView(R.layout.activity_location);
//        shadowView = (View) findViewById(R.id.shadow);
//        menu_icon = findViewById(R.id.menu_icon);
//        shadowView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                    closeMenu();
//            }
//        });
//        menu_icon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openMenu();
//            }
//        });
//        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);
//        FragmentManager fm = getSupportFragmentManager();
//        mMenuFragment = (LeftMenuFragment) fm.findFragmentById(R.id.id_container_menu);
//        mLeftDrawerLayout.setOnMenuSlideListener(this);
//
//        if (mMenuFragment == null) {
//            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new LeftMenuFragment()).commit();
//        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        getPoints();

        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioButtonListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                }
                if (checkedId == R.id.customicon) {

                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                }
            }
        };
        group.setOnCheckedChangeListener(radioButtonListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        mLocClient.setLocOption(option);
        mLocClient.start();
        initListener();
    }
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.mipmap.icon_marka);
    /**
     * 当前地点击点
     */
    private LatLng currentPt;
    private String touchType;

    private void initListener() {
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {

            @Override
            public void onTouch(MotionEvent event) {

            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker != null && marker.getExtraInfo() != null) {

                    Placemark pk = (Placemark) marker.getExtraInfo().get("pk");
                    if (pk != null) {
                        showPopupWindow(pk);
                    }
                }
                return false;
            }
        });

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            /**
             * 单击地图
             */
            @Override
            public void onMapClick(LatLng point) {
                touchType = "单击地图";
                currentPt = point;
                updateMapState();
            }

            /**
             * 单击地图中的POI点
             */
            @Override
            public boolean onMapPoiClick(MapPoi poi) {
                touchType = "单击POI点";
                currentPt = poi.getPosition();
                updateMapState();
                return false;
            }
        });
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            /**
             * 长按地图
             */
            @Override
            public void onMapLongClick(LatLng point) {
                touchType = "长按";
                currentPt = point;
                updateMapState();
            }
        });
        mBaiduMap.setOnMapDoubleClickListener(new BaiduMap.OnMapDoubleClickListener() {
            /**
             * 双击地图
             */
            @Override
            public void onMapDoubleClick(LatLng point) {
                touchType = "双击";
                currentPt = point;
                updateMapState();
            }
        });

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus status) {
                updateMapState();
            }

            @Override
            public void onMapStatusChangeStart(MapStatus status, int reason) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus status) {
                updateMapState();
            }

            @Override
            public void onMapStatusChange(MapStatus status) {
                updateMapState();
            }
        });
    }
    /**
     * 更新地图状态显示面板
     */
    Marker lastP =null;
    private void updateMapState() {

        if (currentPt != null) {
            if(lastP!=null){
                lastP.remove();
            }
            MarkerOptions  ooA = new MarkerOptions().position(currentPt).icon(bdA);
            // mBaiduMap.clear();
            lastP = (Marker) mBaiduMap.addOverlay(ooA);
        }


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(mCurrentLat)
                    .longitude(mCurrentLon).build();
            mBaiduMap.setMyLocationData(locData);
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onMenuSlide(float offset) {
//        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
//        int alpha = (int) Math.round(offset * 255 * 0.4);
//        String hex = Integer.toHexString(alpha).toUpperCase();
//        Log.d("gaolei", "color------------" + "#" + hex + "000000");
//        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mCurrentDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(20.0f);//设置缩放比例
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
        //为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onStop() {
        //取消注册传感器监听
        mSensorManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    private PopupWindow mPopWindow;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showPopupWindow(Placemark pk) {
        showPopFormBottom(pk);
//        View contentView= LayoutInflater.from(LocationDemo.this).inflate(R.layout.popu_map,null);
////        contentView.setBackground(getResources().getDrawable(x));
//         mPopWindow = new PopupWindow(contentView,
//                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
//        mPopWindow.setContentView(contentView);
//        mPopWindow.showAtLocation(contentView, Gravity.CENTER,0, 300);
    }
    private Placemark cpk = null;
    TakePhotoPopWin takePhotoPopWin =null;
    public void showPopFormBottom(Placemark pk) {
        this. cpk = pk;
        takePhotoPopWin = new TakePhotoPopWin(this);
//        设置Popupwindow显示位置（从底部弹出）
        takePhotoPopWin.setData(pk);
        takePhotoPopWin.showAtLocation(findViewById(R.id.bmapView), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        getWindow().setAttributes(params);
        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        takePhotoPopWin.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
    }


    public void morePage(View v){
        if(takePhotoPopWin!=null){
            takePhotoPopWin.dismiss();
            takePhotoPopWin= null;
        }
        //详情
        Log.i(TAG, "morePage: ");
//        Intent intent = new Intent(this, ExpectAnimScrollActivity.class);
//        intent.putExtra("pk",cpk);
//        startActivity(intent);
    }

    public void submitPage(View v){
        //详情
        Log.i(TAG, "submitPage: ");
        if(takePhotoPopWin!=null){
            takePhotoPopWin.dismiss();
            takePhotoPopWin= null;
        }
        //详情
        Log.i(TAG, "morePage: ");
//        Intent intent = new Intent(this, ReportActivity.class);
//        intent.putExtra("pk",cpk);
//        startActivity(intent);
    }
    public void Canclepoup(View v){
        if(mPopWindow!=null){
            mPopWindow.dismiss();
            mPopWindow = null;
        }
    }
    //读取json
    public  String getJson(String fileName,Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    //json --》 list
    public List<Placemark>  getDefultPoints(String json){
        List<Placemark>  points = null;
        try {
            JSONArray ja = new JSONArray(json);
            points = new ArrayList<Placemark>();
            for (int i = 0;i<ja.length();i++){
                JSONObject jb =  ja.getJSONObject(i);
                String newname = null;
                try {
                    newname  = jb.getString("newname");
                }catch (Exception e){
                    e.printStackTrace();
                    newname = null;
                }
                double[] latLng =null;
                String name = null;
                if(newname!=null&&newname.length()>24) {
                    latLng = new double[2];
                    String point0 = newname.substring(newname.lastIndexOf("(") + 1, newname.lastIndexOf(","));
                    Log.i(TAG, "getDefultPoints: point0="+point0);
                    latLng[0] = Double.parseDouble(point0.substring(0, 12));
                    latLng[1] = Double.parseDouble(point0.substring(13, 24));
                    name =newname.substring(0, newname.lastIndexOf("("));
                }else{
                    latLng =null;
                    name = null;
                }
                Placemark pk = new Placemark(name,latLng,newname,jb.getString("citynum"),
                        jb.getString("street"),jb.getString("community"),
                        jb.getString("company"),jb.getString("dangername"),
                        jb.getString("address"),jb.getString("type"),
                        jb.getString("x"),jb.getString("y"),
                        jb.getString("features"),jb.getString("long"),
                        jb.getString("height"),jb.getString("slope"),
                        jb.getString("stability"),jb.getString("object"),
                        jb.getString("number"),jb.getString("area"),
                        jb.getString("peoples"),jb.getString("loss"),
                        jb.getString("grade"),jb.getString("danger"),
                        jb.getString("work"),jb.getString("contacts"),
                        jb.getString("tel"),jb.getString("precautions"),
                        jb.getString("process"),jb.getString("isdoing"),
                        jb.getString("year"),jb.getString("management"),
                        jb.getString("doself"),jb.getString("note")
                );
                points.add(pk);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //获取JSONObject中的数组数据
        return  points;
    }

    public void getPoints(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Placemark>  points = getDefultPoints(getJson("points.json",LocationActivity.this));
                Message msg = new Message();
                msg.what = 100;
                msg.obj = points;
                handler.sendMessage(msg);
            }
        }).start();
    }

    public void setMark(Placemark pk){
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.COMMON);
        // sourceLatLng待转换坐标
        converter.coord(new LatLng( pk.getLatLng()[1],pk.getLatLng()[0]));
        LatLng desLatLng = converter.convert();
        View view = View.inflate(LocationActivity.this, R.layout.markerv, null);
        // 填充数据
        ImageView iconView = (ImageView) view.findViewById(R.id.point_icon);
        TextView nameView = (TextView) view.findViewById(R.id.point_name);
        iconView.setImageResource(R.drawable.icon_gcoding);
        nameView.setText(pk.getName());
        // 构建BitmapDescriptor
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(view);
        Marker marker =(Marker) mBaiduMap.addOverlay( new MarkerOptions().position(desLatLng)
                .zIndex(1)
                .title(pk.getName())
                .draggable(true)
                .icon(bitmap));
        Bundle bundle3 = new Bundle();
        bundle3.putSerializable("pk",pk);
        marker.setExtraInfo(bundle3);
    }

//    public void openMenu() {
//        mLeftDrawerLayout.openDrawer();
//        shadowView.setVisibility(View.VISIBLE);
//    }
//
//    public void closeMenu() {
//        mLeftDrawerLayout.closeDrawer();
//        shadowView.setVisibility(View.GONE);
//    }
}
