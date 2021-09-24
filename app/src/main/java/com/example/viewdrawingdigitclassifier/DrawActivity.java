package com.example.viewdrawingdigitclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.divyanshu.draw.widget.DrawView;

public class DrawActivity extends AppCompatActivity {
    // 레이아웃에 배치한 뷰를 참조하기 위한 변수
    DrawView drawView;
    TextView resultView;
    Button classifyBtn, clearBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        // 화면에 배치된 뷰 찾아오기
        drawView = findViewById(R.id.drawView);
        resultView = findViewById(R.id.resultView);
        classifyBtn = findViewById(R.id.classifyBtn);
        clearBtn = findViewById(R.id.clearBtn);

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