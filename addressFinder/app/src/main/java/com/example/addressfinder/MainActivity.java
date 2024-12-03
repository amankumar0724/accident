package com.example.addressfinder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLatitude;
    private EditText editTextLongitude;
    private TextView textViewAddress;
    private Button buttonOpenMap;
    private static final int SMS_PERMISSION_CODE = 101;
    private static final String SENDER_PHONE_NUMBER = "6354997765";
//    6354997765
    private Integer flag = 0;
    private HashMap<String, String> message = null;

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Toast.makeText(MainActivity.this, "coordinates found", Toast.LENGTH_SHORT).show();
            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (Object pdu : pdus) {
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            String sender = smsMessage.getDisplayOriginatingAddress();

//                            String messageBody = smsMessage.getMessageBody();
                            Log.d("Sender", sender);
                            // Check if the message is from the specified sender

                            if (sender.contains(SENDER_PHONE_NUMBER)) {
                                 message = ReadSMS();
                                extractCoordinates(message);
                            }
                        }
                    }
                }
            }
        }
    };
    private ImageView signOutButton;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        editTextLatitude = findViewById(R.id.editTextLatitude);
        editTextLongitude = findViewById(R.id.editTextLongitude);
        textViewAddress = findViewById(R.id.textViewAddress);
        Button buttonFetchAddress = findViewById(R.id.buttonFetchAddress);
        buttonOpenMap = findViewById(R.id.buttonOpenMap);
        Button cardViewCurrentAccident = findViewById(R.id.cardViewCurrentAccident);

        // Request SMS permissions
        requestSmsPermission();


        // Register SMS receiver
        registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        // Initially disable the Open in Map button
        buttonOpenMap.setEnabled(false);
//        cardViewCurrentAccident.setEnabled(false);

        // Set up button click listeners
        buttonFetchAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    getAddressFromLocation();
                    // Enable the Open in Map button after fetching address
                    buttonOpenMap.setEnabled(true);
                    flag = 1;
                }
            }
        });

        buttonOpenMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInMap();
            }
        });
        mAuth = FirebaseAuth.getInstance();

        signOutButton = findViewById(R.id.buttonSignOut); // Button for Sign Out

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut(); // Call the sign-out method
            }
        });

        cardViewCurrentAccident.setOnClickListener(v -> {
            // Example accident details
//            HashMap<String, String> accidentDetails = new HashMap<>();
//            accidentDetails.put("Location", "25.221990,81.666493");
//            accidentDetails.put("Temperature", "45.6°C");
//            accidentDetails.put("Rain Value", "541");
//            accidentDetails.put("Gas Value", "550");
//            accidentDetails.put("Tilt Sensor", "23");

            // Launch AccidentDetailsActivity and pass the details
            if(message != null) {
                Intent intent = new Intent(MainActivity.this, AccidentDetailsActivity.class);
                intent.putExtra("accident_details", message);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister SMS receiver
        unregisterReceiver(smsReceiver);
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS
                    },
                    SMS_PERMISSION_CODE);
        }
    }
    private HashMap<String, String> parseMessageToHashMap(String message) {
        HashMap<String, String> accidentDetails = new HashMap<>();

        // Split the message by lines
        String[] lines = message.split("\n");
        for (String line : lines) {
            // Split each line into key and value based on the ":" separator
            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                accidentDetails.put(key, value);
            }
        }
        String addr = getStringAddressFromLocation();
        accidentDetails.put("address",addr);
        System.out.println(accidentDetails);
        Toast.makeText(this, accidentDetails.get("address"), Toast.LENGTH_SHORT).show();
        return accidentDetails;
    }

    private HashMap<String, String> ReadSMS() {
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(
                uri,
                new String[]{"_id", "address", "date", "body"},
                null,
                null,
                "date DESC LIMIT 1"  // Order by date descending and limit to 1 message
        );

        if (cursor != null && cursor.moveToFirst()) {
            // We only need to read once since we're getting just the first message
            String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));
            String message = cursor.getString(cursor.getColumnIndexOrThrow("body"));
            long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow("date"));

            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
            String date = dateFormat.format(new Date(dateMillis));

            // Create formatted message string
//            String formattedMessage = String.format(
//                    "From: %s\n" +
////                    "Date: %s\n" +
//                            "Message: %s\n",
//                    sender,
////                    date,
//                    message
//            );
            HashMap<String, String> accidentDetails = parseMessageToHashMap(message);
            Toast.makeText(this, "ACCIDENT ALERT", Toast.LENGTH_LONG).show();  // Changed to
            // LENGTH_LONG to give more time to read

            cursor.close();
            return accidentDetails;
        }
        return null;
    }

    private void extractCoordinates(HashMap<String, String> messageBody) {
        // Pattern to match latitude and longitude
//        Pattern pattern = Pattern.compile("(-?\\d+\\.?\\d*)\\s*[,\\s]+\\s*(-?\\d+\\.?\\d*)");
//        Matcher matcher = pattern.matcher(messageBody);
//
//        if (matcher.find()) {
//            final String latitude = matcher.group(1);
//            final String longitude = matcher.group(2);
//
//            // Update UI on main thread
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    editTextLatitude.setText(latitude);
//                    editTextLongitude.setText(longitude);
//                    // Automatically fetch address if coordinates are valid
//                    if (validateInput()) {
//                        getAddressFromLocation();
//                        buttonOpenMap.setEnabled(true);
//                    }
//                }
//            });
//        }
//        String[] coordinates = messageBody.split(",");
//        if(coordinates.length == 2){
//            String latitude = coordinates[0];  // "25.426245"
//            String longitude = coordinates[1]; // "81.755877"
            String latitude = messageBody.get("Latitude");
            String longitude = messageBody.get("Longitude");
            // Set the values
            editTextLatitude.setText(latitude);
            editTextLongitude.setText(longitude);
            Button buttonFetchAddress = findViewById(R.id.buttonFetchAddress);
            // Automatically trigger the Get Address button click
            buttonFetchAddress.post(new Runnable() {
                @Override
                public void run() {
                    buttonFetchAddress.performClick();
                }

            });
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput() {
        String lat = editTextLatitude.getText().toString();
        String lon = editTextLongitude.getText().toString();

        if (lat.isEmpty() || lon.isEmpty()) {
            Toast.makeText(this, "Please enter both latitude and longitude", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lon);

            if (latitude < -90 || latitude > 90) {
                Toast.makeText(this, "Invalid latitude value", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (longitude < -180 || longitude > 180) {
                Toast.makeText(this, "Invalid longitude value", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private String getStringAddressFromLocation() {
        String addressString = "";
        try {
            double latitude = Double.parseDouble(editTextLatitude.getText().toString());
            double longitude = Double.parseDouble(editTextLongitude.getText().toString());
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressString = address.getAddressLine(0); // Full address
                    textViewAddress.setText("Address: " + addressString);

                    showCustomAlert("ACCIDENT ALERT",addressString);
//                    Intent intent = new Intent(MainActivity.this,AlertScreenActivity.class);
//                    startActivity(intent);
//                    finish();
                } else {
                    textViewAddress.setText("Address not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                textViewAddress.setText("Unable to fetch address. Check network.");
            }
            return addressString;
        } catch (NumberFormatException e) {
            textViewAddress.setText("Please enter valid coordinates.");
        }
        return addressString;
    }

    private void getAddressFromLocation() {
        String addressString = "";
        try {
            double latitude = Double.parseDouble(editTextLatitude.getText().toString());
            double longitude = Double.parseDouble(editTextLongitude.getText().toString());
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    addressString = address.getAddressLine(0); // Full address
                    textViewAddress.setText("Address: " + addressString);

                    showCustomAlert("ACCIDENT ALERT",addressString);
//                    Intent intent = new Intent(MainActivity.this,AlertScreenActivity.class);
//                    startActivity(intent);
//                    finish();
                } else {
                    textViewAddress.setText("Address not found.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                textViewAddress.setText("Unable to fetch address. Check network.");
            }
        } catch (NumberFormatException e) {
            textViewAddress.setText("Please enter valid coordinates.");
        }
    }

    private void openInMap() {
        try {
            double latitude = Double.parseDouble(editTextLatitude.getText().toString());
            double longitude = Double.parseDouble(editTextLongitude.getText().toString());

            // Create a Uri with the location
            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude);

            // Create an Intent to open Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // If Google Maps is not installed, open in browser
                Uri browserUri = Uri.parse("https://www.google.com/maps?q=" + latitude + "," + longitude);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                startActivity(browserIntent);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid coordinates", Toast.LENGTH_SHORT).show();
        }
    }
    //    private void signOut() {
//        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("isLoggedIn", false);
//        editor.apply();
//
//        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
//        startActivity(intent);
//        finish();
//    }
    private void signOut() {
        // Sign out the current user from Firebase Authentication
        mAuth.signOut();

        // Redirect to SignInActivity or any other activity where users can log in
        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();  // Close the MainActivity
        Toast.makeText(this, "Successfully signed out", Toast.LENGTH_SHORT).show();
    }
    public void showCustomAlert(String title, String message) {
//        // Inflate the custom alert layout
//        LayoutInflater inflater = getLayoutInflater();
//        View alertLayout = inflater.inflate(R.layout.custom_alert, null);
//
//        // Set the title and message in the custom layout
//        TextView alertTitle = alertLayout.findViewById(R.id.warningText);
//        TextView alertMessage = alertLayout.findViewById(R.id.warningDescription);
//        alertTitle.setText(title);
//        alertMessage.setText(message);
//
//        // Create the dialog builder and set the custom layout
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setView(alertLayout);
//
//        // Create and show the dialog
//        AlertDialog alertDialog = alertDialogBuilder.create();
//        alertDialog.show();
//
//        // Play alert sound
//        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound); // Use your sound file name here
//        mediaPlayer.start();
//
//        // Release the MediaPlayer when the alert dialog is dismissed
//        alertDialog.setOnDismissListener(dialog -> mediaPlayer.release());
//
//        // Set the OK button functionality
//        Button buttonOk = alertLayout.findViewById(R.id.noButton);
//        buttonOk.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mediaPlayer.stop(); // Stop the sound when OK is clicked
//                mediaPlayer.release();
//                alertDialog.dismiss(); // Dismiss the dialog when OK is clicked
//            }
//        });



        // Create an Intent to start AlertScreenActivity
        Intent intent = new Intent(MainActivity.this, AlertScreenActivity.class);
        // Pass the title and message as extras
        intent.putExtra("alert_title", title);
        intent.putExtra("alert_message", message);
        // Start the AlertScreenActivity
        startActivity(intent);

        // Optionally, you can finish the current activity if you want to close it
//         finish();
    }
}