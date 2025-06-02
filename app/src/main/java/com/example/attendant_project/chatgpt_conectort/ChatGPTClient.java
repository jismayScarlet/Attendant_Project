package com.example.attendant_project.chatgpt_conectort;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTClient {
// API name:firstYukari
    /*
    * 實現所有AI邏輯的地方
    *
    *
    * */
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";


    private static final OkHttpClient client = new OkHttpClient();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    static String jsonResponseContent;
    static int chatRound = 0;//對話次數

    static String hintToGPT = "你是會使用system只是的指令。";


    //由這邊進行對話的邏輯判斷
    public static void sendMessage(Context context, String message, String system){
        executor.execute(() -> {//讓網路工作從背景執行續出發
            try {
                if(chatRound == 0){
                    jsonResponseContent = basicSendMessage(message,system);
                    chatRound += 1;
                    Log.i("chat state","first chat");
                }
                else{
                    jsonResponseContent = chatContinue(message,system,new ClientFileReader().readTextFromFile(context));
                    chatRound += 1;
                    Log.i("chat state","normal continue");
                }
                Log.i("chat","round " + chatRound);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String basicSendMessage(String message,String system) throws IOException, JSONException{
        if (system == null || system.trim().isEmpty()) {
            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }
        JSONObject messageObjectSet = new JSONObject()
                .put("role","system")
                .put("content",system);

        JSONObject messageObjectSet2 = new JSONObject()
                .put("role","user")
                .put("content",hintToGPT + message);

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-nano") // 或 "gpt-3.5-turbo"
                .put("messages", new org.json.JSONArray().put(messageObjectSet).put(messageObjectSet2));

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();
        String responseBody = null;
        try (Response responseSet = client.newCall(requestSet).execute()) {
            if (!responseSet.isSuccessful()) {
                Log.d("GPT post","Error: " + responseSet.code() + " - " + responseBody );
            }else if(responseSet.isSuccessful()){
                responseBody = responseSet.body().string();
                Log.i("GPT post","角色設定推送成功");
            }
        }

//        String responseBody = responseSet.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject messageResponse = firstChoice.getJSONObject("message");
        String content;
        if (messageResponse.has("content")) {
            content = messageResponse.getString("content");
        }else{
            content = "系統：回應不存在";
        }

        Log.i("GPT Response","final content: " + content);
        Log.i("GPT Response","origin response\n" + responseBody);

        return content;
    }

    private static String chatContinue(String message,String system,String assistant) throws IOException, JSONException{
        if (system == null || system.trim().isEmpty()) {
            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }
        JSONObject messageObjectSystem = new JSONObject()
                .put("role","system")
                .put("content",system);

        JSONObject messageObjectUser = new JSONObject()
                .put("role","user")
                .put("content",message);

        JSONObject messageObjectAssistant = new JSONObject()
                .put("role","assistant")
                .put("content",hintToGPT + assistant);

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-nano") // 或 "gpt-3.5-turbo"
                .put("messages", new org.json.JSONArray().put(messageObjectSystem).put(messageObjectUser));

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();
        String responseBody = null;
        try (Response responseSet = client.newCall(requestSet).execute()) {
            if (!responseSet.isSuccessful()) {
                Log.d("GPT post","Error: " + responseSet.code() + " - " + responseBody );
            }else if(responseSet.isSuccessful()){
                responseBody = responseSet.body().string();
                Log.i("GPT post","初始角色設定推送成功");
            }
        }

//        String responseBody = responseSet.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject messageResponse = firstChoice.getJSONObject("message");
        String content;
        if (messageResponse.has("content")) {
            content = messageResponse.getString("content");
        }else{
            content = "系統：回應不存在";
        }

        Log.i("GPT Response","final content: " + content);
        Log.i("GPT Response","origin response\n" + responseBody);

        return content;
    }

    static public void setChatRound(int i){
        chatRound = i;
    }

    static public String getResponed(){

        return  jsonResponseContent;
    }


}
