package com.example.attendant_project.chatgpt_conectort;


import android.app.Activity;
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

public class ChatGPTClient extends Activity {
// API name:firstYukari
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private static final OkHttpClient client = new OkHttpClient();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    static String jsonResponseContent;




    public static void callChatGPTInBackground(String message) {//讓網路工作從背景執行續出發
        executor.execute(() -> {
            try {
                sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void callChatGPTSetSystemRole(String message){
        executor.execute(() -> {
            try {
                firstCharactorSet(message);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void firstCharactorSet(String system) throws IOException, JSONException{
        if (system == null || system.trim().isEmpty()) {
            system = "你是一位友善的 AI 助手。"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }
        JSONObject messageObjectSet = new JSONObject()
                .put("role","system")
                .put("content",system);

        JSONObject messageObjectSet2 = new JSONObject()
                .put("role","user")
                .put("content","你了解我給你的設定就回應我: ＂知道了,Master＂");

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
                return;
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


    }

    private static void sendMessage(String message) throws IOException, JSONException {
        JSONObject messageObject = new JSONObject()
                .put("role", "user")
                .put("content", message);

        JSONObject json = new JSONObject()
                .put("model", "gpt-4.1-nano") // 或 "gpt-3.5-turbo"
                .put("messages", new org.json.JSONArray().put(messageObject));

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.d("GPT post","Error: " + response.code() + " - " + response.body().string());
                return;
            }else if(response.isSuccessful()){
            Log.i("GPT post","對話送出成功");
        }

            String responseBody = response.body().string();
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
            jsonResponseContent = content;

            Log.i("GPT Response","final content: " + content);
            Log.i("GPT Response","origin response\n" + responseBody);
        }
    }

    static public String getResponed(){

        return  jsonResponseContent;
    }


}
