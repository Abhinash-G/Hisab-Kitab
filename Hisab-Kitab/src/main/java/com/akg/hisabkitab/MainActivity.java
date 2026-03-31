package com.akg.hisabkitab;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private EditText keywordInput;
    private Button startDateButton;
    private Button endDateButton;
    private Button startScanButton;
    private ProgressBar progressBar;
    private TextView feedbackText;

    // Date variables (epoch milliseconds)
    private Long selectedStartDate = null;
    private Long selectedEndDate = null;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeUI();

        // Request READ_SMS permission if not already granted
        requestPermissions();

        // Set up button click listeners
        setupListeners();

        // Display initial feedback
        updateFeedback("Ready to scan. Please check that permissions are granted.");
    }

    private void initializeUI() {
        keywordInput = findViewById(R.id.keywordInput);
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        startScanButton = findViewById(R.id.startScanButton);
        progressBar = findViewById(R.id.progressBar);
        feedbackText = findViewById(R.id.feedbackText);
    }

    private void setupListeners() {
        startDateButton.setOnClickListener(v -> showDatePicker(true));

        endDateButton.setOnClickListener(v -> showDatePicker(false));

        startScanButton.setOnClickListener(v -> onStartScanClicked());
    }

    private void showDatePicker(final boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();

        Long selectedDate = isStartDate ? selectedStartDate : selectedEndDate;
        if (selectedDate != null) {
            calendar.setTimeInMillis(selectedDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, dayOfMonth, 0, 0, 0);
                    selectedCal.set(Calendar.MILLISECOND, 0);
                    long timestamp = selectedCal.getTimeInMillis();

                    if (isStartDate) {
                        selectedStartDate = timestamp;
                        startDateButton.setText("Start: " + formatDate(timestamp));
                    } else {
                        selectedEndDate = timestamp;
                        endDateButton.setText("End: " + formatDate(timestamp));
                    }
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
     * Handle Start Scan button click
     */
    private void onStartScanClicked() {
        if (hasReadSmsPermission()) {
            updateFeedback("❌ READ_SMS permission required. Please grant it in app settings.");
            return;
        }

        String keywords = keywordInput.getText() != null ? keywordInput.getText().toString().trim() : "";
        String start = selectedStartDate != null ? formatDate(selectedStartDate) : "Auto (Last sync)";
        String end = selectedEndDate != null ? formatDate(selectedEndDate) : "Today";

        updateFeedback("Ready to scan with:\n" +
                "📝 Keywords: " + (keywords.isEmpty() ? "All messages" : keywords) + "\n" +
                "📅 Start Date: " + start + "\n" +
                "📅 End Date: " + end + "\n\nNote: Full implementation will proceed in Step 4-5");
    }

    /**
     * Update feedback message displayed to user
     */
    private void updateFeedback(String message) {
        feedbackText.setText(message);
    }

    /**
     * Check if READ_SMS permission is granted
     */
    private boolean hasReadSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request READ_SMS permission at runtime (for Android 6.0+)
     */
    private void requestPermissions() {
        if (hasReadSmsPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_REQUEST_CODE);
        } else {
            updateFeedback("✅ Permissions granted. Ready to scan.");
        }
    }

    /**
     * Handle permission request callback
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateFeedback("✅ READ_SMS permission granted. Ready to scan.");
                startScanButton.setEnabled(true);
            } else {
                updateFeedback("❌ READ_SMS permission denied. Cannot proceed with scanning. Please grant permission in app settings.");
                startScanButton.setEnabled(false);
            }
        }
    }
}