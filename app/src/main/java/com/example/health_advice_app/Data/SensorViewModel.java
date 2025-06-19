package com.example.health_advice_app.Data;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SensorViewModel extends ViewModel {
    private List<SensorData> dataList = new ArrayList<>();
    private int count = 0;

    public void addData(SensorData data) {
        dataList.add(data);
    }
    public void plusCount() {count++;}
    public int getCount() {return count;}
    public void reset() {
        count = 0;
        dataList = new ArrayList<>();
    }

    public List<SensorData> getDataList() {
        return dataList;
    }
}
