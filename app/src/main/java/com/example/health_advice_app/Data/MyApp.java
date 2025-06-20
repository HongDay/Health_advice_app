package com.example.health_advice_app.Data;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyApp extends Application {
    private long timestamp = 0;
    private String content = "";

    private List<MyData> myDataList = new ArrayList<>();

    public List<MyData> getMyDataList() {
        return myDataList;
    }

    public void setMyDataList(List<MyData> list) {
        this.myDataList = list;
    }

    public void calDuration(long ts, String cont){
        if (Objects.equals(cont, content)) {
            myDataList.remove(myDataList.size()-1);
            long t = timestamp;
            int dur = 1 + (int)(Math.round((ts - timestamp) / 60.0)) ;
            myDataList.add(new MyData(t, dur, cont));
        }
        else {
            timestamp = ts;
            content = cont;
            long t = ts;
            myDataList.add(new MyData(t, 1, cont));
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 필요한 초기화
    }
}

