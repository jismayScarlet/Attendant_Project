package com.example.attendant_project.chatgpt_conectort;

import android.content.Context;
import android.util.Log;

public class CatchGPTTool {
    //放各種GPT Tool抓取到的資訊的處理方法

    private Context context;
    private ClientFileIO clientFileIO;

    public CatchGPTTool(Context context){
        this.context = context;
        clientFileIO = new ClientFileIO();
    }

    public void saveMoodContext( String mood, String moodContent){
        String report="";
        switch (mood){
            case "快樂":
                fileOverride(context,"mood_happy",moodContent);
                report = "mood_happy";
                break;
            case "悲傷":
                fileOverride(context,"mood_sad",moodContent);
                report = "mood_sad";
                break;
            case "恐懼":
                fileOverride(context,"mood_fear",moodContent);
                report = "mood_fear";
                break;
            case "憤怒":
                fileOverride(context,"mood_angry",moodContent);
                report = "mood_angry";
                break;
            case "厭惡":
                fileOverride(context,"mood_disgust",moodContent);
                report = "mood_disgust";
                break;
            case "平靜":
                fileOverride(context,"mood_calm",moodContent);
                report = "mood_calm";
                break;
        }
        Log.i("AI system", report + "存檔成功");
    }

    private void fileOverride(Context context,String fileName,String content) {
        clientFileIO.saveTextToFile(context, fileName,
                clientFileIO.readTextFromFile(context, fileName), content);
    }

    public String loadMoodContent(String mood){
        switch (mood) {
            case "快樂":
                return clientFileIO.readTextFromFile(context, "mood_calm");
            case "悲傷":
                return clientFileIO.readTextFromFile(context, "mood_happy");
            case "恐懼":
                return clientFileIO.readTextFromFile(context, "mood_sad");
            case "憤怒":
                return clientFileIO.readTextFromFile(context, "mood_disgust");
            case "厭惡":
                return clientFileIO.readTextFromFile(context, "mood_happy");
            case "平靜":
                return clientFileIO.readTextFromFile(context, "mood_calm");
            default:
                return "沒有適當的情緒事件";
        }
    }

    private void dataClean(int mood){//想要清洗的時候用程式碼在內部呼叫
        switch (mood) {
            case 1:
                fileOverride(context,"mood_happy","");
                break;
            case 2:
                fileOverride(context,"mood_sad","");
                break;
            case 3:
                fileOverride(context,"mood_fear","");
                break;
            case 4:
                fileOverride(context,"mood_angry","");
                break;
            case 5:
                fileOverride(context,"mood_disgust","");
                break;
            case 6:
                fileOverride(context,"mood_calm","");
                break;
            case 0:
                CatchGPTTool catchGPTTool = new CatchGPTTool(context);
                for(int i=1;i<7;i++){
                    catchGPTTool.dataClean(i);
                }
                break;
        }
    }

}
