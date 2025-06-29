package com.example.attendant_project.chatgpt_conectort;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.attendant_project.R;

public class CommandInputViewControler extends Activity {
    private EditText et_chatBox;
    TextView tv_allMessage;
    Button btn_confireAndSend,btn_stopChatAndClear;
    ScrollView scrollView;
    Intent intent = getIntent();
    String assisPrefs_name = null;
    String[] assistannameFile = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.command_fragment);
        et_chatBox = findViewById(R.id.et_chatBox);
        tv_allMessage = findViewById(R.id.tv_chatLog);
        btn_confireAndSend = findViewById(R.id.btn_confireAndSend);
        btn_stopChatAndClear = findViewById(R.id.btn_stopChatAndClear);
        findViewById(R.id.cii_fragment).requestFocus();
        scrollView = findViewById(R.id.scrollView);
        assisPrefs_name = intent.getStringExtra("assisPrefs_name");
        assistannameFile = intent.getStringArrayExtra("assistannameFile");

        setBtn_stopChatAndClear();
        setBtn_confireAndSend();
        chatBoxPushDownEnterKey();
    }



    private void sendToChatLog(String message){
        StringBuilder builder = new StringBuilder();
        builder.append(tv_allMessage.getText());
        builder.append(message + "\n");
        Command command = new Command(CommandInputViewControler.this);
        command.command(message,assisPrefs_name,assistannameFile);
        builder.append(command.getResultContent() + "\n");//非同步
        tv_allMessage.setText(builder.toString());
    }

    private void setBtn_stopChatAndClear(){
        btn_stopChatAndClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(CommandInputViewControler.this)
                        .setMessage("是否要結束命令工具?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show()
                        ;
            }
        });
    }

    private void setBtn_confireAndSend(){
        btn_confireAndSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = String.valueOf(et_chatBox.getText());
                sendToChatLog(message);
                et_chatBox.setText(null);
            }
        });
    }

    private void chatBoxPushDownEnterKey(){
        et_chatBox.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String message = String.valueOf(et_chatBox.getText());
                    sendToChatLog(message);
                    et_chatBox.setText(null);
                    Log.d("Key", "按下 Enter 鍵");
                    return true; // 表示已處理事件，不再冒泡
                }
            }
            return false;
        });
    }

}

