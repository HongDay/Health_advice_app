package com.example.health_advice_app;

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
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Calendar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;

import com.example.health_advice_app.Data.FFT;
import com.example.health_advice_app.Data.SensorData;
import com.example.health_advice_app.Data.SensorViewModel;
import com.example.health_advice_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
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

        requestGPSpermission();
        clickButton();
        clickStudy();
        clickClass();
        clickElse();
        clickExercise();
        clickSleep();
        clickEmail();
    }

    protected void clickStudy(){
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "study";
            }
        });
    }

    protected void clickExercise(){
        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "exercise";
            }
        });
    }

    protected void clickClass(){
        binding.button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "class";
            }
        });
    }

    protected void clickSleep(){
        binding.button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "sleep";
            }
        });
    }

    protected void clickElse(){
        binding.button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category = "else";
            }
        });
    }

    protected void requestGPSpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            requestAUDIOpermission();
        }
    }

    protected void requestAUDIOpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 2);
        }
        else {
            setting(); // 이후에 다른 권한이 필요할 경우 여기에 순차적으로 실행.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // GPS 권한 허용 → 오디오 권한 요청
                requestAUDIOpermission();
            } else {
                Toast.makeText(this, "need GPS permission! If not, app will terminate", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 모든 권한 완료
                setting();
            } else {
                Toast.makeText(this, "need MIC permission! If not, app will terminate", Toast.LENGTH_SHORT).show();
            }
        }
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

        binding.tvFirst.setText("GPS setting complete !! ...");

        // 데시벨, 파형, 주파수 측정
        int sampleRate = 44100;
        bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 2);
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        binding.tvFirst.setText("Audio setting complete !! ...");

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

        binding.tvFirst.setText("Sensors setting complete !! ...");

//        if (sensorManager == null) {
//            binding.tvFirst.setText("sensorManager laoding fail !!!");
//            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//            if (sensorManager == null) {
//                Log.e("Sensor", "SensorManager 초기화 실패");
//                return;
//            }
//        }
        binding.tvFirst.setText("setting done, you can press button.");
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

    protected void clickButton(){
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRepeatMeasure();
            }
        });
    }

    protected void clickEmail(){
        binding.email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<SensorData> dataList = sensorViewModel.getDataList();  // ViewModel에서 데이터 가져옴

                File csvFile = new File(getExternalFilesDir(null), "sensor_data.csv");

                try (FileWriter writer = new FileWriter(csvFile)) {

                    writer.write("category,sec,week,latitude,longitude,lux,accelx,accely,accelz,gyroX,gyroY,gyroZ,decibel,peak," +
                            "mag1,mag2,mag3,mag4,mag5,mag6,mag7,mag8,mag9,mag0," +
                            "bssidcnt,rssisum," +
                            "idx1,idx2,idx3,idx4,idx5,idx6,idx7,idx8,idx9,idx0," +
                            "rssi1,rssi2,rssi3,rssi4,rssi5,rssi6,rssi7,rssi8,rssi9,rssi0," +
                            "\n");

                    for (SensorData data : dataList) {
                        writer.write(data.toCsvLine());
                    }

                    writer.flush();

                    Toast.makeText(getApplicationContext(), "CSV 파일 저장 완료:\n" + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "CSV 저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
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

                startMeasurement();
                handler.postDelayed(this, 5000);
            }

        };

        handler.postDelayed(measureTask, 5000);
    }
}