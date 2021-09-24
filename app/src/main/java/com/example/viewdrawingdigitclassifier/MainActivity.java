package com.example.viewdrawingdigitclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 화면에 관련된 코드는 여기에 작성
        // 버튼 찾아오기
        Button drawBtn = findViewById(R.id.drawBtn);
        // 버튼을 누르면 동작할 코드 작성
        drawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 출력할 화면을 Intent 로 생성
                Intent intent = new Intent(
                        // Main에서 Draw로 이동
                        MainActivity.this,
                        DrawActivity.class);
                // 새로운 화면을 출력
                startActivity(intent);

            }
        });
    }
}