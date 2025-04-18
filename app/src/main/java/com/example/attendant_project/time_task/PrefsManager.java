package com.example.attendant_project.time_task;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME = "timetaskSet";//固定timetask的SharedPreferences檢索名稱
    public PrefsManager(){


    }

    public static void putTimeSet(Context context,String time_key,int value){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        prefs.edit().putInt(time_key,value).apply();
    }

    public static int getTimeSet(Context context,String time_key,int def){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        return prefs.getInt(time_key,def);
    }

}//測試改動
