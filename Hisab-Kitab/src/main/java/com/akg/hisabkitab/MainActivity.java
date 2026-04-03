package com.akg.hisabkitab;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.akg.hisabkitab.models.SMSMessage;
import com.akg.hisabkitab.utils.PreferencesManager;
import com.akg.hisabkitab.utils.SMSQueryHelper;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private EditText keywordInput;
    private Button startDateButton;
    private Button endDateButton;
    private Button startScanButton;
    private Button sendButton;
    private Button resetButton;
    private ProgressBar progressBar;
    private TextView feedbackText;

    // Date variables (epoch milliseconds)
    private Long selectedStartDate = null;
    private Long selectedEndDate = null;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";

    // SMS Query Helper
    private SMSQueryHelper smsQueryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: Initializing MainActivity");

        // Initialize SMS Query Helper
        smsQueryHelper = new SMSQueryHelper(this);

        // Initialize UI components
        initializeUI();

        // Request READ_SMS permission if not already granted
        requestPermissions();

        // Set up button click listeners
        setupListeners();

        // Display initial feedback
        updateFeedback("Ready to scan. Please check that permissions are granted.");
        Log.d(TAG, "onCreate: MainActivity initialization complete");
    }

    /**
     * Finds and assigns all UI view references used by the activity.
     *
     * Initializes fields for keyword input, date buttons, scan/send/reset buttons,
     * progress indicator, and feedback text by locating their views in the layout.
     */
    private void initializeUI() {
        keywordInput = findViewById(R.id.keywordInput);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        startScanButton = findViewById(R.id.startScanButton);
        sendButton = findViewById(R.id.sendButton);
        resetButton = findViewById(R.id.resetButton);
        progressBar = findViewById(R.id.progressBar);
        feedbackText = findViewById(R.id.feedbackText);
    }

    /**
     * Binds click handlers for the activity's buttons.
     *
     * - Tapping the start date button opens a date picker to choose the start date.
     * - Tapping the end date button opens a date picker to choose the end date.
     * - Tapping the start scan button begins an SMS scan.
     * - Tapping the reset button clears the stored last-sync timestamp and resets selected dates.
     * - Tapping the send button shows a short toast indicating the feature is not implemented.
     */
    private void setupListeners() {
        startDateButton.setOnClickListener(v -> showDatePicker(true));

        endDateButton.setOnClickListener(v -> showDatePicker(false));

        startScanButton.setOnClickListener(v -> onStartScanClicked());

        resetButton.setOnClickListener(v -> onResetClicked());

        sendButton.setOnClickListener(v ->
            Toast.makeText(this, "Send functionality not implemented yet", Toast.LENGTH_SHORT).show()
        );
    }

    /**
     * Clears the persisted "last sync" timestamp and updates the UI to reflect the reset state.
     *
     * Attempts to remove the stored last sync timestamp from preferences, shows a Toast indicating
     * success or failure, and updates the feedback text. On success it clears any selected start/end
     * dates and resets the start/end date button labels. If an exception occurs, logs the error and
     * updates the feedback with the exception message.
     */
    private void onResetClicked() {
        Log.d(TAG, "onResetClicked: Resetting sync timestamp");
        try {
            boolean success = PreferencesManager.getInstance(this).clearLastSyncTimestamp();
            String message = success ? "Sync timestamp reset successfully" : "Failed to reset sync timestamp";

            // Showing a Toast (LENGTH_LONG as a "medium" duration)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();

            if (success) {
                updateFeedback("✅ Sync timestamp cleared. The next scan will include all available messages.");
                // Also clear selected dates to reflect a fresh state
                selectedStartDate = null;
                selectedEndDate = null;
                startDateButton.setText(R.string.SELECT_START_DATE);
                startDateButton.setContentDescription(getString(R.string.SELECT_START_DATE));
                endDateButton.setText(R.string.SELECT_END_DATE);
                endDateButton.setContentDescription(getString(R.string.SELECT_END_DATE));
            } else {
                updateFeedback("❌ Failed to reset sync timestamp in preferences.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resetting sync timestamp", e);
            updateFeedback("❌ Error resetting sync timestamp: " + e.getMessage());
        }
    }

    /**
     * Opens a date picker and applies the chosen date to either the start or end selection.
     *
     * When a date is chosen the method sets the corresponding epoch timestamp:
     * - for the start date, the timestamp is set to 00:00:00.000 of the selected day;
     * - for the end date, the timestamp is set to 23:59:59.999 of the selected day.
     * The associated button text is updated to display the formatted selected date.
     *
     * @param isStartDate true to edit the start date, false to edit the end date
     */
    private void showDatePicker(final boolean isStartDate) {
        Log.d(TAG, "showDatePicker: Showing date picker for " + (isStartDate ? "start" : "end") + " date");

        final Calendar calendar = Calendar.getInstance();

        Long selectedDate = isStartDate ? selectedStartDate : selectedEndDate;
        if (selectedDate != null) {
            calendar.setTimeInMillis(selectedDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();

                    if (isStartDate) {
                        // Start of the day: 00:00:00.000
                        selectedCal.set(year, month, dayOfMonth, 0, 0, 0);
                        selectedCal.set(Calendar.MILLISECOND, 0);
                        selectedStartDate = selectedCal.getTimeInMillis();
                        String formattedDate = "Start: " + formatDate(selectedStartDate);
                        startDateButton.setText(formattedDate);
                        startDateButton.setContentDescription(formattedDate);
                    } else {
                        // End of the day: 23:59:59.999
                        selectedCal.set(year, month, dayOfMonth, 23, 59, 59);
                        selectedCal.set(Calendar.MILLISECOND, 999);
                        selectedEndDate = selectedCal.getTimeInMillis();
                        String formattedDate = "End: " + formatDate(selectedEndDate);
                        endDateButton.setText(formattedDate);
                        endDateButton.setContentDescription(formattedDate);
                    }

                    Log.d(TAG, "Date selected - " + (isStartDate ? "start" : "end") + ": " +
                            formatDate(isStartDate ? selectedStartDate : selectedEndDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    /**
     * Format timestamp to readable date string
     */
    private String formatDate(long timestamp) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(timestamp));
    }

    /**
     * Initiates an SMS scan using the current keyword and date-range inputs and updates the UI with progress and results.
     *
     * <p>Checks for READ_SMS permission and aborts with a feedback message if permission is missing. While scanning,
     * shows a progress indicator, disables the Start Scan button, and displays the selected filters. On completion,
     * re-enables the button and either displays the found messages or shows an error message.</p>
     */
    private void onStartScanClicked() {
        Log.d(TAG, "onStartScanClicked: Starting SMS scan process");

        if (lacksReadSmsPermission()) {
            Log.w(TAG, "onStartScanClicked: READ_SMS permission not granted");
            updateFeedback("❌ READ_SMS permission required. Please grant it in app settings.");
            return;
        }

        // Get user inputs
        String keywords = keywordInput.getText() != null ? keywordInput.getText().toString().trim() : "";
        // Show progress
        progressBar.setVisibility(android.view.View.VISIBLE);
        startScanButton.setEnabled(false);
        updateFeedback("🔄 Scanning SMS messages...\n\n" +
                "📝 Keywords: " + (keywords.isEmpty() ? "All messages" : keywords) + "\n" +
                "📅 Start Date: " + (selectedStartDate != null ? formatDate(selectedStartDate) : "Auto (Last sync)") + "\n" +
                "📅 End Date: " + (selectedEndDate != null ? formatDate(selectedEndDate) : "Today"));

        Log.d(TAG, "onStartScanClicked: Starting async SMS query");
        Log.d(TAG, "onStartScanClicked: Keywords: " + keywords);
        Log.d(TAG, "onStartScanClicked: Start date: " + selectedStartDate);
        Log.d(TAG, "onStartScanClicked: End date: " + selectedEndDate);

//         Start async SMS query
         smsQueryHelper.querySMSAsync(keywords, selectedStartDate, selectedEndDate, new SMSQueryHelper.SMSQueryCallback() {
             @Override
             public void onSuccess(List<SMSMessage> messages) {
                 Log.d(TAG, "SMS query success: " + messages.size() + " messages found");
                 runOnUiThread(() -> {
                     progressBar.setVisibility(android.view.View.GONE);
                     startScanButton.setEnabled(true);
                     displaySMSResults(messages);
                 });
             }

             @Override
             public void onError(Exception error) {
                 Log.e(TAG, "SMS query error", error);
                 runOnUiThread(() -> {
                     progressBar.setVisibility(android.view.View.GONE);
                     startScanButton.setEnabled(true);
                     updateFeedback("❌ Error scanning SMS: " + error.getMessage());
                 });
             }
         });
    }

    /**
     * Update feedback message displayed to user
     */
    private void updateFeedback(String message) {
        feedbackText.setText(message);
    }

    /**
     * Check if READ_SMS permission is NOT granted (lacks permission)
     */
    private boolean lacksReadSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request READ_SMS permission at runtime (for Android 6.0+)
     */
    private void requestPermissions() {
        boolean needsPermission = lacksReadSmsPermission();
        Log.d(TAG, "requestPermissions: Permission needed: " + needsPermission);

        if (needsPermission) {
            Log.d(TAG, "requestPermissions: Requesting READ_SMS permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            Log.d(TAG, "requestPermissions: READ_SMS permission already granted");
            updateFeedback("✅ Permissions granted. Ready to scan.");
        }
    }

    /**
     * Handle permission request callback
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: Permission result received, requestCode: " + requestCode);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Log.d(TAG, "onRequestPermissionsResult: Permission granted: " + granted);

            if (granted) {
                updateFeedback("✅ READ_SMS permission granted. Ready to scan.");
                startScanButton.setEnabled(true);
            } else {
                updateFeedback("❌ READ_SMS permission denied. Cannot proceed with scanning. Please grant permission in app settings.");
                startScanButton.setEnabled(false);
            }
        }
    }

    /**
     * Display SMS query results as JSON in the feedback text
     */
    private void displaySMSResults(List<SMSMessage> messages) {
        Log.d(TAG, "displaySMSResults: Displaying " + messages.size() + " SMS messages");

        if (messages.isEmpty()) {
            updateFeedback("✅ Scan complete!\n\n📊 Results: 0 SMS messages found matching criteria.");
            Log.d(TAG, "displaySMSResults: No messages found");
            return;
        }

        // Convert to JSON
        JSONArray jsonArray = new JSONArray();
        for (SMSMessage message : messages) {
            jsonArray.put(message.toJson());
        }

        String jsonString;
        try {
            jsonString = jsonArray.toString(2); // Pretty print with 2-space indentation
        } catch (Exception e) {
            Log.e(TAG, "Error formatting JSON", e);
            jsonString = jsonArray.toString(); // Fallback to compact format
        }

        // Create summary
        String summary = String.format("✅ Scan complete!\n\n📊 Results: %d SMS messages found\n\n📄 JSON Output:\n%s",
                messages.size(), jsonString);

        updateFeedback(summary);
        Log.d(TAG, "displaySMSResults: JSON displayed with " + messages.size() + " messages");
    }
}