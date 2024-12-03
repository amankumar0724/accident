package com.example.addressfinder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.Manifest;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlertScreenActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private static TextView timerText;
    private AppCompatButton yesButton;
    private AppCompatButton noButton;
    static int timerValue = 10;  // Starting value for timer
    private static final int CALL_PERMISSION_CODE = 103;
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    private boolean isCallInProgress = false; // To track if the call is already in progress
    private boolean isYesClicked = false; // To track if Yes button was clicked



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
    private static boolean isActivityOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_alert);  // Ensure your XML file is named correctly
        if (isActivityOpen) {
            finish();  // Close the previous instance
            return;
        }
        isActivityOpen = true;
        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        timerText = findViewById(R.id.timerText);
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);
        alertTitleTextView = findViewById(R.id.warningText);
        alertMessageTextView = findViewById(R.id.warningDescription);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

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

        // Initialize PhoneStateListener
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        // The phone is ringing (incoming call)
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // The call has been accepted (call in progress)
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // The call has ended or is disconnected
                        // Handle cleanup or finish tasks
                        if (isCallInProgress) {
                            // Call ended, reset the flag to prevent further call initiation
                            isCallInProgress = false;
                            Toast.makeText(AlertScreenActivity.this, "Call ended or aborted",
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        // Set up button listeners
        yesButton.setOnClickListener(v -> {

//            mediaPlayer.stop(); // Stop the sound when OK is clicked
//            mediaPlayer.release();
            stopMediaPlayer();
            isYesClicked = true; // Mark that the "Yes" button was clicked
            stopMediaPlayer();
            isActivityOpen = false;
            // Prevent any further calls if "Yes" is clicked
            if (isCallInProgress) {
                // If a call was already in progress, abort it or handle appropriately
                Toast.makeText(this, "Call aborted as you clicked Yes", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity or take appropriate action
            } else {
                finish(); // Close the activity
            }
            finish();  // Close this activity and go back
        });

        noButton.setOnClickListener(v -> {
            // Handle "Not Fine" button click
//            mediaPlayer.stop(); // Stop the sound when OK is clicked
//            mediaPlayer.release();
            stopMediaPlayer();
            isActivityOpen = false;
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                initiateEmergencyCall();
//            } else {
//                Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show();
//                requestCallPermission(); // Request permission if not granted
//            }
            if (!isYesClicked) { // Ensure no call is initiated if "Yes" was clicked
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    initiateEmergencyCall();
                } else {
                    Toast.makeText(this, "Call permission not granted", Toast.LENGTH_SHORT).show();
                    requestCallPermission();
                }
            } else {
                // Prevent the call initiation since "Yes" was clicked
                Toast.makeText(this, "Call request was aborted by user clicking Yes", Toast.LENGTH_SHORT).show();
            }
            finish();  // Close this activity and go back
        });
    }
    private void requestCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Call permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Call permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void initiateEmergencyCall() {
//        String emergencyNumber = "6367474404";//piyush
//        // Replace with the actual emergency contact
//        // number
//        Intent callIntent = new Intent(Intent.ACTION_CALL);
//        callIntent.setData(Uri.parse("tel:" + emergencyNumber));
//        startActivity(callIntent);
        if (isCallInProgress) {
            // If the call is already in progress, don't initiate a new one
            return;
        }

        // Flag that a call is in progress
        isCallInProgress = true;

        // Example: Emergency call to 911
        String emergencyNumber = "6367474404";
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + emergencyNumber));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            Toast.makeText(this, "Permission not granted for calling", Toast.LENGTH_SHORT).show();
        }
    }
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    // In onPause() method, unregister the listener
    @Override
    protected void onPause() {
        super.onPause();
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
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
        stopMediaPlayer();
        isActivityOpen = false;// Ensure the MediaPlayer is properly stopped and released
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
