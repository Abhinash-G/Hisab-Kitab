package com.akg.hisabkitab.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SMSMessage: Data class representing a parsed SMS message from device storage.
 *
 * Represents an individual SMS message with essential fields needed for
 * expense tracking: timestamp, sender identification, and message content.
 */
public class SMSMessage {
    public final long timestamp;    // SMS date in milliseconds (epoch)
    public final String sender;     // Sender address (e.g., "VM-HDFCBK")
    public final String body;       // SMS message content

    /**
     * Create a new SMSMessage instance.
     *
     * @param timestamp SMS date in milliseconds since epoch
     * @param sender Sender's phone number or identifier
     * @param body Message content
     */
    public SMSMessage(long timestamp, String sender, String body) {
        this.timestamp = timestamp;
        this.sender = sender != null ? sender : "";
        this.body = body != null ? body : "";
    }

    /**
     * Convert SMSMessage to JSON object.
     *
     * @return JSONObject representation
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("timestamp", timestamp);
            json.put("sender", sender);
            json.put("body", body);
        } catch (JSONException e) {
            try {
                Log.e("SMSMessage", "Error converting to JSON", e);
            } catch (RuntimeException re) {
                // Ignore for unit tests where Log may not be available
                e.printStackTrace();
            }
        }
        return json;
    }

    @Override
    public String toString() {
        return "SMSMessage{" +
                "timestamp=" + timestamp +
                ", sender='" + sender + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SMSMessage that = (SMSMessage) o;

        if (timestamp != that.timestamp) return false;
        if (!sender.equals(that.sender)) return false;
        return body.equals(that.body);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(timestamp);
        result = 31 * result + sender.hashCode();
        result = 31 * result + body.hashCode();
        return result;
    }
}