package com.example.trial2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private SmsReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, 1);
        } else {
            registerSmsReceiver();
        }
    }

    // Register the SmsReceiver
    private void registerSmsReceiver() {
        smsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
    }

    // Unregister SmsReceiver when the app is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }

    // Handle SMS permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            registerSmsReceiver();
        } else {
            Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    // Inner class for SmsReceiver
    public class SmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = sms.getMessageBody();
                    String sender = sms.getOriginatingAddress();

                    // Show the received message in a toast (or use it as needed)
                    Toast.makeText(context, "From: " + sender + "\nMessage: " + messageBody, Toast.LENGTH_LONG).show();

                    // Process message if it contains location info, for example
                    String[] parts = messageBody.split("Latitude: | Longitude: ");
                    if (parts.length >= 3) {
                        try {
                            double latitude = Double.parseDouble(parts[1].split(",")[0].trim());
                            double longitude = Double.parseDouble(parts[2].trim());
                            // Call a method to handle the coordinates, e.g., open in Google Maps
                            openMapWithCoordinates(latitude, longitude);
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Error parsing coordinates", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }

        private void openMapWithCoordinates(double latitude, double longitude) {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude));
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            }
        }
    }
}
