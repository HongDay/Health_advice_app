package com.example.health_advice_app.Data;

public class MyData {
    private long timestamp;
    private final int durationMinutes;
    private String content;

    public MyData(long timestamp, int durationMinutes, String content) {
        this.timestamp = timestamp;
        this.durationMinutes = durationMinutes;
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getContent() {
        return content;
    }

}
