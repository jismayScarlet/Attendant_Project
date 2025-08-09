package com.example.attendant_project.MemoBuddy;


import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.example.attendant_project.chatgpt_conectort.JsonMessageTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AIOption {
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    static JsonMessageTool JMT = new JsonMessageTool();
    static String result = null;
    static Future<String> future = null;

    public class CallbackInterface {
        public interface OnMessageReady {
            void onResult(String callbackResult);
        }
    }

    public static void sendMessage(String message, CallbackInterface.OnMessageReady callback)  {
        executorService.execute(()->{
            try {
            result = sendMessageSimple(message);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onResult(result);
            });
        });
    }

    public static void sendImage(Context context, Uri imageUri, CallbackInterface.OnMessageReady callback){
        executorService.execute(()->{
                try{
                    result = sendImageData(context, imageUri);
                } catch (JSONException e) {
                throw new RuntimeException("AIOption JSONException\n" + e);
            } catch (IOException e) {
                throw new RuntimeException("AIOption IOException\n" + e);
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onResult(result);
            });
        });


    }

    private static String sendMessageSimple(String message) throws IOException, JSONException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String refrenceTime = LocalDateTime.now().format(formatter).toString();

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4o-mini-2024-07-18") // 預設 gpt-4.1-nano
                .put("messages", new org.json.JSONArray()
                        .put(JMT.systemObject("使用中文。" +
                                "檢查訊息的日期時間並轉換成YYYY-MM-DD HH:mm，如果內容沒有時間就回應YYYY-MM-DD，如果內容只有時間且沒有日期就回應現在的日期加 HH:mm。" +
                                "作為參考的\"現在日期時間是\":"+
                                refrenceTime
                        + "，\n不要返回除了時間日期以外的訊息。"))
                        .put(JMT.userObject(message)));

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        //解析回傳
        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();

        JSONObject messageResponse = JMT.jsonDefaultPostAndResponse(requestSet,client);
        String content;
        if (messageResponse.has("content")) {
            content = messageResponse.getString("content");
        }else{
            content = "系統：回應不存在";
        }

        Log.i("GPT Response","final content: " + content);
        Log.i("GPT post","In Object \n" + jsonSet);

        return content;
    }

    private static String sendImageData(Context context, Uri imageUri) throws JSONException, IOException {
        String testHttpsImage = "https://en.pimg.jp/096/332/442/1/96332442.jpg";
        String content;
        byte[] imageBytes = new byte[0];
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                imageBytes = inputStream.readAllBytes(); // Java 9+，否則手動讀取
            }else{
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
                imageBytes = baos.toByteArray();
            }
        }

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-mini") // 預設 o4-mini-2025-04-16
                .put("messages", new JSONArray()
                        .put(JMT.base64ImageObject(base64Image,"辨識場景並在20字以內盡可能描述細節",false))
//                        .put(JMT.httpsImageObject(testHttpsImage,"辨識場景內的物件",false))
                )
                .put("max_tokens",300);
        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json"));

                //解析回傳
        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();

        JSONObject imageParseResponse = JMT.jsonDefaultPostAndResponse(requestSet,client);

        if (imageParseResponse.has("content")) {
            content = imageParseResponse.getString("content");
        }else{
            throw new JSONException("content is unExist.\n");
        }

        Log.i("image parse response\n",imageParseResponse.toString());
        if (TextUtils.isEmpty(content)){
            return "人工智慧回傳訊息為空";
        }else {
            return content;
        }
    }
}
