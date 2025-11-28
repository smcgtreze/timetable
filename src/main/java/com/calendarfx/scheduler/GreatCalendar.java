package com.calendarfx.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class GreatCalendar {
    private String name;
    protected List<GreatEntry> entries;

    @JsonCreator
    public GreatCalendar(@JsonProperty("name") String name
                        ,@JsonProperty("entries") List<GreatEntry> entries
                         ) {
        this.name = name;
        this.entries = entries;
    }


    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public List<GreatEntry> getEntries() {
        return entries;
    }

    public static class GreatEntry {
        protected String id;
        protected String title;
        protected LocalDateTime start;
        protected LocalDateTime end;
        protected boolean fullDay;
        protected String location;

        public GreatEntry() {}

        @JsonCreator
        public GreatEntry(@JsonProperty("id") String id,
                          @JsonProperty("title") String title,
                          @JsonProperty("start") LocalDateTime start,
                          @JsonProperty("end") LocalDateTime end,
                          @JsonProperty("fullDay") boolean fullDay,
                          @JsonProperty("location") String location) {
            this.id = id;
            this.title = title;
            this.start = start;
            this.end = end;
            this.fullDay = fullDay;
            this.location = location;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public LocalDateTime getStart() { return start; }
        public void setStart(LocalDateTime start) { this.start = start; }

        public LocalDateTime getEnd() { return end; }
        public void setEnd(LocalDateTime end) { this.end = end; }

        public boolean isFullDay() { return fullDay; }
        public void setFullDay(boolean fullDay) { this.fullDay = fullDay; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

}

