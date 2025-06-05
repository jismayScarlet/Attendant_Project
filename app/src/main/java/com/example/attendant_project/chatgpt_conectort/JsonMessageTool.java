package com.example.attendant_project.chatgpt_conectort;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public JSONObject catchMoodTool(String toolName) throws JSONException{//每種功能就用一種函數去定義
        JSONObject toolObject = new JSONObject()
                .put("type", "function")
                .put("function", new JSONObject()
                        .put("name",toolName)
                        .put("description","當使用者有明確的情緒時抓住使用者表達了的情緒，限定從＂快樂、悲傷、恐懼、憤怒、厭惡、、平靜＂之中做出分類")
                        .put("parameters", new JSONObject()
                                .put("type", "object")
                                .put("properties", new JSONObject()
                                        .put("mood", new JSONObject()
                                                .put("type", "string")
                                                .put("description", "使用者的心情")
                                        )
                                        .put("contextOfMood",new JSONObject()
                                                .put("type","string")
                                                .put("description","情緒發生的情境")
                                        )
                                )
                                .put("required", new JSONArray()
                                        .put("mood")
                                        .put("contextOfMood"))
                        )
                );
        return toolObject;
    }


}
