package com.calendarfx.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreatCalendarTest {

    private GreatCalendar greatCalendar;
    private List<GreatCalendar.GreatEntry> entries;

    @BeforeEach
    void setUp() {
        entries = new ArrayList<>();
        greatCalendar = new GreatCalendar("TestCalendar", entries);
    }

    @Test
    void testConstructor() {
        GreatCalendar calendar = new GreatCalendar("TestCal", entries);
        assertEquals("TestCal", calendar.getName());
        assertEquals(entries, calendar.getEntries());
    }

    @Test
    void testGetName() {
        assertEquals("TestCalendar", greatCalendar.getName());
    }

    @Test
    void testGetEntries() {
        assertEquals(entries, greatCalendar.getEntries());
    }

    @Test
    void testGetEntriesEmpty() {
        assertTrue(greatCalendar.getEntries().isEmpty());
    }

    @Test
    void testGetEntriesWithItems() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 15, 17, 0);
        GreatCalendar.GreatEntry entry = new GreatCalendar.GreatEntry("1", "Meeting", start, end, false, "Room A");
        entries.add(entry);

        assertEquals(1, greatCalendar.getEntries().size());
        assertEquals("Meeting", greatCalendar.getEntries().get(0).getTitle());
    }

    @Test
    void testMultipleEntries() {
        LocalDateTime start1 = LocalDateTime.of(2024, 1, 15, 9, 0);
        LocalDateTime end1 = LocalDateTime.of(2024, 1, 15, 12, 0);
        entries.add(new GreatCalendar.GreatEntry("1", "Meeting 1", start1, end1, false, "Room A"));

        LocalDateTime start2 = LocalDateTime.of(2024, 1, 15, 13, 0);
        LocalDateTime end2 = LocalDateTime.of(2024, 1, 15, 17, 0);
        entries.add(new GreatCalendar.GreatEntry("2", "Meeting 2", start2, end2, false, "Room B"));

        assertEquals(2, greatCalendar.getEntries().size());
    }

    @Test
    void testNullEntries() {
        greatCalendar = new GreatCalendar("TestCalendar", null);
        assertNull(greatCalendar.getEntries());
    }
}

