package com.example.attendant_project.chatgpt_conectort;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.attendant_project.sqlite.DatabaseHelper;

public class Command {
    Context contextFrom = null;
    String resultContent = null;
    static boolean ENDOW = false;
    boolean startSuccese = false;
    ClientFileIO clientFileIO = new ClientFileIO();
    final EditText insertMessage = new EditText(contextFrom);

    public String getResultContent(){
        return resultContent;
    }

    public static void setENDOW(Boolean set){ENDOW = set;}
    public static Boolean getENDOW(){return ENDOW;}

    public Command(Context context){
        contextFrom = context;
    }

    public void command(String message,String assisPrefs_name,String[] assistannameFile) {
            ENDOW = true;
            switch (message) {
                case "清除聊天紀錄":
                    new MemoryOrganizer(contextFrom.getApplicationContext()).memoryClean();
                    resultContent = "AIThoughts 清除程序執行完畢";
                    Log.i("AIThoughts", "清除AIThoughts finish");
                    break;

                case "調出聊天紀錄":
                    String content = new MemoryOrganizer(contextFrom).showMemory();
                    if (!TextUtils.isEmpty(content)) {
//                        Toast.makeText(contextFrom.getApplicationContext(),"以下為AIThought的記憶資料:\n" + content,Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "以下為AIThought的記憶資料:\n" + content);
                        resultContent = "以下為AIThought的記憶資料:\n" + content;
                    } else {
//                        Toast.makeText(contextFrom.getApplicationContext(), "以下為AIThought的記憶資料:\n" + content, Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "沒有記憶資料");
                        resultContent = "沒有記憶資料：" + content;
                    }
                    break;
                case "立刻執行聊天紀錄整理":
                        startSuccese = new MemoryOrganizer(contextFrom).starOrganizerWithoutRT();
                        content = new MemoryOrganizer(contextFrom).showMemory();
                    if (startSuccese && !TextUtils.isEmpty(content)) {
//                        Toast.makeText(contextFrom.getApplicationContext(), "記憶整理完成，新記憶資料:\n" + content, Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "執行記憶整理成功，等待整理結果：\n");
                        resultContent = "記憶整理完成，新記憶資料:\n" + content;
                    } else if (!startSuccese) {
                        Toast.makeText(contextFrom.getApplicationContext(), "系統異常:思考功能未被載入\n", Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "系統異常:思考功能未被載入"  + content);
//                        resultContent = "系統異常:思考功能未被載入";
                    } else if (TextUtils.isEmpty(content)) {
                        Toast.makeText(contextFrom.getApplicationContext(), "系統異常:無已儲存系統記憶", Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "系統異常:無已儲存系統記憶");
                    } else {
                        resultContent = "其他未檢知異常";
                    }
                    break;
                case "插入聊天紀錄":
                    new AlertDialog.Builder(contextFrom)
                            .setMessage("新增的 聊天紀錄 內容")
                            .setView(insertMessage)
                            .setPositiveButton("插入", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new MemoryOrganizer(contextFrom).memoryInsert(insertMessage.getText().toString());
                                    Toast.makeText(contextFrom, "插入聊天紀錄完成", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                    break;
                case "清除助手全部名稱":
                    boolean a = new ClientFileIO().cleanAssistantName(contextFrom, assisPrefs_name, assistannameFile[0]);
                    boolean b = new ClientFileIO().cleanAssistantName(contextFrom, assisPrefs_name, assistannameFile[1]);
                    if (a && b) {
                        Toast.makeText(contextFrom, "清除助手全名 finish", Toast.LENGTH_SHORT).show();
                        Log.i("data clean", "清除助手全部名稱 finish");
                    }
                    break;
                case "調出系統角色設定":
                    resultContent = clientFileIO.readTextFromFile(contextFrom,"systemRoleInsert");
                    break;
                case "清除系統角色設定":
                    clientFileIO.deleteFile(contextFrom, "systemRoleInsert");
                    Toast.makeText(contextFrom, "動態角色設定資料 刪除完成", Toast.LENGTH_SHORT).show();
                    break;
                case "插入系統角色設定":
                    new AlertDialog.Builder(contextFrom)
                            .setMessage("新增的 系統角色設定 內容")
                            .setView(insertMessage)
                            .setPositiveButton("插入", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    clientFileIO.fileOverride(contextFrom,"systemRoleInsert",insertMessage.getText().toString(),0,0);
                                    Toast.makeText(contextFrom, "插入系統角色設定", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                    break;
                case "清除系統記憶使用者訊息":
                    new DatabaseHelper(contextFrom).destroyDataBase(contextFrom);
                    Toast.makeText(contextFrom, "使用者關聯資料 刪除完成", Toast.LENGTH_SHORT).show();
                    break;
                case "清單":
                    resultContent =
                        "\n清除聊天紀錄\n" +
                        "調出聊天紀錄\n" +
                        "立刻執行聊天紀錄整理\n" +
                        "   注意，聊天紀錄會經由系統精簡後再儲存，可能會有若干過度精簡，如有需要補充，請再重新插入。\n" +
                        "插入聊天紀錄\n" +
                        "   注意，格式必須依賴以下否則會無法成功辨識；\n" +
                        "   マスター：輸入內容(換行)\n" +
                        "   你的系統小名：輸入內容(換行)\n" +
                        "   若一直無法辨識，請使用\"立刻執行聊天紀錄整理\"將現在存在的聊天紀錄整理後存檔\n" +
                        "清除助手全部名稱\n" +
                        "調出系統角色設定\n" +
                        "清除系統角色設定\n" +
                        "插入系統角色設定\n" +
                        "清除系統記憶使用者訊息\n";
                    break;
            }
        }

}
