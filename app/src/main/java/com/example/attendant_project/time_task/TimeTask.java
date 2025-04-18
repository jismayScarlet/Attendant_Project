package com.example.attendant_project.time_task;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import androidx.annotation.Nullable;

import com.example.attendant_project.R;


public class TimeTask extends Activity {
    /*
    功能：
    1.獲取現在時間並顯示
    2.按下按鈕設定時間
    */

    TextView timeNow ,timeSetView1,timeResout;
    Switch swt_alarm_set;SwitchControler switchControler;
    Button timeSetButton1,timeSetRestoreButton1,musicSetButton1;
    int nowHour,nowMinute;
    private PeriodicTaskHandler periodicTaskHandler; //背景執行緒CLASS
    private int setTime[] = new int[3];
    private int timeCurrent[] = new int[2];
    private Long theLessTime = (long) 0.0;
    private boolean switchChecke = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_time_left);
        timeNow = findViewById(R.id.tv_current_time);
        timeSetView1 = findViewById(R.id.et_target_time);
        timeSetButton1 = findViewById(R.id.btn_set_time);
        timeSetRestoreButton1 = findViewById(R.id.btn_time_restore);
        musicSetButton1 = findViewById(R.id.btn_custom_ringtone);
        timeResout = findViewById(R.id.tv_remaining_time);
        swt_alarm_set = findViewById(R.id.swt_alarm_set);


        //**獲取當前時間
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
//        ZonedDateTime now = ZonedDateTime.now(); //實際時區依照裝置

        // 設定格式 (yyyy-MM-dd HH:mm:ss)
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:s");
//        String nowtimeShow = now.format(formatter);
//        timeNow.setText("目前時間： " + nowtimeShow);

        //**彈出對話框設定時間1
        nowHour = now.getHour();//獲取現在時間
        nowMinute = now.getMinute();
//        timeCurrent = new int[]{nowHour, nowMinute}; //會創造新陣列
//        timeCurrent[0] = nowHour; timeCurrent[1] = nowMinute ; //不會

        ZonedDateTime defaultTimeSet = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
//        ZonedDateTime defaultTimeSet = ZonedDateTime.now(); //實際時區依照裝置
            setTime[0] = defaultTimeSet.getHour();
            setTime[1] = defaultTimeSet.getMinute();
            setTime[2] = 0;//初始化設定時間
            Log.i("初始化設定", "設定時間 " + setTime[0] + ":" + setTime[1] + ":" + setTime[2]);
            int hourRestore = PrefsManager.getTimeSet(getApplicationContext(), "Hour", -1);
            int minuteRestore = PrefsManager.getTimeSet(getApplicationContext(), "Minute", -1);
            if(PrefsManager.getTimeSet(getApplicationContext(),"Hour",-1) == -1 || PrefsManager.getTimeSet(getApplicationContext(),"Minute",-1) == -1){
                timeSetRestoreButton1.setText("︿使用上次啟動任務時間 : 請使用上面按鈕設定時間並啟動");
            }else{
                timeSetRestoreButton1.setText("︿使用上次啟動任務時間 " + hourRestore + ":" + minuteRestore);
                Log.i("初始化設定", "檢視紀錄時間 " + hourRestore + ":" + hourRestore + ":" + setTime[2]);
                }

//        ZonedDateTime defaultTimeSet = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
//            setTime[0] = defaultTimeSet.getHour();
//            setTime[1] = defaultTimeSet.getMinute();
//            setTime[2] = 0;//初始化設定時間
//            Log.i("初始化設定", "設定時間 " + setTime[0] + ":" + setTime[1] + ":" + setTime[2]);

        //** 設置固定背景程序計算時間差
        periodicTaskHandler = new PeriodicTaskHandler(this);
        periodicTaskHandler.getTimeTask(timeResout);
        periodicTaskHandler.setCurrentTimeView(timeNow);
        periodicTaskHandler.setTaskTime(setTime,false);

        timeSetButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(TimeTask.this,new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int Hour, int Minute) {
                            setTime[0] = Hour;
                            setTime[1] = Minute;
                            setTime[2] = 0;
                            switchChecke = true;
                            periodicTaskHandler.setTaskTime(setTime, switchChecke);
                            PrefsManager.putTimeSet(getApplicationContext(), "Hour", Hour);
                            PrefsManager.putTimeSet(getApplicationContext(), "Minute", Minute);
                        timeSetView1.setText("設定時間：" + setTime[0] + ":" + setTime[1]);
                        timeSetRestoreButton1.setText("︿使用上次啟動任務時間 " + hourRestore +":"+ minuteRestore);
                    }
                },nowHour,nowMinute,true);
                timePickerDialog.show();
            }
        });//設定Alarm時間

        timeSetRestoreButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(PrefsManager.getTimeSet(getApplicationContext(),"Hour",-1) != -1 && PrefsManager.getTimeSet(getApplicationContext(),"Minute",-1) != -1){
                    setTime[0] = PrefsManager.getTimeSet(getApplicationContext(),"Hour",-1);
                    setTime[1] = PrefsManager.getTimeSet(getApplicationContext(),"Minute",-1);
                    setTime[2] = 0;
                    switchChecke = true;
                    periodicTaskHandler.setTaskTime(setTime, switchChecke);
                    timeSetView1.setText("設定時間：" + setTime[0] + ":" + setTime[1]);
                }else{
                    Toast.makeText(getApplicationContext(),"沒有紀錄",Toast.LENGTH_SHORT);
                }
            }
        });//設定Alarm時間為記錄時間

        switchControler = new SwitchControler(this);
        swt_alarm_set.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 使用者想從 false → true
                    if (switchChecke == false) {
                        // 還原回 false
                        buttonView.setChecked(false);

                        // 可選：提示使用者為什麼不能開啟
                        Toast.makeText(getApplicationContext(), "設定時間後可使用", Toast.LENGTH_SHORT).show();
                    }else{
                        theLessTime = periodicTaskHandler.getTheLess();//獲取倒計時器剩餘時間
                        Log.i("從switch投給Alarm設定時間",String.valueOf(theLessTime));
                        switchControler.switchOn(theLessTime);
                        timeSetRestoreButton1.setText("︿使用上次啟動任務時間 " + PrefsManager.getTimeSet(getApplicationContext(),"Hour",-1)+":"+ PrefsManager.getTimeSet(getApplicationContext(),"Minute",-1));
                    }
                }else if(isChecked == false){
                    switchChecke = false;
                    switchControler.switchOff();
                    periodicTaskHandler.setCountdowerOfff();
                }


            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        periodicTaskHandler.start();
    }

    @Override
    protected  void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        periodicTaskHandler.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        periodicTaskHandler.release();
    }


}



