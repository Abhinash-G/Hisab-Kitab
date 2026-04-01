package com.akg.hisabkitab.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.util.Pair;

import com.akg.hisabkitab.models.SMSMessage;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for SMSQueryHelper class
 * Tests all core functionality and edge cases
 */
@RunWith(RobolectricTestRunner.class)
public class SMSQueryHelperTest {

    private SMSQueryHelper smsQueryHelper;
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication().getApplicationContext();
        smsQueryHelper = new SMSQueryHelper(context);
    }

    // ==================== parseKeywords() Tests ====================

    /**
     * Test 1: parseKeywords handles single keyword
     */
    @Test
    public void testParseKeywordsSingle() {
        List<String> result = smsQueryHelper.parseKeywords("HDFCBK");
        assertEquals("Should parse single keyword", 1, result.size());
        assertEquals("Keyword should match", "HDFCBK", result.get(0));
    }

    /**
     * Test 2: parseKeywords handles multiple keywords
     */
    @Test
    public void testParseKeywordsMultiple() {
        List<String> result = smsQueryHelper.parseKeywords("HDFCBK, SBI, ICICI");
        assertEquals("Should parse 3 keywords", 3, result.size());
        assertEquals("First keyword", "HDFCBK", result.get(0));
        assertEquals("Second keyword", "SBI", result.get(1));
        assertEquals("Third keyword", "ICICI", result.get(2));
    }

    /**
     * Test 3: parseKeywords handles null input
     */
    @Test
    public void testParseKeywordsNull() {
        List<String> result = smsQueryHelper.parseKeywords(null);
        assertEquals("Should return empty list for null", 0, result.size());
    }

    /**
     * Test 4: parseKeywords handles empty string
     */
    @Test
    public void testParseKeywordsEmpty() {
        List<String> result = smsQueryHelper.parseKeywords("");
        assertEquals("Should return empty list for empty string", 0, result.size());
    }

    /**
     * Test 5: parseKeywords handles whitespace
     */
    @Test
    public void testParseKeywordsWithWhitespace() {
        List<String> result = smsQueryHelper.parseKeywords("  HDFCBK  ,  SBI  ");
        assertEquals("Should parse 2 keywords", 2, result.size());
        assertEquals("Should trim whitespace", "HDFCBK", result.get(0));
        assertEquals("Should trim whitespace", "SBI", result.get(1));
    }

    // ==================== buildKeywordWhereClause() Tests ====================

    /**
     * Test 6: buildKeywordWhereClause with single keyword
     */
    @Test
    public void testBuildKeywordWhereClauseSingle() {
        List<String> keywords = Arrays.asList("HDFCBK");
        String result = smsQueryHelper.buildKeywordWhereClause(keywords);
        assertEquals("Should build WHERE clause", "(address LIKE ?)", result);
    }

    /**
     * Test 7: buildKeywordWhereClause with multiple keywords
     */
    @Test
    public void testBuildKeywordWhereClauseMultiple() {
        List<String> keywords = Arrays.asList("HDFCBK", "SBI");
        String result = smsQueryHelper.buildKeywordWhereClause(keywords);
        assertTrue("Should contain OR logic", result.contains(" OR "));
        assertTrue("Should have 2 LIKE clauses", result.split("LIKE").length == 3); // LIKE appears twice
    }

    /**
     * Test 8: buildKeywordWhereClause with empty list
     */
    @Test
    public void testBuildKeywordWhereClauseEmpty() {
        List<String> keywords = Arrays.asList();
        String result = smsQueryHelper.buildKeywordWhereClause(keywords);
        assertEquals("Should return empty string", "", result);
    }

    // ==================== buildDateWhereClause() Tests ====================

    /**
     * Test 9: buildDateWhereClause returns correct format
     */
    @Test
    public void testBuildDateWhereClause() {
        String result = smsQueryHelper.buildDateWhereClause(1000, 2000);
        assertEquals("Should build date WHERE clause", "(date >= ? AND date <= ?)", result);
    }

    // ==================== buildSelection() Tests ====================

    /**
     * Test 10: buildSelection with keywords and dates
     */
    @Test
    public void testBuildSelectionWithKeywordsAndDates() {
        List<String> keywords = Arrays.asList("HDFCBK", "SBI");
        Pair<String, String[]> result = smsQueryHelper.buildSelection(keywords, 1000, 2000);

        assertNotNull("Should return Pair", result);
        String whereClause = result.first;
        String[] args = result.second;

        assertTrue("WHERE clause should contain keywords", whereClause.contains("address LIKE"));
        assertTrue("WHERE clause should contain dates", whereClause.contains("date"));
        assertEquals("Should have 4 args (2 keywords + 2 dates)", 4, args.length);
    }

    /**
     * Test 11: buildSelection with only dates (no keywords)
     */
    @Test
    public void testBuildSelectionOnlyDates() {
        List<String> keywords = Arrays.asList();
        Pair<String, String[]> result = smsQueryHelper.buildSelection(keywords, 1000, 2000);

        assertNotNull("Should return Pair", result);
        String whereClause = result.first;
        String[] args = result.second;

        assertTrue("WHERE clause should contain dates", whereClause.contains("date"));
        assertEquals("Should have 2 args (only dates)", 2, args.length);
    }

    // ==================== SMSMessage Tests ====================

    /**
     * Test 12: SMSMessage creation and fields
     */
    @Test
    public void testSMSMessageCreation() {
        SMSMessage message = new SMSMessage(1234567890L, "VM-HDFCBK", "Test message");
        assertEquals("Timestamp should match", 1234567890L, message.timestamp);
        assertEquals("Sender should match", "VM-HDFCBK", message.sender);
        assertEquals("Body should match", "Test message", message.body);
    }

    /**
     * Test 13: SMSMessage handles null values
     */
    @Test
    public void testSMSMessageNullHandling() {
        SMSMessage message = new SMSMessage(1000, null, null);
        assertEquals("Null sender should become empty string", "", message.sender);
        assertEquals("Null body should become empty string", "", message.body);
    }

    /**
     * Test 14: SMSMessage equals and hashCode
     */
    @Test
    public void testSMSMessageEqualsHashCode() {
        SMSMessage msg1 = new SMSMessage(1000, "VM-HDFC", "Test");
        SMSMessage msg2 = new SMSMessage(1000, "VM-HDFC", "Test");
        SMSMessage msg3 = new SMSMessage(2000, "VM-HDFC", "Test");

        assertEquals("Equal messages should be equal", msg1, msg2);
        assertNotEquals("Different messages should not be equal", msg1, msg3);
        assertEquals("Equal messages should have same hashCode", msg1.hashCode(), msg2.hashCode());
    }

    // ==================== Integration Tests ====================

    /**
     * Test 15: Full query simulation (without real SMS data)
     */
    @Test
    public void testQuerySMSIntegration() {
        // This test verifies the query doesn't crash, but won't return real SMS
        // (ContentProvider mocking would require more setup)
        List<SMSMessage> messages = smsQueryHelper.querySMS("HDFCBK", null, null);
        assertNotNull("Should return list (may be empty)", messages);
    }

    /**
     * Test 16: Keyword parsing and WHERE clause building flow
     */
    @Test
    public void testKeywordParsingFlow() {
        String keywordString = "HDFCBK, SBI, ICICI";
        List<String> keywords = smsQueryHelper.parseKeywords(keywordString);
        assertEquals("Should parse 3 keywords", 3, keywords.size());

        String whereClause = smsQueryHelper.buildKeywordWhereClause(keywords);
        assertTrue("WHERE clause should contain all keywords", whereClause.contains("LIKE"));

        Pair<String, String[]> selection = smsQueryHelper.buildSelection(keywords, 1000, 2000);
        assertEquals("Should have correct args", 5, selection.second.length); // 3 keywords + 2 dates
    }
}

