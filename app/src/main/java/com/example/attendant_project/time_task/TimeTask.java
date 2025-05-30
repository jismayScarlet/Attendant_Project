package com.example.attendant_project.time_task;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.attendant_project.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;


public class TimeTask extends AppCompatActivity {
    /*
    功能：
    1.獲取現在時間並顯示
    2.按下按鈕設定時間
    */

    TextView tv_timeNow ,tv_target_time,tv_remaining_time,tv_ringtone1,tv_costedTime;
    Switch swt_alarm_set;
    CallAlarm callAlarm;
    Button btn_timeSet,btn_timeSetRestore,btn_musicSet,btn_timeCostSet,btn_taskconfirm,btn_outPutFile,btn_clear,btn_sample,btn_timeSaver;
    ImageButton btn_usage;
    int nowHour,nowMinute;
    private PeriodicTaskHandler periodicTaskHandler; //背景執行緒CLASS
    private int setTime[] = new int[3];
    private Long theLessTime = (long) 0.0;
    private boolean switchChecke = false;//用來檢查有沒有輸入任務時間，未來直接改用data == null來檢查就好
    private EditText et_taskName,et_taskDetailExcept,et_taskDetailRealize,et_taskDetailReplenish;
    String timeSetMemery = null;
    int[] cost = null;int costTotal = 0;
    EditText ett_timeSave;
    CheckedTextView ctv_releaseTime;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_time_left);
        tv_timeNow = findViewById(R.id.tv_current_time);
        tv_target_time = findViewById(R.id.tv_target_time);
        btn_timeSet = findViewById(R.id.btn_set_time);
        btn_timeSetRestore = findViewById(R.id.btn_time_restore);
        btn_musicSet = findViewById(R.id.btn_custom_ringtone);
        tv_remaining_time = findViewById(R.id.tv_remaining_time);
        swt_alarm_set = findViewById(R.id.swt_alarm_set);
        tv_ringtone1 = findViewById(R.id.tv_ringtone1);
        btn_timeCostSet = findViewById(R.id.btn_timeCostSet);
        et_taskName = findViewById(R.id.et_taskName);
        et_taskDetailExcept = findViewById(R.id.et_taskDetailExcept);
        et_taskDetailRealize = findViewById(R.id.et_taskDetailRealize);
        et_taskDetailReplenish = findViewById(R.id.et_taskDetailReplenish);
        btn_taskconfirm = findViewById(R.id.btn_taskconfirm);
        tv_costedTime = findViewById(R.id.tv_costedTime);
        btn_outPutFile = findViewById(R.id.btn_outPutFile);
        btn_usage = findViewById(R.id.btn_usage);
        btn_clear = findViewById(R.id.btn_clear);
        btn_sample = findViewById(R.id.btn_sample);
        btn_timeSaver = findViewById(R.id.btn_timeSaver);
        ett_timeSave = findViewById(R.id.ett_timeSave);
        ctv_releaseTime = findViewById(R.id.ctv_releaseTime);

        TextWatcher textWatcher = new TextWatcher();

        // 找到根佈局 將焦點從editText改往版面的 rootLayout
        ConstraintLayout layout = findViewById(R.id.root_layout);
        layout.setFocusableInTouchMode(true);
        layout.requestFocus();

        ToggleCountdownController toggleCountdownController = new ToggleCountdownController(ett_timeSave,ctv_releaseTime,btn_timeSaver);

        //**獲取當前時間
//        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
        ZonedDateTime now = ZonedDateTime.now(); //實際時區依照裝置

        // 設定格式 (yyyy-MM-dd HH:mm:ss)
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:s");
//        String nowtimeShow = now.format(formatter);
//        tv_timeNow.setText("目前時間： " + nowtimeShow);

        //**彈出對話框設定時間1
        nowHour = now.getHour();//獲取現在時間
        nowMinute = now.getMinute();
//        timeCurrent = new int[]{nowHour, nowMinute}; //會創造新陣列
//        timeCurrent[0] = nowHour; timeCurrent[1] = nowMinute ; //不會

//        ZonedDateTime defaultTimeSet = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
        ZonedDateTime defaultTimeSet = ZonedDateTime.now(); //實際時區依照裝置
            setTime[0] = defaultTimeSet.getHour();
            setTime[1] = defaultTimeSet.getMinute();
            setTime[2] = 0;//初始化設定時間
            Log.i("初始化設定", "設定時間 " + setTime[0] + ":" + setTime[1] + ":" + setTime[2]);
            int hourRestore = PrefsManager.getTimeSet(getApplicationContext(), "Hour", -1);
            int minuteRestore = PrefsManager.getTimeSet(getApplicationContext(), "Minute", -1);
            if(PrefsManager.getTimeSet(getApplicationContext(),"Hour",-1) == -1 || PrefsManager.getTimeSet(getApplicationContext(),"Minute",-1) == -1){
                btn_timeSetRestore.setText("＜＜復原  : 無紀錄");
            }else{
                btn_timeSetRestore.setText("＜＜復原  " + hourRestore + ":" + minuteRestore);
                Log.i("初始化設定", "檢視紀錄時間 " + hourRestore + ":" + hourRestore + ":" + setTime[2]);
                }

//        ZonedDateTime defaultTimeSet = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
//            setTime[0] = defaultTimeSet.getHour();
//            setTime[1] = defaultTimeSet.getMinute();
//            setTime[2] = 0;//初始化設定時間
//            Log.i("初始化設定", "設定時間 " + setTime[0] + ":" + setTime[1] + ":" + setTime[2]);

        //** 設置固定背景程序計算時間差
        periodicTaskHandler = new PeriodicTaskHandler(this.getApplicationContext());
        periodicTaskHandler.getTimeTask(tv_remaining_time);
        periodicTaskHandler.setCurrentTimeView(tv_timeNow);
        periodicTaskHandler.setTaskTime(setTime,false);

        btn_timeSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swt_alarm_set.isChecked() != true) {
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
                                timeSetMemery = "設定時間：" + setTime[0] + ":" + setTime[1];
                            tv_target_time.setText(timeSetMemery);
                                btn_timeSetRestore.setText("＜＜復原  " + hourRestore + ":" + minuteRestore);

                        }
                    },nowHour,nowMinute,true);
                    timePickerDialog.show();
                    }else{
                        Toast.makeText(getApplicationContext(),"任務進行中 時間設定被鎖定",Toast.LENGTH_SHORT).show();
                }
            }
        });//設定Alarm時間

        btn_timeSetRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swt_alarm_set.isChecked() != true) {
                    if (PrefsManager.getTimeSet(getApplicationContext(), "Hour", -1) != -1 && PrefsManager.getTimeSet(getApplicationContext(), "Minute", -1) != -1) {
                        setTime[0] = PrefsManager.getTimeSet(getApplicationContext(), "Hour", -1);
                        setTime[1] = PrefsManager.getTimeSet(getApplicationContext(), "Minute", -1);
                        setTime[2] = 0;
                        switchChecke = true;
                        periodicTaskHandler.setTaskTime(setTime, switchChecke);
                        tv_target_time.setText("設定時間：" + setTime[0] + ":" + setTime[1]);
                    } else {
                        Toast.makeText(getApplicationContext(), "沒有紀錄", Toast.LENGTH_SHORT);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"任務進行中 時間設定被鎖定",Toast.LENGTH_SHORT).show();
                }
            }
        });//設定Alarm時間為記錄時間

        callAlarm = new CallAlarm(TimeTask.this);
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
                        callAlarm.switchOn(theLessTime);
                        btn_timeSetRestore.setText("＜＜復原  " + PrefsManager.getTimeSet(getApplicationContext(),"Hour",-1)+":"+ PrefsManager.getTimeSet(getApplicationContext(),"Minute",-1));
                    }
                }else if(isChecked == false){
                    switchChecke = false;
                    callAlarm.switchOff();
                    periodicTaskHandler.setCountdowerOff();
                    btn_timeCostSet.setClickable(true);
                    btn_musicSet.setClickable(true);
                    btn_timeSet.setClickable(true);
                }


            }
        });

        btn_musicSet.setOnClickListener(new View.OnClickListener() {//變更個人鈴聲
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);//Intent.ACTION_OPEN_DOCUMENT 具有可將讀取實例進行串流讀取，並授予檔案久持權限
                intent.addCategory(Intent.CATEGORY_OPENABLE);//表明只接受能被ContentResolver.openFileDescriptor(Uri, String)開啟的URIs格式
                intent.setType("audio/*");
                startActivityForResult(intent,0);
            }
        });

        setTv_ringtone();

        btn_taskconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swt_alarm_set.isChecked() && periodicTaskHandler.getTaskState()) {
                cost = periodicTaskHandler.getTimeCost();
                Log.i("costTime costTime comfirm",cost[0] + " " + cost[1]);
                costTotal = cost[0] + cost[1];
                new PeriodicTaskHandler(TimeTask.this);

                    if(costTotal < 1){
                        tv_costedTime.setText("總花費不足1分鐘");
                    }else {
                        tv_costedTime.setText("總花費 " + costTotal + " 分鐘");
                    }

                    textWatcher.timeInfoSave(TimeTask.this, "costTimeInfo", Integer.toString(costTotal), Integer.toString(cost[1]), timeSetMemery);//把確認的時間拋出去紀錄

                    periodicTaskHandler.setTaskState(false);
                    tv_costedTime.setVisibility(View.VISIBLE);
                    periodicTaskHandler.setExtraTimeSwitch(false);
                    swt_alarm_set.setChecked(false);
                    switchChecke = false;
                    callAlarm.switchOff();
                    periodicTaskHandler.setCountdowerOff();
                    btn_timeCostSet.setClickable(true);
                    btn_musicSet.setClickable(true);
                    btn_timeSet.setClickable(true);
                    periodicTaskHandler.allValueReset();
                }else{
                    Toast.makeText(TimeTask.this,"任務尚未開始",Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_timeCostSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(periodicTaskHandler.getCostTimeSwitchCheck(0) || periodicTaskHandler.getCostTimeSwitchCheck(1)){
                    Toast.makeText(getApplicationContext(),"任務運作中，無法更改時間",Toast.LENGTH_LONG);
                }else {
                    showInputDialog(TimeTask.this, new InputCallback() {
                        @Override
                        public void onInputConfirmed(String input) {
                            // 這裡是 callback 被執行的地方

                            periodicTaskHandler.settimeCost(btn_timeCostSet, input);
                            btn_timeCostSet.setText("花費時間" + input + "分鐘");

                            Log.i("callback", "costTime功能啟動");

                        }
                    });
                }
            }
        });


        EditText editTextPage[] = new EditText[]{et_taskName,et_taskDetailExcept,et_taskDetailRealize,et_taskDetailReplenish};

        for(int i=0;i<=3;i++) {textWatcher.readTextFromFile(TimeTask.this, i, editTextPage);}//軟體啟動時還原紀錄的EditText訊息
        textWatcher.setupDebouncedWatcher(TimeTask.this,editTextPage);//監視EditText的內容變更


        btn_outPutFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent outPutIntent = new Intent(TimeTask.this,TextControler.class);//建立並啟動存檔
                    outPutIntent.putExtra("TIME_INFO",textWatcher.getTimeInfo());
                    startActivity(outPutIntent);
                    Toast.makeText(TimeTask.this, "任務內容完成儲存", Toast.LENGTH_LONG).show();
            }
        });

        btn_usage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimeTask.this,DocumentationControl.class);
                startActivity(intent);
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(TimeTask.this)
                        .setMessage("清除後無法還原")
                        .setNegativeButton("取消",null)
                        .setPositiveButton("清除任務內容", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btn_timeCostSet.setText("預計花費時間");
                                tv_target_time.setText(null);
                                tv_remaining_time.setText("距離目標剩餘時間");
                                et_taskName.setText(null);
                                et_taskDetailExcept.setText(null);
                                et_taskDetailRealize.setText(null);
                                et_taskDetailReplenish.setText(null);
                                tv_costedTime.setText(null);
                                new TextWatcher().clearTheInfo(TimeTask.this);
                            }
                        })
                        .setNeutralButton("清除儲存時間", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                toggleCountdownController.onFinishOutCall();
                            }
                        })
                        .show();
            }
        });

        btn_sample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(TimeTask.this)
                        .setMessage("是否設定為＂補充內容＂之範本")
                        .setNegativeButton("取消",null)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StringBuilder builder = new StringBuilder();
                                try {
                                    FileOutputStream fos = TimeTask.this.openFileOutput("sample", Context.MODE_PRIVATE);
                                    fos.write(String.valueOf(et_taskDetailReplenish.getText()).getBytes(StandardCharsets.UTF_8));
                                    fos.flush();
                                } catch (IOException e) {
                                    Log.e("儲存", "儲存檔案失敗: " + e.getMessage());
                                }
                            }
                        })
                        .setNeutralButton("使用範本", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StringBuilder builder = new StringBuilder();
                                try (FileInputStream fis = TimeTask.this.openFileInput("sample");
                                     BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        builder.append(line).append("\n");
                                    }
                                    et_taskDetailReplenish.setText(String.valueOf(builder));
                                } catch (IOException e) {
                                    Log.e("儲存", "讀取檔案失敗: " + e.getMessage());
                                }
                            }
                        })
                        .show();
            }
        });


    }

    //獲取按鈕得到的資訊用callback丟出去
    public interface InputCallback {
        void onInputConfirmed(String input);
    }

    public void showInputDialog(Context context, InputCallback callback) {//實例化呼叫dialog傳值得功能
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(TimeTask.this)
                .setTitle("輸入預計花費時間")
                .setMessage("分鐘為單位")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String userInput = input.getText().toString();
                        callback.onInputConfirmed(userInput);
                    }
                })
                .show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//接收呼叫檔案選擇器的回傳
        super.onActivityResult(requestCode, resultCode, data);
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        Uri uriSet = null;
        if(requestCode == 0) {
            if (resultCode == RESULT_OK && data != null) {
                uriSet = data.getData();
                PrefsManager.putMusicSet(getApplicationContext(),"customization_music",uriSet.toString());
                // 取得並保留 URI 存取權限
                @SuppressLint("WrongConstant")
                final int takeFlags = data.getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);//取得久持權限
                TimeTask.this.getContentResolver().takePersistableUriPermission(uriSet, takeFlags);



                Log.i("setMusic uri", uriSet.toString());
            } else {
                tv_ringtone1.setText("鈴聲設定失敗!");
                Log.e("show the uri", "intent 回應失敗");
            }
        }else{
            Log.e("error","got wrong requestCode");
        }
        setTv_ringtone();

    }




    protected void setTv_ringtone(){
        if("unknow".equals( PrefsManager.getMusicSet(getApplicationContext(),"customization_music","unknow"))){
            tv_ringtone1.setText("預設鈴聲");
        }else{
            try {//將路徑轉換成UTF-8，用字串解碼器將尾串交給String
                String path = PrefsManager.getMusicSet(getApplicationContext(), "customization_music", "unknow");
                String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
                String pathPick = decodedPath.substring(decodedPath.lastIndexOf("/") + 1);
                tv_ringtone1.setText(pathPick);
                Log.i("textView",pathPick);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        }
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



