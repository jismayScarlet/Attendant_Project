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
    boolean ENDOW = true;

    public String getResultContent(){
        return getResultContent();
    }

    public Command(Context context){
        contextFrom = context;
    }

    public void command(String message,String assisPrefs_name,String[] assistannameFile) {
            switch (message) {
                case "清除AIThoughts":
                    new MemoryOrganizer(contextFrom.getApplicationContext()).memoryClean();
                    resultContent = "AIThoughts 清除程序執行完畢";
                    Log.i("AIThoughts", "清除AIThoughts finish");
                    break;

                case "調出AIThoughts":
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
                case "AIThoughts oragnize now":
                    boolean startSuccese = new MemoryOrganizer(contextFrom).starOrganizerWithoutRT();
                    content = new MemoryOrganizer(contextFrom).showMemory();
                    if (startSuccese && !TextUtils.isEmpty(content)) {
//                        Toast.makeText(contextFrom.getApplicationContext(), "記憶整理完成，新記憶資料:\n" + content, Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "記憶整理完成，新記憶資料:\n" + content);

                        resultContent = "記憶整理完成，新記憶資料:\n" + content;
                    } else if (!startSuccese) {
                        Toast.makeText(contextFrom.getApplicationContext(), "記憶整理完成，新記憶資料:\n" + content, Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "系統異常:思考功能未被載入");
//                        resultContent = "系統異常:思考功能未被載入";
                    } else if (TextUtils.isEmpty(content)) {
                        Toast.makeText(contextFrom.getApplicationContext(), "系統異常:無已儲存系統記憶", Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts", "系統異常:無已儲存系統記憶");
//                        resultContent = "系統異常:無已儲存系統記憶";
                    } else {
                        resultContent = "其他未檢知異常";
                    }
                    break;
                case "AIThoughts 訊息插入":
                    final EditText insertMessage = new EditText(contextFrom);
                    new AlertDialog.Builder(contextFrom)
                            .setMessage("新增的AIThoughts內容")
                            .setView(insertMessage)
                            .setPositiveButton("插入", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new MemoryOrganizer(contextFrom).memoryInsert(insertMessage.getText().toString());
                                    Toast.makeText(contextFrom, "插入記憶完成", Toast.LENGTH_LONG).show();
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
                case "systemRole inserted information delete":
                    ClientFileIO clientFileIO = new ClientFileIO();
                    clientFileIO.deleteFile(contextFrom, "systemRoleInsert");
                    Toast.makeText(contextFrom, "動態角色設定資料 刪除完成", Toast.LENGTH_SHORT).show();
                    break;
                case "user information delete":
                    new DatabaseHelper(contextFrom).destroyDataBase(contextFrom);
                    Toast.makeText(contextFrom, "使用者關聯資料 刪除完成", Toast.LENGTH_SHORT).show();
                    break;
                case "清單":
                    resultContent =
                            "\n清除AIThoughts\n" +
                                    "調出AIThoughts\n" +
                                    "AIThoughts oragnize now\n" +
                                    "AIThoughts 訊息插入\n" +
                                    "清除助手全部名稱\n" +
                                    "systemRole inserted information delete\n" +
                                    "user information delete\n";
                    break;
            }
        }

}
