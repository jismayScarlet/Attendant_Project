package com.example.attendant_project.time_task;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class PeriodicTaskHandler {
    /*
    功能： 背景執行重複顯示剩餘時間
    兼任 計算計時器剩餘時間
    */
    Context context;
    private HandlerThread handlerThread;
    private Handler handler,handler2;//兩者共用一個Thread，由handler管理資源
    private boolean isRunning = false,counteDownSwitch,timeCostSwitch = false,extraTimeSwitch = false,counteDownStop = false,taskStart = false;
    private static final long INTERVAL = 1000; // 1秒
    private int timeSeting[] = new int[3];
    private int costTimeLess = 0;//設定時間 & 剩餘時間，秒
    private int extraTime = 0;//超時累積時間(秒
    private int costTimeSave = 0 ;//紀錄未超時的時間(分鐘
    Long seconds,minutes,hours;
    private Long lesstimeToal= (long) 0.0;//計算與實際時間差值之秒數總和,用來設定Alarm的倒數時間
    private String theLessString = null;//顯示剩餘時間用字串
    int timeLessTatal = 0;int nowTatal = 0;int timeSetingTatal = 0;
    int nowHour,nowMinute,nowSecond;
    TextView view,currentTimeView;
    private Button btn_timeCost;
    MediaPlayer mediaPlayer = null;





    public PeriodicTaskHandler(Context context) {
        handlerThread = new HandlerThread("PeriodicHandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler2 = new Handler(handlerThread.getLooper());
        this.context = context;
//        timeChangCheckr = nowSecond + 1;//啟動自我檢查秒時變化
//        costTimeLess = 30;
    }

    private final Runnable periodicTask = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                /*
                * 我覺得可能可以把裡面的不需要每次都新增的變數以
                * */

                // 執行背景任務
                Log.d("PeriodicTask", "執行背景任務: " + System.currentTimeMillis());

                //**定義current時間來源
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
                //ZonedDateTime now = ZonedDateTime.now(); //實際時區依照裝置
                // 設定格式 (yyyy-MM-dd HH:mm:ss)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String nowtimeShow = now.format(formatter);
                //**


                currentTimeView.post(new Runnable() {
                    @Override
                    public void run() {
                        currentTimeView.setText("目前時間： " + nowtimeShow);
                    }
                });

                nowHour = now.getHour();//獲取現在時間
                nowMinute = now.getMinute();
                nowSecond = now.getSecond();


                timeSetingTatal = timeSeting[0] * 60 * 60 + timeSeting[1] * 60 + timeSeting[2];
                nowTatal = nowHour * 60 * 60 + nowMinute * 60 + nowSecond;
                Log.i("設定","nowtatal " + String.valueOf(nowTatal));
                Log.i("設定","timeSettatal " + String.valueOf(timeSetingTatal));

                if(timeSetingTatal >= nowTatal){
                    timeLessTatal = timeSetingTatal - nowTatal;
                }else {
                    timeLessTatal = (24 * 3600 - nowTatal) + timeSetingTatal;
                }
                Log.i("設定","timeLessTatal " + String.valueOf(timeLessTatal));

                lesstimeToal = Long.valueOf(timeLessTatal * 1000);

                hours = TimeUnit.MILLISECONDS.toHours(lesstimeToal);
                minutes = TimeUnit.MILLISECONDS.toMinutes(lesstimeToal) % 60;
                seconds = TimeUnit.MILLISECONDS.toSeconds(lesstimeToal) % 60;
                Log.i("投入Alarm的時長",hours +":"+ minutes +":"+ seconds);
                Log.i("預計投入Alarm的毫秒時間", String.valueOf(lesstimeToal));
                String currenttimeCheck = nowHour + ":" + nowMinute;
                Log.i("check the current time",currenttimeCheck);
                //用來紀錄投入Alarm的時間

                if(counteDownSwitch == true && timeLessTatal == 1) {
                    taskStart = true;
                    timeCostSwitch = true;//啟動任務消耗時間計數器
                }else if(counteDownSwitch == false || timeLessTatal <= 0){//這個功能會在view非活躍的階段失效
                    if(timeCostSwitch){
                        theLessString = "任務進行中";
                    }else {
                        theLessString = "To Reset";

                    }
                    counteDownSwitch = false;
                }else if(timeLessTatal > 0){
                    theLessString = String.valueOf(hours) +":"+ String.valueOf(minutes) +":"+ String.valueOf(seconds);

                }

                view.post(new Runnable() {
                    @Override
                    public void run() {view.setText("剩餘時間：" + theLessString);}
                });


                // 再次安排執行，保持週期性運行
                handler.postDelayed(this, INTERVAL);

            }
        }
    };

    private final Runnable timeCostHandler  = new Runnable() {
        @Override
        public void run() {
            Log.i("costTime","Thread_timecost costTimeLess " + costTimeLess);

            if(timeCostSwitch) {
                    if (costTimeLess == 0) {
                        timeCostSwitch = false;//將檢查功能關閉
                        if(btn_timeCost != null) {
                            btn_timeCost.post(new Runnable() {
                                @Override
                                public void run() {
                                    btn_timeCost.setText("＝任務超時＝");
                                }
                            });
//                            btn_timeCost.setText("＝任務超時＝");
                            Uri raingtoneURI = null;
                            Toast.makeText(context.getApplicationContext(), "＝＝開始任務超時＝＝", Toast.LENGTH_LONG).show();
                            if("unknow".equals( PrefsManager.getMusicSet(context.getApplicationContext(),"customization_music","unknow") ) ){
                                raingtoneURI = Uri.parse("android.resource://" + context.getApplicationContext().getPackageName() + "/raw/default_ringtone");//預設音效
                            }else {
                                raingtoneURI = Uri.parse(PrefsManager.getMusicSet(context.getApplicationContext(),"customization_music","unknow"));
                            }
                            mediaPlayer = MediaPlayer.create(context, raingtoneURI);
                            mediaPlayer.start();
                            extraTimeSwitch = true;
                        }
                    } else {
                        if(costTimeLess / 60 <= 1  && counteDownStop == false){
                            if(btn_timeCost != null) {
                            btn_timeCost.post(new Runnable() {
                                @Override
                                public void run() {
                                    btn_timeCost.setText("剩餘不到 1 分鐘");
                                }
                            });
//                                btn_timeCost.setText("剩餘不到 1 分鐘");
                                costTimeLess -= 1;
                            }
                        }else if(costTimeLess / 60 > 1  && counteDownStop == false){
                            if(btn_timeCost != null) {
                            btn_timeCost.post(new Runnable() {
                                @Override
                                public void run() {
                                    btn_timeCost.setText("剩餘 " + (costTimeLess / 60) + " 分鐘");
                                }
                            });
//                                btn_timeCost.setText("=剩餘 " + (costTimeLess / 60) + " 分鐘=");
                                costTimeLess -= 1;
                            }
                        }else{
                            if(btn_timeCost != null) {
                                btn_timeCost.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        btn_timeCost.setText("已花費 " + (costTimeSave - (costTimeLess / 60)) + " 分鐘");
                                    }
                                });

                            }
                        }
                        Log.i("costTime", "less " + costTimeLess);
                    }

                }

            if(extraTimeSwitch) {
                    btn_timeCost.setClickable(false);
                    extraTime += 1;
                    if (extraTime > 60) {
                        btn_timeCost.setText("超時 " + (extraTime / 60) + " 分鐘");
                        btn_timeCost.setClickable(false);
                    }
                    Log.i("costTime", "extraTime " + extraTime);
            }
            // 再次安排執行，保持週期性運行
            handler2.postDelayed(this, INTERVAL);
        }

    };

    //---------------------------------------------------------------------生命週期
    // 開始週期性執行
    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.post(periodicTask); // 立即執行一次，然後進入週期執行
            handler2.post(timeCostHandler);
        }
    }

    // 停止週期性執行
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(periodicTask);
        handler2.removeCallbacks(timeCostHandler);
    }

    // 釋放資源
    public void release() {
        stop();
        handlerThread.quitSafely();
    }

    //---------------------------------------------------------------------自訂輸出入

    public void setTaskTime(int [] timeSeting,boolean counteDownSwitch){
        this.timeSeting = timeSeting;
        this.counteDownSwitch = counteDownSwitch;
    }

    public boolean getTaskState(){//任務被啟動時會轉true
        return taskStart;
    }
    public void setTaskState(boolean b){
        this.taskStart = b;
    }

    public void setCountdowerOff(){
        counteDownSwitch = false;
    }
    public void setCurrentTimeView(TextView view){
        this.currentTimeView = view;
    }

    public void getTimeTask(TextView view){
        this.view = view;
    }

    public Long getTheLess(){
        Log.d("回傳設定時間", String.valueOf(lesstimeToal));
        Long returnTime = lesstimeToal;
        return returnTime;
    }

    public void settimeCost(Button btn,String input){
        btn_timeCost = btn;
        costTimeLess = Integer.valueOf(input) * 60;//分鐘轉換成秒
        costTimeSave = Integer.valueOf(input);
    }

    public int [] getTimeCost() {//回傳額外以及預計時間
        counteDownStop = true;
        if (costTimeLess == 0) {
            return new int[]{costTimeSave, (extraTime / 60)};
        }else{
           return new int[]{(costTimeSave - (costTimeLess/60)),0};
       }

    }

    public void setExtraTimeSwitch(boolean set){
        extraTimeSwitch = set;
    }

    public boolean getCostTimeSwitchCheck(int check){
        if(check == 0){
            return timeCostSwitch;
        } else if (check == 1) {
            return extraTimeSwitch;
        }else{
            return false;
        }

    }

    public void allValueReset(){
        timeCostSwitch = false;
        extraTimeSwitch = false;
        counteDownStop = false;
        timeSeting[0] = 0;timeSeting[1] = 0;timeSeting[2] = 0;
        costTimeLess = 0;
        extraTime = 0;

    }


}

