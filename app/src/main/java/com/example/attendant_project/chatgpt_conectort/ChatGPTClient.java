package com.example.attendant_project.chatgpt_conectort;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChatGPTClient {
// API name:firstYukari
    /*
    * 實現所有AI邏輯的地方
    *
    *
    * */
//對外測試金鑰
// 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

    //原始金鑰
    //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
    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    static boolean ENDOW = false;
    static String assistantName = null,assistantNickName = null;
    static final String assistannameFile[] = {"AssistantName","AssistantNickName"};
    static final String assisPrefs_name = "assistantName";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    static JsonMessageTool JMT = new JsonMessageTool();
    static String discourseSentence,translatedSentence;
//    private static Map<Integer,String> roleContent;
//    private static Objects[] roleContentList = new Objects[2000];
    private static Context contextFrom;
    private static String system;
    private static String lastContent;
    private static float frequencyPenalty_attr = 0.1F;//重複率 -2.0 ~ 2.0 (預設0.8)
    private final static float highLevel_frequencyPenalty_attr = 1.8F;
    private final static float defualt_frequencyPenalty_attr = 0.8F;
    private static float temperature_attr = 0.7F;//隨機性 0.0 ~ 2.0 (預設0.7)

    private final static float highLevel_temperature_attr = 1.4F;
    private final static float defualt_temperature_attr = 0.7F;

    public ChatGPTClient(){


    }

    static public void EndowReset(){
        ENDOW = false;
    }


    //由這邊進行對話的邏輯判斷
    public static void sendMessage(Context context, String message){
        contextFrom = context;
        ClientFileIO cfi = new ClientFileIO();
        assistantName = cfi.getAssistantName(contextFrom,assisPrefs_name,assistannameFile[0],"克莉絲蒂娜");
        assistantNickName = cfi.getAssistantName(contextFrom,assisPrefs_name,assistannameFile[1],"助手");
        if(commandPromptStatement(message)){
            switch (message){
                case "清除AIThoughts":
                    new MemoryOrganizer(context).memoryClean();
                    discourseSentence = "AIThoughts 清除程序執行完畢";
                    Log.i("AIThoughts","清除AIThoughts finish");
                    break;

                case "調出AIThoughts":
                    String content = new MemoryOrganizer(contextFrom).showMemory();
                    if(!TextUtils.isEmpty(content)){
//                        Toast.makeText(context.getApplicationContext(),"以下為AIThought的記憶資料:\n" + content,Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts","以下為AIThought的記憶資料:\n" + content );
                        discourseSentence = "以下為AIThought的記憶資料:\n" + content;
                    }
                    else{
                        Toast.makeText(context.getApplicationContext(),"以下為AIThought的記憶資料:\n" + content,Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts","沒有記憶資料");
//                        discourseSentence = "沒有記憶資料";
                    }
                    break;
                case "AIThoughts oragnize now":
                    boolean startSuccese = new MemoryOrganizer(contextFrom).starOrganizerWithoutRT();
                    content = new MemoryOrganizer(contextFrom).showMemory();
                    if(startSuccese && !TextUtils.isEmpty(content)){
                        Toast.makeText(context.getApplicationContext(),"記憶整理完成，新記憶資料:\n" + content,Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts","記憶整理完成，新記憶資料:\n" + content);
//                        discourseSentence = "記憶整理完成，新記憶資料:\n" + content;
                    }else if(!startSuccese){
                        Toast.makeText(context.getApplicationContext(),"記憶整理完成，新記憶資料:\n" + content,Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts","系統異常:思考功能未被載入");
//                        discourseSentence = "系統異常:思考功能未被載入";
                    }else if(TextUtils.isEmpty(content)){
                        Toast.makeText(context.getApplicationContext(),"系統異常:無已儲存系統記憶",Toast.LENGTH_LONG).show();
                        Log.i("AIThoughts","系統異常:無已儲存系統記憶");
//                        discourseSentence = "系統異常:無已儲存系統記憶";
                    }else{
                        discourseSentence = "其他未檢知異常";
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
                                    Toast.makeText(contextFrom,"插入記憶完成",Toast.LENGTH_LONG).show();
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
                    boolean a = new ClientFileIO().cleanAssistantName(contextFrom,assisPrefs_name,assistannameFile[0]);
                    boolean b = new ClientFileIO().cleanAssistantName(contextFrom,assisPrefs_name,assistannameFile[1]);
                    if(a && b){
                        Toast.makeText(context,"清除助手全名 finish",Toast.LENGTH_SHORT);
                        Log.i("data clean","清除助手全部名稱 finish");
                    }
                    break;
                case "清單":
                    discourseSentence =
                            "清除AIThoughts\n" +
                            "調出AIThoughts\n" +
                            "AIThoughts oragnize now\n" +
                            "AIThoughts 訊息插入\n" +
                            "清除助手全部名稱\n" +
                            "退出命令字串模式";
                    break;
                case "退出命令字串模式":
                    ENDOW = false;
                    discourseSentence = "已退出命令字串模式";
                    break;
            }
        } else if(!commandPromptStatement(message)){
             if (TextUtils.isEmpty(assistantName) || TextUtils.isEmpty(assistantNickName) || assistantName.equals("克莉絲蒂娜") || assistantNickName.equals("助手")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(contextFrom);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(contextFrom);

                final EditText input = new EditText(contextFrom);
                final EditText input2 = new EditText(contextFrom);
                input.setInputType(InputType.TYPE_CLASS_TEXT); // 可根據需求更換輸入型態

                builder .setTitle("請問你是我的マスター嗎？")
                        .setMessage("我沒有名子，請告訴我的完整名子")
                        .setView(input)
                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                assistantName = input.getText().toString();
                                cfi.putAssistantName(contextFrom,assisPrefs_name,assistannameFile[0],assistantName);
                                Toast.makeText(contextFrom,"我知道了，我的名子是 " + assistantName,Toast.LENGTH_SHORT);
                                builder2 .setTitle("請問你是我的マスター嗎？")
                                        .setMessage("我的  小名 ")
                                        .setView(input2)
                                        .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                assistantNickName = input2.getText().toString();
                                                cfi.putAssistantName(contextFrom,assisPrefs_name,assistannameFile[1],assistantNickName);
                                                Toast.makeText(contextFrom,"我以後會以 " + assistantNickName + " 自稱。",Toast.LENGTH_SHORT);
                                                discourseSentence = "『你要下達重要命令之前請直接呼喊我的全名』";
                                            }
                                        })
                                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        })
                                        .show();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();

            }else if(!TextUtils.isEmpty(assistantName) || !TextUtils.isEmpty(assistantNickName) || !assistantName.equals("克莉絲蒂娜") || !assistantNickName.equals("助手")){

                 executor.execute(() -> {//讓網路工作及存取工作從背景執行續出發
                     try {
                         String roleSet = new ClientFileIO().getRoleSet(contextFrom);
                         system = "記住本系統的唯一名稱為\"" + assistantNickName + "\"  ，但這是個小名，全名是你跟使用者的秘密。" + roleSet;
                         ClientFileIO clientFileIO = new ClientFileIO();
                         String chatLog = clientFileIO.readTextFromFile(contextFrom);
                         Map chatLogMap = clientFileIO.readTextFromFileForPost(contextFrom,assistantNickName);
                        String memoryAssistant = new MemoryOrganizer(contextFrom).memoryOrganizerExclusiveFileIO(true,null);

                         if (!TextUtils.isEmpty(memoryAssistant) && chatLog.isBlank()) {
                             discourseSentence = newBasicSendMessage(message,system,memoryAssistant);
                             Log.i("chat state", "first chat with memory");
                         }else if (!chatLog.isBlank()) {
                             discourseSentence = chatContinue(message, system, chatLogMap,memoryAssistant);
                             //                    jsonResponseContent = normalChatContinue(message,system,chatLogMap);
                             Log.i("chat state", "normal continue");
                         }else {
                             discourseSentence = basicSendMessage(message, system);
                             Log.i("chat state", "first chat");
                         }
                     } catch (SocketTimeoutException e) {
                         discourseSentence = "(腦袋轉不動)";
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     } catch (JSONException e) {
                         throw new RuntimeException(e);
                     }
                 });
             }
        }
    }

    private static boolean commandPromptStatement(String content){
        //攔截關鍵字詞語來啟動系統提示
        String checkName = new ClientFileIO().getAssistantName(contextFrom,assisPrefs_name,assistannameFile[0],null);
        if(checkName != null) {
            if (content.equals(checkName)) {
                discourseSentence = checkName + " 在此聽從你的命令";
                ENDOW = true;
            }
        }
        return ENDOW;
    }

    public static String sendMessageSample(String system,String message,String model) throws IOException, JSONException {
        if (system == null) {
            Log.w("GPT post", "system prompt 為 null，套用預設值");
            throw new IOException("senMessageSample system null\n");
        }
        JSONObject messageObjectSet = new JSONObject()
                .put("role","system")
                .put("content",system);

        JSONObject messageObjectSet2 = new JSONObject()
                .put("role","user")
                .put("content", message);

        JSONObject jsonSet = new JSONObject()
                .put("model", model) // 預設 gpt-4.1-nano
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

    public static String sendMessageSample(String system,String message,String assistant,String model) throws IOException, JSONException {
        if (system == null || system.trim().isEmpty()) {
            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }
        JSONObject messageSystem = new JSONObject()
                .put("role","system")
                .put("content",system);

        JSONObject messageUser = new JSONObject()
                .put("role","user")
                .put("content", message);

        JSONObject messageAssistant = new JSONObject()
                .put("role","assistant")
                .put("content", assistant);

        JSONObject jsonSet = new JSONObject()
                .put("model", model) // 預設 gpt-4.1-nano
                .put("messages", new org.json.JSONArray()
                        .put(messageSystem)
                        .put(messageUser)
                        .put(messageAssistant));

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

    private static String basicSendMessage(String message,String system) throws IOException, JSONException {
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

    private static String newBasicSendMessage(String message,String system,String memory) throws IOException, JSONException {
        if (system == null || system.trim().isEmpty()) {
            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-nano") // 預設 gpt-4.1-nano
                .put("messages", new org.json.JSONArray()
                        .put(JMT.systemObject(system))
                        .put(JMT.userObject(message))
                        .put(JMT.assistantObject(memory)));

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


    private static String chatContinue(String message,String system,Map chatLog,String organizedMemory) throws IOException, JSONException,SocketTimeoutException {
        String[] usermessage = (String[]) chatLog.get("userMessage");
        String[] assistant = (String[]) chatLog.get("assistant");

        if (system == null || system.trim().isEmpty()) {
            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
            Log.w("GPT post", "system prompt 為 null，套用預設值");
        }
        String systemNew = system + "。在這之後的是你的對話記憶摘要：" + organizedMemory;


        JSONArray messageArray = new JSONArray()
                .put(JMT.systemObject(systemNew));

        int i = 0;
        while (!TextUtils.isEmpty(usermessage[i]) || !TextUtils.isEmpty(assistant[i]) ){
            while(!TextUtils.isEmpty(usermessage[i])){
                messageArray
                        .put(JMT.userObject(usermessage[i]));
                i++;
            }
            while (!TextUtils.isEmpty(assistant[i])){
                messageArray
                        .put(JMT.assistantObject(assistant[i]));
                i++;
            }
        }
        messageArray.put(JMT.userObject(message));


        String[] netSearch_p_TCN = {"searchContent"};
        String[] netSearch_p_TCD = {"想要搜尋的內容"};
        String[] systemRoleInsert_TCN = {"systemRoleInsert"};
        String[] systemRoleInsert_TCD = {"你的角色被要求的改變內容"};
        String[] userAbout_TCN = {"userAboutTitle","deeptell"};
        String[] userAbout_TCD = {"事情的重點","事情的細節內容"};

        JSONArray toolsArray = new JSONArray()
                .put(
                        JMT.GPTTools_Post("netSearch"
                                ,"使用者希望搜尋或使用者覺得需要更寬廣的訊息的時候"
                                ,netSearch_p_TCN
                                ,netSearch_p_TCD)
                )
                .put(
                        JMT.GPTTools_Post("systemRoleInsert"
                        ,"使用者對你的角色有描述，或希望你有什麼特質的時候呼叫"
                        ,systemRoleInsert_TCN
                        ,systemRoleInsert_TCD)
                )
                .put(
                        JMT.GPTTools_Post("userAbout"
                ,"關於使用者密切關聯的的任何一切訊息,包含但不限於：喜好、厭惡、害怕、生氣的事物，生日、血型、身高、體重、星座。"
                        ,userAbout_TCN
                        ,userAbout_TCD)
                )
                ;

        //tool_choice測試用
//        JSONObject toolFunction = new JSONObject()
//                .put("name", "netSearch");
//        JSONObject functionType = new JSONObject()
//                .put("type", "function")
//                .put("function", toolFunction);
//        JSONObject toolChoice = functionType;
        //測試用

        JSONObject jsonSet = new JSONObject()
                .put("model", "gpt-4.1-nano") // 或 "gpt-3.5-turbo"
                .put("messages", messageArray)

                .put("frequency_penalty", frequencyPenalty_attr)
                .put("temperature", temperature_attr)
                .put("tools",toolsArray)
                .put("tool_choice", "auto");//auto, none, required

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();

        JSONObject messageResponse = JMT.jsonDefaultPostAndResponse(requestSet,client);

        String content = null;

        if (messageResponse.has("tool_calls")) {

            JSONArray toolCalls = messageResponse.getJSONArray("tool_calls");

            Map<String,String> searchCall = JMT.GPTTools_Response(toolCalls,"netSearch",netSearch_p_TCN);
            if (searchCall != null) {
                String funcionName = searchCall.get("functionName");
                if(funcionName.equals("netSearch")){
                    String searchContent = searchCall.get("searchContent");
                    SearchTool searchTool = new SearchTool(API_URL,API_KEY,systemNew);
                    Request searchRequest = searchTool.searchJSONSetCompletionsMode(searchContent);
                    JSONObject searchResponse  = JMT.jsonDefaultPostAndResponse(searchRequest,client);
                    if (searchResponse.has("content") && !searchResponse.isNull("content")) {
                        content = searchResponse.getString("content");

                    }
                    Log.i("search response",searchResponse.toString());
                }
            }



        }

        Log.i("GPT Response","continueChat content:\n " + content);
        Log.i("GPT post","In Object \n" + jsonSet);

        lastContent = content;
        return content;
    }

    static public String getResponed(){
        return discourseSentence;
    }

    static public void putDiscourseSentence(String message){discourseSentence = message;}

    static public String getTranslatedSentence(){
        return translatedSentence;
    }

    static  public void translateToJapen(String text, TextToSpeech tts) throws JSONException {
        executor.execute(()->{
            try {
                JSONObject messageObjectSet = new JSONObject()
                        .put("role", "system")
                        .put("content", "あなたは中国語から日本語への翻訳者になりました");

                JSONObject messageObjectSet2 = new JSONObject()
                        .put("role", "user")
                        .put("content", text);

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

                JSONObject messageResponse = JMT.jsonDefaultPostAndResponse(requestSet, client);
                if (messageResponse.has("content")) {
                    translatedSentence = messageResponse.getString("content");
                }
            }catch (JSONException e){
                throw new RuntimeException("GPT中翻日 JSON post error:\n" + e);
            }
            tts.speak(translatedSentence,TextToSpeech.QUEUE_FLUSH,null,"tts1");
        });
    }


    static public void setTemperature(int temperature){
        temperature_attr = temperature;
    }

}
