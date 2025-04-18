package com.example.attendant_project.time_task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.example.attendant_project.R;

/*
* 播放鬧鐘音效
* */

public class AlarmReceiver extends BroadcastReceiver {
    MediaPlayer mediaPlayer;
    boolean mediaState = false;
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "鬧鐘響了！", Toast.LENGTH_SHORT).show();

        // 播放音樂作為鬧鐘
        mediaPlayer = MediaPlayer.create(context, R.raw.test); // 把音樂放在 res/raw
        mediaPlayer.start();
        mediaState = true;
    }



}

