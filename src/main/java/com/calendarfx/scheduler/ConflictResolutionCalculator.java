package com.calendarfx.scheduler;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictResolutionCalculator {
    Map<Entry<?>, ConflictRule> currentConflicts = new HashMap<>();
    private final List<PersonalProfile> personForms;
    private final List<ConflictRule> ruleForms;

    public class ConflictResult{
        boolean successful;
        Entry entry;
        public ConflictResult(Entry entry, boolean successful){}
    }

    public ConflictResolutionCalculator(final List<ConflictRule> ruleForms, final List<PersonalProfile> personForms){
        this.personForms = personForms;
        this.ruleForms = ruleForms;
    }

    public boolean hasConflict(Entry<?> entry) {
        if (ruleForms == null || ruleForms.isEmpty()) return false;

        boolean existsConflict = false;
        final String calendarName = entry.getCalendar().getName();
        PersonalProfile personalProfile = personForms.stream()
                .filter(p -> p.getName().equals(calendarName))
                .findFirst()
                .orElse(null);

        for (ConflictRule rule : ruleForms) {
            if (!rule.isActive()) continue;

            boolean ruleConflict = false;
            ConflictRule.Operator operator = rule.getOperator();
            switch (rule.getField()) {
                case NAME -> ruleConflict = calendarName.equals(rule.getValue());
                case WORKING_HOURS -> {
                    if (personalProfile != null) {
                        ruleConflict = evaluate( rule.getValue(),operator,String.valueOf(personalProfile.getWorkingHours()));
                    }
                }
                case PREFERRED_SHIFT -> {
                    if (personalProfile != null) {
                        Object preferredShift = personalProfile.getPreferredShift();
                        ruleConflict = preferredShift != null && evaluate( rule.getValue(),operator, preferredShift.toString());
                    }
                }
                case JOB -> {
                    if (personalProfile != null) {
                        Object job = personalProfile.getJob();
                        ruleConflict = job != null && evaluate( rule.getValue(),operator, job.toString());
                    }
                }
                case EMAIL -> {
                    if (personalProfile != null) {
                        Object email = personalProfile.getEmail();
                        ruleConflict = email != null && evaluate( rule.getValue(),operator, email.toString());
                    }
                }
            }

            if (ruleConflict) {
                existsConflict = true;
                currentConflicts.put(entry, rule);
            }
        }
        return existsConflict;
    }

    private boolean evaluate(String left, ConflictRule.Operator operator, String right) {
        return switch (operator) {
            case ConflictRule.Operator.EQUALS -> left.equals(right);
            case ConflictRule.Operator.NOT_EQUALS -> !left.equals(right);
            case ConflictRule.Operator.GREATER  -> Double.parseDouble(left) >  Double.parseDouble(right);
            case ConflictRule.Operator.LESSER  -> Double.parseDouble(left) <  Double.parseDouble(right);
            default -> false;
        };
    }


    public boolean calculate() {

        List<ConflictResult> conflictResults = new ArrayList<>();
        List<Entry<?>> resolvedEntries = new ArrayList<>();

        // Iterate safely (no mutation inside forEach)
        for (var entry : currentConflicts.keySet()) {
            ConflictRule rule = currentConflicts.get(entry);

            boolean solved = switch (rule.getField()) {
                case WORKING_HOURS -> solveWorkingHours(entry, rule);
                case PREFERRED_SHIFT -> solvePreferredShift(entry, rule);
                default -> false;
            };

            // After attempting resolution, re-check conflict
            boolean stillConflicted = hasConflict(entry);

            if (!stillConflicted) {
                resolvedEntries.add(entry);
            }

            conflictResults.add(new ConflictResult(entry, !stillConflicted));
        }

        // Remove resolved entries AFTER iteration
        resolvedEntries.forEach(currentConflicts::remove);

        return currentConflicts.isEmpty();
    }

    private boolean solveWorkingHours(Entry<?> entry, ConflictRule rule) {
        Calendar calendar = entry.getCalendar();
        if (calendar == null) return false;

        List<Entry<?>> calendarEntries = calendar.findEntries("");
        return switch (rule.getOperator()) {
            case LESSER -> solveWorkingHoursLesser(calendarEntries);
            case GREATER -> solveWorkingHoursGreater(calendarEntries);
            default -> false;
        };
    }

    private boolean solveWorkingHoursLesser(List<Entry<?>> entries) {
        List<Entry<?>> removable = entries.stream()
                .filter(e -> e.getDuration().toHours() <= 1
                || e.getDuration().toHours() > 4
                || e.getEndTime().isAfter(LocalTime.of(17, 0))
                || e.getStartTime().isBefore(LocalTime.of(9, 0)))
                .toList();

        removable.forEach(e -> {
            e.changeEndTime(e.getEndTime().minusMinutes(30));
            e.changeStartTime(e.getStartTime().plusMinutes(30));
        });

        return !removable.isEmpty();
    }

    private boolean solveWorkingHoursGreater(List<Entry<?>> entries) {
        List<Entry<?>> addable = entries.stream()
                .filter(e -> e.getDuration().toHours() <= 1 ||
                e.getEndTime().isBefore(LocalTime.of(16, 0)) ||
                e.getStartTime().isAfter(LocalTime.of(9, 0)))
                .toList();

        addable.forEach(e -> {
            e.changeEndTime(e.getEndTime().plusMinutes(30));
            e.changeStartTime(e.getStartTime().minusMinutes(30));
        });

        return !addable.isEmpty();
    }

    private boolean solvePreferredShift(Entry<?> entry, ConflictRule rule) {
        // Attempt to solve preferred shift, this is secondary and might not be solved
        return false;
    }




}
