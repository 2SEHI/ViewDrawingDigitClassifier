package com.example.viewdrawingdigitclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.divyanshu.draw.widget.DrawView;

import java.util.Locale;

public class DrawActivity extends AppCompatActivity {
    // 레이아웃에 배치한 뷰를 참조하기 위한 변수
    DrawView drawView;
    TextView resultView;
    Button classifyBtn, clearBtn;

    // 분류기를 저장할 변수
    Classifier cls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        // 화면에 배치된 뷰 찾아오기
        drawView = findViewById(R.id.drawView);
        // 그리는 선의 두께를 변경
        drawView.setStrokeWidth(100.0f);
        // 배경색을 검정색으로 그리고 텍스트로 흰색으로 설정
        drawView.setBackgroundColor(Color.BLACK);
        drawView.setColor(Color.WHITE);

        resultView = findViewById(R.id.resultView);
        classifyBtn = findViewById(R.id.classifyBtn);
        clearBtn = findViewById(R.id.clearBtn);

        cls = new Classifier(this);
        try {
            cls.init();
        }catch(Exception e){
            Log.e("초기화 작업 실패", e.getLocalizedMessage());
        }

        // classifyBtn을 클릭했을 때 처리
        classifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DrawView에 그린 내용을 읽어오기
                Bitmap image = drawView.getBitmap();
                // image가 null일 경우 에러가 납니다.
                // 디버깅(테스트)을 할 때는 이 코드가 유효해서 아래 콘솔창에 출력하지만
                // 빌드해서 배포(release)할 때는 Log코드는 전부 제거됩니다
                // 그래서 로그를 찍을 때는 System.out이 아닌 Log를 이용하여
                // 불필요한 출력을 줄이는 것이 좋습니다.
                Log.e("image", image.toString());
                
                // 추론
                Pair<Integer, Float> res = cls.classify(image);
                
                // 결과 만들기
                String outStr = String.format(Locale.ENGLISH, "%d, %.0f%%", res.first, res.second * 100.0f);

                // 결과 출력
                resultView.setText(outStr);
            }
        });

        // clearBtn을 클릭했을 때 처리
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // DrawView에 그린 그림을 삭제
                drawView.clearCanvas();
            }
        });
    }
}