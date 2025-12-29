package com.calendarfx.scheduler;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ConflictResolutionCalculator {
    private final ObservableMap<String, ConflictRule> currentConflicts =
            FXCollections.observableHashMap();
    private final HashMap<String, Entry<?>> entriesMap = new HashMap<>();
    private final List<PersonalProfile> personForms;
    private final List<ConflictRule> ruleForms;
    private CalendarView calendarView;
    private CalendarView originalCalendarView;

    public class ConflictResult{
        boolean successful;
        Entry entry;
        public ConflictResult(Entry entry, boolean successful){}
    }

    public ConflictResolutionCalculator(final List<ConflictRule> ruleForms, final List<PersonalProfile> personForms){
        this.personForms = personForms;
        this.ruleForms = ruleForms;
    }

    public ObservableMap<String, ConflictRule> getCurrentConflicts() {
        return currentConflicts;
    }

    public HashMap<String, Entry<?>> getEntriesMap() {
        return entriesMap;
    }

    public CalendarView getCalendarView() {
        return calendarView;
    }

    public CalendarView getOriginalCalendarView() {
        return originalCalendarView;
    }

    public void setCalendarView(final CalendarView calendarView) {
        this.originalCalendarView = calendarView;
        CalendarView snapshot = new CalendarView();

        List<CalendarSource> deepCopy =
                deepCopySources(calendarView.getCalendarSources());

        snapshot.getCalendarSources().setAll(deepCopy);

        this.calendarView = snapshot;
    }

    public List<CalendarSource> deepCopySources(List<CalendarSource> sources) {
        GreatCalendarSerializer serializer = new GreatCalendarSerializer();

        return sources.stream()
                .map(source -> {
                    CalendarSource newSource = new CalendarSource(source.getName());
                    for (Calendar cal : source.getCalendars()) {
                        GreatCalendar dto = serializer.fromCalendar(cal);
                        Calendar newCal = serializer.toCalendar(dto);
                        newSource.getCalendars().add(newCal);
                    }
                    return newSource;
                })
                .toList();
    }

    public void applySnapshot(CalendarView original, CalendarView snapshot) {
        GreatCalendarSerializer serializer = new GreatCalendarSerializer();

        // Build new CalendarSources from snapshot
        List<CalendarSource> newSources = snapshot.getCalendarSources().stream()
                .map(source -> {
                    CalendarSource newSource = new CalendarSource(source.getName());

                    for (Calendar cal : source.getCalendars()) {
                        GreatCalendar dto = serializer.fromCalendar(cal);
                        Calendar newCal = serializer.toCalendar(dto);
                        newSource.getCalendars().add(newCal);
                    }

                    return newSource;
                })
                .toList();

        // Replace the original sources
        original.getCalendarSources().setAll(newSources);
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
                Optional<Entry<?>> optEntry = findEntryById(calendarView, entry.getId());
                optEntry.ifPresent(snapshotEntry -> entriesMap.put(entry.getId(), snapshotEntry));
                currentConflicts.put(entry.getId(), rule);
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
        for (var entryId : currentConflicts.keySet()) {
            ConflictRule rule = currentConflicts.get(entryId);
            Entry<?> entry =  entriesMap.get(entryId);

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
        resolvedEntries.forEach( entry -> {
            currentConflicts.remove(entry.getId());
            entriesMap.remove(entry.getId());
        });

        return currentConflicts.isEmpty();
    }

    private boolean solveWorkingHours(Entry<?> entry, ConflictRule rule) {
        List<Entry<?>> calendarEntries = List.of(entry);
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
                || e.getEndTime().isAfter(LocalTime.of(18, 0))
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

    public static Optional<Entry<?>> findEntryById(CalendarView view, String id) {
        if(view == null) return Optional.empty();
        return view.getCalendarSources().stream()
                .flatMap(src -> src.getCalendars().stream())
                .flatMap(cal -> cal.findEntries("").stream())
                .filter(e ->((Entry<?>)e).getId().equals(id))
                .findFirst();
    }

}
