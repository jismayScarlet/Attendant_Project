package com.example.attendant_project.chatgpt_conectort;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.attendant_project.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Locale;

public class AITalkerLayout extends AppCompatActivity {
    Context context = AITalkerLayout.this;
    private EditText et_chatBox;
    TextView tv_allMessage;
    Button btn_confireAndSend,btn_stopChatAndClear,btn_voiceInput,btn_postLog;

    CheckBox cb_tts,cb_LongMemery;

    HandlerThread AITalkerThread = new HandlerThread("AITalkerThread");//創建一個副thread
    private Handler handler;
    private Handler handler2;
    private Runnable inputFinishedRunnable;
    ScrollView scrollView;
    TextToSpeech tts;
    String voiceRecognizer;
    MemoryOrganizer mo = new MemoryOrganizer(AITalkerLayout.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.llm_talker_layout);
        et_chatBox = findViewById(R.id.et_chatBox);
        tv_allMessage = findViewById(R.id.tv_chatLog);
        btn_confireAndSend = findViewById(R.id.btn_confireAndSend);
        btn_stopChatAndClear = findViewById(R.id.btn_stopChatAndClear);
        btn_voiceInput = findViewById(R.id.btn_voiceInput);
        btn_postLog = findViewById(R.id.btn_postLog);
        cb_tts = findViewById(R.id.cb_tts);
        cb_LongMemery = findViewById(R.id.cb_longMemery);
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
        ChatGPTClient.EndowReset();
        messageSendButton();
        stopAndClearChat();
        voiceInputSwitch();
        postLog();
        mo.organize();
        mo.startOrganizer();
        longMemoryChecker();

        scrollView.post(() -> {
            scrollView.smoothScrollTo(0, tv_allMessage.getBottom());
            Log.d("scroll","success");
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // 設定語言，例如中文、英文等
                    int result = tts.setLanguage(Locale.JAPANESE);
//                    int result = tts.setLanguage(Locale.TAIWAN);
//                    Log.i("TTS", tts.getVoices().toString());
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        // 初始化成功後可以開始朗讀
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler2.removeCallbacks(checkGPTRespone);
        AITalkerThread.quitSafely();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        mo.shutDownOrganize();
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
        String nameTest = new ClientFileIO().readTextFromFile(AITalkerLayout.this,"AssistantName");
        if(!TextUtils.isEmpty(nameTest))
        {
            tv_allMessage.append( "マスター：" + et_chatBox.getText() + "\n\n");//冠名 マスター：、結月ゆかり： 被鎖定，更改會影響到ClientFileReader取資料
        }
        ChatGPTClient.sendMessage(this,message);
        et_chatBox.setText(null);
    }


    //////送出訊息

    String lastString = "0",nowString = "1";
    int nothingToSay = 0;
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
                            String nickName = null;
                            nickName = new ClientFileIO().getAssistantName(AITalkerLayout.this,"assistantName","AssistantNickName","助手");
                                tv_allMessage.append(nickName + "：" + nowString + "\n\n");
                            try {if(cb_tts.isChecked()){ChatGPTClient.translateToJapen(nowString,tts);}}
                            catch (JSONException e) {throw new RuntimeException("GPT中翻日 JSON post 失敗" + e);}

                            Log.i("TTS","tts.isSpeaking " + tts.isSpeaking() + "\n" + ChatGPTClient.getTranslatedSentence());

                        }
                    });
                    lastString = nowString;

                    // 延遲執行以等候 TextView 完成排版
                    // 滑動到底位置

                    scrollView.postDelayed(() -> {
                    scrollView.smoothScrollTo(0, tv_allMessage.getBottom());
                    Log.d("scroll","success");
                    },100);
                }
            }
            handler2.postDelayed(this,500);
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

    private void voiceInputSwitch(){
     btn_voiceInput
             .setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     voiceInput();
                 }
             });
//             .setOnTouchListener(new View.OnTouchListener() {
//         @Override
//         public boolean onTouch(View v, MotionEvent event) {
//            recognizer.startListening(intent);
//             return false;
//         }
//     });
    }

    private void voiceInput(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請開始說話");

        try {
            startActivityForResult(intent,01);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "您的裝置不支援語音輸入", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 01 && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                voiceRecognizer = results.get(0);
                et_chatBox.setText(voiceRecognizer);
                Log.i("voice recobnizer",voiceRecognizer);
                // 處理語音辨識結果
                messageSend();
            }
        }
    }

    public void postLog(){
            btn_postLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(AITalkerLayout.this)
                            .setTitle("對話紀錄送出")
                            .setMessage("確定要將內容送往 任務工具 的 補充內容 嗎？")
                            .setNegativeButton("取消", null)
                            .setNeutralButton("送出後清除對話內容", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new GPTMemoryTool(AITalkerLayout.this).fileOverride("taskDetailReplenish", String.valueOf(tv_allMessage.getText()), 0, 0);
                                    tv_allMessage.setText(null);
                                }
                            })
                            .setPositiveButton("送出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new GPTMemoryTool(AITalkerLayout.this).fileOverride("taskDetailReplenish", String.valueOf(tv_allMessage.getText()), 0, 0);
                                }
                            })
                            .show();
                }
            });
    }


    private void longMemoryChecker(){
        cb_LongMemery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(cb_LongMemery.isChecked()) {
                    mo.restart();//重新計時
                }
                else if (!cb_LongMemery.isChecked()) {
                    mo.pauseOrangize();
                }
            }
        });
    }
}
