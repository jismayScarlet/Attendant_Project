package com.example.attendant_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendant_project.chatgpt_conectort.AITalkerLayout;
import com.example.attendant_project.time_task.TimeTaskLayout;

public class MainActivity extends AppCompatActivity {
    Button btn_attendant,btn_ai_talker;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        btn_attendant = findViewById(R.id.btn_attendant);
        btn_ai_talker = findViewById(R.id.btn_ai_talker);

        btn_attendant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //啟動task_timeLeft邏輯群
                Intent timeTaskIntent =new Intent(MainActivity.this, TimeTaskLayout.class);
                startActivity(timeTaskIntent);
                //task_timeLeft邏輯群
            }
        });

        btn_ai_talker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatGPTIntent = new Intent(MainActivity.this, AITalkerLayout.class);
                startActivity(chatGPTIntent);
            }
        });

        Intent chatGPTIntent = new Intent(MainActivity.this, AITalkerLayout.class);
        startActivity(chatGPTIntent);
    }


}
