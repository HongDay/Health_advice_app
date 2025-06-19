package com.example.health_advice_app.Data;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class MyApp extends Application {

    private List<MyData> myDataList = new ArrayList<>();

    public List<MyData> getMyDataList() {
        return myDataList;
    }

    public void setMyDataList(List<MyData> list) {
        this.myDataList = list;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 필요한 초기화
    }
}

