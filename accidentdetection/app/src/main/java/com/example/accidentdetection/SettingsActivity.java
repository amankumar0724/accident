package com.example.accidentdetection;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private EditText emergencyContactInput;
    private Button saveButton;
    private static final String PREFS_NAME = "AccidentAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        emergencyContactInput = findViewById(R.id.emergencyContactInput);
        saveButton = findViewById(R.id.saveButton);

        // Load saved emergency contact
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedContact = preferences.getString("emergencyContact", "");
        emergencyContactInput.setText(savedContact);

        saveButton.setOnClickListener(v -> {
            // Save emergency contact
            String emergencyContact = emergencyContactInput.getText().toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("emergencyContact", emergencyContact);
            editor.apply();
            Toast.makeText(SettingsActivity.this, "Emergency contact saved!", Toast.LENGTH_SHORT).show();
        });
    }
}
