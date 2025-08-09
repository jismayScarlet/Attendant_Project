package com.example.attendant_project.chatgpt_conectort;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class JsonMessageTool {

    public JSONObject systemObject(String system) throws JSONException {
        if (!system.isBlank()) {
        JSONObject systemObject = new JSONObject()
                .put("role","system")
                .put("content",system);
        return systemObject;
        }else{
            return null;
        }

    };

    public JSONObject userObject(String message) throws JSONException{
        if(!message.isBlank()){
        JSONObject userObject = new JSONObject()
                .put("role","user")
                .put("content",message);
            return  userObject;
        }else{
            return null;
        }

    }

    public JSONObject assistantObject(String assistant) throws JSONException{
        if (!assistant.isBlank()) {
            JSONObject assistaneObject = new JSONObject()
                    .put("role","assistant")
                    .put("content",assistant);
            return assistaneObject;
        }else{
            return null;
        }
    }

    public JSONObject base64ImageObject(String BASE64Image, String require, boolean highQuality) throws JSONException{
        String BASE64 = "data:image/jpeg;base64," + BASE64Image;
        if(BASE64.length() > 27000000){
            throw new RuntimeException("圖片超過 OpenAI 上限，請縮小圖片再嘗試");
        }else {
            if (!BASE64Image.isBlank()) {
                JSONObject userObject = new JSONObject()
                        .put("role", "user")
                        .put("content", new JSONArray()
                                .put(new JSONObject()
                                        .put("type", "text")
                                        .put("text", require))
                                .put(new JSONObject()
                                        .put("type", "image_url")
                                        .put("image_url", new JSONObject()
                                                .put("url", BASE64)
                                                .put("detail", highQuality ? "high" : "low"))));
                return userObject;
            } else {
                return null;
            }
        }
    }

    public JSONObject httpsImageObject(String https, String require, boolean highQuality) throws JSONException{
            if (!https.isBlank()) {
                JSONObject userObject = new JSONObject()
                        .put("role", "user")
                        .put("content", new JSONArray()
                                .put(new JSONObject()
                                        .put("type", "text")
                                        .put("text", require))
                                .put(new JSONObject()
                                        .put("type", "image_url")
                                        .put("image_url", new JSONObject()
                                                .put("url", https)
                                                .put("detail", highQuality ? "high" : "low"))));
                return userObject;
            } else {
                return null;
            }

    }

    private JSONObject propertie_toolComponent(String[] name,String[] description,int qua) throws JSONException {
        JSONObject toolComponent = new JSONObject();
        JSONObject toolComponentFig = new JSONObject();
        for(int q=0;q<qua;q++){
                toolComponentFig
                        .put("type", "string")
                        .put("description", description[q]);
                toolComponent.put(name[q],toolComponentFig);
        }
        return toolComponent;
    }

    /*
    GPT API tools工具
    GPTTools(名稱,功能,GPT回傳項目名稱,項目功能)
    ※名稱與項目數量要相同，否則返回IllegalStateException
    * */
    public JSONObject GPTTools_Post(String functionName,String description,String[] p_TCN,String[] p_TCD) throws JSONException{//每種功能就用一種函數去定義
        //propertie_toolComponentName
        //propertie_toolComponentDescription
        if(p_TCD.length == p_TCN.length) {
            int quantity = p_TCN.length;//propertie_toolComponenQuantity
            JSONObject toolObject = new JSONObject()
                    .put("type", "function")
                    .put("function", new JSONObject()
                            .put("name", functionName)
                            .put("description", description)
                            .put("parameters", new JSONObject()
                                    .put("type", "object")
                                    .put("properties",
                                            propertie_toolComponent(p_TCN, p_TCD, quantity)
                                    )
                            )
                    );
            return toolObject;
        }else {
            throw new IllegalStateException("GPT toolComponent quentity error");
        }
    }

    public JSONObject GPTTools_Post(String functionName,String description,String[] p_TCN,String[] p_TCD,String[] requiredTCN) throws JSONException{//每種功能就用一種函數去定義
        //帶有必要項選擇的版本
        //propertie_toolComponentName
        //propertie_toolComponentDescription
        JSONArray requiredComponemt = new JSONArray();
        for(int i = 0;i < requiredTCN.length;i++) {
            requiredComponemt.put(requiredTCN[i]);
        }
        if(p_TCD.length == p_TCN.length) {
            int quantity = p_TCN.length;//propertie_toolComponenQuantity
            JSONObject toolObject = new JSONObject()
                    .put("type", "function")
                    .put("function", new JSONObject()
                            .put("name", functionName)
                            .put("description", description)
                            .put("parameters", new JSONObject()
                                    .put("type", "object")
                                    .put("properties",
                                            propertie_toolComponent(p_TCN, p_TCD, quantity)
                                    )
                                    .put("required",requiredComponemt)
                            )
                    );
            return toolObject;
        }else {
            throw new IllegalStateException("GPT toolComponent quentity error");
        }
    }

    /*
    * GPT API tools responseAnalysis工具
    *GPTTools_Response(投入回傳的tool_calls Array,tool名稱群組)
    * 返回執行結果的陣列
    *   String[function數量+2保留區間][總tool工具數量] compenentResponse
    *   注意
    *       [0][x] = tooleCall_id
    *       [1][x] = 對應的 function Name
    *       [2up][x] = compenentResponse
    * */
    public String[][] GPTTools_Response(JSONArray toolArray,String[] componentName) throws JSONException {
        int quentityInArray = toolArray.length();
        int componentQuentity = componentName.length;
        String[][] responseComponent = new String[quentityInArray+2][componentQuentity];//tool運作後回傳的結果
        for (int j = 0; j < quentityInArray; j++) {//獲取response的tools數量
            JSONObject toolCall = toolArray.getJSONObject(j);
            responseComponent[0][j] = toolCall.getString("id");//呼叫行為的唯一id
            JSONObject function = toolArray.getJSONObject(j).getJSONObject("function");
            responseComponent[1][j] = function.getString("name");//functionName
            if (function.has("arguments")) {
                JSONObject args = new JSONObject(function.getString("arguments"));
                int realToolQuentity = 0;//總名單與split名單長度並不一致
                for(int k=0;k < componentQuentity;k++) {
                    if (args.has(componentName[k])) {
                        responseComponent[j + 2][realToolQuentity] = componentName[k] + args.getString(componentName[k]);
                        realToolQuentity += 1;
                    }
                }
            } else if (!function.has("arguments")) {
                throw new IllegalStateException("GPT API Responsent function error");
            }
        }
        return responseComponent;
    }

    /*
    * 輸出單元: key word
    * resId = 回應唯一id
    * 對應tool name之回應
    * */
    public Map<String,String> GPTTools_Response(JSONArray toolArray,String functionName,String[] componentName) throws JSONException {
        int quentityInArray = toolArray.length();
        Map<String,String> toolResponse = new HashMap<>();
        for (int j = 0; j < quentityInArray; j++) {//獲取response的tools數量
            JSONObject toolCall = toolArray.getJSONObject(j);
            if(toolCall.getString("type").equals("function")) {
                JSONObject function = toolArray.getJSONObject(j).getJSONObject("function");
                toolResponse.put("resId", toolCall.getString("id"));//呼叫行為的唯一id
                if (function.getString("name").equals(functionName)) {//functionName
                    toolResponse.put("functionName", functionName);
                    if (function.has("arguments")) {
                        JSONObject args = new JSONObject(function.getString("arguments"));
                        for (int k = 0; k < componentName.length; k++) {
                                toolResponse.put(componentName[k], args.optString(componentName[k],"noData"));
                                Log.d("response debug",functionName + " " + componentName[k] + " " + args.optString(componentName[k],"noData"));
                        }
                        toolResponse.put("arguments",function.getString("arguments"));
                    }else if (!function.has("arguments")) {
                        throw new IllegalStateException("GPT API Responsent function error");
                    }
                } else if (!function.getString("name").equals(functionName)) {
                    return null;
                }
            }
        }
        return toolResponse;
    }

    //傳送與接收的橋接段
    public JSONObject jsonDefaultPostAndResponse(Request request, OkHttpClient client) throws JSONException {
        String responseBody = null;
        try (Response responseSet = client.newCall(request).execute()) {

            if (!responseSet.isSuccessful()) {
                Log.e("GPT post", "HTTP錯誤碼: " + responseSet.code());
                if (responseBody != null) {
                    Log.e("GPT post", "錯誤回傳內容: " + responseSet);
                }else if (responseBody == null || responseBody.trim().isEmpty()) {// ✅ response 成功但空內容
                    Log.d("GPT post","API 回傳為空 回傳資訊:\n" + responseSet);
                    throw new RuntimeException("API 回傳為空，請確認網路或參數\n" + responseSet);
                } else {
                    Log.e("GPT post", "錯誤但無內容回傳");
//                    responseBody = "{\"error\": \"空內容\"}";
                }
                throw new RuntimeException("API 回傳錯誤（" + responseSet.code() + "）");
            }else if(responseSet.isSuccessful()){
                ResponseBody body = responseSet.body();
                if (body != null) {
                    responseBody = body.string();
                }
                Log.i("GPT post","JSON數據交換成功");
            }
        }catch (SocketTimeoutException e){
            throw new RuntimeException("net串流異常\n" + e);
        }
        catch (IOException e) {
            throw new RuntimeException("檔案處理異常\n" + e);
        }
        JSONObject jsonResponse = new JSONObject(responseBody);
        if (!jsonResponse.has("choices")) {
            throw new RuntimeException("GPT 回傳 JSON 格式錯誤：缺少 'choices'");
        }
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices.length() == 0) {
            throw new RuntimeException("GPT 回傳 'choices' 為空");
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        if (!firstChoice.has("message")) {
            throw new RuntimeException("GPT 回傳缺少 'message'");
        }
        Log.i("GPT Response","origin response\n" + responseBody);
        return firstChoice.getJSONObject("message");
    }

    public String jsonPostAndResponseOraginResult(Request request, OkHttpClient client) throws JSONException {
        String responseBody = null;
        try (Response responseSet = client.newCall(request).execute()) {

            if (!responseSet.isSuccessful()) {
                Log.e("GPT post", "HTTP錯誤碼: " + responseSet.code());
                if (responseBody != null) {
                    Log.e("GPT post", "錯誤回傳內容: " + responseSet);
                } else if (responseBody == null || responseBody.trim().isEmpty()) {// ✅ response 成功但空內容
                    Log.d("GPT post", "API 回傳為空 回傳資訊:\n" + responseSet);
                    throw new RuntimeException("API 回傳為空，請確認網路或參數\n" + responseSet);
                } else {
                    Log.e("GPT post", "錯誤但無內容回傳");
//                    responseBody = "{\"error\": \"空內容\"}";
                }
                throw new RuntimeException("API 回傳錯誤（" + responseSet.code() + "）");
            } else if (responseSet.isSuccessful()) {
                ResponseBody body = responseSet.body();
                if (body != null) {
                    responseBody = body.string();
                }
                Log.i("GPT post", "JSON數據交換成功");
            }
        } catch (SocketTimeoutException e) {
            throw new RuntimeException("net串流異常\n" + e);
        } catch (IOException e) {
            throw new RuntimeException("檔案處理異常\n" + e);
        }
        return responseBody;
    }
    /*
    * String toolsFeedBack(
    *   toolName：運行的tool工具名稱,
    *   arguments：捕捉到的啟動function的元素,
    *   resId：tool運行後response的捕捉元素，可以解讀之後再傳回給GPT,
    *   functionFeedBack：本地端接收GPT啟動要求後，完結的返還訊息,
    *   GPTModel：運行的GPT版本，為了未來可能會使用到高版本功能預留的項目,
    *   API_URL & API_KEY：post的必要資訊
    *   OkHttpClient client：建構GPT的JSON restful message必要套件
    */
    public String toolsFeedBack
            (String functionName,String arguments,String resId,String functionFeedBack,String GPTModel,String API_URL,String API_KEY,OkHttpClient client,Context context) throws IOException, JSONException  {
        JSONObject messageObjectTool = new JSONObject()
                .put("role","tool")
                .put("tool_call_id",resId)
                .put("name",functionName)
                .put("content",functionFeedBack);//執行工具之後得到的結果回傳給GPT

        JSONObject funtion = new JSONObject()
                .put("name",functionName)
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

        JSONObject messageObjectSystem = systemObject(new ClientFileIO().getRoleSet(context));

        JSONObject jsonSet = new JSONObject()
                .put("model", GPTModel) // 或 "gpt-3.5-turbo"
                .put("messages", new org.json.JSONArray()
                        .put(messageObjectAssistant)
                        .put(messageObjectTool)
                        .put(messageObjectSystem));

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json"));

        //解析回傳
        Request requestSet = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();

        JSONObject messageResponse = new JsonMessageTool().jsonDefaultPostAndResponse(requestSet,client);
        String content;
        if (messageResponse.has("content")) {
            content = messageResponse.getString("content");
        } else if (messageResponse.isNull("content")) {
            content = "(思考中)";
        } else{
            content = "系統：回應不存在";
        }

        Log.i("GPT post","In Object In tool\n" + jsonSet);
        Log.i("GPT Response","mixed tool content: " + content);
        Log.i("chat state","tool feedback");
        return content;
    }


}
