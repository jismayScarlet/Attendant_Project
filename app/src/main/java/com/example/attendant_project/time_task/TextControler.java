package com.example.attendant_project.time_task;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TextControler extends AppCompatActivity {  //建立實體之後要用startActivity來啟動
    private final String[] archiveName = {"taskName", "taskDetailExcept", "taskDetailRealize", "taskDetailReplenish"};
    private String timeInfo = null;
    private String stringTidied = null;//整理過的儲存訊息
    private ActivityResultLauncher<Intent> createDocumentLauncher = null;
    private ActivityResultLauncher<Intent> openDocumentLauncher = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        timeInfo = getIntent().getStringExtra("TIME_INFO"); // 從 Intent 傳入 timeInfo
        allInfoOutPut();//彙整所有editText資料

//        createDocumentLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data = result.getData();
//                        if (data != null) {
//                            Uri uri = data.getData();
//                            writeTextToUri(uri, stringTidied);//已讀取本次要紀錄的文檔
//                            finish();
//                        }
//                    }
//                }
//        );
//
//        // 啟動檔案選擇器
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("text/plain"); // 設定 MIME 類型，*/* 表示所有檔案類型
//        intent.putExtra(Intent.EXTRA_TITLE, "taskLog.txt");
//        createDocumentLauncher.launch(intent);

        // 啟動檔案選擇器以選擇現有檔案
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

// 在 createDocumentLauncher 的回調中寫入內容
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

// ActivityResultLauncher 的回調
        openDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri uri = data.getData();
                            if (uri != null) {
                                appendOrOverwriteToUri(uri, stringTidied);
                                Toast.makeText(this, "任務內容完成儲存", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                        }
                    }

                    // 如果取消或資料為空，則轉向新建檔案
                    createNewDocument("taskLog.txt",stringTidied);

                }
        );

        Toast.makeText(this, "[覆蓋存檔]\n若無舊檔 請按返回建立新檔", Toast.LENGTH_LONG).show();
        openDocumentLauncher.launch(intent);

    }
    //封裝寫入流程
    private void appendOrOverwriteToUri(Uri uri, String newContent) {
        String existing = readTextFromUri(uri);
        String merged = existing + "\n" + newContent;
        writeTextToUri(uri, merged);
    }

    // 讀取檔案內容的方法
    private String readTextFromUri(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    private void writeTextToUri(Uri uri, String content) {
        try (OutputStream outputStream = getContentResolver().openOutputStream(uri, "wt")) {
            if (outputStream != null) {
                outputStream.write(content.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // 建立新檔案的方法
    private void createNewDocument(String fileName, String content) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);


        createDocumentLauncher.launch(intent);
    }

//    private void writeTextToUri(Uri uri, String text) {//寫入檔案
//        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
//            if (outputStream != null) {
//                outputStream.write(text.getBytes(StandardCharsets.UTF_8));
//                outputStream.flush();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void allInfoOutPut() {
        StringBuilder content = new StringBuilder();
        String[] labels = {"任務:", "目標:", "結果:", ""};
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("u/LLL/dd");
        String date = now.format(dateTimeFormatter);

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
                content.append(date + "\n" + "＝非預約型任務＝\n");
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
