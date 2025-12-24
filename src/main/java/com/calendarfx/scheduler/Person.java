package com.calendarfx.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {

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

    @JsonCreator
    public Person(
            @JsonProperty("Working Hours") int workingHours,
            @JsonProperty("Email") String email,
            @JsonProperty("Job") String job,
            @JsonProperty("Age") int age,
            @JsonProperty("Name") String name
    ) {
        this.workingHours = workingHours;
        this.email = email;
        this.job = job;
        this.age = age;
        this.name = name;
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
}