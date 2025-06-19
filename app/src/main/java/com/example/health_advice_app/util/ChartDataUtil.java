package com.example.health_advice_app.util;

import com.example.health_advice_app.Data.MyData;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartDataUtil {

    /** 사용자 기록을 분 단위로 합산하여 DataPoint 배열로 리턴 */
    public static DataPoint[] toDataPoints(List<MyData> dataList) {
        Map<String, Integer> sums = new HashMap<>();
        for (MyData d : dataList) {
            String act = d.getContent();
            int mins = (int) d.getTimestamp(); // timestamp가 분 단위라면 그대로
            sums.put(act, sums.getOrDefault(act, 0) + mins);
        }
        List<DataPoint> list = new ArrayList<>();
        int idx = 0;
        for (Map.Entry<String, Integer> e : sums.entrySet()) {
            list.add(new DataPoint(idx + 1, e.getValue()));
            idx++;
        }
        return list.toArray(new DataPoint[0]);
    }

    /** 20대 대학생 평균 데이터를 임의로 설정해서 DataPoint 배열로 리턴 */
    public static DataPoint[] averageDataPoints() {
        // 순서: 수업, 공부, 운동, 수면 (분 단위)
        double[] avg = {180, 240, 60, 480};
        DataPoint[] pts = new DataPoint[avg.length];
        for (int i = 0; i < avg.length; i++) {
            pts[i] = new DataPoint(i + 1, avg[i]);
        }
        return pts;
    }

    /** X축 레이블 텍스트 배열 (카테고리 순서) */
    public static String[] labels() {
        return new String[]{"Class", "Study", "Exercise", "Sleep"};
    }
}