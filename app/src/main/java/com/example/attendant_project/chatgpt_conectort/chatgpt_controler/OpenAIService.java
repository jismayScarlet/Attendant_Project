package com.example.attendant_project.chatgpt_conectort.chatgpt_controler;

import android.os.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OpenAIService {
    private static final String API_KEY = "你的API Key";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();

    public static String sendMessage(String systemPrompt, List<Message> history) throws IOException {
        JSONArray messages = new JSONArray();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        }

        for (Message m : history) {
            messages.put(new JSONObject().put("role", m.getRole()).put("content", m.getContent()));
        }

        JSONObject requestBody = new JSONObject()
                .put("model", "gpt-4o")
                .put("messages", messages);

        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String json = response.body().string();
            JSONObject result = new JSONObject(json);
            return result.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        }
    }
}

