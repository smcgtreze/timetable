package com.calendarfx.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConflictResolutionCalculatorTest {

    private ConflictResolutionCalculator calculator;
    private List<ConflictRule> rules;
    private List<PersonalProfile> profiles;

    @BeforeEach
    void setUp() {
        rules = new ArrayList<>();
        profiles = new ArrayList<>();
        calculator = new ConflictResolutionCalculator(rules, profiles);
    }

    @Test
    void testConstructor() {
        assertNotNull(calculator.getCurrentConflicts());
        assertNotNull(calculator.getEntriesMap());
        assertTrue(calculator.getCurrentConflicts().isEmpty());
        assertTrue(calculator.getEntriesMap().isEmpty());
    }

    @Test
    void testGetCurrentConflicts() {
        assertNotNull(calculator.getCurrentConflicts());
        assertTrue(calculator.getCurrentConflicts().isEmpty());
    }

    @Test
    void testGetEntriesMap() {
        assertNotNull(calculator.getEntriesMap());
        assertTrue(calculator.getEntriesMap().isEmpty());
    }

    @Test
    void testGetSnapshotCalendarViewNullInitially() {
        assertNull(calculator.getSnapshotCalendarView());
    }

    @Test
    void testGetOriginalCalendarViewNullInitially() {
        assertNull(calculator.getOriginalCalendarView());
    }

    @Test
    void testCalculateWithNoConflicts() {
        boolean result = calculator.calculate();
        assertTrue(result);
        assertTrue(calculator.getCurrentConflicts().isEmpty());
    }

    @Test
    void testConflictResultClass() {
        String testId = "test-entry-1";
        ConflictResolutionCalculator.ConflictResult result = 
            new ConflictResolutionCalculator.ConflictResult(null, true);
        
        assertTrue(result.successful);
        assertNull(result.entry);
    }

    @Test
    void testConflictResultClassFailed() {
        ConflictResolutionCalculator.ConflictResult result = 
            new ConflictResolutionCalculator.ConflictResult(null, false);
        
        assertFalse(result.successful);
    }
}
