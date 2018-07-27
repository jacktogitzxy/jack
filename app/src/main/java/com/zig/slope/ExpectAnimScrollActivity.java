package com.zig.slope;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.github.florent37.expectanim.ExpectAnim;
import com.zig.slope.bean.Placemark;
import com.zig.slope.callback.ScrollViewListener;
import com.zig.slope.view.ObservableScrollView;


import cn.bingoogolapple.bgabanner.BGABanner;

import static com.github.florent37.expectanim.core.Expectations.alpha;
import static com.github.florent37.expectanim.core.Expectations.height;
import static com.github.florent37.expectanim.core.Expectations.leftOfParent;
import static com.github.florent37.expectanim.core.Expectations.rightOfParent;
import static com.github.florent37.expectanim.core.Expectations.sameCenterVerticalAs;
import static com.github.florent37.expectanim.core.Expectations.scale;
import static com.github.florent37.expectanim.core.Expectations.toRightOf;
import static com.github.florent37.expectanim.core.Expectations.topOfParent;

//import butterknife.BindDimen;
//import butterknife.BindView;
//import butterknife.ButterKnife;


public class ExpectAnimScrollActivity extends Activity {
    BGABanner bgaBanner;
    View username;
    View avatar;
    View follow;
    View backbground;
    ObservableScrollView scrollView;
    int height;
    TextView[] tvs;

    private ExpectAnim expectAnimMove;
    private Placemark pk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expectanim_activity_scroll);
       // ButterKnife.bind(this);
        Intent intent = getIntent();
        if(intent!=null&&intent.getSerializableExtra("pk")!=null){
            pk = (Placemark) intent.getSerializableExtra("pk");
            initView();
        }


        this.expectAnimMove = new ExpectAnim()
                .expect(avatar)
                .toBe(
                        topOfParent().withMarginDp(20),
                        leftOfParent().withMarginDp(20),
                        scale(0.5f, 0.5f)
                )

                .expect(username)
                .toBe(
                        toRightOf(avatar).withMarginDp(16),
                        sameCenterVerticalAs(avatar),

                        alpha(0.5f)
                )

                .expect(follow)
                .toBe(
                        rightOfParent().withMarginDp(20),
                        sameCenterVerticalAs(avatar)
                )

                .expect(backbground)
                .toBe(
                        height(height).withGravity(Gravity.LEFT, Gravity.TOP)
                )

                .toAnimation();
        scrollView.setScrollViewListener(new ScrollViewListener() {
            @Override
            public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {

                final float percent = (y * 1f) / scrollView.getMaxScrollAmount();
                Log.i("zxy", "onScrollChanged: percent=="+percent);
                expectAnimMove.setPercent(percent);
            }
        });
//        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                final float percent = (scrollY * 1f) / v.getMaxScrollAmount();
//                expectAnimMove.setPercent(percent);
//            }
//        });
    }

    private void initView() {
         username  =findViewById(R.id.username);
         avatar  =findViewById(R.id.avatar);
         follow  =findViewById(R.id.follow);
        bgaBanner = findViewById(R.id.poitBanner);
        bgaBanner.setDelegate(new BGABanner.Delegate() {
            @Override
            public void onBannerItemClick(BGABanner bgaBanner, View view, Object o, int i) {
                //onclick
            }
        });
        bgaBanner.setData(R.drawable.bga, R.drawable.bgb,R.drawable.bgc,R.drawable.bgd);
//        bgaBanner.setAutoPlayAble(true);
        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExpectAnimScrollActivity.this,ReportActivity.class);
                intent.putExtra("pk",pk);
                startActivity(intent);
            }
        });
         backbground  =findViewById(R.id.background);
         scrollView  = (ObservableScrollView) findViewById(R.id.scrollview);
         height  = (int)getResources().getDimension(R.dimen.height);
        tvs = new TextView[32];
        tvs[0] = (TextView) findViewById(R.id.username);
//        tvs[1] = (TextView) findViewById(R.id.dital);
        tvs[2] = (TextView) findViewById(R.id.dital3);
        tvs[3] = (TextView) findViewById(R.id.dital4);
        tvs[4] = (TextView) findViewById(R.id.dital5);
        tvs[5] = (TextView) findViewById(R.id.dital6);
        tvs[6] = (TextView) findViewById(R.id.dital7);
        tvs[7] = (TextView) findViewById(R.id.dital8);
        tvs[8] = (TextView) findViewById(R.id.dital9);
        tvs[9] = (TextView) findViewById(R.id.dital10);
        tvs[10] = (TextView) findViewById(R.id.dital11);
        tvs[11] = (TextView) findViewById(R.id.dital12);
        tvs[12] = (TextView) findViewById(R.id.dital13);
        tvs[13] = (TextView) findViewById(R.id.dital14);
        tvs[14] = (TextView) findViewById(R.id.dital15);
        tvs[15] = (TextView) findViewById(R.id.dital16);
        tvs[16] = (TextView) findViewById(R.id.dital17);
        tvs[17] = (TextView) findViewById(R.id.dital18);
        tvs[18] = (TextView) findViewById(R.id.dital19);
        tvs[19] = (TextView) findViewById(R.id.dital20);
        tvs[20] = (TextView) findViewById(R.id.dital21);
        tvs[21] = (TextView) findViewById(R.id.dital22);
        tvs[22] = (TextView) findViewById(R.id.dital23);
        tvs[23] = (TextView) findViewById(R.id.dital24);
        tvs[24] = (TextView) findViewById(R.id.dital25);
        tvs[25] = (TextView) findViewById(R.id.dital26);
        tvs[26] = (TextView) findViewById(R.id.dital27);
        tvs[27] = (TextView) findViewById(R.id.dital28);
        tvs[28] = (TextView) findViewById(R.id.dital29);
        tvs[29] = (TextView) findViewById(R.id.dital30);
        tvs[30] = (TextView) findViewById(R.id.dital31);
        tvs[31] = (TextView) findViewById(R.id.dital32);

        tvs[0].setText("新区编号:"+pk.getName());
//        tvs[1].setText("全市编号:"+pk.getCitynum());
        tvs[2].setText("街道:"+pk.getStreet());
        tvs[3].setText("社区:"+pk.getCommunity());
        tvs[4].setText("牵头单位:"+pk.getCompany());
        tvs[5].setText("预测稳定性:"+pk.getStability());
        tvs[6].setText("隐患点名称:"+pk.getDangername());
        tvs[7].setText("隐患点最新交通位置:"+pk.getAddress());
        tvs[8].setText("隐患类型（市、省厅文件）:"+pk.getType());
        tvs[9].setText("中心X坐标:"+pk.getX());
        tvs[10].setText("中心Y坐标:"+pk.getY());
        tvs[11].setText("岩性特征:"+pk.getFeatures());
        tvs[12].setText("坡长:"+pk.getLongs()+"（m）");
        tvs[13].setText("坡高:"+pk.getHeight()+"（m）");
        tvs[14].setText("坡度:"+pk.getSlope()+"（m）");


        tvs[15].setText("威胁对象:"+pk.getObject());
        tvs[16].setText("涉险数量:"+pk.getNumber());
        tvs[17].setText("涉险面积:"+pk.getArea()+"（m2）");
        tvs[18].setText("威胁人数:"+pk.getPeoples());
        tvs[19].setText("潜在经济损失:"+pk.getLoss()+"（万）");
        tvs[20].setText("隐患等级:"+pk.getGrade());
        tvs[21].setText("预测危险性:"+pk.getDanger());
        tvs[22].setText("防灾责任单位（社区工作站）:"+pk.getWork());
        tvs[23].setText("联系人:"+pk.getContacts());
        tvs[24].setText("联系电话:"+pk.getTel());
        tvs[25].setText("采取预防措施:"+pk.getPrecautions());
        tvs[26].setText("治理进度:"+pk.getProcess());
        tvs[27].setText("纳入年度防治方案治理计划:"+pk.getYear());
        tvs[28].setText("落实管理维护:"+pk.getIsdoing());
        tvs[29].setText("管理维护单位:"+pk.getManagement());
        tvs[30].setText("自行治理:"+pk.getDoself());
        tvs[31].setText("备注:"+pk.getNote());
    }


}
