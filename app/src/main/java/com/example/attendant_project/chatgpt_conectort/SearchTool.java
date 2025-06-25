package com.example.attendant_project.chatgpt_conectort;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SearchTool {
    private final String API_URL_responses = "https://api.openai.com/v1/responses";
    private String API_KEY = null;
    private String API_URL_completions = "https://api.openai.com/v1/chat/completions";
    private String system;
    private String AIMode = "gpt-4o-mini-search-preview";//completions 預設gpt-4o-mini-search-preview
    private String search_context_size = "medium";//high、medium、low 三種效率模式

    public SearchTool(String API_KEY) {
        this.API_KEY = API_KEY;
    }

    JsonMessageTool JMT = new JsonMessageTool();

    public SearchTool(String API_URL_responses, String API_KEY, String system) {
        this.API_KEY = API_KEY;
        API_URL_completions = API_URL_responses;
        this.system = system;
    }

    public Request searchJSONSetCompletionsMode(String userMessage) throws JSONException {
        JSONObject jsonSet = new JSONObject();

        JSONArray message = new JSONArray()
                .put(JMT.systemObject(system))
                .put(JMT.userObject(userMessage));

        JSONObject approximate = new JSONObject()
                    .put("country", "TW")
                    .put("timezone", "Asia/Taipei");
        JSONObject user_location = new JSONObject()
                    .put("type", "approximate")
                    .put("approximate",approximate);

        // Tools = web_search_options
        JSONObject web_search_options = new JSONObject()
                .put("user_location",user_location)
                .put("search_context_size",search_context_size);

        jsonSet.put("model", AIMode)
            .put("messages", message)
            .put("web_search_options", web_search_options);

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        Request searchRequest = new Request.Builder()
                .url(API_URL_completions)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();
        Log.d("search post",jsonSet.toString(2));
        return searchRequest;
    }

    public Request searchJSONSetResponseMode(String userMessage, String model) throws JSONException {
        JSONArray tools = new JSONArray();
        JSONObject type = new JSONObject();
        type.put("type", "web_search_preview");
        tools.put(type);


        JSONObject jsonSet = new JSONObject()
            .put("model", model)
            .put("tools", tools)
            .put("input", "使用繁體中文回應: " + userMessage)
//            .put("user_location",new JSONObject()
//                    .put("type","approximate")
//                    .put("city","Taipei")
//                    .put("country","TW")
//                    .put("region","Taiwan")
//                    .put("timezone","Asia/Taipei"))
// 無法生效
            .put("tool_choice","required");//auto, none, required

        RequestBody bodySet = RequestBody.create(
                jsonSet.toString(),
                MediaType.get("application/json")
        );

        Request searchRequest = new Request.Builder()
                .url(API_URL_responses)
                .header("Authorization", "Bearer " + API_KEY)
                .post(bodySet)
                .build();

        return searchRequest;
    }

}
