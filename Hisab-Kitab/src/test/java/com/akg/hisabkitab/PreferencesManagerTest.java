package com.akg.hisabkitab;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import static org.junit.Assert.*;

import com.akg.hisabkitab.utils.PreferencesManager;

/**
 * Unit tests for PreferencesManager class
 * Tests all core functionality and edge cases
 */
@RunWith(RobolectricTestRunner.class)
public class PreferencesManagerTest {

    private PreferencesManager preferencesManager;
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        // Reset singleton for each test
        preferencesManager = PreferencesManager.getInstance(context);
        preferencesManager.clearLastSyncTimestamp(); // Clean state
    }

    /**
     * Test 1: getLastSyncTimestamp returns null on first call (no previous sync)
     */
    @Test
    public void testGetLastSyncTimestampReturnsNullOnFirstCall() {
        Long result = preferencesManager.getLastSyncTimestamp();
        assertNull("First call should return null for no previous sync", result);
    }

    /**
     * Test 2: setLastSyncTimestamp saves and retrieves correctly
     */
    @Test
    public void testSetAndGetLastSyncTimestamp() {
        long testTimestamp = System.currentTimeMillis();
        boolean saveSuccess = preferencesManager.setLastSyncTimestamp(testTimestamp);

        assertTrue("Save operation should succeed", saveSuccess);
        Long retrievedTimestamp = preferencesManager.getLastSyncTimestamp();
        assertNotNull("Retrieved timestamp should not be null", retrievedTimestamp);
        assertEquals("Timestamp should match saved value", testTimestamp, (long) retrievedTimestamp);
    }

    /**
     * Test 3: setLastSyncTimestamp rejects invalid (zero or negative) timestamps
     */
    @Test
    public void testSetLastSyncTimestampRejectsInvalidTimestamps() {
        // Test with zero
        boolean resultZero = preferencesManager.setLastSyncTimestamp(0);
        assertFalse("Zero timestamp should be rejected", resultZero);

        // Test with negative
        boolean resultNegative = preferencesManager.setLastSyncTimestamp(-1);
        assertFalse("Negative timestamp should be rejected", resultNegative);

        // Verify nothing was saved
        assertNull("No timestamp should be saved after rejection", preferencesManager.getLastSyncTimestamp());
    }

    /**
     * Test 4: clearLastSyncTimestamp removes the stored timestamp
     */
    @Test
    public void testClearLastSyncTimestamp() {
        long testTimestamp = System.currentTimeMillis();
        preferencesManager.setLastSyncTimestamp(testTimestamp);
        assertNotNull("Timestamp should be saved", preferencesManager.getLastSyncTimestamp());

        boolean clearSuccess = preferencesManager.clearLastSyncTimestamp();
        assertTrue("Clear operation should succeed", clearSuccess);
        assertNull("Timestamp should be null after clear", preferencesManager.getLastSyncTimestamp());
    }

    /**
     * Test 5: hasPreviousSync returns false initially
     */
    @Test
    public void testHasPreviousSyncReturnsFalseInitially() {
        boolean result = preferencesManager.hasPreviousSync();
        assertFalse("Should return false when no sync timestamp exists", result);
    }

    /**
     * Test 6: hasPreviousSync returns true after saving timestamp
     */
    @Test
    public void testHasPreviousSyncReturnsTrueAfterSave() {
        long testTimestamp = System.currentTimeMillis();
        preferencesManager.setLastSyncTimestamp(testTimestamp);

        boolean result = preferencesManager.hasPreviousSync();
        assertTrue("Should return true after saving timestamp", result);
    }

    /**
     * Test 7: getMillisecondsSinceLastSync returns null when no previous sync
     */
    @Test
    public void testGetMillisecondsSinceLastSyncReturnsNullWhenNoSync() {
        Long result = preferencesManager.getMillisecondsSinceLastSync();
        assertNull("Should return null when no previous sync exists", result);
    }

    /**
     * Test 8: getMillisecondsSinceLastSync returns positive value after sync
     */
    @Test
    public void testGetMillisecondsSinceLastSyncReturnsPositiveValue() {
        long testTimestamp = System.currentTimeMillis() - 5000; // 5 seconds ago
        preferencesManager.setLastSyncTimestamp(testTimestamp);

        Long result = preferencesManager.getMillisecondsSinceLastSync();
        assertNotNull("Should return milliseconds since sync", result);
        assertTrue("Should return positive value", result > 0);
        assertTrue("Should be approximately 5000ms or greater", result >= 5000);
    }

    /**
     * Test 9: Singleton instance returns same object on multiple calls
     */
    @Test
    public void testSingletonInstanceReturnsSameObject() {
        PreferencesManager instance1 = PreferencesManager.getInstance(context);
        PreferencesManager instance2 = PreferencesManager.getInstance(context);

        assertSame("Singleton should return same instance", instance1, instance2);
    }

    /**
     * Test 10: Data persists across PreferencesManager instances
     */
    @Test
    public void testDataPersistsAcrossInstances() {
        long testTimestamp = System.currentTimeMillis();
        
        // Save with first instance
        PreferencesManager instance1 = PreferencesManager.getInstance(context);
        instance1.setLastSyncTimestamp(testTimestamp);

        // Retrieve with second instance reference
        PreferencesManager instance2 = PreferencesManager.getInstance(context);
        Long retrievedTimestamp = instance2.getLastSyncTimestamp();

        assertEquals("Data should persist across instances", testTimestamp, (long) retrievedTimestamp);
    }

    /**
     * Test 11: Multiple saves overwrite previous values
     */
    @Test
    public void testMultipleSavesOverwritePreviousValues() {
        long timestamp1 = 1000000L;
        long timestamp2 = 2000000L;

        preferencesManager.setLastSyncTimestamp(timestamp1);
        assertEquals("First timestamp should be saved", timestamp1, (long) preferencesManager.getLastSyncTimestamp());

        preferencesManager.setLastSyncTimestamp(timestamp2);
        assertEquals("Second timestamp should overwrite first", timestamp2, (long) preferencesManager.getLastSyncTimestamp());
    }

    /**
     * Test 12: Large timestamp values are handled correctly
     */
    @Test
    public void testLargeTimestampValues() {
        long largeTimestamp = Long.MAX_VALUE - 1000;
        boolean saveSuccess = preferencesManager.setLastSyncTimestamp(largeTimestamp);

        assertTrue("Should save large timestamp", saveSuccess);
        Long retrieved = preferencesManager.getLastSyncTimestamp();
        assertNotNull("Should retrieve large timestamp", retrieved);
        assertEquals("Large timestamp should match", largeTimestamp, (long) retrieved);
    }
}

