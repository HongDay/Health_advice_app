package com.example.health_advice_app;

import android.content.Context;
import android.content.Intent;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.Toast;

import com.example.health_advice_app.Data.FFT;
import com.example.health_advice_app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private AudioRecord audioRecord;
    private Thread audioThread;
    private SensorManager sensorManager;
    private SensorEventListener sensorListener;
    private int firstmeasure = 1;

    private double latitude = 0.0, longitude = 0.0;
    private float lux = 0f;
    private float[] accel = new float[3];
    private float[] gyro = new float[3];
    private double decibel = 0.0;
    private double peak = 0.0;
    private double[] magnitude = new double[512];

    private int bufferSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestGPSpermission();
        clickButton();
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

    }

    protected void clickButton(){
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasurement();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    StringBuilder info = new StringBuilder();

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

                    // 6. FFT 주파수 대역별 크기
                    info.append("🎵 FFT 주파수 대역 크기\n");
                    double binHz = 44100.0 / 1024.0;
                    double[] bandEdges = {100, 200, 400, 800, 1200, 1700, 2400, 3200, 4500, 6000, 8000};

                    for (int b = 0; b < bandEdges.length - 1; b++) {
                        int startBin = (int)(bandEdges[b] / binHz);
                        int endBin = (int)(bandEdges[b + 1] / binHz);
                        double sum = 0;
                        for (int i = startBin; i < endBin; i++) {
                            sum += magnitude[i];
                        }
                        double avg = sum / (endBin - startBin);
                        info.append(String.format(Locale.US, "[%.0f~%.0fHz] %.2f\n", bandEdges[b], bandEdges[b + 1], avg));
                    }

                    // TextView에 설정
                    binding.tvFirst.setText(info.toString());
                }, 5000); // 측정 종료 타이밍
            }
        });
    }
}