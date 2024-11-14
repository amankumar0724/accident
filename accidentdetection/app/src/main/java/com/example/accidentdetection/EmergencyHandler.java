package com.example.accidentdetection;

import android.telephony.SmsManager;
import android.widget.Toast;
import android.content.Context;

public class EmergencyHandler {

    private Context context;

    public EmergencyHandler(Context context) {
        this.context = context;
    }

    public void sendEmergencyMessage(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "Emergency message sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }
}
