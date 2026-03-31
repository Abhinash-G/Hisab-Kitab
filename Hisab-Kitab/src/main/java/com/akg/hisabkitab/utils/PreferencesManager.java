package com.akg.hisabkitab.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * PreferencesManager: Manages persistent storage of the last sync timestamp
 * for incremental SMS sync strategy using SharedPreferences.
 *
 * Features:
 * - Thread-safe access to SharedPreferences
 * - Stores last sync timestamp for incremental sync
 * - Proper error handling for SharedPreferences operations
 * - Synchronized methods to prevent race conditions
 */
public class PreferencesManager {

    private static final String PREFS_FILE_NAME = "expense_tracker_prefs";
    private static final String LAST_SYNC_TIMESTAMP_KEY = "last_sync_timestamp";
    private static final String TAG = "PreferencesManager";

    private static SharedPreferences sharedPreferences;
    private static volatile PreferencesManager instance;

    /**
     * Private constructor for singleton pattern
     */
    private PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get singleton instance of PreferencesManager (thread-safe)
     * @param context Android application context
     * @return PreferencesManager singleton instance
     */
    public static PreferencesManager getInstance(Context context) {
        if (instance == null) {
            synchronized (PreferencesManager.class) {
                if (instance == null) {
                    instance = new PreferencesManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Retrieve the last sync timestamp from SharedPreferences.
     * Returns null if no previous sync has been recorded.
     *
     * @return Last sync timestamp in milliseconds, or null if not set
     */
    public synchronized Long getLastSyncTimestamp() {
        try {
            // -1 is used as a sentinel value to indicate "not set"
            long timestamp = sharedPreferences.getLong(LAST_SYNC_TIMESTAMP_KEY, -1);
            if (timestamp == -1) {
                Log.d(TAG, "No previous sync timestamp found. First-time sync detected.");
                return null;
            }
            Log.d(TAG, "Retrieved last sync timestamp: " + timestamp);
            return timestamp;
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving last sync timestamp", e);
            return null;
        }
    }

    /**
     * Save the current sync timestamp to SharedPreferences.
     * Should only be called after a successful sync with the backend.
     *
     * @param timestamp Timestamp in milliseconds to save
     * @return true if save was successful, false otherwise
     */
    public synchronized boolean setLastSyncTimestamp(long timestamp) {
        if (timestamp <= 0) {
            Log.w(TAG, "Invalid timestamp provided: " + timestamp + ". Skipping save.");
            return false;
        }

        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(LAST_SYNC_TIMESTAMP_KEY, timestamp);
            boolean success = editor.commit(); // Use commit() for synchronous write
            if (success) {
                Log.d(TAG, "Successfully saved last sync timestamp: " + timestamp);
            } else {
                Log.w(TAG, "Failed to commit last sync timestamp to SharedPreferences");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error saving last sync timestamp", e);
            return false;
        }
    }

    /**
     * Clear the stored sync timestamp from SharedPreferences.
     * Useful for testing purposes or resetting sync state.
     *
     * @return true if clear was successful, false otherwise
     */
    public synchronized boolean clearLastSyncTimestamp() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(LAST_SYNC_TIMESTAMP_KEY);
            boolean success = editor.commit();
            if (success) {
                Log.d(TAG, "Successfully cleared last sync timestamp");
            } else {
                Log.w(TAG, "Failed to clear last sync timestamp from SharedPreferences");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing last sync timestamp", e);
            return false;
        }
    }

    /**
     * Check if a sync has been performed previously.
     *
     * @return true if a sync timestamp exists, false otherwise
     */
    public synchronized boolean hasPreviousSync() {
        try {
            return sharedPreferences.contains(LAST_SYNC_TIMESTAMP_KEY);
        } catch (Exception e) {
            Log.e(TAG, "Error checking for previous sync", e);
            return false;
        }
    }

    /**
     * Get the number of milliseconds since the last sync.
     * Returns null if no previous sync exists.
     *
     * @return Milliseconds since last sync, or null if never synced
     */
    public synchronized Long getMillisecondsSinceLastSync() {
        try {
            Long lastTimestamp = getLastSyncTimestamp();
            if (lastTimestamp == null) {
                return null;
            }
            long currentTime = System.currentTimeMillis();
            long millisecondsSince = currentTime - lastTimestamp;
            Log.d(TAG, "Milliseconds since last sync: " + millisecondsSince);
            return millisecondsSince;
        } catch (Exception e) {
            Log.e(TAG, "Error calculating milliseconds since last sync", e);
            return null;
        }
    }
}

