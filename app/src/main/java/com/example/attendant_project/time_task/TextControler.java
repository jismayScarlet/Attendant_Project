package com.example.attendant_project.time_task;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TextControler extends AppCompatActivity {  //建立實體之後要用startActivity來啟動
    private final String[] archiveName = {"taskName", "taskDetailExcept", "taskDetailRealize", "taskDetailReplenish"};
    private String timeInfo = null;
    private String stringTidied = null;
    private ActivityResultLauncher<Intent> createDocumentLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        timeInfo = getIntent().getStringExtra("TIME_INFO"); // 從 Intent 傳入 timeInfo
        allInfoOutPut();//彙整所有editText資料

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            writeTextToUri(uri, stringTidied);
                            finish();
                        }
                    }
                }
        );

        // 啟動檔案選擇器
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain"); // 設定 MIME 類型，*/* 表示所有檔案類型
        intent.putExtra(Intent.EXTRA_TITLE, "taskLog.txt");
        createDocumentLauncher.launch(intent);

    }

    private void writeTextToUri(Uri uri, String text) {//寫入檔案
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(text.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void allInfoOutPut() {
        StringBuilder content = new StringBuilder();
        String[] labels = {"任務:", "目標:", "結果:", ""};

        try {
            // Read archiveName[0] (task)
            try (FileInputStream fis = this.openFileInput(archiveName[0]);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                String line = reader.readLine();
                if (line != null) {
                    content.append(labels[0]).append(line).append("\n");
                }
            }

            // Append timeInfo
            if (timeInfo != null) {
                content.append(timeInfo).append("\n");
            } else {
                content.append("＝非預約型任務＝\n");
                Log.w("儲存", "timeInfo is null");
            }

            // Read archiveName[1] to archiveName[3]
            for (int i = 1; i <= 3 && i < archiveName.length; i++) {
                try (FileInputStream fis = this.openFileInput(archiveName[i]);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
                    String line = reader.readLine();
                    if (line != null) {
                        content.append(labels[i]).append(line).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            Log.e("儲存", "字串整理階段錯誤：" + e.getMessage());
        }

        this.stringTidied = content.toString();
    }

//    private void saveToDownloads(){//未實行的功能
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.Downloads.DISPLAY_NAME, "taskLog.txt");
//        values.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
//        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
//
//        ContentResolver resolver = getContentResolver();
//        Uri uri = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
//        }
//
//        try (OutputStream out = resolver.openOutputStream(uri)) {
//            out.write(stringTidied.getBytes(StandardCharsets.UTF_8));
//            out.flush();
//        }catch(IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
