package com.calendarfx.scheduler;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

public interface CalendarSerializer {
    Calendar toCalendar(GreatCalendar dto);
    Entry<?> toEntry(GreatCalendar.GreatEntry dto);
    GreatCalendar fromCalendar(Calendar calendar);
    GreatCalendar.GreatEntry fromEntry(Entry<?> entry);
}

