package com.example.health_advice_app.Data;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SensorViewModel extends ViewModel {
    private final List<SensorData> dataList = new ArrayList<>();

    public void addData(SensorData data) {
        dataList.add(data);
    }

    public List<SensorData> getDataList() {
        return dataList;
    }
}
