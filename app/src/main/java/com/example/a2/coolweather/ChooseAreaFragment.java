package com.example.a2.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a2.coolweather.R;
import com.example.a2.coolweather.db.City;
import com.example.a2.coolweather.db.County;
import com.example.a2.coolweather.db.Province;
import com.example.a2.coolweather.util.HttpUtil;
import com.example.a2.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import android.util.Log;

/**
 * Created by 猫2 on 2018/4/12.
 */

public class ChooseAreaFragment extends Fragment {

    public static final String DEBUG_TAG="FindHere";
    public static final int LEVEL_PROVINCE =0;
    public static final int LEVEL_CITY =1;
    public static final int LEVEL_COUNTY =2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList=new ArrayList<>();

    /*
    各个列表
     */

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    //当前选中的级别
    private int currentLevel;

    //@SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view =inflater.inflate(R.layout.choose_area,container,false);
        titleText=(TextView) view.findViewById(R.id.title_text);
        backButton=(Button) view.findViewById(R.id.back_button);
        listView=(ListView) view.findViewById(R.id.list_view);

        adapter=new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,
                dataList);
        listView.setAdapter(adapter);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=provinceList.get(position);
                    Log.d(DEBUG_TAG,"Click on Province");
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId=countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.swipeRefresh.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }

                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();

    }

    /*
        query函数
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if(provinceList.size()>0){
            dataList.clear();
            Log.d(DEBUG_TAG,"province db exists");
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

        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        //Cursor cursor=DataSupport.findBySQL("Select * from City where provinceid=?",String.valueOf(selectedProvince.getId()));
       //没有取回任何数据
        cityList=DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);

        cityList=DataSupport.findAll(City.class);
        //findAll才有数据
        Log.d(DEBUG_TAG,"provinceid= "+String.valueOf(selectedProvince.getId()));
        /*
        问题关键所在！！！！
        数据在handle类里存进去后，cityList为空，循环读取并循环写入，死循环；
         */
        Log.d(DEBUG_TAG,"citylist 长短为 "+cityList.size());
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else{
            Log.d(DEBUG_TAG,"city list not exist,and querying city");
           int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            Log.d(DEBUG_TAG,"province code is "+provinceCode);
            queryFromServer(address,"city");
        }
    }

    private void queryCounties(){

        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);

        if(countyList.size()>0){
            dataList.clear();
            for(County county:countyList){
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

    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText=response.body().string();
                boolean result=false;
                if("province".equals(type)){
                    result= Utility.handleProvinceResponse(responseText);
                    Log.d(DEBUG_TAG,"handle province");
                }else if("city".equals(type)){
                    result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                    Log.d(DEBUG_TAG,"handle city,responseText is"+responseText);
                    Log.d(DEBUG_TAG,"handle city,ProvinceId is"+selectedProvince.getId());
                }else if("county".equals(type)){
                    result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                }

                if(result){
                    Log.d(DEBUG_TAG,"after handle,response_result not empty");
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                Log.d(DEBUG_TAG,"server and province");
                                queryProvinces();
                            }else if("city".equals(type)){
                                Log.d(DEBUG_TAG,"query_server for city and go to queryCity");
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"load error",Toast.LENGTH_SHORT).show();
                    }
                });

            }




        });

    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog=new ProgressDialog(getActivity());
            progressDialog.setMessage("loading...");
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
