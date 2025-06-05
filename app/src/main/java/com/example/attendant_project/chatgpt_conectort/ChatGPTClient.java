package com.example.attendant_project.chatgpt_conectort;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
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
//    private static Map<Integer,String> roleContent;
//    private static Objects[] roleContentList = new Objects[2000];
    private static Context contextFrom;
    private static String system;
    private static String lastContent;
    private static float frequencyPenalty_attr = 0.8F;//重複率 -2.0 ~ 2.0
    private static float temperature_attr = 0.7F;//隨機性 0.0 ~ 2.0

    public ChatGPTClient(){//作為還原default用
        temperature_attr = 0.7F;
        frequencyPenalty_attr = 0.0F;

    }


    //由這邊進行對話的邏輯判斷
    public static void sendMessage(Context context, String message){
        executor.execute(() -> {//讓網路工作從背景執行續出發
            try {
                system = new ClientFileIO().getRoleSet(context);
                ClientFileIO clientFileIO = new ClientFileIO();
                String chatLog = clientFileIO.readTextFromFile(context);
                Map chatLogMap = clientFileIO.readTextFromFileForPost(context);

                contextFrom = context;
                if(!chatLog.isBlank()){
                    jsonResponseContent = chatContinue(message,system,chatLogMap);
//                    jsonResponseContent = normalChatContinue(message,system,chatLogMap);
                    Log.i("chat state","normal continue");
                }else{
                    jsonResponseContent = basicSendMessage(message,system);
                    Log.i("chat state","first chat");
                }
            } catch (SocketTimeoutException e){
                jsonResponseContent = "(腦袋轉不動)";
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String basicSendMessage(String message,String chatLog) throws IOException, JSONException {
        if (system == null || system.trim().isEmpty()) {
            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }
        JSONObject messageObjectSet = new JSONObject()
                .put("role","system")
                .put("content",system);

        JSONObject messageObjectSet2 = new JSONObject()
                .put("role","user")
                .put("content", message);

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-nano") // 預設 gpt-4.1-nano
                .put("messages", new org.json.JSONArray()
                        .put(messageObjectSet)
                        .put(messageObjectSet2));

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
        Log.i("GPT post","In Object \n" + jsonSet);
        Log.i("GPT Response","origin response\n" + responseBody);

        return content;
    }


    private static String chatContinue(String message,String system,Map chatLog) throws IOException, JSONException {
        String[] usermessage = (String[]) chatLog.get("userMessage");
        String[] assistant = (String[]) chatLog.get("assistant");

        if (system == null || system.trim().isEmpty()) {
            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }

        JsonMessageTool JMT = new JsonMessageTool();
        JSONArray messageArray = new JSONArray()
                .put(JMT.systemObject(system));
        int i = 0;
//        try {
//            boolean u = usermessage[i].isBlank();
//            boolean a = assistant[i].isBlank();
//        }catch (NullPointerException e){
//
//        }
        while(usermessage[i] != null){
            messageArray
                    .put(JMT.userObject(usermessage[i]));
            while (assistant[i] != null){
                messageArray
                        .put(JMT.assistantObject(assistant[i]));
                break;
            }
            i++;
            if(usermessage[i] == null && assistant[i] == null){
                messageArray.put(JMT.userObject(message));
                break;
            }
        }

        JSONArray toolsArray = new JSONArray().put(JMT.catchMoodTool("catchMasterMood"));

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-nano") // 或 "gpt-3.5-turbo"
                .put("messages", messageArray)

                .put("frequency_penalty", frequencyPenalty_attr)
                .put("temperature", temperature_attr)
                .put("tools",toolsArray)
                .put("tool_choice", "auto");

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();

        //解析回傳
        String responseBody = null;
        try (Response responseSet = client.newCall(requestSet).execute()) {
            if (!responseSet.isSuccessful()) {
                Log.d("GPT post","Error: " + responseSet.code() + " - " + responseBody );
            }else if(responseSet.isSuccessful()){
                responseBody = responseSet.body().string();
                Log.i("GPT post","初始角色設定推送成功");
            }
        }

        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject messageResponse = firstChoice.getJSONObject("message");

        String content = null;String userMood;String moodContext;String resId;

        if (messageResponse.has("content") && !messageResponse.isNull("content")) {
            content = messageResponse.getString("content");
            if(content.equals(lastContent)){
                Log.i("chat state","temperature change");
                frequencyPenalty_attr = 1.0F;
                temperature_attr = 1.2F;
                return chatContinue(message + "，再思考一遍，或往前面的文章閱讀",system,chatLog);
            }else{
                Log.i("chat state","temperature turn to default");
                frequencyPenalty_attr = 0.0F;
                temperature_attr = 0.7F;
            }
        }else if (messageResponse.has("tool_calls")) {
            JSONArray toolCalls = messageResponse.getJSONArray("tool_calls");
            JSONObject toolCall = toolCalls.getJSONObject(0);
            resId = toolCall.getString("id");
            for (int j = 0; j < toolCalls.length(); j++) {
                JSONObject function = toolCalls.getJSONObject(j).getJSONObject("function");
                if (function.has("arguments")) {
                    JSONObject args = new JSONObject(function.getString("arguments"));
                    userMood = args.getString("mood");//抓到使用者的情緒
                    moodContext = args.getString("contextOfMood");//抓到情緒發生的情境
//                    String testFeedBack = "的時候，我應該參考這些活動來改變使用者的情緒 發出:TERESISISI的笑聲";
                    CatchGPTTool catchGPTTool = new CatchGPTTool(contextFrom);
                    catchGPTTool.saveMoodContext(userMood,moodContext);
                    String Countermeasures = catchGPTTool.loadMoodContent(userMood);
                    content = toolsFeedBack_ofMoodContext("使用者的情緒" + userMood + "發生的環境" + moodContext,resId,"使用者發生" + userMood + "情緒的時候，我應該參考後面這些活動來改變使用者的情緒:" + Countermeasures);
                    Log.i("user mood", "使用者的情緒 " + userMood + "，發生的情境 " + moodContext);
                } else if (!function.has("arguments")) {
                    content = "...";
                }
            }
        }else{
            content = "系統：回應不存在";
        }

        Log.i("GPT Response","continueChat content: " + content);
        Log.i("GPT post","In Object \n" + jsonSet);
        Log.i("GPT Response","origin response\n" + responseBody);

        lastContent = content;
        return content;
    }


    private static String toolsFeedBack_ofMoodContext(String arguments,String resId,String functionFeedBack) throws IOException, JSONException  {


        JSONObject messageObjectTool = new JSONObject()
                .put("role","tool")
                .put("tool_call_id",resId)
                .put("name","catchMasterMood")
                .put("content",functionFeedBack);//執行工具之後得到的結果回傳給GPT

        JSONObject funtion = new JSONObject()
                .put("name","catchMasterMood")
                .put("arguments",arguments);//捕捉到的啟動function的元素

        JSONObject ToolCalls = new JSONObject()
                .put("id",resId)
                .put("type","function")
                .put("function",funtion);

        JSONObject messageObjectAssistant = new JSONObject()
                .put("role","assistant")
                .put("tool_calls",new JSONArray()
                        .put(ToolCalls))
                .put("content",null);

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-nano") // 或 "gpt-3.5-turbo"
                .put("messages", new org.json.JSONArray()
                        .put(messageObjectAssistant)
                        .put(messageObjectTool));

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json"));

        //解析回傳
        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();
        String responseBody = null;
        try (Response responseSet = client.newCall(requestSet).execute()) {
            if (!responseSet.isSuccessful()) {
//                Log.d("GPT post","Error: " + responseSet.code() + " - " + responseBody );
                throw new JSONException("Error: " + responseSet.code() + " - " + responseBody) ;
            }else if(responseSet.isSuccessful()){
                responseBody = responseSet.body().string();
                Log.i("GPT post","初始角色設定推送成功");
            }
        }

        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject messageResponse = firstChoice.getJSONObject("message");
        String content;
        if (messageResponse.has("content")) {
            content = messageResponse.getString("content");
        } else if (messageResponse.isNull("content")) {
            content = "(思考中)";
        } else{
            content = "系統：回應不存在";
        }

        Log.i("GPT Response","mixed tool content: " + content);
        Log.i("GPT Response","origin response\n" + responseBody);
        Log.i("GPT post","In Object In tool\n" + jsonSet);
        Log.i("chat state","tool feedback");
        return content;
    }



    private static JSONObject jsonDefaultPostAndResponse(Request request) throws JSONException {
        String responseBody = null;
        try (Response responseSet = client.newCall(request).execute()) {
            if (!responseSet.isSuccessful()) {
                Log.d("GPT post","Error: " + responseSet.code() + " - " + responseBody );
            }else if(responseSet.isSuccessful()){
                responseBody = responseSet.body().string();
                Log.i("GPT post","初始角色設定推送成功");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        return firstChoice.getJSONObject("message");
    }

    static public String getResponed(){

        return  jsonResponseContent;
    }


}
