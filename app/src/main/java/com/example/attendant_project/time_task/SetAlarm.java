package com.example.attendant_project.time_task;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/*
* 設定鬧鐘
* */

public class SetAlarm {
    Context context; // 獲取 Application Context
    Long alarmtime;
    PendingIntent pendingIntent;AlarmManager alarmManager;
    public SetAlarm(Context context) {
        this.context = context.getApplicationContext(); // 獲取 Application Context
    }

    public boolean setAlarm(){
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE );

        if(alarmtime != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + alarmtime, pendingIntent);
//            setAlarmtime(alarmtime * 1000);
            Log.d("設定鬧鈴瞬間傳值", String.valueOf(alarmtime) + " ms");
            Toast.makeText(context, "鬧鐘已啟動", Toast.LENGTH_SHORT).show();
            return true;
        }else if(alarmtime <= 0){
            Toast.makeText(context,"時間未設定",Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            Log.e("鬧鈴時間設定投入異常","alarmtime 投入時間!= null or <=0");
            return false;
        }

    }

    public void setAlarmtime(long alarmtime){
        Log.d("設定鬧鈴瞬間","設定毫秒值 " + String.valueOf(alarmtime));
            this.alarmtime = alarmtime;
        }

    public void AlarmCancel(){
        pendingIntent.cancel();
        alarmManager.cancel(pendingIntent);
        Toast.makeText(context,"鬧鐘已取消",Toast.LENGTH_SHORT).show();
    }

    }




