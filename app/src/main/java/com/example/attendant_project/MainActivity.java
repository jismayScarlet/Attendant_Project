package com.example.attendant_project;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendant_project.time_task.TimeTask;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //啟動task_timeLeft邏輯群
        Intent intent =new Intent(this, TimeTask.class);
        startActivity(intent);
        //task_timeLeft邏輯群

    }
}
