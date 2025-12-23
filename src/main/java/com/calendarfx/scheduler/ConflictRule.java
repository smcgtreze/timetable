package com.calendarfx.scheduler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConflictRule {

    @JsonCreator
    public ConflictRule(
            @JsonProperty("field") FieldType field,
            @JsonProperty("operator") Operator operator,
            @JsonProperty("value") String value,
            @JsonProperty("active") boolean active) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.active = active;
    }

    public enum FieldType {
        NAME, WORKING_HOURS, PREFERRED_SHIFT, JOB
    }

    public enum Operator {
        EQUALS, NOT_EQUALS, GREATER, LESSER
    }

    private FieldType field;
    private Operator operator;
    private String value;
    private boolean active = true;

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public FieldType getField() { return field; }
    public void setField(FieldType field) { this.field = field; }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) { this.operator = operator; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}