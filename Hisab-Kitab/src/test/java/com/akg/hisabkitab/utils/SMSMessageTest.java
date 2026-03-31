package com.akg.hisabkitab.utils;

import org.junit.Test;

import static org.junit.Assert.*;

import com.akg.hisabkitab.models.SMSMessage;

/**
 * Unit tests for SMSMessage data class
 */
public class SMSMessageTest {

    /**
     * Test 1: Create SMSMessage with valid data
     */
    @Test
    public void testSMSMessageCreation() {
        long timestamp = 1234567890L;
        String sender = "VM-HDFCBK";
        String body = "Rs.500 debited";

        SMSMessage message = new SMSMessage(timestamp, sender, body);

        assertEquals("Timestamp should match", timestamp, message.timestamp);
        assertEquals("Sender should match", sender, message.sender);
        assertEquals("Body should match", body, message.body);
    }

    /**
     * Test 2: SMSMessage with null sender
     */
    @Test
    public void testSMSMessageNullSender() {
        SMSMessage message = new SMSMessage(1000, null, "Test body");
        assertEquals("Null sender should become empty string", "", message.sender);
        assertEquals("Body should be preserved", "Test body", message.body);
    }

    /**
     * Test 3: SMSMessage with null body
     */
    @Test
    public void testSMSMessageNullBody() {
        SMSMessage message = new SMSMessage(1000, "VM-HDFC", null);
        assertEquals("Sender should be preserved", "VM-HDFC", message.sender);
        assertEquals("Null body should become empty string", "", message.body);
    }

    /**
     * Test 4: SMSMessage with both null
     */
    @Test
    public void testSMSMessageBothNull() {
        SMSMessage message = new SMSMessage(1000, null, null);
        assertEquals("Null sender should become empty string", "", message.sender);
        assertEquals("Null body should become empty string", "", message.body);
    }

    /**
     * Test 5: SMSMessage equals - identical messages
     */
    @Test
    public void testSMSMessageEqualsIdentical() {
        SMSMessage msg1 = new SMSMessage(1000, "HDFC", "Test");
        SMSMessage msg2 = new SMSMessage(1000, "HDFC", "Test");

        assertEquals("Identical messages should be equal", msg1, msg2);
    }

    /**
     * Test 6: SMSMessage equals - different timestamp
     */
    @Test
    public void testSMSMessageEqualsDifferentTimestamp() {
        SMSMessage msg1 = new SMSMessage(1000, "HDFC", "Test");
        SMSMessage msg2 = new SMSMessage(2000, "HDFC", "Test");

        assertNotEquals("Different timestamps should not be equal", msg1, msg2);
    }

    /**
     * Test 7: SMSMessage equals - different sender
     */
    @Test
    public void testSMSMessageEqualsDifferentSender() {
        SMSMessage msg1 = new SMSMessage(1000, "HDFC", "Test");
        SMSMessage msg2 = new SMSMessage(1000, "SBI", "Test");

        assertNotEquals("Different senders should not be equal", msg1, msg2);
    }

    /**
     * Test 8: SMSMessage equals - different body
     */
    @Test
    public void testSMSMessageEqualsDifferentBody() {
        SMSMessage msg1 = new SMSMessage(1000, "HDFC", "Test1");
        SMSMessage msg2 = new SMSMessage(1000, "HDFC", "Test2");

        assertNotEquals("Different bodies should not be equal", msg1, msg2);
    }

    /**
     * Test 9: SMSMessage equals - same object
     */
    @Test
    public void testSMSMessageEqualsSameObject() {
        SMSMessage msg = new SMSMessage(1000, "HDFC", "Test");
        assertEquals("Object should equal itself", msg, msg);
    }

    /**
     * Test 10: SMSMessage equals - null comparison
     */
    @Test
    public void testSMSMessageEqualsNull() {
        SMSMessage msg = new SMSMessage(1000, "HDFC", "Test");
        assertNotEquals("Should not equal null", msg, null);
    }

    /**
     * Test 11: SMSMessage equals - different class
     */
    @Test
    public void testSMSMessageEqualsDifferentClass() {
        SMSMessage msg = new SMSMessage(1000, "HDFC", "Test");
        assertNotEquals("Should not equal different class", msg, "not an SMSMessage");
    }

    /**
     * Test 12: SMSMessage hashCode - identical messages
     */
    @Test
    public void testSMSMessageHashCodeIdentical() {
        SMSMessage msg1 = new SMSMessage(1000, "HDFC", "Test");
        SMSMessage msg2 = new SMSMessage(1000, "HDFC", "Test");

        assertEquals("Identical messages should have same hashCode", 
                msg1.hashCode(), msg2.hashCode());
    }

    /**
     * Test 13: SMSMessage hashCode - different messages
     */
    @Test
    public void testSMSMessageHashCodeDifferent() {
        SMSMessage msg1 = new SMSMessage(1000, "HDFC", "Test");
        SMSMessage msg2 = new SMSMessage(2000, "SBI", "Other");

        // Note: different objects can have same hashCode, but usually won't
        // This test just verifies the method doesn't crash
        assertNotNull("HashCode should exist", msg1.hashCode());
        assertNotNull("HashCode should exist", msg2.hashCode());
    }

    /**
     * Test 14: SMSMessage toString
     */
    @Test
    public void testSMSMessageToString() {
        SMSMessage message = new SMSMessage(1000, "HDFC", "Test");
        String toString = message.toString();

        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain timestamp", toString.contains("1000"));
        assertTrue("toString should contain sender", toString.contains("HDFC"));
        assertTrue("toString should contain body", toString.contains("Test"));
    }

    /**
     * Test 15: SMSMessage with zero timestamp
     */
    @Test
    public void testSMSMessageZeroTimestamp() {
        SMSMessage message = new SMSMessage(0, "HDFC", "Test");
        assertEquals("Should handle zero timestamp", 0, message.timestamp);
    }

    /**
     * Test 16: SMSMessage with negative timestamp
     */
    @Test
    public void testSMSMessageNegativeTimestamp() {
        SMSMessage message = new SMSMessage(-1000, "HDFC", "Test");
        assertEquals("Should handle negative timestamp", -1000, message.timestamp);
    }

    /**
     * Test 17: SMSMessage with large timestamp
     */
    @Test
    public void testSMSMessageLargeTimestamp() {
        long largeTimestamp = System.currentTimeMillis();
        SMSMessage message = new SMSMessage(largeTimestamp, "HDFC", "Test");
        assertEquals("Should handle large timestamp", largeTimestamp, message.timestamp);
    }

    /**
     * Test 18: SMSMessage with empty strings
     */
    @Test
    public void testSMSMessageEmptyStrings() {
        SMSMessage message = new SMSMessage(1000, "", "");
        assertEquals("Should handle empty sender", "", message.sender);
        assertEquals("Should handle empty body", "", message.body);
    }

    /**
     * Test 19: SMSMessage with long content
     */
    @Test
    public void testSMSMessageLongContent() {
        String longBody = "a".repeat(1000);
        SMSMessage message = new SMSMessage(1000, "HDFC", longBody);
        assertEquals("Should handle long body", longBody, message.body);
        assertEquals("Should have correct length", 1000, message.body.length());
    }

    /**
     * Test 20: SMSMessage with special characters
     */
    @Test
    public void testSMSMessageSpecialCharacters() {
        String specialBody = "Rs.500 debited from a/c **1234 on 30-03-26 to UPI/merchant.";
        SMSMessage message = new SMSMessage(1000, "VM-HDFCBK", specialBody);
        assertEquals("Should handle special characters", specialBody, message.body);
    }
}

