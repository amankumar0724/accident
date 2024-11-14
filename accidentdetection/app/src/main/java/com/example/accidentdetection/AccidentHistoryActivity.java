package com.example.accidentdetection;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AccidentHistoryActivity extends AppCompatActivity {

    private TextView historyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_history);

        historyText = findViewById(R.id.historyText);

        // Get intent data
        boolean emergencySent = getIntent().getBooleanExtra("emergencySent", false);
        String location = getIntent().getStringExtra("location");

        // Update history with accident details
        String historyLog = "Accident occurred at " + location + ".\nEmergency Sent: " + emergencySent;
        historyText.setText(historyLog);
    }
}
