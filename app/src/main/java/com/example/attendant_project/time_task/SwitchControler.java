package com.example.attendant_project.time_task;

import android.content.Context;
import android.widget.Switch;

public class SwitchControler extends Switch {
    SetAlarm setAlarm;


    public SwitchControler(Context context){
        super(context);
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
