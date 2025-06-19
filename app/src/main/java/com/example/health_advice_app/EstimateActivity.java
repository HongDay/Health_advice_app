package com.example.health_advice_app;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.health_advice_app.Data.FFT;
import com.example.health_advice_app.Data.SensorData;
import com.example.health_advice_app.Data.SensorViewModel;
import com.example.health_advice_app.databinding.ActivityEstimateBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EstimateActivity extends AppCompatActivity {
    private ActivityEstimateBinding binding;
    private SensorViewModel sensorViewModel;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private AudioRecord audioRecord;
    private Thread audioThread;
    private SensorManager sensorManager;
    private SensorEventListener sensorListener;
    private WifiManager mWifiManager;
    private IntentFilter mIntentFilter;
    private Handler scanHandler;
    // 클래스 멤버로 선언
    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable scanRunnable;
    private boolean isScanRequested = false;
    private static final String TAG = "MainActivity";
    private int firstmeasure = 1;

    private double latitude = 0.0, longitude = 0.0;
    private float lux = 0f;
    private float[] accel = new float[3];
    private float[] gyro = new float[3];
    private double decibel = 0.0;
    private double peak = 0.0;
    private double[] magnitude = new double[512];
    private int bssidCnt = 0;
    private int rssiSum = 0;
    private Map<String, Integer> bssidToIndex = new HashMap<>(); // 근데 얘는 서버에 저장하는게 맞을듯
    private int[] top10Rssi = {0,0,0,0,0,0,0,0,0,0};
    private int[] top10BssidIndex = {0,0,0,0,0,0,0,0,0,0};
    private long seconds;
    private int week;

    private int bufferSize;
    private String category = "null";
    private float[] scalerMean = new float[]{
            35.570527f, 129.219184f, 625.886798f, -0.03319653f, 0.37283892f, 8.9577285f,
            0.00130513f, -0.00033624f, -0.001152f, 34.7757803f, 672.599571f, 31751.9310f,
            20147.0227f, 11668.9246f, 5390.419f, 3391.8493f, 2289.3544f, 1911.4099f,
            1527.9446f, 1376.7317f, 1766.741f, 35.5655814f, -576.5055f, -38.32193f,
            -41.61918f, -43.36365f, -49.7514848f, -61.265975f, -66.0f, -67.68222f,
            -69.91156f, -71.98905f, -73.53052f, 0.1550626f
    };

    private float[] scalerStd = new float[]{
            0.00290304f, 0.0280633f, 2713.1103f, 1.489209f, 1.467125f, 3.491136f,
            0.1870229f, 0.1726982f, 0.1282379f, 13.4019552f, 1831.7251f, 109496.837f,
            58282.0597f, 32941.9445f, 16337.5804f, 12033.9056f, 7986.2448f, 6415.703f,
            5098.6843f, 4995.515f, 6271.593f, 29.0020265f, 82.9884684f, 11.2342774f,
            9.7020783f, 9.5815264f, 6.8627741f, 9.7729735f, 12.2091376f, 12.7914823f,
            14.0810195f, 14.8776557f, 15.3404507f, 0.3619644f
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                Log.e(TAG, "Scan results available");

                if (!isScanRequested) {
                    Log.e(TAG, "Ignoared : onRceive called while scan request is failed");
                    return;
                }

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    List<ScanResult> scanResults = mWifiManager.getScanResults();
                    bssidCnt = scanResults.size();

                    List<ScanResult> sortedResults = new ArrayList<>(scanResults);
                    sortedResults.sort((a,b) -> Integer.compare(b.level, a.level));

                    rssiSum = 0;

                    int limit = Math.min(10, bssidCnt);
                    for (int i = 0; i < limit; i++) {
                        ScanResult sr = sortedResults.get(i);
                        rssiSum += sr.level;
                        top10Rssi[i] = (sr.level);

                        int bssidIndex;
                        String curBssid = sr.BSSID;
                        if (bssidToIndex.containsKey(curBssid)){
                            bssidIndex = bssidToIndex.get(curBssid);
                        } else {
                            bssidToIndex.put(curBssid, bssidToIndex.size());
                            bssidIndex = bssidToIndex.get(curBssid);
                        }
                        top10BssidIndex[i] = (bssidIndex);
                    }
                    Log.e(TAG, "Scan results displayed!!");
                } else {
                    Log.e(TAG, "@@@@@ no permission for scanning !!");
                }
            } else {
                Log.e(TAG, "Scan results unavailable!!!");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEstimateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sensorViewModel = new ViewModelProvider(this).get(SensorViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        setting();
        clickScan();
    }

    protected void clickScan() {
        binding.btnScanstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRepeatMeasure();
            }
        });
    }

    protected void setting() {
        // GPS
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("GPS", "위치 수신됨: " + latitude + ", " + longitude);

            }
        };

        // 데시벨, 파형, 주파수 측정
        int sampleRate = 44100;
        bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 2);
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        // Sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    lux = event.values[0];
                } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    accel[0] = event.values[0];
                    accel[1] = event.values[1];
                    accel[2] = event.values[2];
                } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    gyro[0] = event.values[0];
                    gyro[1] = event.values[1];
                    gyro[2] = event.values[2];
                    sensorManager.unregisterListener(this);
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        // WiFi
        registerReceiver(mReceiver, mIntentFilter);
    }

    protected void startMeasurement() {
        // GPS 시작
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (firstmeasure == 0) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4500, 3, locationListener);
            else {
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    latitude = lastLocation.getLatitude();
                    longitude = lastLocation.getLongitude();
                    Log.d("GPS", "최근 위치 사용: " + latitude + ", " + longitude);
                } else {
                    Log.d("GPS", "최근 위치 정보 없음 (null)");
                }
                firstmeasure = 0;
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // 음성인식 시작
        audioThread = new Thread(() -> {
            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 4500) {
                // 파형 통계 계산
                int read = audioRecord.read(buffer, 0, bufferSize);
                double sum = 0, max = 0;
                for (int i = 0; i < read; i++) {
                    sum += buffer[i] * buffer[i];
                    max = Math.max(max, Math.abs(buffer[i]));
                }
                if (read > 0) {
                    double rms = Math.sqrt(sum / (double) read);
                    decibel = 20 * Math.log10(rms);
                    peak = max;
                }

                // FFT 분석, 주파수 대역별 크기 계산
                double[] real = new double[1024];
                double[] imag = new double[1024];
                for (int i = 0; i < 1024 && i < read; i++) {
                    real[i] = buffer[i];  // 실수부
                    imag[i] = 0;          // 허수부 0
                }

                FFT fft = new FFT(1024);
                fft.fft(real, imag);

                for (int i = 0; i < 1024 / 2; i++) {
                    magnitude[i] = Math.sqrt(real[i]*real[i] + imag[i]*imag[i]);
                }
            }
            audioRecord.stop();
        });
        audioThread.start();

        // 센서 시작
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

        // Wifi 시작
        scanHandler = new Handler();
        scanRunnable = new Runnable() {
            @Override
            public void run() {
                boolean scanStarted = mWifiManager.startScan();
                if (!scanStarted) {
                    isScanRequested = false;
                    Log.e(TAG, "WiFi scan failed..");
                } else {
                    isScanRequested = true;
                    Log.e(TAG, "Scan request success");
                }
            }
        };
        scanHandler.post(scanRunnable);

        // 시각
        Calendar calendar = Calendar.getInstance();
        seconds = calendar.get(Calendar.HOUR_OF_DAY) * 3600L
                + calendar.get(Calendar.MINUTE) * 60L
                + calendar.get(Calendar.SECOND);
        week = calendar.get(Calendar.DAY_OF_WEEK);
    }

    protected void logprint() {
        StringBuilder info = new StringBuilder();

        info.append("\n 시각, 요일 정보\n");
        info.append("초 = ").append(seconds).append("\n");
        info.append("요일 = ").append(week).append("\n");

        // 1. 위치 정보
        info.append("📍 위치 정보\n")
                .append("위도: ").append(latitude).append("\n")
                .append("경도: ").append(longitude).append("\n\n");

        // 2. 조도 센서
        info.append("💡 조도 센서\n")
                .append("조도: ").append(lux).append(" lx\n\n");

        // 3. 가속도 센서
        info.append("📈 가속도계\n")
                .append("x: ").append(accel[0]).append("\n")
                .append("y: ").append(accel[1]).append("\n")
                .append("z: ").append(accel[2]).append("\n\n");

        // 4. 자이로 센서
        info.append("🌀 자이로 센서\n")
                .append("x: ").append(gyro[0]).append("\n")
                .append("y: ").append(gyro[1]).append("\n")
                .append("z: ").append(gyro[2]).append("\n\n");

        // 5. 오디오 정보
        info.append("🔊 오디오 정보\n")
                .append("데시벨: ").append(String.format(Locale.US, "%.2f", decibel)).append(" dB\n")
                .append("최대 진폭: ").append(String.format(Locale.US, "%.2f", peak)).append("\n\n");

        info.append("\n🛜 WIFI 정보\n");
        info.append("BSSID count = ").append(bssidCnt).append("\n");
        info.append("RSSI sum = ").append(rssiSum).append("\n");

        // TextView에 설정
        binding.tvLog.setText(info.toString());
    }

    private void startRepeatMeasure() {
        startMeasurement();

        Runnable measureTask = new Runnable() {
            @Override
            public void run() {
                double[] bands = new double[10];
                double binHz = 44100.0 / 1024.0;
                double[] bandEdges = {100, 200, 400, 800, 1200, 1700, 2400, 3200, 4500, 6000, 8000};
                for(int b = 0; b < 10; b++){
                    int startBin = (int) (bandEdges[b] / binHz);
                    int endBin = (int) (bandEdges[b + 1] / binHz);
                    double sum = 0;
                    for (int i = startBin; i < endBin; i++) {
                        sum += magnitude[i];
                    }
                    double avg = sum / (endBin - startBin);
                    bands[b] = avg;
                }

                SensorData data = new SensorData(
                        category, latitude, longitude, lux,
                        accel[0], accel[1], accel[2],
                        gyro[0], gyro[1], gyro[2],
                        decibel, peak,
                        bands[0], bands[1], bands[2], bands[3], bands[4], bands[5],
                        bands[6], bands[7], bands[8], bands[9],
                        bssidCnt, rssiSum,
                        top10BssidIndex[0], top10BssidIndex[1], top10BssidIndex[2],
                        top10BssidIndex[3], top10BssidIndex[4], top10BssidIndex[5],
                        top10BssidIndex[6], top10BssidIndex[7], top10BssidIndex[8], top10BssidIndex[9],
                        top10Rssi[0], top10Rssi[1], top10Rssi[2], top10Rssi[3], top10Rssi[4],
                        top10Rssi[5], top10Rssi[6], top10Rssi[7], top10Rssi[8], top10Rssi[9],
                        seconds, week
                );

                sensorViewModel.addData(data);
                sensorViewModel.plusCount();

                if(sensorViewModel.getCount() == 12){
                    // data preprocessing
                    List<SensorData> drived = sensorViewModel.getDataList();
                    float[][][] input = new float[1][12][34];

                    for (int t = 0; t < 12; t++) {
                        float[] values = drived.get(t).toArrayWithoutFirst();
                        float[] norm = new float[34];

                        for (int i = 0; i < 34; i++) {
                            norm[i] = (values[i] - scalerMean[i]) / scalerStd[i];
                        }

                        input[0][t] = norm; // 정규화된 시점 데이터를 삽입
                    }


                    // model training
                    TFLiteModel model = new TFLiteModel(EstimateActivity.this);
                    int result = model.predict(input);

                    if (result == 0) binding.tvGuess.setText("In class !!" + seconds);
                    else if (result == 1) binding.tvGuess.setText("Doing Something Else !!" + seconds);
                    else if (result == 2) binding.tvGuess.setText("Sleeping !!" + seconds);
                    else if (result == 3) binding.tvGuess.setText("Studying !!" + seconds);

                    // display the button (am I wrong?)
                    // pop up the alert window to check if result is really correct

                    // Store data to pass it to next activity


                    sensorViewModel.reset();
                }

                startMeasurement();
                logprint();
                handler.postDelayed(this, 5000);
            }

        };

        handler.postDelayed(measureTask, 5000);
    }

}
