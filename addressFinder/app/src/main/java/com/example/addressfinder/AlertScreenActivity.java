package com.example.addressfinder;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.AppCompatButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlertScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private static TextView timerText;
    private AppCompatButton yesButton;
    private AppCompatButton noButton;
    static int timerValue = 10;  // Starting value for timer

    private static Handler handler = new Handler();
//    private Runnable countdownRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (timerValue >= 0) {
//                timerText.setText(String.valueOf(timerValue));
//                int progress = (int) (((float) timerValue / 10) * 100);
//                progressBar.setProgress(progress);
//
//                if (timerValue > 0) {
//                    timerValue--;
//                    handler.postDelayed(this, 1000);
//                } else {
//                    // Timer has reached 0, close the page or perform final action
//                    // For example:
//                    // finish(); // If this is in an Activity
//                    // or call a method to navigate away
//                }
//            }
//        }
//    };
    Runnable executorBasedCountdown = null;
    public void executorBasedCountdown() {
        System.out.println("Executor-based Countdown Started:");
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Runnable countdown = new Runnable() {
            int remainingTime = 10;
            @Override
            public void run() {
                if (remainingTime >= 0) {
                    System.out.println("Time remaining: " + remainingTime + " seconds");
                    timerText.setText(String.valueOf(remainingTime));
                    remainingTime--;
                } else {
//                    System.out.println("Countdown Complete!");
//                    Intent resultIntent = new Intent();
//                    resultIntent.putExtra("result_key", "Some result value");
//                    setResult(RESULT_OK, resultIntent);

                    // Automatically trigger "No" button click
                    runOnUiThread(() -> noButton.performClick());
                    finish();
                    executor.shutdown();
                }
            }
        };
        executor.scheduleAtFixedRate(countdown, 0, 1, TimeUnit.SECONDS);
    }

    private TextView alertTitleTextView;
    private TextView alertMessageTextView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_alert);  // Ensure your XML file is named correctly

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        timerText = findViewById(R.id.timerText);
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);
        alertTitleTextView = findViewById(R.id.warningText);
        alertMessageTextView = findViewById(R.id.warningDescription);

        // Get the intent that started this activity
        Intent intent = getIntent();
        String alertTitle = intent.getStringExtra("alert_title");
        String alertMessage = intent.getStringExtra("alert_message");


        // Set the alert title and message
        alertTitleTextView.setText(alertTitle);
        alertMessageTextView.setText(alertMessage);
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound); // Use your sound file name here
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        // Start the countdown

//        handler.post(executorBasedCountdown);
        executorBasedCountdown();

        // Set up button listeners
        yesButton.setOnClickListener(v -> {

//            mediaPlayer.stop(); // Stop the sound when OK is clicked
//            mediaPlayer.release();
            stopMediaPlayer();
            finish();  // Close this activity and go back
        });

        noButton.setOnClickListener(v -> {
            // Handle "Not Fine" button click


//            mediaPlayer.stop(); // Stop the sound when OK is clicked
//            mediaPlayer.release();
            stopMediaPlayer();
            finish();  // Close this activity and go back
        });
    }
    private void stopMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset(); // Reset the MediaPlayer to its uninitialized state
                mediaPlayer.release(); // Release resources
                mediaPlayer = null; // Nullify the reference
            } catch (IllegalStateException e) {
                e.printStackTrace(); // Handle or log the exception
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMediaPlayer(); // Ensure the MediaPlayer is properly stopped and released
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        // Stop the countdown when the activity is paused (e.g., user switches apps)
//        handler.removeCallbacks(executorBasedCountdown);
//    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Restart the countdown when the activity is resumed
//        if (timerValue > 0) {
//            handler.post(executorBasedCountdown);
//        }
//    }
}
