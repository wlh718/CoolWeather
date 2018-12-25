package com.example.administrator.coolweather20;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.coolweather20.database.City;
import com.example.administrator.coolweather20.database.County;
import com.example.administrator.coolweather20.database.County_Addition;
import com.example.administrator.coolweather20.database.Province;
import com.example.administrator.coolweather20.gson.Weather;
import com.example.administrator.coolweather20.util.HttpUtil;
import com.example.administrator.coolweather20.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/12/4.
 */

public class ChooseAreaActivity extends AppCompatActivity {
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private int number=0;
    private ProgressDialog progressDialog;
    private TextView title;
    private Button btn_back;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;
    private County_Addition county_addition;
    private Boolean flag=false;

    @Nullable
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_area);
        inited();
        //判断是否是从WeatherAddActivity跳过来的
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        flag=prefs.getBoolean("Where",false);

        //判断城市添加表里有多少组数据
        List<County_Addition> countyAdditions=DataSupport.findAll(County_Addition.class);
        Log.d("TAG","number:"+number+"");
        for(County_Addition countyAddition:countyAdditions){
            number++;
        }
        Log.d("ChooseAreaActivity",number+"");
        Log.d("ChooseAreaActivity",flag+"");

        if((flag==false)&&(number>0)){
            Intent intent1=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
            startActivity(intent1);
            finish();
        }
        SharedPreferences.Editor editor= PreferenceManager.
                getDefaultSharedPreferences(ChooseAreaActivity.this).edit();
        editor.putBoolean("Where",false);
        editor.apply();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    County_Addition countySave=new County_Addition();
                    Log.d("TAG","number:"+number+"");
                    if(number==0){
                        countySave.setDef(true);
                        countySave.setCountyWeatherId(countyList.get(position).getWeatherId());
                        countySave.setWeatherName(countyList.get(position).getCountyName());
                        countySave.setCountyWeather("");
                        countySave.save();
                        if(flag==true)
                            finish();
                        else if(flag==false) {
                            Intent intent3 = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                            startActivity(intent3);
                            finish();
                        }
                    }else{
                        Boolean flag2=true;
                        List<County_Addition> countyAdditions=DataSupport.findAll(County_Addition.class);
                        for(County_Addition countyAddition:countyAdditions){
                            Log.d("Choose",countyList.get(position).getCountyName());
                            Log.d("Choose",countyAddition.getWeatherName());
                            if(countyAddition.getWeatherName().equals(countyList.get(position).getCountyName())){
                                Toast.makeText(ChooseAreaActivity.this,"已添加该城市",Toast.LENGTH_SHORT).show();
                                flag2=false;
                            }
                            Log.d("Choose",flag2+"");
                        }
                        if(flag2){
                            countySave.setDef(false);
                            countySave.setCountyWeatherId(countyList.get(position).getWeatherId());
                            countySave.setWeatherName(countyList.get(position).getCountyName());
                            countySave.setCountyWeather("");
                            countySave.save();
                            finish();
                        }
                    }

                }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }
                if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    private void inited(){
        title=(TextView)findViewById(R.id.text_title);
        btn_back=(Button)findViewById(R.id.btn_back);
        listView=(ListView)findViewById(R.id.listView);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        county_addition=new County_Addition();
    }
    private void queryProvinces(){
        title.setText("中国");
        btn_back.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            for(Province province:provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    private void queryCities(){
        title.setText(selectedProvince.getProvinceName());
        btn_back.setVisibility(View.VISIBLE);
        cityList=DataSupport.where("provinceId=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    private void queryCounties(){
        title.setText(selectedCity.getCityName());
        btn_back.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityId=?",String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size()>0){
            dataList.clear();
            Boolean flag=false;
            for(County county:countyList){
                List<County_Addition> countyAdditions=DataSupport.findAll(County_Addition.class);
                for(County_Addition countyAddition:countyAdditions){
                    if(countyAddition.getWeatherName().equals(county.getCountyName())){
                        flag=true;
                        break;
                    }
                }
                if(flag){
                    dataList.add("√|"+county.getCountyName());
                    flag=false;
                } else
                    dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    private void queryFromServer(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(ChooseAreaActivity.this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
