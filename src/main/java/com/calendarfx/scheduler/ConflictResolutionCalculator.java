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

    // snapshotCalendarView holds the deep-copied snapshot,
    // originalCalendarView holds the original view passed in.
    private CalendarView snapshotCalendarView;
    private CalendarView originalCalendarView;

    public static class ConflictResult {
        public final boolean successful;
        public final Entry<?> entry;

        public ConflictResult(Entry<?> entry, boolean successful) {
            this.entry = entry;
            this.successful = successful;
        }
    }

    public ConflictResolutionCalculator(final List<ConflictRule> ruleForms, final List<PersonalProfile> personForms) {
        this.personForms = personForms;
        this.ruleForms = ruleForms;
    }

    public ObservableMap<String, ConflictRule> getCurrentConflicts() {
        return currentConflicts;
    }

    public HashMap<String, Entry<?>> getEntriesMap() {
        return entriesMap;
    }

    public CalendarView getSnapshotCalendarView() {
        return snapshotCalendarView;
    }

    public CalendarView getOriginalCalendarView() {
        return originalCalendarView;
    }

    /**
     * Stores the original view and creates a deep snapshot copy which will be used for
     * conflict detection/resolution operations.
     */
    public void setCalendarView(final CalendarView originalView) {
        this.originalCalendarView = originalView;
        CalendarView snapshot = new CalendarView();

        List<CalendarSource> deepCopy =
                deepCopySources(originalView.getCalendarSources());

        snapshot.getCalendarSources().setAll(deepCopy);

        this.snapshotCalendarView = snapshot;
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
                        ruleConflict = evaluate(rule.getValue(), operator, String.valueOf(personalProfile.getWorkingHours()));
                    }
                }
                case PREFERRED_SHIFT -> {
                    if (personalProfile != null) {
                        Object preferredShift = personalProfile.getPreferredShift();
                        ruleConflict = preferredShift != null && evaluate(rule.getValue(), operator, preferredShift.toString());
                    }
                }
                case JOB -> {
                    if (personalProfile != null) {
                        Object job = personalProfile.getJob();
                        ruleConflict = job != null && evaluate(rule.getValue(), operator, job.toString());
                    }
                }
                case EMAIL -> {
                    if (personalProfile != null) {
                        Object email = personalProfile.getEmail();
                        ruleConflict = email != null && evaluate(rule.getValue(), operator, email.toString());
                    }
                }
            }

            if (ruleConflict) {
                existsConflict = true;
                // look up the entry in the snapshot (not the original) to preserve snapshot state
                Optional<Entry<?>> optEntry = findEntryById(snapshotCalendarView, entry.getId());
                optEntry.ifPresent(snapshotEntry -> entriesMap.put(entry.getId(), snapshotEntry));
                currentConflicts.put(entry.getId(), rule);
            }
        }
        return existsConflict;
    }

    private boolean evaluate(String left, ConflictRule.Operator operator, String right) {
        // Guard parsing operations to avoid NumberFormatException propagating
        try {
            return switch (operator) {
                case EQUALS -> left.equals(right);
                case NOT_EQUALS -> !left.equals(right);
                case GREATER -> {
                    double l = Double.parseDouble(left);
                    double r = Double.parseDouble(right);
                    yield l > r;
                }
                case LESSER -> {
                    double l = Double.parseDouble(left);
                    double r = Double.parseDouble(right);
                    yield l < r;
                }
                default -> false;
            };
        } catch (NumberFormatException ex) {
            // Invalid numeric input for GREATER/LESSER comparisons; treat as non-matching
            return false;
        }
    }

    public boolean calculate() {

        List<ConflictResult> conflictResults = new ArrayList<>();
        List<Entry<?>> resolvedEntries = new ArrayList<>();

        // Iterate safely over a snapshot of the key set to avoid concurrent-modification surprises
        for (String entryId : new ArrayList<>(currentConflicts.keySet())) {
            ConflictRule rule = currentConflicts.get(entryId);
            Entry<?> entry = entriesMap.get(entryId);

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
        resolvedEntries.forEach(entry -> {
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
        if (view == null) return Optional.empty();
        return view.getCalendarSources().stream()
                .flatMap(src -> src.getCalendars().stream())
                .flatMap(cal -> cal.findEntries("").stream())
                .filter(e -> ((Entry<?>) e).getId().equals(id))
                .findFirst();
    }
}