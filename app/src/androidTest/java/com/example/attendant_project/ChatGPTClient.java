package com.example.attendant_project;

public class ChatGPTClient {

//        private  static String normalChatContinue(String message,String system,Map chatLog) throws IOException, JSONException{
//        String[] usermessage = (String[]) chatLog.get("userMessage");
//        String[] assistant = (String[]) chatLog.get("assistant");
////        Log.d("String lentgh","user:" + usermessage.length + " assi:" + assistant.length);
//
//        if (system == null || system.trim().isEmpty()) {
//            system = "你沒有正確載入角色設定，你現在是具什麼都不回應的空殼"; // 預設 System Prompt
//            Log.w("GPT post", "system prompt 為 null，套用預設值");
//        }
//
//        JsonMessageTool JMT = new JsonMessageTool();
//        JSONArray messageArray = new JSONArray()
//                .put(JMT.systemObject(system));
//        int i = 0;
//        while(!usermessage[i].isBlank()){
//            messageArray
//                    .put(JMT.userObject(usermessage[i]));
//            while (!assistant[i].isBlank()){
//                messageArray
//                        .put(JMT.assistantObject(assistant[i]));
//                break;
//            }
//            i++;
//            if(usermessage[i] == null && assistant[i] == null){
//                messageArray.put(JMT.userObject(message));
//                break;
//            }
//        }
//
////            messageArray.put(JMT.userObject(message));
//
//        JSONObject jsonSet = new JSONObject()
//                .put("model", "gpt-4.1-nano") // 或 "gpt-3.5-turbo"
//                .put("messages", messageArray);
//
//        RequestBody bodySet = RequestBody.create(
//                jsonSet.toString(),
//                MediaType.get("application/json")
//        );
//
//
//        //解析回傳
//        Request requestSet = new Request.Builder()
//                .url(API_URL)
//                .header("Authorization", "Bearer " + API_KEY)
//                .post(bodySet)
//                .build();
//        String responseBody = null;
//        try (Response responseSet = client.newCall(requestSet).execute()) {
//            if (!responseSet.isSuccessful()) {
//                Log.d("GPT post","Error: " + responseSet.code() + " - " + responseBody );
//            }else if(responseSet.isSuccessful()){
//                responseBody = responseSet.body().string();
//                Log.i("GPT post","初始角色設定推送成功");
//            }
//        }
//
//        JSONObject jsonResponse = new JSONObject(responseBody);
//        JSONArray choices = jsonResponse.getJSONArray("choices");
//        JSONObject firstChoice = choices.getJSONObject(0);
//        JSONObject messageResponse = firstChoice.getJSONObject("message");
//        String content;
//        if (messageResponse.has("content")) {
//            content = messageResponse.getString("content");
//        }else{
//            content = "系統：回應不存在";
//        }
//
//        Log.i("GPT Response","continueChat content: " + content);
//        Log.i("GPT post","In Object \n" + jsonSet);
//        Log.i("GPT Response","origin response\n" + responseBody);
//
//        return content;
//    }


}
