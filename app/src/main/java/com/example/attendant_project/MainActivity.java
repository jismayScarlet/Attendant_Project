package com.example.attendant_project;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.attendant_project.MemoBuddy.View.MemoBuddyFragment;
import com.example.attendant_project.fragment_manager.AITalkerFragment;
import com.example.attendant_project.fragment_manager.FrontPageFragment;
import com.example.attendant_project.fragment_manager.TimeTaskFragment;
import com.example.attendant_project.time_task.PrefsManager;

public class MainActivity extends AppCompatActivity {
    Button bt_pageLaunch;
    Spinner spinner;
    String selectedItem = "default";
    String[] items = new String[]{"TimeTask", "AITalker", "MemoBuddy"};
    PrefsManager prefsManager;
    static final String prefsName = "mainActivitySpinnerSelected";
    static final String prefsKey = "spinnerSelected";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        bt_pageLaunch = findViewById(R.id.pageLaunch);
//        btn_attendant = findViewById(R.id.btn_attendant);
//        btn_ai_talker = findViewById(R.id.btn_ai_talker);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_mainlayout,new FrontPageFragment()).commit();

//        btn_attendant.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //啟動task_timeLeft邏輯群
////                Intent timeTaskIntent =new Intent(MainActivity.this, TimeTaskLayout.class);
////                startActivity(timeTaskIntent);
//                //task_timeLeft邏輯群
//                fm.beginTransaction().replace(R.id.fragment_mainlayout,new TimeTaskFragment()).commit();
//            }
//        });
//
//        btn_ai_talker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent chatGPTIntent = new Intent(MainActivity.this, AITalkerLayout.class);
////                startActivity(chatGPTIntent);
//                fm.beginTransaction().replace(R.id.fragment_mainlayout,new AITalkerFragment()).commit();
//            }
//        });


        bt_pageLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (selectedItem) {
                    case "TimeTask":
                        fm.beginTransaction().replace(R.id.fragment_mainlayout, new TimeTaskFragment()).commit();
                        break;
                    case "AITalker":
                        fm.beginTransaction().replace(R.id.fragment_mainlayout,new AITalkerFragment()).commit();
                        break;
                    case "MemoBuddy":
                        fm.beginTransaction().replace(R.id.fragment_mainlayout,new MemoBuddyFragment()).commit();
                        break;
                    case "default":
                        fm.beginTransaction().replace(R.id.fragment_mainlayout,new FrontPageFragment()).commit();
                        break;
                }
            }
        });

        spinner = findViewById(R.id.toolSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(prefsManager.getInt(getApplicationContext(),prefsName,prefsKey,0));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = parent.getItemAtPosition(position).toString();
                prefsManager.setInt(getApplicationContext(),prefsName,prefsKey,position);
                Log.d("Spinner", "選取: " + selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedItem = "default";
                fm.beginTransaction().replace(R.id.fragment_mainlayout,new FrontPageFragment()).commit();
            }
        });
    }

}
