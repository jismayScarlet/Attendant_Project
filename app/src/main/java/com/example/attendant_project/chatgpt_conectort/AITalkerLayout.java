package com.example.attendant_project.chatgpt_conectort;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.attendant_project.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AITalkerLayout extends AppCompatActivity {
    Context context = this;
    private EditText et_chatBox;
    TextView tv_allMessage;
    Button btn_confireAndSend;
    String namePick = "aiChatStram";

    HandlerThread AITalkerThread = new HandlerThread("AITalkerThread");//創建一個副thread
    private Handler handler;
    private Handler handler2;
    private Runnable inputFinishedRunnable;
    ScrollView scrollView;
    private String roleDate;
    int requestcode = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_taker);
        et_chatBox = findViewById(R.id.et_chatBox);
        tv_allMessage = findViewById(R.id.tv_allMessage);
        btn_confireAndSend = findViewById(R.id.btn_confireAndSend);
        findViewById(R.id.constraintlayout).requestFocus();
        scrollView = findViewById(R.id.scrollView);

        AITalkerThread.start();//要先啟動才能拿到looper
        handler = new Handler(AITalkerThread.getLooper());//如果Hanlder(這邊使用getMAinLooper會得到主Thread
        handler2 = new Handler(AITalkerThread.getLooper());//如果Hanlder(這邊使用getMAinLooper會得到主Thread
        handler.post(inputFinishedRunnable);
        handler2.post(checkGPTRespone);

        Intent getSystemRoleSet = new Intent(this, ClientFileReader.class);
        startActivityForResult(getSystemRoleSet,requestcode);

        chatBoxChangeWatcher();
        readTextFromFile();Log.d("talker box", "過往紀錄初始完畢");
        messageSendButton();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler2.removeCallbacks(checkGPTRespone);
        AITalkerThread.quitSafely();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestcode && resultCode == RESULT_OK) {
            if (data != null) {
                roleDate = data.getStringExtra("roleDate");
                ChatGPTClient.callChatGPTSetSystemRole(roleDate);
            }
        }
    }

    private void chatBoxChangeWatcher() {
        tv_allMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (inputFinishedRunnable != null) {
                    handler.removeCallbacks(inputFinishedRunnable);
                }
                inputFinishedRunnable = () -> {
                    saveTextToFile(context, namePick, s.toString());
//                    Log.d("Talker box", "輸入完成: " + s.toString() + "\n");

                };
                handler.postDelayed(inputFinishedRunnable, 500);
            }
        });
    }

    String lastString = "0",nowString = "1";
    private Runnable checkGPTRespone = new Runnable() {
        @Override
        public void run() {
            if (ChatGPTClient.getResponed() != null) {
                nowString = ChatGPTClient.getResponed();
                if (!lastString.equals(nowString) ) {
                    Log.d("GPT Response","at Thread response = " + nowString + " " + lastString);
                    Handler uiHandler = new Handler(Looper.getMainLooper());//丟回UI Thread
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_allMessage.append("助手： " + nowString + "\n");
                        }
                    });
                    lastString = nowString;

                    // 延遲執行以等候 TextView 完成排版
                    // 滑動到底位置
                    scrollView.post(() -> {
                    scrollView.smoothScrollTo(0, tv_allMessage.getBottom());
                    Log.d("scroll","success");
                    });
                }
            }
         handler.postDelayed(this,1000);
        }
    };

    private void messageSendButton(){
        btn_confireAndSend.setOnClickListener(new View.OnClickListener() {
            String message;
            @Override
            public void onClick(View v) {
                tv_allMessage.append( "我： " + et_chatBox.getText() + "\n");
                message = String.valueOf(et_chatBox.getText());
                ChatGPTClient.callChatGPTInBackground(message);
                et_chatBox.setText(null);
            }
        });
    }



    private void saveTextToFile(Context context, String fileName, String content) {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        } catch (IOException e) {
            Log.e("儲存", "儲存檔案失敗: " + e.getMessage());
        }
    }

    private void readTextFromFile() {//開啟軟體時讀取字串
        if ( tv_allMessage == null) {
            Log.e("儲存", "無效的索引或 TextInputEditText 陣列");
            return;
        }
        StringBuilder builder = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(namePick);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException e) {
            Log.e("儲存", "讀取檔案失敗: " + e.getMessage());
        }
        tv_allMessage.setText(builder.toString());
    }

}
