package com.akg.hisabkitab.utils;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

/**
 * SyncStrategy: Determines the date range for SMS queries.
 *
 * Implements both manual date range selection and incremental sync logic.
 * - Manual: User provides explicit start and end dates
 * - Incremental: Uses last sync timestamp from PreferencesManager
 *
 * Logic Flow:
 * IF user_provided_start_date AND user_provided_end_date:
 *   USE manual date range from UI
 * ELSE:
 *   last_timestamp = PreferencesManager.getLastSyncTimestamp()
 *   IF last_timestamp is null:
 *     USE default (e.g., 30 days ago to today)
 *   ELSE:
 *     start_date = last_timestamp
 *     end_date = current_time
 */
public final class SyncStrategy {

    private SyncStrategy() {
        // Utility class - prevent instantiation
    }

    private static final String TAG = "SyncStrategy";
    private static final long DEFAULT_DAYS_BACK = 100L;
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

    /**
     * Determine the date range for SMS query.
     *
     * Priority order:
     * 1. If both user dates provided → use manual range
     * 2. If no dates provided → use incremental sync (last_sync_timestamp or default)
     *
     * @param context Android context for accessing PreferencesManager
     * @param userStartDate User-provided start date (epoch ms), or null
     * @param userEndDate User-provided end date (epoch ms), or null
     * @return Pair of (startDate, endDate) as epoch milliseconds
     */
    public static Pair<Long, Long> getDateRange(Context context, Long userStartDate, Long userEndDate) {
        // Manual Range: Both dates provided by user
        if (userStartDate != null && userEndDate != null) {
            Log.d(TAG, "Using manual date range: " + userStartDate + " to " + userEndDate);
            return new Pair<>(userStartDate, userEndDate);
        }

        // Incremental Sync: Use last sync timestamp or default
        PreferencesManager prefManager = PreferencesManager.getInstance(context);
        Long lastSyncTimestamp = prefManager.getLastSyncTimestamp();

        long startDate;
        long endDate = System.currentTimeMillis();

        if (lastSyncTimestamp == null) {
            // First-time sync: Use default (30 days ago)
            startDate = endDate - (DEFAULT_DAYS_BACK * MILLIS_PER_DAY);
            Log.d(TAG, "First-time sync: Using default " + DEFAULT_DAYS_BACK + " days back");
        } else {
            // Incremental sync: Use last sync timestamp
            startDate = lastSyncTimestamp;
            Log.d(TAG, "Incremental sync: Using last sync timestamp: " + lastSyncTimestamp);
        }

        Log.d(TAG, "Final date range: " + startDate + " to " + endDate);
        return new Pair<>(startDate, endDate);
    }

    /**
     * Check if the sync is incremental (not manual).
     *
     * @param userStartDate User-provided start date, or null
     * @param userEndDate User-provided end date, or null
     * @return true if using incremental strategy, false if manual
     */
    public static boolean isIncrementalSync(Long userStartDate, Long userEndDate) {
        boolean isIncremental = userStartDate == null || userEndDate == null;
        Log.d(TAG, "isIncrementalSync: " + isIncremental);
        return isIncremental;
    }

    /**
     * Validate date range.
     *
     * @param startDate Start date in milliseconds
     * @param endDate End date in milliseconds
     * @return true if startDate <= endDate, false otherwise
     */
    public static boolean isValidDateRange(long startDate, long endDate) {
        boolean valid = startDate <= endDate;
        if (!valid) {
            Log.w(TAG, "Invalid date range: startDate (" + startDate + ") > endDate (" + endDate + ")");
        }
        return valid;
    }
}
