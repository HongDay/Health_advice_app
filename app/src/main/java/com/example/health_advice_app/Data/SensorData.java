package com.example.health_advice_app.Data;

public class SensorData {
    public String category;
    public double latitude;
    public double longitude;
    public float lux;
    public float accelx;
    public float accely;
    public float accelz;
    public float zyrox;
    public float zyroy;
    public float zyroz;
    public double decibel;
    public double peak;
    public double mag1;
    public double mag2;
    public double mag3;
    public double mag4;
    public double mag5;
    public double mag6;
    public double mag7;
    public double mag8;
    public double mag9;
    public double mag0;
    public int bssidcnt;
    public int rssisum;
    public int idx1;
    public int idx2;
    public int idx3;
    public int idx4;
    public int idx5;
    public int idx6;
    public int idx7;
    public int idx8;
    public int idx9;
    public int idx0;
    public int rssi1;
    public int rssi2;
    public int rssi3;
    public int rssi4;
    public int rssi5;
    public int rssi6;
    public int rssi7;
    public int rssi8;
    public int rssi9;
    public int rssi0;
    public long sec;
    public long week;

    public SensorData(
            String category,
            double latitude, double longitude, float lux,
            float accelx, float accely, float accelz,
            float zyrox, float zyroy, float zyroz,
            double decibel, double peak,
            double mag1, double mag2, double mag3, double mag4, double mag5,
            double mag6, double mag7, double mag8, double mag9, double mag0,
            int bssidcnt, int rssisum,
            int idx1, int idx2, int idx3, int idx4, int idx5,
            int idx6, int idx7, int idx8, int idx9, int idx0,
            int rssi1, int rssi2, int rssi3, int rssi4, int rssi5,
            int rssi6, int rssi7, int rssi8, int rssi9, int rssi0,
            long sec, long week
    ) {
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lux = lux;
        this.accelx = accelx;
        this.accely = accely;
        this.accelz = accelz;
        this.zyrox = zyrox;
        this.zyroy = zyroy;
        this.zyroz = zyroz;
        this.decibel = decibel;
        this.peak = peak;
        this.mag1 = mag1;
        this.mag2 = mag2;
        this.mag3 = mag3;
        this.mag4 = mag4;
        this.mag5 = mag5;
        this.mag6 = mag6;
        this.mag7 = mag7;
        this.mag8 = mag8;
        this.mag9 = mag9;
        this.mag0 = mag0;
        this.bssidcnt = bssidcnt;
        this.rssisum = rssisum;
        this.idx1 = idx1;
        this.idx2 = idx2;
        this.idx3 = idx3;
        this.idx4 = idx4;
        this.idx5 = idx5;
        this.idx6 = idx6;
        this.idx7 = idx7;
        this.idx8 = idx8;
        this.idx9 = idx9;
        this.idx0 = idx0;
        this.rssi1 = rssi1;
        this.rssi2 = rssi2;
        this.rssi3 = rssi3;
        this.rssi4 = rssi4;
        this.rssi5 = rssi5;
        this.rssi6 = rssi6;
        this.rssi7 = rssi7;
        this.rssi8 = rssi8;
        this.rssi9 = rssi9;
        this.rssi0 = rssi0;
        this.sec = sec;
        this.week = week;
    }

    // CSV 한 줄로 변환
    public String toCsvLine() {
        return category + "," + sec + "," + week + "," + latitude + "," + longitude + "," + lux + "," +
                accelx + "," + accely + "," + accelz + "," +
                zyrox + "," + zyroy + "," + zyroz + "," +
                decibel + "," + peak + "," +
                mag1 + "," + mag2 + "," + mag3 + "," + mag4 + "," + mag5 + "," +
                mag6 + "," + mag7 + "," + mag8 + "," + mag9 + "," + mag0 + "," +
                bssidcnt + "," + rssisum + "," +
                idx1 + "," + idx2 + "," + idx3 + "," + idx4 + "," + idx5 + "," +
                idx6 + "," + idx7 + "," + idx8 + "," + idx9 + "," + idx0 + "," +
                rssi1 + "," + rssi2 + "," + rssi3 + "," + rssi4 + "," + rssi5 + "," +
                rssi6 + "," + rssi7 + "," + rssi8 + "," + rssi9 + "," + rssi0 + "," +
                 "\n";
    }

    public float[] toArrayWithoutFirst() {
        return new float[] {(float)latitude, (float)longitude, lux, accelx, accely, accelz,
                            zyrox, zyroy, zyroz, (float)decibel, (float)peak, (float)mag1, (float)mag2,
                            (float)mag3, (float)mag4, (float)mag5, (float)mag6, (float)mag7, (float)mag8,
                            (float)mag9, (float)mag0, bssidcnt, rssisum, rssi1, rssi2, rssi3, rssi4, rssi5,
                            rssi6, rssi7, rssi8, rssi9, rssi0, 0
                            }; // var1 제외
    }
}
