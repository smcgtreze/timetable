package com.calendarfx.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonalProfile {
    private final String name;
    private int workingHours;
    private String email;
    private String job;
    private int age;
    private String preferredShift;

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

    // Note: Intentionally there is no setter for the personal name. The user may not change the name after it has been set.
    // This parameter is used to do the mapping between calendars and personalProfiles.
    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPreferredShift(String preferredShift) {
        this.preferredShift = preferredShift;
    }
}