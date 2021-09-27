package com.example.viewdrawingdigitclassifier;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.util.Log;
import android.util.Pair;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class Classifier {

    // 직접 생성하면 안되고 Component한테 넘겨 받아야 합니다.
    Context context;

    // 모델의 이름을 저장할 변수
    public static final String MODEL_NAME = "keras_model_resNet.tflite";

    // tflite를 사용하기 위해서 필요한 변수
    Interpreter interpreter;

    // 이미지 전처리를 위한 변수
    int modelInputWidth, modelInputHeight, modelInputChannel;

    // 출력을 위한 인스턴스 변수 선언
    int modelOutputClasses;


    // 생성자 : 인스턴스를 만들자마자 대입받음
    public Classifier(Context context){
        this.context = context;
    }

    // 사용자 정의 초기화 메소드
    public void init() throws  IOException{
        // tflite 파일의 이름을 이용하여 ByteBuffer를 생성
        ByteBuffer model = loadModelFile(MODEL_NAME);
        // python언어로 만들어진 tflite모델을 byte단위로 읽어온 다음에
        // 각 바이트의 순서를 맞추는 설정입니다.
        model.order(ByteOrder.nativeOrder());
        // tflite 모델을 메모리에 올리기
        interpreter = new Interpreter(model);
        // 이미지 전처리를 위한 변수값을 설정하는 메소드 호출
        initModelShape();
    }

    // 이미지 크기를 변환하는 메소드
    private Bitmap resizeBitmap(Bitmap bitmap){
        // bitmat을 modelInputWidth, modelInputHeight 크기로 변환하고
        // filter는 최근접 보간법을 사용합니다
        // 보간법은 이미지를 늘릴 때 사용할 방법
        return Bitmap.createScaledBitmap(
                bitmap,
                modelInputWidth,
                modelInputHeight,
                false);
    }

    // 컬러 이미지를 흑백으로 변환하는 메소드
    private ByteBuffer convertBitmapToGrayByteBuffer(Bitmap bitmap){

        // Bitmap의 내용을 정수 배열로 변환
        ByteBuffer byteBuffer =
                ByteBuffer.allocateDirect(bitmap.getByteCount());
        byteBuffer.order(ByteOrder.nativeOrder());
        
        // 빈 정수 배열을 설정
        int [] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        // ByteBuffer의 내용을 정수 배열로 변환
        bitmap.getPixels(pixels, 0, bitmap.getWidth(),
                0 , 0, bitmap.getWidth(),
                bitmap.getHeight());

        // rgb 값을 추출
        // bit 단위 연산(bit and , bit or, bit xor, shift)은
        // 이미지 처리나 윈도우 프로그램의 키보드 이벤트를 처리할 때 사용합니다.
        for (int pixel :pixels){
            // 파이썬에서 255.0으로 나누어서 정규화했던 알고리즘을 직접 구현
            int r = pixel >> 16 & 0xFF;
            int g = pixel >> 8 & 0xFF;
            int b = pixel & 0xFF;

            // 이미지 정규화
            float avgPixelValue = (r + g + b) / 3.0f;
            float normalizedPixelValue = avgPixelValue / 255.0f;

            // 정규화한 값을 다시 저장
            byteBuffer.putFloat(normalizedPixelValue);
        }
        return byteBuffer;
    }



    // 이미지 전처리를 위한 변수의 값을 설정하는 메소드
    private void initModelShape(){
        // 입력 데이터를 가져와서 구조를 읽기

        Tensor inputTensor =
                interpreter.getInputTensor(0);
        // 채널수, 가로, 세로 크기를 가져와서 저장
        int [] inputShape = inputTensor.shape();
        modelInputChannel = inputShape[0]; // 채널수
        modelInputWidth = inputShape[1]; // 가로 크기
        modelInputHeight = inputShape[2]; // 세로 크기

        // 출력값 설정하는 코드
        Tensor outputTenser = interpreter.getOutputTensor(0);
        int [] outputShape = outputTenser.shape();
        modelOutputClasses = outputShape[1];
    }

    // context를 주입받기 위한 setter 메소드
    public void setContext(Context context){
        this.context = context;
    }

    // tflite 파일의 이름을 받아서 Model을 로드한 후 리턴하는 메소드
    private ByteBuffer loadModelFile(String modelName) throws IOException {
        // assets 디렉토리에 접근하기 위한 객체를 생성
        AssetManager am = context.getAssets();
        // 파일 읽고 쓰기가 가능한 객체를 생성해서 파일을 가져오기
        AssetFileDescriptor afd = am.openFd(modelName);
        // 파일의 내용 읽어오기 위해서 스트림 생성 - 연결
        FileInputStream fis = new FileInputStream(
                afd.getFileDescriptor());
        FileChannel fc = fis.getChannel();
        // 시작 위치를 설정
        long startOffset = afd.getStartOffset();
        // 읽어올 크기를 설정
        long declaredLength = afd.getDeclaredLength();
        // 파일의 내용을 읽어서 ByteBuffer 로 변환한 후 리턴
        return fc.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset, declaredLength);
    }

    // 추론을 위한 메소드
    public Pair<Integer, Float> classify(Bitmap bitmap){
        // 이미지 resize후, 흑백으로 변환 
        ByteBuffer buffer = convertBitmapToGrayByteBuffer(resizeBitmap(bitmap));
        // 출력 결과를 저장할 배열을 생성
        float [][] result = new float[1][modelOutputClasses];

        // 추론
        // buffer 가 입력되는 이미지이고 결과는 result에 저장
        // 분류를 할 때는 특정 클래스인지 확률을 리턴합니다
        // 실수 10개를 반환합니다.
        interpreter.run(buffer, result);

        // 추론 결과를 해석하여 반환
        return argmax(result[0]);
    }

    // 추론 결과를 해석하는 메소드
    // Pair : Map과 비슷합니다.
    private Pair<Integer, Float> argmax(float [] array){
        Log.e("분류 확률", Arrays.toString(array));

        // 가장 큰 값과 가장 큰 값의 인덱스 찾기
        int argmax = 0;
        float max = array[0];
        
        for(int i=0; i < array.length; i++){
            // 비교할 값 가져오기
            float f = array[i];
            if(f > max){
                max = f; // 최대값
                argmax = i; // 최대값을 가진 인덱스
            }
        }
        return new Pair<>(argmax, max);
    }
}
