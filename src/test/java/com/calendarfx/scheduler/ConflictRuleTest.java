package com.calendarfx.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConflictRuleTest {

    private ConflictRule rule;

    @BeforeEach
    void setUp() {
        rule = new ConflictRule(
                ConflictRule.FieldType.WORKING_HOURS,
                ConflictRule.Operator.GREATER,
                "40",
                true
        );
    }

    @Test
    void testConstructor() {
        assertEquals(ConflictRule.FieldType.WORKING_HOURS, rule.getField());
        assertEquals(ConflictRule.Operator.GREATER, rule.getOperator());
        assertEquals("40", rule.getValue());
        assertTrue(rule.isActive());
    }

    @Test
    void testGetField() {
        assertEquals(ConflictRule.FieldType.WORKING_HOURS, rule.getField());
    }

    @Test
    void testSetField() {
        rule.setField(ConflictRule.FieldType.NAME);
        assertEquals(ConflictRule.FieldType.NAME, rule.getField());
    }

    @Test
    void testGetOperator() {
        assertEquals(ConflictRule.Operator.GREATER, rule.getOperator());
    }

    @Test
    void testSetOperator() {
        rule.setOperator(ConflictRule.Operator.LESSER);
        assertEquals(ConflictRule.Operator.LESSER, rule.getOperator());
    }

    @Test
    void testGetValue() {
        assertEquals("40", rule.getValue());
    }

    @Test
    void testSetValue() {
        rule.setValue("50");
        assertEquals("50", rule.getValue());
    }

    @Test
    void testIsActive() {
        assertTrue(rule.isActive());
    }

    @Test
    void testSetActive() {
        rule.setActive(false);
        assertFalse(rule.isActive());
    }

    @Test
    void testDifferentFieldTypes() {
        ConflictRule nameRule = new ConflictRule(ConflictRule.FieldType.NAME, ConflictRule.Operator.EQUALS, "John", true);
        assertEquals(ConflictRule.FieldType.NAME, nameRule.getField());

        ConflictRule jobRule = new ConflictRule(ConflictRule.FieldType.JOB, ConflictRule.Operator.EQUALS, "Manager", true);
        assertEquals(ConflictRule.FieldType.JOB, jobRule.getField());

        ConflictRule emailRule = new ConflictRule(ConflictRule.FieldType.EMAIL, ConflictRule.Operator.EQUALS, "test@example.com", true);
        assertEquals(ConflictRule.FieldType.EMAIL, emailRule.getField());

        ConflictRule shiftRule = new ConflictRule(ConflictRule.FieldType.PREFERRED_SHIFT, ConflictRule.Operator.EQUALS, "9to5", true);
        assertEquals(ConflictRule.FieldType.PREFERRED_SHIFT, shiftRule.getField());
    }

    @Test
    void testDifferentOperators() {
        ConflictRule equalsRule = new ConflictRule(ConflictRule.FieldType.NAME, ConflictRule.Operator.EQUALS, "Test", true);
        assertEquals(ConflictRule.Operator.EQUALS, equalsRule.getOperator());

        ConflictRule notEqualsRule = new ConflictRule(ConflictRule.FieldType.NAME, ConflictRule.Operator.NOT_EQUALS, "Test", true);
        assertEquals(ConflictRule.Operator.NOT_EQUALS, notEqualsRule.getOperator());

        ConflictRule greaterRule = new ConflictRule(ConflictRule.FieldType.WORKING_HOURS, ConflictRule.Operator.GREATER, "40", true);
        assertEquals(ConflictRule.Operator.GREATER, greaterRule.getOperator());

        ConflictRule lesserRule = new ConflictRule(ConflictRule.FieldType.WORKING_HOURS, ConflictRule.Operator.LESSER, "20", true);
        assertEquals(ConflictRule.Operator.LESSER, lesserRule.getOperator());
    }

    @Test
    void testInactiveRule() {
        ConflictRule inactiveRule = new ConflictRule(ConflictRule.FieldType.WORKING_HOURS, ConflictRule.Operator.GREATER, "40", false);
        assertFalse(inactiveRule.isActive());
    }

    @Test
    void testGetFullName() {
        String fullName = rule.getFullName();
        assertNotNull(fullName);
        assertTrue(fullName.contains("WORKING_HOURS"));
        assertTrue(fullName.contains("GREATER"));
        assertTrue(fullName.contains("40"));
    }
}

