package com.calendarfx.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for PersonalProfileMapper.
 * Note: Form-based testing is excluded as it requires FormsFX GUI initialization.
 */
class PersonalProfileMapperTest {

    @Test
    void testMapperConstructor() {
        PersonalProfileMapper mapper = new PersonalProfileMapper();
        assertNotNull(mapper);
    }

    @Test
    void testConstantsAreDefined() {
        assertEquals("Name", PersonalProfileMapper.NAME);
        assertEquals("Email", PersonalProfileMapper.EMAIL);
        assertEquals("Job", PersonalProfileMapper.JOB);
        assertEquals("Age", PersonalProfileMapper.AGE);
        assertEquals("Preferred Shift", PersonalProfileMapper.PREFERRED_SHIFT);
    }
}

