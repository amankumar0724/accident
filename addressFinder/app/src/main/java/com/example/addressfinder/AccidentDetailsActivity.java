package com.example.addressfinder;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class AccidentDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_details); // Update with the correct layout

        // Get the accident details HashMap from the Intent
        HashMap<String, String> accidentDetails = (HashMap<String, String>) getIntent().getSerializableExtra("accident_details");

        // Find all the TextViews to display the accident details

        TextView tvLatitute = findViewById(R.id.tvLatitude);
        TextView tvLongitude = findViewById(R.id.tvLongitude);
        TextView tvTemperature = findViewById(R.id.tvTemperature);
        TextView tvRainValue = findViewById(R.id.tvRainValue);
        TextView tvGasValue = findViewById(R.id.tvGasValue);
        TextView tvTiltValue = findViewById(R.id.tvTiltValue);

        // Set the TextViews with the values from the HashMap
        String[] coordinates = accidentDetails.get("Location").split(",");
        tvLatitute.setText("Latitude : "+coordinates[0]);
        tvLongitude.setText("Longitude : "+coordinates[1]);
        tvTemperature.setText(accidentDetails.get("Temperature"));
        tvRainValue.setText(accidentDetails.get("Rain Value"));
        tvGasValue.setText(accidentDetails.get("Gas Value"));
        tvTiltValue.setText(accidentDetails.get("Tilt Sensor"));
    }
}
