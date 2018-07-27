package com.zig.slope;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.cjt2325.cameralibrary.util.FileUtil;
import com.zig.slope.bean.Placemark;
import com.zig.slope.common.utils.PreferenceManager;
import com.zig.slope.view.MyImageBt;
import com.zig.slope.view.PicFragment;
import com.zig.slope.view.VideoFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.KeyValue;
import org.xutils.http.RequestParams;
import org.xutils.http.body.MultipartBody;
import org.xutils.x;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import slope.zxy.com.login_module.LoginMActivity;


public class ReportActivity extends BaseActivity {
    private ImageView video_upload;//,photo_upload;
    // 文件路径
//    private String path = "";
//    private String vpath = "";
    private TextView report_name1, report_name, report_worker;// report_worker_tel, report_date;
    private EditText report_note;
    // 播放按钮
    private MyImageBt [] plays;
    private Placemark pk;
    private PreferenceManager pm;
    private String TAG="REPORT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        setStatusBar();
        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("pk") != null) {
            pk = (Placemark) intent.getSerializableExtra("pk");
        }
        pm = PreferenceManager.getInstance(ReportActivity.this);
        initView();
        initListener();
    }

    private void initListener() {

        video_upload.setOnClickListener(onclicks);
//        photo_upload.setOnClickListener(onclicks);
        for (int i=0;i<plays.length;i++){
            plays[i].setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    // 显示播放页面
                    MyImageBt btn = (MyImageBt) v;
                    int type = btn.getType();
                    if (type == 2) {
                        String vpath = btn.getPath();
                        if (vpath != null && !vpath.equalsIgnoreCase("")) {
                            VideoFragment bigPic = VideoFragment.newInstance(vpath);
                            android.app.FragmentManager mFragmentManager = getFragmentManager();
                            FragmentTransaction transaction = mFragmentManager.beginTransaction();
                            transaction.replace(R.id.main_menu, bigPic);
                            transaction.commit();
                        }
                    }
                    if(type==1){
                        String path = btn.getPath();
                        if (path != null && !path.equalsIgnoreCase("")) {
                            PicFragment pcf = PicFragment.newInstance(path);
                            android.app.FragmentManager mFragmentManager = getFragmentManager();
                            FragmentTransaction transaction = mFragmentManager.beginTransaction();
                            transaction.replace(R.id.main_menu, pcf);
                            transaction.commit();
                        }
                    }
                }
            });
            plays[i].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MyImageBt btn = (MyImageBt) view;
                    btn.setType(0);
                    btn.setPath(null);
                    btn.setImageResource(R.mipmap.addimage);
                    return true;
                }
            });
        }

    }

    private void initView() {
        video_upload = (ImageView) findViewById(R.id.video_upload);
        plays = new MyImageBt[4];
        plays[0] = (MyImageBt) findViewById(R.id.play);
        plays[1] = (MyImageBt) findViewById(R.id.play0);
        plays[2] = (MyImageBt) findViewById(R.id.play1);
        plays[3] = (MyImageBt) findViewById(R.id.play2);
        report_name1 = (TextView) findViewById(R.id.report_name1);
        report_worker = (TextView) findViewById(R.id.report_worker);
//        report_worker_tel = (TextView) findViewById(R.id.report_worker_tel);
//        report_date = (TextView) findViewById(R.id.report_date);
        report_note = (EditText) findViewById(R.id.report_note);
        report_worker.setText(pm.getPackage("operatorName"));
        if (pk != null) {//补充数据
            report_name1.setText(pk.getName());
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
//获取当前时间
        Date date = new Date(System.currentTimeMillis());
   //     report_date.setText("上报日期：" + simpleDateFormat.format(date));
    }
//上传数据
    public void startReportdo(View v) {
        String text = report_note.getText().toString();
       // upLaodImg(pk.getName(),pm.getPackage("operatorID"),"1","0","0",text,path,path,path);
    }

    View.OnClickListener onclicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(ReportActivity.this, CameraActivity.class);
            startActivityForResult(intent, 100);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.i("zxy", "onActivityResult: ==========requestCode==" + requestCode);
        if (resultCode == 101) {
            String vpath = null;
            String  path = data.getStringExtra("bpath");
            boolean isvideo = data.getBooleanExtra("isvideo",false);
            if(isvideo) {
                vpath = data.getStringExtra("url");
            }
            View view = View.inflate(ReportActivity.this, R.layout.photosign, null);
            // 填充数据
            ImageView iconView = (ImageView) view.findViewById(R.id.photo_icon);
            TextView nameView = (TextView) view.findViewById(R.id.photo_name);
            TextView nameView1 = (TextView) view.findViewById(R.id.photo_name1);
            TextView nameView2 = (TextView) view.findViewById(R.id.photo_name2);
            iconView.setImageBitmap(BitmapFactory.decodeFile(path));
            nameView.setText("巡查员姓名:张三");
            nameView1.setText("巡查日期:2018.7.17");
            nameView2.setText("巡查地点:234");
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromView(view);
            for(int i = 0;i<plays.length;i++){
                if(plays[i].getType()==0){
                    if(isvideo) {
                        plays[i].setType(2);
                        plays[i].setPath(vpath);
                    }else{
                        plays[i].setType(1);
                        plays[i].setPath(FileUtil.saveBitmap("JCamera",bitmap.getBitmap()));
                    }
                    plays[i].setImageBitmap(bitmap.getBitmap());
                    return;
                }
            }
        }
        if (resultCode == 103) {
            Toast.makeText(this, "请检查相机权限~", Toast.LENGTH_SHORT).show();
        }
    }



//http://divitone.3322.org:8081/fx/filesUpload?slopeCode=admin&patrollerID=277&isContainPic=1&isContainVideo=1&videoAddress=d:/123

    public void upLaodImg( final String... param) {
        final ProgressDialog dia = new ProgressDialog(this);
        dia.setMessage("上报中....");
        dia.show();

        RequestParams params = new RequestParams("http://divitone.3322.org:8081/fx/filesUpload");//参数是路径地址
        List<KeyValue> list = new ArrayList<>();
//        for (int i = 6; i < list.size(); i++) {//遍历图片，我传的图片为下标6开始的位置
//            Log.i(TAG, "upLaodImg: file="+param[i]);
//            try {
//                list.add(new KeyValue("multipart"//图片数组，或者单个图片的上传参数名
//                        , new File(param[i])));//这个参数取出来是图片在手机里的地址
//                Log.i(TAG, "upLaodImg: file="+param[i]);
//            } catch (Exception e) {
//            }
//        }
        File file = new File(param[6]);
        list.add(new KeyValue("multipart" , file));
        File file1 = new File(param[7]);
        list.add(new KeyValue("multipart" , file1));
        File file2 = new File(param[8]);
        list.add(new KeyValue("multipart" , file2));
        list.add(new KeyValue("slopeCode", param[0]));
        list.add(new KeyValue("patrollerID", param[1]));
        list.add(new KeyValue("isContainPic", param[2]));
        list.add(new KeyValue("isContainVideo", param[3]));
        list.add(new KeyValue("videoAddress", param[4]));
        list.add(new KeyValue("content", param[5]));
        //设置编码格式为UTF-8，保证参数不乱码
        MultipartBody body = new MultipartBody(list, "UTF-8");
        params.setRequestBody(body);
        x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                Log.i(TAG, "onSuccess: ");
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                dia.dismiss();
                Toast.makeText(ReportActivity.this,"上传失败",Toast.LENGTH_SHORT).show();
                Log.i(TAG, "onError: ");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Log.i(TAG, "onCancelled: ");
            }

            @Override
            public void onFinished() {

                dia.dismiss();
                Toast.makeText(ReportActivity.this,"上传成功",Toast.LENGTH_SHORT).show();
                ReportActivity.this.finish();
                Log.i(TAG, "onFinished: ");
            }
        });
    }
}
