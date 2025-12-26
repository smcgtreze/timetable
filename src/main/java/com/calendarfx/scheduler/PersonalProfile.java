package com.calendarfx.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonalProfile {

    @JsonProperty("Working Hours")
    private final int workingHours;

    @JsonProperty("Email")
    private final String email;

    @JsonProperty("Job")
    private final String job;

    @JsonProperty("Age")
    private final int age;

    @JsonProperty("Name")
    private final String name;

    @JsonProperty("Preferred Shift")
    private final String preferredShift;

    @JsonCreator
    public PersonalProfile(
            @JsonProperty("Working Hours") int workingHours,
            @JsonProperty("Email") String email,
            @JsonProperty("Job") String job,
            @JsonProperty("Age") int age,
            @JsonProperty("Name") String name,
            @JsonProperty("Preferred Shift") String preferredShift
    ) {
        this.workingHours = workingHours;
        this.email = email;
        this.job = job;
        this.age = age;
        this.name = name;
        this.preferredShift = preferredShift;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public String getEmail() {
        return email;
    }

    public String getJob() {
        return job;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getPreferredShift() {
        return preferredShift;
    }
}