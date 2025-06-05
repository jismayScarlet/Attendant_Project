package com.example.attendant_project.chatgpt_conectort;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import java.util.HashMap;
import java.util.Map;

public class ClientFileIO {
    String line = null;
    private final String fileName = "aiChatStram";//當前對話紀錄
    private String resolut = null;



    public String getRoleSet(Context context){
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

    public void saveTextToFile(Context context, String fileName,String oraginContent,String appendContent) {
        try{
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            StringBuilder builder = new StringBuilder(oraginContent);
            if (builder.length() > 5000) {
                // 訊息超過 5000，刪除最前面 500 個字符，理想上是呼叫GPT自己整理一下內容再放回去
                builder.delete(0, Math.min(500, builder.length()));
            }
            String finishContent = String.valueOf(builder.append(appendContent).append("\n"));
            fos.write(finishContent.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            Log.i("儲存", "成功寫入檔案: " + fileName);
        } catch (IOException e) {
            Log.e("儲存", "儲存檔案失敗: " + e.getMessage());
        }
    }

//    public void appendRawTextToFile(Context context, @RawRes int rawResId, String appendContent, String outputFileName) {
//        File outputFile = new File(context.getFilesDir(), outputFileName);
//        try (
//                InputStream inputStream = context.getResources().openRawResource(rawResId);
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//                FileOutputStream fos = new FileOutputStream(outputFile, false); // false = overwrite
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))
//        ) {
//            // Step 1: 讀取原始 raw 資源內容
//            String line;
//            while ((line = reader.readLine()) != null) {
//                writer.write(line);
//                writer.newLine();
//            }
//
//            // Step 2: 加上新的內容
//            writer.write(appendContent);
//            writer.newLine();
//
//            writer.flush();
//
//            Log.i("檔案", "成功將 raw 與新內容寫入: " + outputFile.getAbsolutePath());
//        } catch (IOException e) {
//            Log.e("檔案", "操作失敗: " + e.getMessage());
//        }
//    }


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
            Handler uiHandler = new Handler(Looper.getMainLooper());//丟回UI Thread
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "聊天紀錄上限2000字",Toast.LENGTH_LONG);
                }
            });

            // 刪除最前面 100 個字符
            builder.delete(0, Math.min(100, builder.length()));
        }
        return builder.toString();
    }

    public String readTextFromFile(Context context,String fileName) {
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
        return builder.toString();
    }

    public Map readTextFromFileForPost(Context context) {
        String line;int userMessageN = 0,assistantN = 0;
        String[] userMessage = new String[250],assistant = new String[250];
        //總字數限制2000(建立在chatLog字數限制規則上)，假設每行10個字就也100行

        try (FileInputStream fis = context.openFileInput(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            int counter = 0;
            while ((line = reader.readLine()) != null) {
                //reader.readLine()從前面開始讀file
                String[] part = line.split("マスター：",2);//切成兩行
                if(part.length < 2){//沒有找到字串導致
                    part = line.split("結月ゆかり：",2);

                    if(part.length < 2){
                     Log.i("file IO","RFP read blank");
                     continue;
                    }  else{
                        assistant[assistantN] = part[1];
                        assistantN += 1;
                    }
                }else {
                    userMessage[userMessageN] = part[1];
                    userMessageN += 1;
                }
            }

        } catch (IOException e) {
            Log.e("儲存", "讀取檔案失敗: " + e.getMessage());
        }
        return getLogSplit(userMessage,assistant);
    }

    private Map<String,String[]> getLogSplit(String[] userMessage,String[] assistant){
        Map<String,String[]> result = new HashMap<>();
        result.put("userMessage",userMessage);
        result.put("assistant",assistant);
        return result;
    }
}

//else if (line.split("").toString() == "") {
//        break;
//        }