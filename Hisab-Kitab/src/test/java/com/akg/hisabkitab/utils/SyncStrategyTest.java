package com.akg.hisabkitab.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.util.Pair;

import static org.junit.Assert.*;

/**
 * Unit tests for SyncStrategy class
 * Tests date range determination logic
 */
@RunWith(RobolectricTestRunner.class)
public class SyncStrategyTest {

    private Context context;
    private PreferencesManager prefManager;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        prefManager = PreferencesManager.getInstance(context);
        prefManager.clearLastSyncTimestamp(); // Clean state
    }

    // ==================== getDateRange() Tests ====================

    /**
     * Test 1: Manual range with both dates provided
     */
    @Test
    public void testGetDateRangeManual() {
        long startDate = 1000L;
        long endDate = 2000L;

        Pair<Long, Long> result = SyncStrategy.getDateRange(context, startDate, endDate);

        assertNotNull("Should return Pair", result);
        assertEquals("Start date should match", startDate, (long) result.first);
        assertEquals("End date should match", endDate, (long) result.second);
    }

    /**
     * Test 2: Incremental sync with no previous sync (default 30 days)
     */
    @Test
    public void testGetDateRangeIncrementalFirstTime() {
        long currentTime = System.currentTimeMillis();

        Pair<Long, Long> result = SyncStrategy.getDateRange(context, null, null);

        assertNotNull("Should return Pair", result);
        long startDate = result.first;
        long endDate = result.second;

        // Start date should be approximately 30 days ago
        long thirtyDaysMs = 30 * 24 * 60 * 60 * 1000L;
        long expectedStart = currentTime - thirtyDaysMs;

        assertTrue("Start date should be in the past", startDate < currentTime);
        assertTrue("Start date should be approximately 30 days ago", 
                Math.abs(startDate - expectedStart) < 1000); // Within 1 second
        assertTrue("End date should be approximately now", Math.abs(endDate - currentTime) < 1000);
    }

    /**
     * Test 3: Incremental sync with previous sync timestamp
     */
    @Test
    public void testGetDateRangeIncrementalWithPreviousSync() {
        long previousSyncTime = System.currentTimeMillis() - 3600000; // 1 hour ago
        prefManager.setLastSyncTimestamp(previousSyncTime);

        Pair<Long, Long> result = SyncStrategy.getDateRange(context, null, null);

        assertNotNull("Should return Pair", result);
        assertEquals("Start date should be previous sync time", previousSyncTime, (long) result.first);
        
        // End date should be approximately now
        long currentTime = System.currentTimeMillis();
        assertTrue("End date should be approximately now", Math.abs(result.second - currentTime) < 1000);
    }

    /**
     * Test 4: Manual range overrides incremental sync
     */
    @Test
    public void testGetDateRangeManualOverridesIncremental() {
        long previousSyncTime = System.currentTimeMillis() - 3600000;
        prefManager.setLastSyncTimestamp(previousSyncTime);

        long manualStart = 5000L;
        long manualEnd = 10000L;

        Pair<Long, Long> result = SyncStrategy.getDateRange(context, manualStart, manualEnd);

        assertNotNull("Should return Pair", result);
        assertEquals("Should use manual start date", manualStart, (long) result.first);
        assertEquals("Should use manual end date", manualEnd, (long) result.second);
    }

    /**
     * Test 5: Only start date provided (should use incremental)
     */
    @Test
    public void testGetDateRangeOnlyStartDate() {
        long startDate = 1000L;

        Pair<Long, Long> result = SyncStrategy.getDateRange(context, startDate, null);

        assertNotNull("Should return Pair", result);
        // Should use incremental sync (default 30 days)
        long currentTime = System.currentTimeMillis();
        long thirtyDaysMs = 30 * 24 * 60 * 60 * 1000L;
        long expectedStart = currentTime - thirtyDaysMs;

        assertTrue("Should use incremental sync", Math.abs(result.first - expectedStart) < 1000);
    }

    /**
     * Test 6: Only end date provided (should use incremental)
     */
    @Test
    public void testGetDateRangeOnlyEndDate() {
        long endDate = 5000L;

        Pair<Long, Long> result = SyncStrategy.getDateRange(context, null, endDate);

        assertNotNull("Should return Pair", result);
        // Should use incremental sync
        long currentTime = System.currentTimeMillis();
        long thirtyDaysMs = 30 * 24 * 60 * 60 * 1000L;
        long expectedStart = currentTime - thirtyDaysMs;

        assertTrue("Should use incremental sync", Math.abs(result.first - expectedStart) < 1000);
    }

    // ==================== isIncrementalSync() Tests ====================

    /**
     * Test 7: isIncrementalSync returns true when dates are null
     */
    @Test
    public void testIsIncrementalSyncTrue() {
        boolean result = SyncStrategy.isIncrementalSync(null, null);
        assertTrue("Should be incremental sync", result);
    }

    /**
     * Test 8: isIncrementalSync returns false when both dates provided
     */
    @Test
    public void testIsIncrementalSyncFalse() {
        boolean result = SyncStrategy.isIncrementalSync(1000L, 2000L);
        assertFalse("Should be manual sync (not incremental)", result);
    }

    /**
     * Test 9: isIncrementalSync with only start date
     */
    @Test
    public void testIsIncrementalSyncPartialStart() {
        boolean result = SyncStrategy.isIncrementalSync(1000L, null);
        assertTrue("Should be incremental sync when end is null", result);
    }

    /**
     * Test 10: isIncrementalSync with only end date
     */
    @Test
    public void testIsIncrementalSyncPartialEnd() {
        boolean result = SyncStrategy.isIncrementalSync(null, 2000L);
        assertTrue("Should be incremental sync when start is null", result);
    }

    // ==================== isValidDateRange() Tests ====================

    /**
     * Test 11: isValidDateRange with valid range
     */
    @Test
    public void testIsValidDateRangeValid() {
        boolean result = SyncStrategy.isValidDateRange(1000, 2000);
        assertTrue("Should be valid range", result);
    }

    /**
     * Test 12: isValidDateRange with equal dates
     */
    @Test
    public void testIsValidDateRangeEqual() {
        boolean result = SyncStrategy.isValidDateRange(1000, 1000);
        assertTrue("Should be valid (dates can be equal)", result);
    }

    /**
     * Test 13: isValidDateRange with inverted dates
     */
    @Test
    public void testIsValidDateRangeInvalid() {
        boolean result = SyncStrategy.isValidDateRange(2000, 1000);
        assertFalse("Should be invalid (startDate > endDate)", result);
    }

    /**
     * Test 14: isValidDateRange with large numbers
     */
    @Test
    public void testIsValidDateRangeLargeNumbers() {
        long start = System.currentTimeMillis() - 1000000000L;
        long end = System.currentTimeMillis();
        
        boolean result = SyncStrategy.isValidDateRange(start, end);
        assertTrue("Should handle large numbers", result);
    }
}

