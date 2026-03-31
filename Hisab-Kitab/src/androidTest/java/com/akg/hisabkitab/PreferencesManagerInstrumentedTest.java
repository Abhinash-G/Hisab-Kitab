package com.akg.hisabkitab;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.akg.hisabkitab.utils.PreferencesManager;

import static org.junit.Assert.*;

/**
 * Instrumented integration test for PreferencesManager
 * Verifies behavior on real Android device/emulator with SharedPreferences
 */
@RunWith(AndroidJUnit4.class)
public class PreferencesManagerInstrumentedTest {

    private PreferencesManager preferencesManager;
    private Context context;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        preferencesManager = PreferencesManager.getInstance(context);
        preferencesManager.clearLastSyncTimestamp(); // Clean state
    }

    /**
     * Test that PreferencesManager works on actual Android device
     */
    @Test
    public void testPreferencesManagerOnDevice() {
        long testTimestamp = System.currentTimeMillis();
        boolean saveSuccess = preferencesManager.setLastSyncTimestamp(testTimestamp);

        assertTrue("Should save timestamp", saveSuccess);
        Long retrieved = preferencesManager.getLastSyncTimestamp();
        assertNotNull("Should retrieve timestamp", retrieved);
        assertEquals("Timestamp should match", testTimestamp, (long) retrieved);
    }

    /**
     * Test that preferences are actually saved to SharedPreferences
     */
    @Test
    public void testPersistenceOnDevice() {
        long testTimestamp = System.currentTimeMillis() - 10000;
        preferencesManager.setLastSyncTimestamp(testTimestamp);

        // Get new instance to verify persistence
        PreferencesManager newInstance = PreferencesManager.getInstance(context);
        Long retrieved = newInstance.getLastSyncTimestamp();

        assertNotNull("Data should persist", retrieved);
        assertEquals("Timestamp should match after new instance", testTimestamp, (long) retrieved);
    }

    /**
     * Test context package name verification
     */
    @Test
    public void testContextPackage() {
        assertEquals("com.akg.hisabkitab", context.getPackageName());
    }
}

