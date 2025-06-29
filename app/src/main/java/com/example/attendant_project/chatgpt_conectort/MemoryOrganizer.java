package com.example.attendant_project.chatgpt_conectort;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MemoryOrganizer {

    /*雖及給GPT送去資訊之後，由GPT自行彙整成結果儲存起來
    * 請GPT以角色設定為原則去處理訊息，
    * 會加在system的後方作為附加訊息
    * */
    Context context;
    private final static String AIThoughts = "AIToughts";//記憶檔案名稱

    HandlerThread handlerThreadInOrganizer = new HandlerThread("memoryOrganization");
    Handler handlerInOrganizer;
    Runnable memoryOrganization;
    int timeSeedMin = 300, timeSeedMax = 1800;//程序啟動等待區間 預設300~600 秒
    private static String thoughts=null;//思考記憶
    private String chatLog=null,system=null,roleSet=null,message=null,assistant=null;
    private static String result = null;

    public MemoryOrganizer(Context context){
        this.context = context;
    }

    public String getResult(){return  result;}

    public void logOrganize(){//對話整理
        ClientFileIO clientFileIO = new ClientFileIO();
        handlerThreadInOrganizer.start();
        handlerInOrganizer = new Handler(handlerThreadInOrganizer.getLooper());
        thoughts = clientFileIO.readTextFromFile(context,AIThoughts);//取得 思考記憶
        chatLog = clientFileIO.readTextFromFile(context,new ClientFileIO().getChatLogName());//當前對話紀錄
        roleSet = clientFileIO.getRoleSet(context);
        if(!TextUtils.isEmpty(thoughts) || !TextUtils.isEmpty(chatLog)){
            system = roleSet;
            String messageMix =
                    "提示：這是對話內容記憶工具。" +
                    "1.保留每句抬頭，如:マスター：" +
                    "2.維持內容的大綱順序。" +
                    "3.確保完全理解內容的主題和要點。" +
                    "4.精簡每句對話並保留重點" +
                    "重要注意：這不是使用者傳遞的訊息，是程式功能。";
            message =  thoughts + chatLog + "。 將前面的段落以下面的方式整理起來，" + messageMix;
            Log.i("memory organizer","(unstarted) organize by:\n" + thoughts);
        }else if(!TextUtils.isEmpty(thoughts)){
            system = roleSet;
            String messageMix =
                    "提示：這是對話內容記憶工具。" +
                    "1.保留每句抬頭，如:マスター：" +
                    "2.維持內容的大綱順序。" +
                    "3.確保完全理解內容的主題和要點。" +
                    "4.精簡每句對話並保留重點" +
                    "重要注意：這不是使用者傳遞的訊息，是程式功能。";
            message =   chatLog + "。 將前面的段落以下面的方式整理起來，" + messageMix;
            Log.i("memory organizer","(unstarted) organize by:\n" + thoughts);
        }else{
            message = "對使用者提出自己的角色設定。";
        }

        memoryOrganization = new Runnable() {
            @Override
            public void run() {
                    try {
                        Toast.makeText(context, "(回憶中)", Toast.LENGTH_SHORT).show();

                        if(!TextUtils.isEmpty(thoughts)) {
                            String organized = ChatGPTClient.sendMessageSample(system, message, assistant, "gpt-4.1-2025-04-14");//o4-mini
                            clientFileIO.saveTextToFile(context, organized, AIThoughts);
                            result = ChatGPTClient.sendMessageSample(roleSet + organized, "隨機一件事情，但不要說出來。","我正準備跟使用者提起角色設定裡面的一件事情。", "gpt-4.1-nano-2025-04-14");
                        }else {
                            result = ChatGPTClient.sendMessageSample(system,message,"gpt-4.1-nano-2025-04-14");
                        }
                        ChatGPTClient.putDiscourseSentence(result);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                startOrganizer();
            }
        };
    }

    public void changeRoleInfo(String title,String detail) throws JSONException, IOException {
        String insertedRoleInfo = new ClientFileIO().readTextFromFile(context,"systemRoleInsert");
        String mix = "[" + title + "，" + detail + "]";
        String roleSet = new ClientFileIO().getRoleSet(context);
        String result =
                ChatGPTClient.sendMessageSample(roleSet,"將後面所有的資料，配合依照系統設定，製成新的系統角色訊息" + insertedRoleInfo + mix,"gpt-4o-2024-11-20");
        new ClientFileIO().saveTextToFile(context,result,"systemRoleInsert");
    }

    /*
    * 外部關閉handler & Thread
    * */
    public void shutDownOrganize(){
            if(memoryOrganization != null) {
                handlerInOrganizer.removeCallbacks(memoryOrganization);
                handlerThreadInOrganizer.quitSafely();
            }
    }

    public void pauseOrangize() {
        if(memoryOrganization != null) {
            handlerInOrganizer.removeCallbacks(memoryOrganization);
            Log.i("memory organizer","organizer is paused");
        }
    }
    public String memoryOrganizerExclusiveFileIO(boolean isInput,String outPutContent){
        if(isInput){
        return  new ClientFileIO().readTextFromFile(context,AIThoughts);
        }else{
            new ClientFileIO().saveTextToFile(context,AIThoughts,outPutContent);
            return "Success";
        }
    }

    public void restart(){//重新計時
        if(memoryOrganization != null) {
            handlerInOrganizer.removeCallbacks(memoryOrganization);
            startOrganizer();
        }
    }

    public void startOrganizer(){
        int radomTimeInt = 0;
        Random randomTime = new Random();
        radomTimeInt = (randomTime.nextInt(Math.abs(timeSeedMax-timeSeedMin)) + timeSeedMin) * 1000;
        if(memoryOrganization != null) {
        handlerInOrganizer.postDelayed(memoryOrganization,radomTimeInt);
        Log.i("memory organizer","下次回圈啟動間隔:" + (radomTimeInt/60000) +" mins");
        }else{
            Log.e("memory organizer","startOrganize fail");
        }
    }

    public boolean starOrganizerWithoutRT(){
        if(memoryOrganization != null) {
            handlerInOrganizer.removeCallbacks(memoryOrganization);
            handlerInOrganizer.post(memoryOrganization);
            startOrganizer();
            return true;
        }else if(memoryOrganization == null){
            logOrganize();
            handlerInOrganizer.post(memoryOrganization);
            return true;
        }else {
            return false;
        }
    }

    public void memoryClean(){
        ClientFileIO clientFileIO = new ClientFileIO();
        if(!clientFileIO.deleteFile(context,AIThoughts)){
            Log.e("memory organizer","memoryClean is fail");
        }else{
            Log.i("memory organizer","memoryClean is success");
        }
        Toast.makeText(context,"對話記憶清除",Toast.LENGTH_LONG);
    }

    public String showMemory()  {
        ClientFileIO clientFileIO = new ClientFileIO();
        String result = null;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(() -> {
            // 執行一些工作
            return clientFileIO.readTextFromFile(context,AIThoughts);
        });

// 等待結果（會阻塞直到完成）
        try {
            result = future.get();
        } catch (ExecutionException e) {
            Throwable realCause = e.getCause();
            realCause.printStackTrace(); // 查看實際錯誤
        } catch (InterruptedException e) {
            throw new RuntimeException("在command AIThought oragnize\n" + e);
        }
        return result;
    }

    public void memoryInsert(String content){
        ClientFileIO clientFileIO = new ClientFileIO();
        StringBuilder builder = clientFileIO.readTextFromFileToStringBulder(context,AIThoughts);
        builder.append(content + "\n");
        clientFileIO.saveTextToFile(context,builder.toString(),AIThoughts);
    }

    public String getAIThoughtsName(){
        return AIThoughts;
    }
}
