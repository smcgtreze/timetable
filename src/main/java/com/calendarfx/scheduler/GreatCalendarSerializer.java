package com.calendarfx.scheduler;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import java.util.List;

public class GreatCalendarSerializer implements CalendarSerializer {

    @Override
    public Calendar toCalendar(GreatCalendar gc) {
        Calendar calendar = new Calendar(gc.getName());
        gc.getEntries().forEach(e -> calendar.addEntry( toEntry(e) ));
        return calendar;
    }

    @Override
    public Entry<?> toEntry(GreatCalendar.GreatEntry ge) {
        Entry<?> entry = new Entry<>(ge.title);
        entry.setId(ge.id);
        entry.setInterval(ge.start, ge.end);
        entry.setFullDay(ge.fullDay);
        entry.setLocation(ge.location);
        return entry;
    }

    @Override
    public GreatCalendar fromCalendar(Calendar calendar) {
        List<GreatCalendar.GreatEntry> dtoEntries = calendar.findEntries("").stream()
                .map(entry -> fromEntry((Entry<?>) entry))
                .toList();

        GreatCalendar gc = new GreatCalendar(calendar.getName(), dtoEntries );
        return gc;
    }

    @Override
    public GreatCalendar.GreatEntry fromEntry(Entry<?> entry) {
        return new GreatCalendar.GreatEntry(
                (entry.getId()),
                (entry.getTitle()),
                (entry.getStartAsLocalDateTime()),
                (entry.getEndAsLocalDateTime()),
                (entry.isFullDay()),
                (entry.getLocation())
        );
    }

}
