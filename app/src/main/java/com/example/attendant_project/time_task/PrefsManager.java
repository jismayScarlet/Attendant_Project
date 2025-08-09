package com.example.attendant_project.time_task;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREF_NAME [] = {"timetaskSet","musicSet"};//固定timetask的SharedPreferences檢索名稱
    private String INSERT_PREF_NAME = null;

    public PrefsManager(){
    }

    public PrefsManager(String fileName){
        INSERT_PREF_NAME = fileName;
    }

    public static void putTimeSet(Context context,String time_key,int value){
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME[0],Context.MODE_PRIVATE);
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

    public void setFloat(Context context,String key,Float value){
        SharedPreferences prefs = context.getSharedPreferences(INSERT_PREF_NAME,Context.MODE_PRIVATE);
        prefs.edit().putFloat(key,value).apply();
    }

    public Float getFloat(Context context,String key,Float def){
        SharedPreferences prefs = context.getSharedPreferences(INSERT_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getFloat(key,def);
    }


    public static void setInt(Context context,String prefName,String key,int value){
        SharedPreferences prefs = context.getSharedPreferences(prefName,Context.MODE_PRIVATE);
        prefs.edit().putInt(key,value).apply();
    }

    public static int getInt(Context context,String prefName, String key, int def){
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return prefs.getInt(key,def);
    }

    public static void setBoolean(Context context,String prefName,String key,boolean value){
        SharedPreferences prefs = context.getSharedPreferences(prefName,Context.MODE_PRIVATE);
        prefs.edit().putBoolean(key,value).apply();
    }

    public static Boolean getBoolean(Context context,String prefName,String key,boolean def){
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return prefs.getBoolean(key,def);
    }

    public static void setFloat(Context context,String prefName,String key,Float value){
        SharedPreferences prefs = context.getSharedPreferences(prefName,Context.MODE_PRIVATE);
        prefs.edit().putFloat(key,value).apply();
    }

    public static Float getString(Context context,String prefName,String key,Float def){
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return prefs.getFloat(key,def);
    }

    public static void setString(Context context,String prefName,String key,String value){
        SharedPreferences prefs = context.getSharedPreferences(prefName,Context.MODE_PRIVATE);
        prefs.edit().putString(key,value).apply();
    }

    public static String getString(Context context,String prefName,String key,String def){
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return prefs.getString(key,def);
    }



    public void setString(Context context,String key,String value){
        SharedPreferences prefs = context.getSharedPreferences(INSERT_PREF_NAME,Context.MODE_PRIVATE);
        prefs.edit().putString(key,value).apply();
    }

    public String getString(Context context,String key,String def){
        SharedPreferences prefs = context.getSharedPreferences(INSERT_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key,def);
    }

}//測試改動
