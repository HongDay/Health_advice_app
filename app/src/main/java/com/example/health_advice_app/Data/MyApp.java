package com.example.health_advice_app.Data;

import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyApp extends Application {
    private long timestamp = -1;
    private String content = "";

    private List<MyData> myDataList = new ArrayList<>();

    public List<MyData> getMyDataList() {
        return myDataList;
    }

    public String getActivity() {return content;}
    public long getTimestamp() {return timestamp;}

    public void removelast() {myDataList.remove(myDataList.size()-1);}
    public void setprev(long prevt, String preva) {
        timestamp = prevt;
        content = preva;
    }

    public void calDuration(long ts, String cont){
        if (Objects.equals(cont, content)) {
            Log.d("tihs", String.valueOf(timestamp));
            myDataList.remove(myDataList.size()-1);
            long t = timestamp;
            int dur = 1 + (int)(Math.round((ts - timestamp) / 60.0)) ;
            Log.d("tihs", String.valueOf(dur));
            myDataList.add(new MyData(t, dur, cont));
        }
        else {
            timestamp = ts;
            Log.d("tihs", String.valueOf(timestamp));
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

