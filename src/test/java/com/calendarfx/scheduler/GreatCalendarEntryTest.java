package com.calendarfx.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class GreatCalendarEntryTest {

    private GreatCalendar.GreatEntry entry;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2024, 1, 15, 9, 0);
        end = LocalDateTime.of(2024, 1, 15, 17, 0);
        entry = new GreatCalendar.GreatEntry("1", "Meeting", start, end, false, "Room A");
    }

    @Test
    void testConstructor() {
        assertEquals("1", entry.getId());
        assertEquals("Meeting", entry.getTitle());
        assertEquals(start, entry.getStart());
        assertEquals(end, entry.getEnd());
        assertFalse(entry.isFullDay());
        assertEquals("Room A", entry.getLocation());
    }

    @Test
    void testNoArgConstructor() {
        GreatCalendar.GreatEntry emptyEntry = new GreatCalendar.GreatEntry();
        assertNull(emptyEntry.getId());
        assertNull(emptyEntry.getTitle());
        assertNull(emptyEntry.getStart());
        assertNull(emptyEntry.getEnd());
        assertFalse(emptyEntry.isFullDay());
        assertNull(emptyEntry.getLocation());
    }

    @Test
    void testGetId() {
        assertEquals("1", entry.getId());
    }

    @Test
    void testSetId() {
        entry.setId("2");
        assertEquals("2", entry.getId());
    }

    @Test
    void testGetTitle() {
        assertEquals("Meeting", entry.getTitle());
    }

    @Test
    void testSetTitle() {
        entry.setTitle("Conference");
        assertEquals("Conference", entry.getTitle());
    }

    @Test
    void testGetStart() {
        assertEquals(start, entry.getStart());
    }

    @Test
    void testSetStart() {
        LocalDateTime newStart = LocalDateTime.of(2024, 1, 16, 10, 0);
        entry.setStart(newStart);
        assertEquals(newStart, entry.getStart());
    }

    @Test
    void testGetEnd() {
        assertEquals(end, entry.getEnd());
    }

    @Test
    void testSetEnd() {
        LocalDateTime newEnd = LocalDateTime.of(2024, 1, 16, 18, 0);
        entry.setEnd(newEnd);
        assertEquals(newEnd, entry.getEnd());
    }

    @Test
    void testIsFullDay() {
        assertFalse(entry.isFullDay());
    }

    @Test
    void testSetFullDay() {
        entry.setFullDay(true);
        assertTrue(entry.isFullDay());
    }

    @Test
    void testGetLocation() {
        assertEquals("Room A", entry.getLocation());
    }

    @Test
    void testSetLocation() {
        entry.setLocation("Room B");
        assertEquals("Room B", entry.getLocation());
    }

    @Test
    void testFullDayEntry() {
        GreatCalendar.GreatEntry fullDayEntry = new GreatCalendar.GreatEntry(
                "3",
                "Holiday",
                LocalDateTime.of(2024, 1, 25, 0, 0),
                LocalDateTime.of(2024, 1, 26, 0, 0),
                true,
                "Everywhere"
        );

        assertTrue(fullDayEntry.isFullDay());
        assertEquals("Holiday", fullDayEntry.getTitle());
    }
}
