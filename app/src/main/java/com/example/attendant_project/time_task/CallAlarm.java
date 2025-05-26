package com.example.attendant_project.time_task;

import android.content.Context;

public class CallAlarm {
    //元名稱叫SwitchControler，但根本沒有switch control的功能，只是被用來呼叫Alarm了
    //而且功能跟setAlarm重複了的感覺，邏輯沒有定義好
    SetAlarm setAlarm;


    public CallAlarm(Context context){
        setAlarm = new SetAlarm(context);

    }

    public void switchOn(Long lesstimeToal){
        setAlarm.setAlarmtime(lesstimeToal);
        setAlarm.setAlarm();

    }

    public void switchOff(){
        setAlarm.AlarmCancel();
    }




}
