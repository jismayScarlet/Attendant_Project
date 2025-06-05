package com.example.attendant_project.time_task;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.attendant_project.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class UsageDocControl extends Activity {
    TextView tv_timtask_usage, tv_development_files;
    Button btn_usage_exchange;
    String line = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetask_usage);
        tv_timtask_usage = findViewById(R.id.tv_timtask_usage);
        tv_development_files = findViewById(R.id.tv_development_files);
        btn_usage_exchange = findViewById(R.id.btn_usage_exchange);


        try {
            String inputString = fileReader("data/timtask_usage.txt");
            tv_timtask_usage.setText(inputString);
//        Log.e("說明文件", "imtask_usage 長度 " + inputString.length());
//        tv_timtask_usage.setHeight(dpToPx(this,600 + (inputString.length()/600)));

            inputString = fileReader("data/development_files.txt");
            tv_development_files.setText(inputString);
//        Log.e("說明文件", "development_files 長度 " + inputString.length());
//        tv_development_files.setHeight(dpToPx(this,600 + (inputString.length()/600)));
        }catch (IOException e){
            throw new RuntimeException("說明文件讀取失敗");
        }

        btn_usage_exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tv_timtask_usage.getVisibility() == VISIBLE){
                    tv_development_files.setVisibility(VISIBLE);
                    tv_timtask_usage.setVisibility(INVISIBLE);
                    btn_usage_exchange.setText("切換 使用說明");
                }else if(tv_development_files.getVisibility() == VISIBLE){
                    tv_timtask_usage.setVisibility(VISIBLE);
                    tv_development_files.setVisibility(INVISIBLE);
                    btn_usage_exchange.setText("切換 開發紀錄");
                }
            }
        });
    }

    private String fileReader(String resId) throws IOException {
        AssetManager assetManager = getApplicationContext().getAssets();
        InputStream inputStream = assetManager.open(resId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content.toString();
    }

//    public static int dpToPx(Context context, float dp) {//像素轉換工具
//        float density = context.getResources().getDisplayMetrics().density;
//        return Math.round(dp * density);
//    }

}