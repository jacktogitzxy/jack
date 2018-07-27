package com.zig.slope.view;


import android.app.Application;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zig.slope.R;


/**
 * Created by gaolei on 17/1/5.
 */

public class LeftMenuFragment extends Fragment {
   private TextView un,uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.main_menu,null);
        un = view.findViewById(R.id.user_name);
        uid = view.findViewById(R.id.user_id);
        return view;
    }


    public void setdata(String var1,String var2){
        un.setText(var1);
        uid.setText(var2);
    }


}
