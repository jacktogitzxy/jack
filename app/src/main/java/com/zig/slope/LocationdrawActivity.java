package com.zig.slope;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.core.Controller;
import com.app.hubert.guide.listener.OnLayoutInflatedListener;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
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
import com.baidu.mapapi.search.district.DistrictResult;
import com.baidu.mapapi.search.district.OnGetDistricSearchResultListener;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.zig.slope.bean.Placemark;
import com.zig.slope.common.utils.PreferenceManager;
import com.zig.slope.test.MainActivity;
import com.zig.slope.view.TakePhotoPopTop;
import com.zig.slope.view.TakePhotoPopWin;
import com.zig.slope.callback.AllInterface;
import com.zig.slope.view.LeftDrawerLayout;
import com.zig.slope.view.LeftMenuFragment;

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
 */
@Route(path = "/map/index")
public class LocationdrawActivity extends BaseActivity implements SensorEventListener, AllInterface.OnMenuSlideListener,OnGetDistricSearchResultListener {
    private String operatorName,operatorID;
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    LeftDrawerLayout mLeftDrawerLayout;
    LeftMenuFragment mMenuFragment;
    MapView mMapView;
    BaiduMap mBaiduMap;
    View shadowView;
    private ImageView splash_img,menu_icon;
    private final int DISMISS_SPLASH = 0;
    private boolean isDestroy;
    List<Placemark> points;
    RadioGroup group;
    private PreferenceManager pm;
    // UI相关
    OnCheckedChangeListener radioButtonListener;
    boolean isFirstLoc = true; // 是否首次定位
    private MyLocationData locData;
    private int currentModel=-1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==100){
                points = (List<Placemark>) msg.obj;
                Log.i("zxy", "handleMessage: points="+points.size());
                currentModel=3;
                setPMarks(points,2);

            }else{
                    Animator animator = AnimatorInflater.loadAnimator(LocationdrawActivity.this, R.animator.splash);
                    animator.setTarget(splash_img);
                    animator.start();
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        operatorName = intent.getStringExtra("userName");
        operatorID = intent.getStringExtra("operatorID");
        pm = PreferenceManager.getInstance(LocationdrawActivity.this);
//        pm.putString("operatorName",operatorName);
//        pm.putString("operatorID",operatorID);
        setContentView(R.layout.activity_locationdraw);
        shadowView = (View) findViewById(R.id.shadow);
        menu_icon = findViewById(R.id.menu_icon);
        shadowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    closeMenu();
            }
        });
        menu_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu();
            }
        });
        mLeftDrawerLayout = (LeftDrawerLayout) findViewById(R.id.id_drawerlayout);
        FragmentManager fm = getSupportFragmentManager();
        mMenuFragment = (LeftMenuFragment) fm.findFragmentById(R.id.id_container_menu);
        mLeftDrawerLayout.setOnMenuSlideListener(this);
        setStatusBar();
        if (mMenuFragment == null) {
            fm.beginTransaction().add(R.id.id_container_menu, mMenuFragment = new LeftMenuFragment()).commit();
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);//获取传感器管理服务
        getPoints();

         group = (RadioGroup) this.findViewById(R.id.radioGroup);
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
        splash_img = (ImageView) findViewById(R.id.splash_img);
        handler.sendEmptyMessageDelayed(DISMISS_SPLASH, 1000);
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
            lastP = (Marker) mBaiduMap.addOverlay(ooA);
        }
        MapStatus ms = mBaiduMap.getMapStatus();
        if(currentModel!=-1) {
            if (ms.zoom < 16 && currentModel != 0) {
                Log.i(TAG, "updateMapState: ms.zoom==" + ms.zoom);

                updateMarker(0);
                currentModel = 0;
            }
            if (ms.zoom >= 16 && ms.zoom < 18 && currentModel != 1) {
                Log.i(TAG, "updateMapState: ms.zoom==" + ms.zoom);

                updateMarker(1);
                currentModel = 1;
            }
            if (ms.zoom >= 19 && currentModel != 2) {
                Log.i(TAG, "updateMapState: ms.zoom==" + ms.zoom);

                updateMarker(2);
                currentModel = 2;
            }
        }


    }

    public void updateMarker(int zoom){
        for (int i = 0;i<markers.size();i++){
            Marker marker = markers.get(i);
            if (marker != null && marker.getExtraInfo() != null) {

                Placemark pk = (Placemark) marker.getExtraInfo().get("pk");
                if (pk != null) {
                    View view = getIcon(pk,zoom);
                    bitmap = BitmapDescriptorFactory.fromView(view);
                    marker.setIcon(bitmap);
                }
            }

        }


    }
    //clermarker
//    public void clerMaker(){
//        if(bitmaps!=null) {
//            for (int i = 0; i < bitmaps.size(); i++) {
//                bitmaps.get(i).recycle();
//            }
//        }
//    }
    public void setPMarks(List<Placemark> points,int zoom){
        for (int i = 0;i<points.size();i++){
            if(points.get(i).getLatLng()!=null) {
                setMark(points.get(i),zoom);
            }
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
        shadowView.setVisibility(offset == 0 ? View.INVISIBLE : View.VISIBLE);
        int alpha = (int) Math.round(offset * 255 * 0.4);
        String hex = Integer.toHexString(alpha).toUpperCase();
        Log.d("gaolei", "color------------" + "#" + hex + "000000");
        shadowView.setBackgroundColor(Color.argb(alpha, 0, 0, 0));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onGetDistrictResult(DistrictResult districtResult) {

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
            mMapView.refreshDrawableState();
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
        isDestroy = true;
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
    }
    private Placemark cpk = null;
    TakePhotoPopWin takePhotoPopWin =null;
    TakePhotoPopTop takePhotoPopTop = null;
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
    public void showPopFormTop(View view) {
        takePhotoPopTop = new TakePhotoPopTop(this);
//        设置Popupwindow显示位置（从底部弹出）
        takePhotoPopTop.showAtLocation(findViewById(R.id.topmenu_icon), Gravity.TOP | Gravity.RIGHT, 0, 220);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.0f;
        getWindow().setAttributes(params);
        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        takePhotoPopTop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void morePage(View v){
        if(takePhotoPopWin!=null){
            takePhotoPopWin.dismiss();
            takePhotoPopWin= null;
        }
        //详情
        Log.i(TAG, "morePage: ");
        Intent intent = new Intent(this, ExpectAnimScrollActivity.class);
        intent.putExtra("pk",cpk);
        startActivity(intent);
        overridePendingTransition(R.anim.pop_enter_anim,R.anim.pop_exit_anim);

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
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtra("pk",cpk);
        startActivity(intent);
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
                List<Placemark>  points = getDefultPoints(getJson("points.json",LocationdrawActivity.this));
                Message msg = new Message();
                msg.what = 100;
                msg.obj = points;
                handler.sendMessage(msg);
            }
        }).start();
    }
    List<Marker> markers = new ArrayList<Marker>();;
    BitmapDescriptor bitmap;
    public void setMark(Placemark pk,int zoom){
        CoordinateConverter converter  = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.COMMON);
        // sourceLatLng待转换坐标
        converter.coord(new LatLng( pk.getLatLng()[1],pk.getLatLng()[0]));
        LatLng desLatLng = converter.convert();
        View view = getIcon(pk,zoom);
        // 构建BitmapDescriptor
        bitmap = BitmapDescriptorFactory.fromView(view);
        Marker marker =(Marker) mBaiduMap.addOverlay( new MarkerOptions().position(desLatLng)
                .zIndex(1)
                .title(pk.getName())
                .draggable(true)
                .icon(bitmap));
        Bundle bundle3 = new Bundle();
        bundle3.putSerializable("pk",pk);
        marker.setExtraInfo(bundle3);
        markers.add(marker);
    }

    public View getIcon(Placemark pk,int zoom) {
        View view = View.inflate(LocationdrawActivity.this, R.layout.markerv, null);
        // 填充数据
        ImageView iconView = (ImageView) view.findViewById(R.id.point_icon);
        TextView nameView = (TextView) view.findViewById(R.id.point_name);
        TextView pnameView = (TextView) view.findViewById(R.id.point_dangername);
        TextView infoView = (TextView) view.findViewById(R.id.point_info);
        if(zoom==0){
            pnameView.setVisibility(View.INVISIBLE);
            infoView.setVisibility(View.INVISIBLE);
        }
        if(zoom==1){
            pnameView.setVisibility(View.INVISIBLE);
            infoView.setVisibility(View.VISIBLE);
            infoView.setText(pk.getWork()+"危险等级:"+pk.getGrade());
            //   +",坡长:"+pk.getLongs()+",坡高:" +pk.getHeight()+",坡度:"+pk.getSlope());
        }
        if(zoom==2){
            pnameView.setVisibility(View.VISIBLE);
            infoView.setVisibility(View.VISIBLE);
            pnameView.setText(pk.getDangername());
            //+",坡长:"+pk.getLongs()+",坡高:"+pk.getHeight()+",坡度:"+pk.getSlope());
            infoView.setText(pk.getWork()+"危险等级:"+pk.getGrade());
            //+",联系人:"+pk.getContacts()+",联系电话:"+pk.getTel()+",治理进度:"+pk.getProcess());
        }
        iconView.setImageResource(R.drawable.icon_gcoding);
        nameView.setText(pk.getName());
        return view;
    }
    public void openMenu() {
        mMenuFragment.setdata(operatorName,operatorID);
        mLeftDrawerLayout.openDrawer();
        shadowView.setVisibility(View.VISIBLE);
    }

    public void closeMenu() {
        mLeftDrawerLayout.closeDrawer();
        shadowView.setVisibility(View.GONE);
    }
    //设置
    public void StartSetting(View v){
        Intent intent =  new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        startActivity(intent);
    }
    //上报
    public  void StartReport(View v){
        Intent intent = new Intent(LocationdrawActivity.this,ReportActivity.class);
        startActivity(intent);
    }
    //个人
    public  void StartPerson(View v){
        Intent intent = new Intent(LocationdrawActivity.this,PersonActivity.class);
        startActivity(intent);
    }
    //签到
    public void registers(View v){
        Intent intent = new Intent(LocationdrawActivity.this,MainActivity.class);
//        intent.putExtra("v1",);
//        intent.putExtra("v2",);
        startActivity(intent);
    }
        //引导
    public void StartGuide(View v){
        Animation enterAnimation = new AlphaAnimation(0f, 1f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);

        Animation exitAnimation = new AlphaAnimation(1f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);
        closeMenu();
        View view = findViewById(R.id.topmenu_icon);
        View view1 = findViewById(R.id.menu_icon);
        NewbieGuide.with(this)
                .setLabel("guide1")
                .alwaysShow(true)//总是显示，调试时可以打开
                .addGuidePage(
                        GuidePage.newInstance()//创建一个实例
                                .addHighLight(view, HighLight.Shape.CIRCLE, 5)//添加高亮的view

                                .setLayoutRes(R.layout.view_guide)//设置引导页布局
                                .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {
                                    @Override
                                    public void onLayoutInflated(View view) {
                                        //引导页布局填充后回调，用于初始化
                                        TextView tv = view.findViewById(R.id.textView);
                                        tv.setText("点击打开分类菜单");
                                    }
                                })
                                .setEnterAnimation(enterAnimation)//进入动画
                                .setExitAnimation(exitAnimation)//退出动画
                )
                .addGuidePage(//添加一页引导页
                        GuidePage.newInstance()//创建一个实例
                                .addHighLight(view1, HighLight.Shape.CIRCLE, 5)//添加高亮的view
                                .setLayoutRes(R.layout.view_guide2)//设置引导页布局
                                .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {
                                    @Override
                                    public void onLayoutInflated(View view) {
                                        //引导页布局填充后回调，用于初始化
                                        TextView tv = view.findViewById(R.id.textView2);
                                        tv.setText("点击打开左侧菜单");
                                    }
                                })
                                .setEnterAnimation(enterAnimation)//进入动画
                                .setExitAnimation(exitAnimation)//退出动画
                )
                .addGuidePage(//添加一页引导页
                        GuidePage.newInstance()//创建一个实例
                                .addHighLight(view1)//添加高亮的view
                                .setLayoutRes(R.layout.view_guide3)//设置引导页布局
                                .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {
                                    @Override
                                    public void onLayoutInflated(View view) {
                                        //引导页布局填充后回调，用于初始化
                                        TextView tv = view.findViewById(R.id.textView3);
                                        tv.setText("点击地图标注的地点显示更多信息");
                                    }
                                })
                                .setEnterAnimation(enterAnimation)//进入动画
                                .setExitAnimation(exitAnimation)//退出动画
                )
                .addGuidePage(//添加一页引导页
                        GuidePage.newInstance()//创建一个实例
                                .addHighLight(group,HighLight.Shape.CIRCLE, 5)//添加高亮的view
                                .setLayoutRes(R.layout.view_guide4)//设置引导页布局
                                .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {
                                    @Override
                                    public void onLayoutInflated(View view) {
                                        //引导页布局填充后回调，用于初始化
                                        TextView tv = view.findViewById(R.id.textView4);
                                        tv.setText("点击切换地图模式");
                                    }
                                })
                                .setEnterAnimation(enterAnimation)//进入动画
                                .setExitAnimation(exitAnimation)//退出动画
                )

                .show();
    }




    //分类标注 边坡，危房。。。。
    public void  selectType(View view){

    }

}
