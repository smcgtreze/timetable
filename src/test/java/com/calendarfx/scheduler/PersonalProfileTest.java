package com.calendarfx.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonalProfileTest {

    private PersonalProfile profile;

    @BeforeEach
    void setUp() {
        profile = new PersonalProfile(
                "john.doe@example.com",
                "Software Engineer",
                30,
                "John Doe",
                "9to5"
        );
    }

    @Test
    void testConstructor() {
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
    void testNameIsImmutable() {
        // Name is set during construction and cannot be changed
        assertEquals("John Doe", profile.getName());
        // There is no setName method intentionally
    }
}
