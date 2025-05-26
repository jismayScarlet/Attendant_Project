package com.example.attendant_project.time_task;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME [] = {"timetaskSet","musicSet"};//固定timetask的SharedPreferences檢索名稱

    public PrefsManager(){


    }

    public static void putTimeSet(Context context,String time_key,int value){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME[0],Context.MODE_PRIVATE);
        prefs.edit().putInt(time_key,value).apply();
    }

    public static int getTimeSet(Context context,String time_key,int def){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME[0],Context.MODE_PRIVATE);
        return prefs.getInt(time_key,def);
    }

    public static void putMusicSet(Context context,String music_key,String value){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME[1],Context.MODE_PRIVATE);
        prefs.edit().putString(music_key,value).apply();
    }

    public static String getMusicSet(Context context, String music_key, String def){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME[1],Context.MODE_PRIVATE);
        return prefs.getString(music_key,def);
    }

}//測試改動
