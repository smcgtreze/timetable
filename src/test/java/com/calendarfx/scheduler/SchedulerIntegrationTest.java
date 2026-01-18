package com.calendarfx.scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for the scheduler module.
 * These tests validate core data model operations.
 */
class SchedulerIntegrationTest {

    @Test
    void testGreatCalendarWithEntries() {
        java.util.List<GreatCalendar.GreatEntry> entries = new java.util.ArrayList<>();
        
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 15, 17, 0);
        GreatCalendar.GreatEntry entry = new GreatCalendar.GreatEntry("1", "Meeting", start, end, false, "Room A");
        entries.add(entry);
        
        GreatCalendar calendar = new GreatCalendar("TestCal", entries);
        
        assertEquals("TestCal", calendar.getName());
        assertEquals(1, calendar.getEntries().size());
        assertEquals("Meeting", calendar.getEntries().get(0).getTitle());
    }

    @Test
    void testPersonalProfileCreation() {
        PersonalProfile profile = new PersonalProfile(
            40,
            "john@example.com",
            "Engineer",
            30,
            "John Doe",
            "9to5"
        );
        
        assertEquals("John Doe", profile.getName());
        assertEquals("john@example.com", profile.getEmail());
        assertEquals("Engineer", profile.getJob());
        assertEquals(30, profile.getAge());
        assertEquals("9to5", profile.getPreferredShift());
        assertEquals(40, profile.getWorkingHours());
    }

    @Test
    void testConflictRuleCreation() {
        ConflictRule rule = new ConflictRule(
            ConflictRule.FieldType.WORKING_HOURS,
            ConflictRule.Operator.GREATER,
            "40",
            true
        );
        
        assertEquals(ConflictRule.FieldType.WORKING_HOURS, rule.getField());
        assertEquals(ConflictRule.Operator.GREATER, rule.getOperator());
        assertEquals("40", rule.getValue());
        assertTrue(rule.isActive());
    }

    @Test
    void testConflictResolutionCalculatorInitialization() {
        java.util.List<ConflictRule> rules = new java.util.ArrayList<>();
        java.util.List<PersonalProfile> profiles = new java.util.ArrayList<>();
        
        ConflictResolutionCalculator calculator = new ConflictResolutionCalculator(rules, profiles);
        
        assertNotNull(calculator);
        assertNotNull(calculator.getCurrentConflicts());
        assertNotNull(calculator.getEntriesMap());
        assertTrue(calculator.getCurrentConflicts().isEmpty());
        assertTrue(calculator.getEntriesMap().isEmpty());
        assertTrue(calculator.calculate());
    }
}
