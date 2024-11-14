package com.example.messagereader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmsMessage {
    private String sender;
    private String messageBody;
    private long timestamp;

    public SmsMessage(String sender, String messageBody, long timestamp) {
        this.sender = sender;
        this.messageBody = messageBody;
        this.timestamp = timestamp;
    }

    // Getter method for sender
    public String getSender() {
        return sender;
    }

    // Getter method for messageBody
    public String getMessageBody() {
        return messageBody;
    }

    // Getter method for timestamp
    public long getTimestamp() {
        return timestamp;
    }

    // Method to format the date
    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Setter methods if needed
    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}