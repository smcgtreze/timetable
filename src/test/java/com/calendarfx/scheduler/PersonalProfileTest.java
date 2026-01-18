package com.calendarfx.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonalProfileTest {

    private PersonalProfile profile;

    @BeforeEach
    void setUp() {
        profile = new PersonalProfile(
                40,
                "john.doe@example.com",
                "Software Engineer",
                30,
                "John Doe",
                "9to5"
        );
    }

    @Test
    void testConstructor() {
        assertEquals(40, profile.getWorkingHours());
        assertEquals("john.doe@example.com", profile.getEmail());
        assertEquals("Software Engineer", profile.getJob());
        assertEquals(30, profile.getAge());
        assertEquals("John Doe", profile.getName());
        assertEquals("9to5", profile.getPreferredShift());
    }

    @Test
    void testGetName() {
        assertEquals("John Doe", profile.getName());
    }

    @Test
    void testGetEmail() {
        assertEquals("john.doe@example.com", profile.getEmail());
    }

    @Test
    void testSetEmail() {
        profile.setEmail("jane.doe@example.com");
        assertEquals("jane.doe@example.com", profile.getEmail());
    }

    @Test
    void testGetJob() {
        assertEquals("Software Engineer", profile.getJob());
    }

    @Test
    void testSetJob() {
        profile.setJob("Product Manager");
        assertEquals("Product Manager", profile.getJob());
    }

    @Test
    void testGetAge() {
        assertEquals(30, profile.getAge());
    }

    @Test
    void testSetAge() {
        profile.setAge(35);
        assertEquals(35, profile.getAge());
    }

    @Test
    void testGetPreferredShift() {
        assertEquals("9to5", profile.getPreferredShift());
    }

    @Test
    void testSetPreferredShift() {
        profile.setPreferredShift("3to11");
        assertEquals("3to11", profile.getPreferredShift());
    }

    @Test
    void testGetWorkingHours() {
        assertEquals(40, profile.getWorkingHours());
    }

    @Test
    void testSetWorkingHours() {
        profile.setWorkingHours(35);
        assertEquals(35, profile.getWorkingHours());
    }

    @Test
    void testNameIsImmutable() {
        // Name is set during construction and cannot be changed
        assertEquals("John Doe", profile.getName());
        // There is no setName method intentionally
    }

    @Test
    void testZeroWorkingHours() {
        PersonalProfile zeroHoursProfile = new PersonalProfile(
                0,
                "test@example.com",
                "Intern",
                22,
                "Test User",
                "flexible"
        );
        assertEquals(0, zeroHoursProfile.getWorkingHours());
    }

    @Test
    void testNullValues() {
        PersonalProfile nullProfile = new PersonalProfile(
                0,
                null,
                null,
                0,
                "Test User",
                null
        );
        assertNull(nullProfile.getEmail());
        assertNull(nullProfile.getJob());
        assertNull(nullProfile.getPreferredShift());
        assertEquals(0, nullProfile.getAge());
    }
}
