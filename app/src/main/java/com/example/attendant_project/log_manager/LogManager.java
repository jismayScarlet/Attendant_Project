package com.example.attendant_project.log_manager;

import android.content.Context;
import android.util.Log;

import com.example.attendant_project.chatgpt_conectort.ClientFileIO;

public class LogManager {
    static Context context = null;
    public static void saveGlobalLog(String content,String time){
        new ClientFileIO().saveTextToFile(context.getApplicationContext(),content + " " + time,"global_log");
        Log.i("globalLog","儲存成功：\n" + content);
    }

    public static String loadGlobalLog(){
        return new ClientFileIO().readTextFromFile(context.getApplicationContext(),"global_log");
    }

}
