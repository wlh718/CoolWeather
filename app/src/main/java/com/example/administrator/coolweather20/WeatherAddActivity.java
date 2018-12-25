package com.example.administrator.coolweather20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.coolweather20.database.County_Addition;
import com.example.administrator.coolweather20.gson.Weather;

import org.litepal.crud.DataSupport;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/7.
 */

public class WeatherAddActivity extends AppCompatActivity{
    private LinkedList<Button> ListBtn_Add;
    private LinkedList<TextView> ListText_Def;
    private Button btn_add,btn_edit,btn_back;
    private LinearLayout linearLayout;
    private int EDITSTATE=0,number=0;
    private int width=0,height=0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){//如果版本号大于或等于21
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//让状态栏也可以显示活动的布局
            getWindow().setStatusBarColor(Color.TRANSPARENT);//将状态栏设置为透明色
        }
        /*获取屏幕宽高*/
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        width=dm.widthPixels;
        height=dm.heightPixels;
        Log.d("WH","Width:"+width+"");
        Log.d("WH","Height:"+height+"");

        setContentView(R.layout.activity_add);
        ListBtn_Add=new LinkedList<Button>();
        ListText_Def=new LinkedList<TextView>();
        linearLayout=(LinearLayout)findViewById(R.id.linearlayout);
        btn_add=(Button)findViewById(R.id.btn_add);
        btn_edit=(Button)findViewById(R.id.btn_edit);
        btn_back=(Button)findViewById(R.id.btn_add_back);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("WeatherAddActivity","onStart");
        ListText_Def.clear();
        ListBtn_Add.clear();
        linearLayout.removeAllViews();
        List<County_Addition> countyAdditions= DataSupport.findAll(County_Addition.class);
        for(County_Addition countyAddition:countyAdditions){//动态添加数据库中所有数据
            addBtn(countyAddition.getWeatherName(),countyAddition.getDef());
        }
        Log.d("WeatherAddActivity",ListText_Def.size()+"");
        Log.d("WeatherAddActivity",ListBtn_Add.size()+"");
        btn_edit.setText("编辑");
        EDITSTATE=0;
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(EDITSTATE==0){
                    btn_edit.setText("确定");
                    EDITSTATE=1;
                }else if(EDITSTATE==1){
                    btn_edit.setText("编辑");
                    EDITSTATE=0;
                }
            }
        });
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(WeatherAddActivity.this,ChooseAreaActivity.class);
                SharedPreferences.Editor editor= PreferenceManager.
                        getDefaultSharedPreferences(WeatherAddActivity.this).edit();
                editor.putBoolean("Where",true);
                editor.apply();
                linearLayout.removeAllViews();
                startActivity(intent);
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断数据库的数据是否为空
                List<County_Addition> countyAdditions= DataSupport.findAll(County_Addition.class);
                for(County_Addition countyAddition:countyAdditions){
                    number++;
                }
                if(number==0){
                    Toast.makeText(WeatherAddActivity.this,"城市不能为空，请添加城市",Toast.LENGTH_SHORT).show();
                }else{
                    number=0;
                    finish();
                }
            }
        });
    }
    private void addBtn(String countyName, Boolean Def_flag){
        //添加外部Layout
        LinearLayout linear_btn = new LinearLayout(this);
        linear_btn.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams liParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linear_btn.setLayoutParams(liParam);
        //添加按钮
        Button btnAdd=new Button(this);
        LinearLayout.LayoutParams btnAddPar=new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, height/8,3);
        btnAddPar.setMargins(0,10,5,10);
        btnAdd.setLayoutParams(btnAddPar);
        btnAdd.setText(countyName);
        btnAdd.setTextColor(Color.argb(255, 255, 255, 255));
        btnAdd.setBackgroundColor(Color.argb(136, 0, 0, 0));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(EDITSTATE==1)
                    delBtn(view);
            }
        });
        linear_btn.addView(btnAdd);
        ListBtn_Add.add(btnAdd);
        //添加TextView
        TextView textDef=new TextView(this);
        LinearLayout.LayoutParams textDefPar = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT, height/8, 1);
        textDefPar.setMargins(0, 10, 0, 10);
        textDef.setLayoutParams(textDefPar);
        if(Def_flag==true){
            textDef.setBackgroundColor(Color.argb(255, 171, 52, 56));
            textDef.setText("默认");
        }else if(Def_flag==false){
            textDef.setText("设为默认");
            textDef.setBackgroundColor(Color.argb(136, 0, 0, 0));
        }
        textDef.setGravity(Gravity.CENTER);
        textDef.setTextColor(Color.argb(255, 255, 255, 255));
        linear_btn.addView(textDef);
        textDef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int k=0;
                for(int i=0;i<ListText_Def.size();i++){
                    ListText_Def.get(i).setBackgroundColor(Color.argb(136, 0, 0, 0));
                    ListText_Def.get(i).setText("设为默认");
                    if(ListText_Def.get(i)==view){
                        view.setBackgroundColor(Color.argb(255, 171, 52, 56));
                        ListText_Def.get(i).setText("默认");
                        k=i;
                    }
                }
                County_Addition countyAddition=new County_Addition();
                countyAddition.setToDefault("Def");
                countyAddition.updateAll();
                County_Addition countyAddition1=new County_Addition();
                countyAddition1.setDef(true);
                countyAddition1.updateAll("WeatherName = ?",ListBtn_Add.get(k).getText().toString());
            }
        });
        ListText_Def.add(textDef);
        linearLayout.addView(linear_btn);
    }
    private void delBtn(View view){
        int countyid;
        Boolean flag=false;
        if(view==null){
            return;
        }
        int iIndex=-1,position=0;
        for(int i=0;i<ListBtn_Add.size();i++){
            if(ListBtn_Add.get(i)==view){
                iIndex=i;
                break;
            }
        }
        if(iIndex>=0){
            List<County_Addition> countyAdditions= DataSupport.findAll(County_Addition.class);
            for(County_Addition countyAddition:countyAdditions){
                if(position==iIndex){
                    countyid=countyAddition.getId();
                    Log.d("TAG",countyid+"");
                    if(countyAddition.getDef()==true){
                        flag=true;
                    }
                    DataSupport.delete(County_Addition.class,countyid);
                    break;
                }
                position++;
            }
            ListBtn_Add.remove(iIndex);
            ListText_Def.remove(iIndex);
            Log.d("TAG",!ListBtn_Add.isEmpty()+"");
            if(flag&&(!ListBtn_Add.isEmpty())){
                Log.d("TAG","First");
                ListText_Def.get(0).setText("默认");
                ListText_Def.get(0).setBackgroundColor(Color.argb(255, 171, 52, 56));
                County_Addition countyAddition=new County_Addition();
                countyAddition.setDef(true);
                countyAddition.updateAll("WeatherName = ?",ListBtn_Add.get(0).getText().toString());
            }
            linearLayout.removeViewAt(iIndex);
        }
    }
}
