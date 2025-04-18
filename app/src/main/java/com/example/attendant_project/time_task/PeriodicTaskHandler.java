package com.example.attendant_project.time_task;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.TextView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class PeriodicTaskHandler {
    /*
    功能： 背景執行重複顯示剩餘時間
    兼任 計算計時器剩餘時間
    */
    private HandlerThread handlerThread;
    private Handler handler;
    private boolean isRunning = false;
    private static final long INTERVAL = 1000; // 1秒
    private int timeSeting[] = new int[3]; private int timeLess[] = new int[3];//設定時間 & 剩餘時間
    private Long lesstimeToal= (long) 0.0;//計算與實際時間差值之秒數總和,用來設定Alarm的倒數時間
    private String theLess = null;//顯示剩餘時間用字串

    int timeLessTatal = 0;int nowTatal = 0;int timeSetingTatal = 0;
    boolean showingOfLesstime ;

    TextView view,currentTimeView;



    int nowHour,nowMinute,nowSecond;

    //**



    public PeriodicTaskHandler(Context context) {
        handlerThread = new HandlerThread("PeriodicHandlerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
//        SetAlarm setAlarm = new SetAlarm(context);
//        setAlarm.setAlarmtime(lesstimeToal);
//        setAlarm.setAlarm();

//        if(timeSeting != null) {
//            timeSetingTatal = timeSeting[0] * 60 * 60 + timeSeting[1] * 60 + timeSeting[2];
//        }else{
//            Log.e("timeSet Error","在時間設置的部分出現空值");
//        }//載入程式時,就將設定時間確認完畢

    }

    private final Runnable periodicTask = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                // 執行背景任務
                Log.d("PeriodicTask", "執行背景任務: " + System.currentTimeMillis());

                //**獲取即刻時間
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Taipei")); //測試用
                //ZonedDateTime now = ZonedDateTime.now(); //實際時區依照裝置
                // 設定格式 (yyyy-MM-dd HH:mm:ss)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String nowtimeShow = now.format(formatter);
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

//                if (timeSetingTatal - nowTatal > 0) {
//                    timeLessTatal = timeSetingTatal - nowTatal;
//                } else if (timeSetingTatal - nowTatal < 0) {
//                    timeLessTatal = nowTatal - timeSetingTatal;
//                } else{
//                    timeLessTatal = 0;
//                }
                if(timeSetingTatal >= nowTatal){
                    timeLessTatal = timeSetingTatal - nowTatal;
                }else {
                    timeLessTatal = (24 * 3600 - nowTatal) + timeSetingTatal;
                }
                Log.i("設定","timeLessTatal " + String.valueOf(timeLessTatal));


//                int test = (timeLess[0]*60*60 + timeLess[1]*60 + timeLess[2])*1000;
//                Log.d("lesstime real value","int " + String.valueOf(test));
//                lesstimeToal = Long.valueOf((timeLess[0]*60*60 + timeLess[1]*60)*1000); //換算成毫秒
                lesstimeToal = Long.valueOf(timeLessTatal);

                long hours = TimeUnit.MILLISECONDS.toHours(lesstimeToal);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(lesstimeToal) % 60;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(lesstimeToal) % 60;
                Log.i("投入Alarm的時刻",hours +":"+ minutes +":"+ seconds);
                Log.i("預計投入Alarm的毫秒時間", String.valueOf(lesstimeToal));
                String currenttimeCheck = nowHour + ":" + nowMinute;
                Log.i("check the current time",currenttimeCheck);
                //用來紀錄投入Alarm的時間



                if(showingOfLesstime == false || timeLessTatal == 0){
                    theLess = "To reSet";
                    showingOfLesstime = false;
                }else{
                    if (timeSeting[0] >= nowHour) {
                        timeLess[0] = timeSeting[0] - nowHour;
                    } else {
                        timeLess[0] = (24 - nowHour) + timeSeting[0];
                    }//時刻

                    if (timeSeting[1] >= nowMinute) {
                        timeLess[1] = timeSeting[1] - nowMinute - 1;
                    } else {
                        timeLess[1] = (60 - nowMinute) + timeSeting[1];
                    }//分刻

                    if (nowSecond != 0) {
                        timeLess[2] = 60 - nowSecond;
                    } else {
                        timeLess[2] = 0;
                    }
                    //秒刻
                    theLess = String.valueOf(timeLess[0]) + ":" + String.valueOf(timeLess[1]) + ":" + String.valueOf(timeLess[2]);
                    }

                view.post(new Runnable() {
                    @Override
                    public void run() {view.setText("剩餘時間：" + theLess);}
                });

                // 再次安排執行，保持週期性運行
                handler.postDelayed(this, INTERVAL);
            }
        }
    };

    // 開始週期性執行
    public void start() {
        if (!isRunning) {
            isRunning = true;
            handler.post(periodicTask); // 立即執行一次，然後進入週期執行
        }
    }

    // 停止週期性執行
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(periodicTask);
    }

    // 釋放資源
    public void release() {
        stop();
        handlerThread.quitSafely();
    }

    public void setTaskTime(int [] timeSeting,boolean showingOfLesstime){
        this.timeSeting = timeSeting;
        this.showingOfLesstime = showingOfLesstime;
    }

    public void setCountdowerOfff(){
        showingOfLesstime = false;
    }
    public void setCurrentTimeView(TextView view){
        this.currentTimeView = view;
    }

    public void getTimeTask(TextView view){
        this.view = view;
    }

    public Long getTheLess(){
        Log.d("回傳設定時間", String.valueOf(timeLessTatal));
        Long returnTime = Long.valueOf(timeLessTatal * 1000);//轉成毫秒在回傳
        return returnTime;
    }
}

