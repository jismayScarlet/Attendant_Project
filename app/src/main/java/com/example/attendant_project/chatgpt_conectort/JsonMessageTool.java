package com.example.attendant_project.chatgpt_conectort;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    private JSONObject propertie_toolComponent(String[] name,String[] description,int qua) throws JSONException {
        JSONObject toolComponent = new JSONObject();
        JSONObject toolComponentFig = new JSONObject();
        for(int q=0;q<qua;q++){
                toolComponentFig
                        .put("type", "string")
                        .put("description", description[q]);
            toolComponent.put(name[q],toolComponentFig) ;
        }
        return toolComponent;
    }

    /*
    GPT API tools工具
    GPTTools(名稱,功能,GPT回傳項目名稱,項目功能)
    ※名稱與項目數量要相同，否則返回IllegalStateException
    * */
    public JSONObject GPTTools(String toolName,String description,String[] p_TCN,String[] p_TCD) throws JSONException{//每種功能就用一種函數去定義
        //propertie_toolComponentName
        //propertie_toolComponentDescription
        if(p_TCD.length == p_TCN.length) {
            int quantity = p_TCN.length;//propertie_toolComponenQuantity
            JSONObject toolObject = new JSONObject()
                    .put("type", "function")
                    .put("function", new JSONObject()
                            .put("name", toolName)
                            .put("description", description)
                            .put("parameters", new JSONObject()
                                    .put("type", "object")
                                    .put("properties",
                                            propertie_toolComponent(p_TCN, p_TCD, quantity)
                                    )
                                    .put("required", new JSONArray()
                                            .put("mood")
                                            .put("contextOfMood"))
                            )
                    );
            return toolObject;
        }else {
            throw new IllegalStateException("GPT toolComponent quentity error");
        }
    }

    JSONObject jsonDefaultPostAndResponse(Request request, OkHttpClient client) throws JSONException {
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
        Log.i("GPT Response","origin response\n" + responseBody);
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray choices = jsonResponse.getJSONArray("choices");
        JSONObject firstChoice = choices.getJSONObject(0);
        return firstChoice.getJSONObject("message");
    }

}
