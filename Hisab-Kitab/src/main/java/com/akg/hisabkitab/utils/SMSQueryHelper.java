package com.akg.hisabkitab.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import com.akg.hisabkitab.models.SMSMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SMSQueryHelper: Encapsulates all SMS query logic from ContentProvider.
 *
 * Responsibilities:
 * 1. Parse comma-separated keywords into list
 * 2. Build dynamic WHERE clauses for keyword and date filtering
 * 3. Query SMS content provider
 * 4. Parse Cursor results into SMSMessage objects
 * 5. Handle threading with ExecutorService
 * 6. Provide comprehensive error handling
 *
 * ContentProvider Details:
 * - URI: content://sms/inbox
 * - Columns: address, body, date (in milliseconds)
 * - Permission: READ_SMS (must be requested at runtime)
 */
public class SMSQueryHelper {

    private static final String TAG = "SMSQueryHelper";
    private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/");
    private static final String[] SMS_PROJECTION = {"address", "body", "date"};
    private static final int COLUMN_ADDRESS = 0;
    private static final int COLUMN_BODY = 1;
    private static final int COLUMN_DATE = 2;

    private final Context context;
    private final ExecutorService executorService;

    /**
     * Create a new SMSQueryHelper instance.
     *
     * @param context Android application context
     */
    public SMSQueryHelper(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Parse comma-separated keywords into a list.
     *
     * @param keywordString Comma-separated keywords (e.g., "HDFCBK, SBI, ICICI")
     * @return List of trimmed keywords, or empty list if input is null/empty
     */
    public List<String> parseKeywords(String keywordString) {
        List<String> keywords = new ArrayList<>();

        if (keywordString == null || keywordString.trim().isEmpty()) {
            Log.d(TAG, "No keywords provided");
            return keywords;
        }

        String[] parts = keywordString.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                keywords.add(trimmed);
                Log.d(TAG, "Parsed keyword: " + trimmed);
            }
        }

        Log.d(TAG, "Total keywords parsed: " + keywords.size());
        return keywords;
    }

    /**
     * Build WHERE clause for keyword filtering.
     *
     * Format: (address LIKE ? OR address LIKE ? OR ...)
     *
     * @param keywords List of keywords to filter by
     * @return WHERE clause fragment, or empty string if no keywords
     */
    public String buildKeywordWhereClause(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            Log.d(TAG, "No keywords provided for WHERE clause");
            return "";
        }

        StringBuilder whereClause = new StringBuilder("(");
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) {
                whereClause.append(" OR ");
            }
            whereClause.append("address LIKE ?");
        }
        whereClause.append(")");

        String result = whereClause.toString();
        Log.d(TAG, "Keyword WHERE clause: " + result);
        return result;
    }

    /**
     * Build WHERE clause for date range filtering.
     *
     * Format: (date >= ? AND date <= ?)
     *
     * @param startDate Start date in milliseconds
     * @param endDate End date in milliseconds
     * @return WHERE clause fragment
     */
    public String buildDateWhereClause(long startDate, long endDate) {
        String whereClause = "(date >= ? AND date <= ?)";
        Log.d(TAG, "Date WHERE clause: " + whereClause + " (" + startDate + " to " + endDate + ")");
        return whereClause;
    }

    /**
     * Build combined WHERE clause and selection arguments.
     *
     * @param keywords List of keywords
     * @param startDate Start date in milliseconds
     * @param endDate End date in milliseconds
     * @return Pair of (whereClause, selectionArgs array)
     */
    public Pair<String, String[]> buildSelection(List<String> keywords, long startDate, long endDate) {
        StringBuilder whereClause = new StringBuilder();
        List<String> args = new ArrayList<>();

        // Add keyword clause if keywords provided
        if (keywords != null && !keywords.isEmpty()) {
            whereClause.append(buildKeywordWhereClause(keywords));
            for (String keyword : keywords) {
                args.add("%" + keyword + "%");
            }
        }

        // Add date clause
        if (whereClause.length() > 0) {
            whereClause.append(" AND ");
        }
        whereClause.append(buildDateWhereClause(startDate, endDate));
        args.add(String.valueOf(startDate));
        args.add(String.valueOf(endDate));

        String finalWhereClause = whereClause.toString();
        String[] finalArgs = args.toArray(new String[0]);

        Log.d(TAG, "Final WHERE clause: " + finalWhereClause);
        Log.d(TAG, "Selection args count: " + finalArgs.length);

        return new Pair<>(finalWhereClause, finalArgs);
    }

    /**
     * Query SMS from ContentProvider and return list of SMSMessage objects.
     *
     * @param keywords Comma-separated keywords (optional, null for all)
     * @param userStartDate User-provided start date in milliseconds (optional)
     * @param userEndDate User-provided end date in milliseconds (optional)
     * @return List of SMSMessage objects matching the filters
     */
    public List<SMSMessage> querySMS(String keywords, Long userStartDate, Long userEndDate) {
        List<SMSMessage> messages = new ArrayList<>();

        // Validate and get date range
        Pair<Long, Long> dateRange = SyncStrategy.getDateRange(context, userStartDate, userEndDate);
        long startDate = dateRange.first;
        long endDate = dateRange.second;

        // Validate date range
        if (!SyncStrategy.isValidDateRange(startDate, endDate)) {
            Log.e(TAG, "Invalid date range provided");
            return messages;
        }

        // Parse keywords
        List<String> keywordList = parseKeywords(keywords);

        // Build selection
        Pair<String, String[]> selection = buildSelection(keywordList, startDate, endDate);
        String whereClause = selection.first;
        String[] selectionArgs = selection.second;

        // Query ContentProvider
        Log.d(TAG, "Querying SMS inbox with: " + whereClause);
        Cursor cursor = context.getContentResolver().query(
                SMS_INBOX_URI,
                SMS_PROJECTION,
                whereClause,
                selectionArgs,
                "date DESC"  // Sort by date descending (newest first)
        );

        if (cursor != null) {
            try {
                Log.d(TAG, "Query returned " + cursor.getCount() + " results");
                while (cursor.moveToNext()) {
                    String address = cursor.getString(COLUMN_ADDRESS);
                    String body = cursor.getString(COLUMN_BODY);
                    long timestamp = cursor.getLong(COLUMN_DATE);

                    SMSMessage message = new SMSMessage(timestamp, address, body);
                    messages.add(message);
                }
                Log.d(TAG, "Parsed " + messages.size() + " SMS messages");
            } finally {
                cursor.close();
            }
        } else {
            Log.w(TAG, "Query returned null cursor");
        }

        // Update last sync timestamp for incremental sync after successful query
        if (SyncStrategy.isIncrementalSync(userStartDate, userEndDate)) {
            PreferencesManager prefManager = PreferencesManager.getInstance(context);
            boolean success = prefManager.setLastSyncTimestamp(endDate);
            if (!success) {
                Log.w(TAG, "Failed to update last sync timestamp after successful query");
            }
        }


        return messages;
    }

    /**
     * Query SMS asynchronously on background thread.
     *
     * @param keywords Comma-separated keywords (optional)
     * @param userStartDate User-provided start date (optional)
     * @param userEndDate User-provided end date (optional)
     * @param callback Callback to receive results on main thread
     */
    public void querySMSAsync(String keywords, Long userStartDate, Long userEndDate,
                             SMSQueryCallback callback) {
        executorService.execute(() -> {
            try {
                List<SMSMessage> messages = querySMS(keywords, userStartDate, userEndDate);
                if (callback != null) {
                    // Post success callback to main thread
                    new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(messages));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in async SMS query", e);
                if (callback != null) {
                    // Post error callback to main thread
                    new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
                }
            }
        });
    }

    /**
     * Shutdown the ExecutorService.
     */
    public void shutdown() {
        executorService.shutdown();
    }

    /**
     * Callback interface for async SMS query results.
     */
    public interface SMSQueryCallback {
        /**
         * Called when query succeeds.
         */
        void onSuccess(List<SMSMessage> messages);

        /**
         * Called when query fails.
         */
        void onError(Exception error);
    }
}