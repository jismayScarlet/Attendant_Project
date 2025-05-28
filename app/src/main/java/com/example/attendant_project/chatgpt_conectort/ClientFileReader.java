package com.example.attendant_project.chatgpt_conectort;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.attendant_project.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ClientFileReader extends Activity {
    String line = null;
    private String resolut = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        InputStream inputStream = getResources().openRawResource(R.raw.system_role_set);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder content = new StringBuilder();

        try {
            while ((line = reader.readLine()) != null) {
                content.append(line);
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
        resolut = content.toString();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("roleDate",resolut);
        setResult(Activity.RESULT_OK,resultIntent);
        finish();
    }





}
