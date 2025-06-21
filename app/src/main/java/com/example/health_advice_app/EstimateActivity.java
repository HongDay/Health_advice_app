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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.health_advice_app.Data.FFT;
import com.example.health_advice_app.Data.MyApp;
import com.example.health_advice_app.Data.SensorData;
import com.example.health_advice_app.Data.SensorViewModel;
import com.example.health_advice_app.activities.ReportActivity;
import com.example.health_advice_app.activities.TimetablePreviewActivity;
import com.example.health_advice_app.databinding.ActivityEstimateBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class EstimateActivity extends AppCompatActivity {
    private ActivityEstimateBinding binding;
    private SensorViewModel sensorViewModel;
    private MyApp appData;
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
    private Runnable measureTask;

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
    private int inclass;

    private int bufferSize;
    private String category = "null";
    private float[] scalerMean = new float[]{
            35.5711037f, 129.210991f, 393.60905f, 0.01545038f,
            0.6406863f, 8.377118f, -0.0111793f, 0.0040375874f,
            0.0064686124f, 34.78479f, 539.03302f, 28546.7458f,
            15076.8445f, 6951.40192f, 3084.5708f, 2061.06494f,
            1504.3542f, 1115.7113f, 878.13397f, 769.793f,
            911.73035f, 42.767426f, -577.4032f, -42.75327f,
            -44.972336f, -46.445957f, -51.02198f, -60.97373f,
            -63.93695f, -65.21917f, -66.57945f, -68.24394f,
            -69.46386f, 0.23236114f
    };

    private float[] scalerStd = new float[]{
            0.002702204f, 0.027795303f, 558.0688f, 2.4679563f,
            2.4680464f, 4.3214917f, 0.4705408f, 0.4357442f,
            0.5515839f, 12.124045f, 1477.0399f, 93497.37f,
            48009.31f, 22668.137f, 9442.361f, 7076.356f,
            5017.2144f, 2963.0161f, 2232.1658f, 1814.5264f,
            1909.5604f, 28.192099f, 81.288116f, 11.619611f,
            10.704564f, 10.158598f, 7.5056524f, 11.245799f,
            13.142919f, 14.058966f, 14.902451f, 15.382227f,
            15.825743f, 0.42233807f
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

        appData = (MyApp) getApplication();

        String name = appData.getName();
        binding.tvHiName.setText("Hi, " + name + " 👋");

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
        clickTimetable();
        clickReport();
        clickAgain();
    }

    protected void clickAgain() {
        binding.btnAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (handler != null && measureTask != null) {
                    handler.removeCallbacks(measureTask);
                }

                if (scanHandler != null && scanRunnable != null) {
                    scanHandler.removeCallbacks(scanRunnable);
                }

                Intent intent = new Intent(EstimateActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void clickTimetable() {
        binding.btnTimetablechk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EstimateActivity.this, TimetablePreviewActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void clickReport() {
        binding.btnGetreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EstimateActivity.this, ReportActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void clickScan() {
        binding.btnScanstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.tvLog.setVisibility(View.VISIBLE);
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

        // 수업정보
        checkAndRunFromTimetable();
    }

    protected void logprint() {
        StringBuilder info = new StringBuilder();
        info.append("In class time = ").append(inclass);
        binding.tvInclass.setText(info.toString());

        info = new StringBuilder();
        info.append("latitude: ").append(latitude).append("\n")
                .append("longitude: ").append(longitude);
        binding.txtLocation.setText(info.toString());

        info = new StringBuilder();
        info.append("Lux: ").append(lux).append(" lx");
        binding.txtLight.setText(info.toString());

        info = new StringBuilder();
        info.append("x: ").append(accel[0]).append("\n")
                .append("y: ").append(accel[1]).append("\n")
                .append("z: ").append(accel[2]);
        binding.txtAccel.setText(info.toString());

        info = new StringBuilder();
        info.append("x: ").append(gyro[0]).append("\n")
                .append("y: ").append(gyro[1]).append("\n")
                .append("z: ").append(gyro[2]);
        binding.txtZyro.setText(info.toString());

        info = new StringBuilder();
        info.append("Decibel: ").append(String.format(Locale.US, "%.2f", decibel)).append(" dB\n")
                .append("Max amp: ").append(String.format(Locale.US, "%.2f", peak));
        binding.txtAudio.setText(info.toString());

        info = new StringBuilder();
        info.append("BSSID count = ").append(bssidCnt).append("\n");
        info.append("RSSI sum = ").append(rssiSum);
        binding.txtWifi.setText(info.toString());
    }

    private void startRepeatMeasure() {
        startMeasurement();

        measureTask = new Runnable() {
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
                        seconds, week, inclass
                );

                sensorViewModel.addData(data);
                sensorViewModel.plusCount();

                if(sensorViewModel.getCount() == 2){
                    binding.tvWrong.setVisibility(View.INVISIBLE);
                    binding.tvWrong2.setVisibility(View.INVISIBLE);
                    binding.btnModify.setVisibility(View.INVISIBLE);
                }

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


                    // model inference
                    TFLiteModel model = new TFLiteModel(EstimateActivity.this);
                    int result = model.predict(input);
                    String activity = "";

                    if (result == 0) {
                        binding.tvGuess.setText("In class !!");
                        activity = "In Class";
                    }
                    else if (result == 1) {
                        binding.tvGuess.setText("Exercising !!");
                        activity = "Workout";
                    }
                    else if (result == 2) {
                        binding.tvGuess.setText("Sleeping !!");
                        activity = "Sleep";
                    }
                    else if (result == 3) {
                        binding.tvGuess.setText("Studying !!");
                        activity = "Study";
                    }
                    else if (result == 4) {
                        binding.tvGuess.setText("Doing Something Else !!");
                        activity = "Other";
                    }

                    String prevActivity = appData.getActivity();
                    long prevTimestamp = appData.getTimestamp();
                    // Store data to pass it to next activity
                    appData.calDuration(seconds-60, activity);

                    // display the button (am I wrong?)
                    // pop up the alert window to check if result is really correct
                    binding.tvWrong.setVisibility(View.VISIBLE);
                    binding.tvWrong2.setVisibility(View.VISIBLE);
                    binding.btnModify.setVisibility(View.VISIBLE);

                    binding.btnModify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new android.app.AlertDialog.Builder(EstimateActivity.this)
                                    .setTitle("Then what were you doing?")
                                    .setItems(new CharSequence[]{"1. Sleeping", "2. Working out", "3. Studying", "4. In Class", "5. Else"},
                                            (dialog, which) -> {
                                                switch (which) {
                                                    case 0:
                                                        if (Objects.equals(prevActivity, "Sleep")){
                                                            appData.removelast();
                                                            appData.setprev(prevTimestamp, prevActivity);
                                                            appData.calDuration(seconds-60, "Sleep");
                                                        }else {
                                                            appData.removelast();
                                                            appData.calDuration(seconds-60, "Sleep");
                                                        }
                                                        Toast.makeText(EstimateActivity.this, "data succesfully modified", Toast.LENGTH_SHORT).show();
                                                        break;

                                                    case 1:
                                                        if (Objects.equals(prevActivity, "Work out")){
                                                            appData.removelast();
                                                            appData.setprev(prevTimestamp, prevActivity);
                                                            appData.calDuration(seconds-60, "Work out");
                                                        }else {
                                                            appData.removelast();
                                                            appData.calDuration(seconds-60, "Work out");
                                                        }
                                                        Toast.makeText(EstimateActivity.this, "data succesfully modified", Toast.LENGTH_SHORT).show();
                                                        break;

                                                    case 2:
                                                        if (Objects.equals(prevActivity, "Study")){
                                                            appData.removelast();
                                                            appData.setprev(prevTimestamp, prevActivity);
                                                            appData.calDuration(seconds-60, "Study");
                                                        }else {
                                                            appData.removelast();
                                                            appData.calDuration(seconds-60, "Study");
                                                        }
                                                        Toast.makeText(EstimateActivity.this, "data succesfully modified", Toast.LENGTH_SHORT).show();
                                                        break;
                                                    case 3:
                                                        if (Objects.equals(prevActivity, "In Class")){
                                                            appData.removelast();
                                                            appData.setprev(prevTimestamp, prevActivity);
                                                            appData.calDuration(seconds-60, "In Class");
                                                        }else{
                                                            appData.removelast();
                                                            appData.calDuration(seconds-60, "In Class");
                                                        }
                                                        Toast.makeText(EstimateActivity.this, "data succesfully modified", Toast.LENGTH_SHORT).show();
                                                        break;
                                                    case 4:
                                                        if (Objects.equals(prevActivity, "Other")){
                                                            appData.removelast();
                                                            appData.setprev(prevTimestamp, prevActivity);
                                                            appData.calDuration(seconds-60, "Other");
                                                        }else{
                                                            appData.removelast();
                                                            appData.calDuration(seconds-60, "Other");
                                                        }
                                                        Toast.makeText(EstimateActivity.this, "data succesfully modified", Toast.LENGTH_SHORT).show();
                                                        break;
                                                }
                                            })
                                    .show();
                        }
                    });

                    sensorViewModel.reset();
                }

                binding.tvLog.setText(String.format("scanning .. ..  (%d/12)", sensorViewModel.getCount()));

                startMeasurement();
                logprint();
                handler.postDelayed(this, 5000);
            }

        };

        handler.postDelayed(measureTask, 5000);
    }

    private void checkAndRunFromTimetable() {
        try {
            // CSV 파일 열기
            File csvFile = new File(getFilesDir(), "timetable.csv");
            BufferedReader reader = new BufferedReader(new FileReader(csvFile));

            // 오전 9시 = 9 * 3600
            int baseTimeInSeconds = 9 * 3600;
            int rowIndex = ((int)seconds - baseTimeInSeconds) / 900;

            // 현재 요일(week = 2~6) → column index = week - 2
            int columnIndex = week - 2;

            // 유효 시간 범위 / 요일 범위 확인
            if (rowIndex < 0 || rowIndex >= 40 || columnIndex < 0 || columnIndex >= 5) {
                inclass = 0;
                reader.close();
                return; // 시간 범위 벗어나면 종료
            }

            // 필요한 row까지 읽기
            String line = null;
            for (int i = 0; i <= rowIndex; i++) {
                line = reader.readLine();
                if (line == null) {
                    reader.close();
                    return; // 파일 끝에 도달
                }
            }

            // 해당 줄에서 column 추출
            String[] cells = line.split(",");
            if (cells.length > columnIndex && "1".equals(cells[columnIndex].trim())) {
                // ✅ 해당 시간에 1이라면 이곳에 실행 코드 작성
                Log.d("Timetable", "✅ 실행 조건 충족: 현재 시간에 1입니다.");
                inclass = 1;
            }
            else {
                Log.d("Timetable", " 현재 시간에 0입니다!!");
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
