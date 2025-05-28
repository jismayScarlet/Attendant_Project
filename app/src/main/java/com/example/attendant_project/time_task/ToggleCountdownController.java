package com.example.attendant_project.time_task;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ToggleCountdownController {
    private EditText editTextTime;
    private CheckedTextView checkedTextView;
    private Button saveButton;

    private long savedSeconds = 0;
    private long remainingSeconds = 0;

    private boolean isCountingDown = false;
    private CountDownTimer countDownTimer;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "timer_prefs";
    private static final String KEY_REMAINING = "remaining_seconds";

    public ToggleCountdownController(EditText editTextTime, CheckedTextView checkedTextView, Button saveButton) {
        this.editTextTime = editTextTime;
        this.checkedTextView = checkedTextView;
        this.saveButton = saveButton;
        prefs = editTextTime.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        remainingSeconds = prefs.getLong(KEY_REMAINING, 0);
        savedSeconds = remainingSeconds; // 初始狀態為上次剩餘時間
        updateDisplay(remainingSeconds);

        setupListeners();
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            String timeStr = editTextTime.getText().toString().trim();
            long parsed = parseTimeInput(timeStr);
            if (parsed > 0) {
                remainingSeconds += parsed; // 累加
                savedSeconds = remainingSeconds;

                stopCountdown(); // 停止倒數重設
                updateDisplay(remainingSeconds);

                // 儲存至 SharedPreferences
                prefs.edit().putLong(KEY_REMAINING, remainingSeconds).apply();
            }
        });

        checkedTextView.setOnClickListener(v -> {
            if (!isCountingDown) {
                startCountdown(remainingSeconds);
            } else {
                stopCountdown();
                updateDisplay(remainingSeconds); // 顯示暫停當下剩餘時間
            }
            isCountingDown = !isCountingDown;
            checkedTextView.setChecked(isCountingDown);
        });
    }

    private long parseTimeInput(String timeStr) {
        if (timeStr.contains(":")) {
            String[] parts = timeStr.split(":");
            try {
                int min = Integer.parseInt(parts[0]);
                int sec = Integer.parseInt(parts[1]);
                return min * 60 + sec;
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            try {
                return Long.parseLong(timeStr);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    private void updateDisplay(long seconds) {
        int min = (int) (seconds / 60);
        int sec = (int) (seconds % 60);
        checkedTextView.setText(String.format(Locale.getDefault(), "剩餘時間: %02d:%02d \n(點我釋放)", min, sec));
    }

    private void startCountdown(long startSeconds) {
        if (startSeconds <= 0) return;

        countDownTimer = new CountDownTimer(startSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSeconds = millisUntilFinished / 1000;
                updateDisplay(remainingSeconds);
                prefs.edit().putLong(KEY_REMAINING, remainingSeconds).apply(); // 寫入
            }

            @Override
            public void onFinish() {
                isCountingDown = false;
                checkedTextView.setChecked(false);
                remainingSeconds = 0;
                checkedTextView.setText("倒數完成");
                prefs.edit().remove(KEY_REMAINING).apply(); // 清除

                // 播放預設通知鈴聲
                Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                Ringtone ringtone = RingtoneManager.getRingtone(checkedTextView.getContext(), notificationUri);
                if (ringtone != null) {
                    ringtone.play();

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // 背景執行的延遲任務
                            ringtone.stop();
                        }
                    }, 5000);
                }
            }

        };
        countDownTimer.start();
    }

    public void onFinishOutCall() {
        stopCountdown(); // 停止倒數
        isCountingDown = false;
        checkedTextView.setChecked(false);
        remainingSeconds = 0;
        checkedTextView.setText("剩餘時間清除");
        prefs.edit().remove(KEY_REMAINING).apply();
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
