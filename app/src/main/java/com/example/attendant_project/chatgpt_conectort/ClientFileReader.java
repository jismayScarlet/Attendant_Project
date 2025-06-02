package com.example.attendant_project.chatgpt_conectort;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.attendant_project.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ClientFileReader {
    String line = null;
    private final String fileName = "aiChatStram";//當前對話紀錄
    private String resolut = null;



    public String getChatMemery(Context context){
        InputStream inputStream = context.getResources().openRawResource(R.raw.system_role_set);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();
        try {
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        resolut = content.toString();

        return  resolut;
    }



    public void saveTextToFile(Context context, String content) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            if(content.length() > 2000){
                StringBuilder builder = new StringBuilder(content);
                builder.delete(0,Math.min(100,builder.length()));
                content = builder.toString();
                Toast.makeText(context,"聊天紀錄上限2000字",Toast.LENGTH_LONG);
            }
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException e) {
            Log.e("儲存", "儲存檔案失敗: " + e.getMessage());
        }
    }

    //開啟軟體時讀取字串
    public String readTextFromFile(Context context) {
        StringBuilder builder = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e("儲存", "讀取檔案失敗: " + e.getMessage());
        }

        // 判斷是否超過 2000 個 token（假設每個 UTF-16 字元當作一個 token）
        if (builder.length() > 2000) {
            Toast.makeText(context,"聊天紀錄上限2000字",Toast.LENGTH_LONG);
            // 刪除最前面 100 個字符
            builder.delete(0, Math.min(100, builder.length()));
        }

        return builder.toString();
    }
}
