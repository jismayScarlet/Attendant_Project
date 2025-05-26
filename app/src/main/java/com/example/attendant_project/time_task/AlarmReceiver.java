package com.example.attendant_project.time_task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/*
* 播放鬧鐘音效
* */

public class AlarmReceiver extends BroadcastReceiver {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean mediaState = false;
    private TextView raingtoneDelault,raingtone;

    private Uri raingtoneURI = null;
    private boolean UriUnull = false;

    public AlarmReceiver(){


    }

    @Override
    public void onReceive(Context context, Intent intent) {

        // 播放音樂作為鬧鐘
        try{
            if("unknow".equals( PrefsManager.getMusicSet(context.getApplicationContext(),"customization_music","unknow") ) ){
                raingtoneURI = Uri.parse("android.resource://" + context.getApplicationContext().getPackageName() + "/raw/default_ringtone");//預設音效
            }else {
                raingtoneURI = Uri.parse(PrefsManager.getMusicSet(context.getApplicationContext(),"customization_music","unknow"));
                //取得久持權限
//                AssetFileDescriptor afd = context.getContentResolver().openAssetFileDescriptor(raingtoneURI, "r");//用來協助其他功能知道從哪裡讀取取得的完整或不完整資源路徑
                //目前的檔案來說，完全不使用也沒關係
//                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//                afd.close();

            }
            mediaPlayer = MediaPlayer.create(context, raingtoneURI);
            String uriTest = raingtoneURI.toString();
            Log.i("設定鈴聲", uriTest);
            if (mediaPlayer != null) {
                mediaPlayer.start();
            } else {
                Log.e("AlarmReceiver (uri)", "MediaPlayer 建立失敗，uri = " + uriTest);
            }
            mediaState = true;
        }catch (IllegalArgumentException e){
            Log.e("Alarm error","路徑無效或空值");
        }
//        catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }catch (IOException e) {
//            throw new RuntimeException(e);
//        }


        Toast.makeText(context.getApplicationContext(), "鬧鐘響了！", Toast.LENGTH_SHORT).show();
    }

    public void setRaingtone(String uri){
        this.raingtoneURI = Uri.parse(uri);
    }

    public void setRaingtone(Uri uri){
        this.raingtoneURI = uri;
    }


//    public String getRaingtoneURL(){
//
//        return ringtoneName;
//    }
}

