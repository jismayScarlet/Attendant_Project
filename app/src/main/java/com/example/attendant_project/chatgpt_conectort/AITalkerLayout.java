package com.example.attendant_project.chatgpt_conectort;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendant_project.R;

public class AITalkerLayout extends AppCompatActivity {
    Context context = AITalkerLayout.this;
    private EditText et_chatBox;
    TextView tv_allMessage;
    Button btn_confireAndSend,btn_stopChatAndClear;


    HandlerThread AITalkerThread = new HandlerThread("AITalkerThread");//創建一個副thread
    private Handler handler;
    private Handler handler2;
    private Runnable inputFinishedRunnable;
    ScrollView scrollView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.llm_talker_layout);
        et_chatBox = findViewById(R.id.et_chatBox);
        tv_allMessage = findViewById(R.id.tv_allMessage);
        btn_confireAndSend = findViewById(R.id.btn_confireAndSend);
        btn_stopChatAndClear = findViewById(R.id.btn_stopChatAndClear);
        findViewById(R.id.constraintlayout).requestFocus();
        scrollView = findViewById(R.id.scrollView);

        AITalkerThread.start();//要先啟動才能拿到looper
        handler = new Handler(AITalkerThread.getLooper());//如果Hanlder(這邊使用getMAinLooper會得到主Thread
        handler2 = new Handler(AITalkerThread.getLooper());//如果Hanlder(這邊使用getMAinLooper會得到主Thread
        handler.post(inputFinishedRunnable);
        handler2.post(checkGPTRespone);


        tv_allMessage.setText(new ClientFileIO().readTextFromFile(AITalkerLayout.this));Log.d("talker box", "過往紀錄初始完畢");

        chatBoxChangeWatcher();


        chatBoxPushDownEnterKey();
        messageSendButton();
        stopAndClearChat();

        scrollView.post(() -> {
            scrollView.smoothScrollTo(0, tv_allMessage.getBottom());
            Log.d("scroll","success");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler2.removeCallbacks(checkGPTRespone);
        AITalkerThread.quitSafely();
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
                    new ClientFileIO().saveTextToFile(context, s.toString());
//                    Log.d("Talker box", "輸入完成: " + s.toString() + "\n");

                };
                handler.postDelayed(inputFinishedRunnable, 500);
            }
        });
    }



    //////送出訊息
    private void chatBoxPushDownEnterKey(){
        et_chatBox.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    messageSend();
                    Log.d("Key", "按下 Enter 鍵");
                    return true; // 表示已處理事件，不再冒泡
                }
            }
            return false;
        });
    }
    private void messageSendButton(){
        btn_confireAndSend.setOnClickListener(new View.OnClickListener() {
            String message;
            @Override
            public void onClick(View v) {
                messageSend();
            }
        });
    }

    private void messageSend(){//統一呼叫傳輸方法
        String message;
        message = String.valueOf(et_chatBox.getText());
        tv_allMessage.append( "マスター：" + et_chatBox.getText() + "\n\n");//冠名 マスター：、結月ゆかり： 被鎖定，更改會影響到ClientFileReader取資料
        ChatGPTClient.sendMessage(AITalkerLayout.this,message);
        et_chatBox.setText(null);
    }


    //////送出訊息

    String lastString = "0",nowString = "1";
    private Runnable checkGPTRespone = new Runnable() {
        @Override
        public void run() {
            if (ChatGPTClient.getResponed() != null) {
                nowString = ChatGPTClient.getResponed();
                if (!lastString.equals(nowString) ) {
                    Log.d("GPT Response","at Thread response now=" + nowString + ",last=" + lastString);
                    Handler uiHandler = new Handler(Looper.getMainLooper());//丟回UI Thread
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_allMessage.append("結月ゆかり：" + nowString + "\n\n");
                        }
                    });
                    lastString = nowString;

                    // 延遲執行以等候 TextView 完成排版
                    // 滑動到底位置

                    scrollView.postDelayed(() -> {
                    scrollView.smoothScrollTo(0, tv_allMessage.getBottom());
                    Log.d("scroll","success");
                    },500);
                }
            }
         handler.postDelayed(this,1000);
        }
    };


    private void stopAndClearChat(){
        btn_stopChatAndClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AITalkerLayout.this)
                        .setTitle("終止並清除現在的話題")
                        .setMessage("將無法復原，你確定嗎?")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                tv_allMessage.setText(null);
                                //中止內容的繼續引用
                            }
                        }).show();
            }
        });
    }





}
