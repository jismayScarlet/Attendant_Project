package com.example.attendant_project.time_task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TextWatcher {
    private Runnable inputFinishedRunnable;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final String[] archiveName = {"taskName", "taskDetailExcept", "taskDetailRealize", "taskDetailReplenish"};
    String timeInfo = null;

    public void setupDebouncedWatcher(Context context,EditText[] page) {
        if (page == null) return;
        for (int i = 0; i < 4 && i < page.length; i++) {
            EditText editText = page[i];
            String namePick = archiveName[i];
            editText.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (inputFinishedRunnable != null) {
                        handler.removeCallbacks(inputFinishedRunnable);
                    }
                    inputFinishedRunnable = () -> {
                        saveTextToFile(context, namePick, s.toString());
                        Log.d("DebouncedText", "輸入完成: " + s.toString());
                    };
                    handler.postDelayed(inputFinishedRunnable, 500);
                }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }
    }

    public void timeInfoSave(Context context, String fileName, String content, String content2, String content3) {
        timeInfo = "任務開始於:" + content3 + " | 總費時:" + content + " 超時:" + content2 + "(分鐘)";
        saveTextToFile(context, fileName, content);
    }

    public String getTimeInfo(){
        return timeInfo;
    }

    private void saveTextToFile(Context context, String fileName, String content) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException e) {
            Log.e("儲存", "儲存檔案失敗: " + e.getMessage());
        }
    }

    public void clearTheInfo(Context context){
        for (int i = 0; i < 4 ; i++) {
            saveTextToFile(context, archiveName[i], "");
            Log.d("DebouncedText", "輸入完成: ");
        }
    }

    public void readTextFromFile(Context context, int i, EditText[] editTexts) {//開啟軟體時讀取字串
        if (i < 0 || i >= archiveName.length || editTexts == null || i >= editTexts.length) {
            Log.e("儲存", "無效的索引或 EditText 陣列");
            return;
        }
        StringBuilder builder = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(archiveName[i]);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            Log.e("儲存", "讀取檔案失敗: " + e.getMessage());
        }
        editTexts[i].setText(builder.toString());
    }
}
