package com.example.trial2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            for (Object pdu : pdus) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                String messageBody = sms.getMessageBody();
                String[] parts = messageBody.split("Latitude: | Longitude: ");

                // Check if message contains coordinates
                if (parts.length >= 3) {
                    try {
                        double latitude = Double.parseDouble(parts[1].split(",")[0].trim());
                        double longitude = Double.parseDouble(parts[2].trim());

                        // Now you have latitude and longitude, you can use them in your app
                        openMapWithCoordinates(context, latitude, longitude);
                    } catch (NumberFormatException e) {
                        Log.e("SmsReceiver", "Error parsing coordinates", e);
                    }
                }
            }
        }
    }

    private void openMapWithCoordinates(Context context, double latitude, double longitude) {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude));
        mapIntent.setPackage("com.google.android.apps.maps");
        mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mapIntent);
    }
}
