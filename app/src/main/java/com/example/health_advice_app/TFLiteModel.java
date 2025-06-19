package com.example.health_advice_app;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TFLiteModel {
    private Interpreter tflite;

    public TFLiteModel(Context context) {
        try {
            // 모델 로드
            MappedByteBuffer modelBuffer = loadModelFile(context, "activity_model.tflite");
            Interpreter.Options options = new Interpreter.Options();
            options.setAllowBufferHandleOutput(true); // 성능 향상 옵션
            tflite = new Interpreter(modelBuffer, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws Exception {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public int predict(float[][][] input) {
        float[][] output = new float[1][4]; // 클래스 개수에 맞게
        tflite.run(input, output);

        // 가장 높은 확률을 가진 인덱스 계산
        float[] probabilities = output[0];
        int maxIndex = 0;
        float maxProb = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIndex = i;
            }
        }

        return maxIndex; // 예측된 클래스 인덱스
    }
}
