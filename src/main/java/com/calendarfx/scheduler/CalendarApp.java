package com.calendarfx.scheduler;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.EntryViewBase;
import com.dlsc.formsfx.model.structure.Form;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CalendarApp extends Application {

    public static final String ADD_BUTTON_STYLE = StyleConstants.ADD_BUTTON_STYLE;
    public static final String CONFLICT_BUTTON_STYLE = StyleConstants.CONFLICT_BUTTON_STYLE;
    public static final String REFRESH_BUTTON_STYLE = StyleConstants.REFRESH_BUTTON_STYLE;
    public static final String EDIT_BUTTON_STYLE = StyleConstants.EDIT_BUTTON_STYLE;
    public static final String WARNING_STYLE = StyleConstants.WARNING_STYLE;
    public static final String TEXT_STYLE = StyleConstants.TEXT_STYLE;
    public static final int PREFERRED_BUTTON_SIZE = 50;
    private static final int ENTRY_CLICK_COUNT = 2;
    private static final int WIDTH = 1300;
    private static final int HEIGHT = 1000;
    private static final String TITLE = "Calendar";
    private static final int BUTTON_SPACING = 10;
    public static final String REFRESH_SIGN = "🗘";
    public static final String SHOCK_SIGN = "⚡";
    public static final String PLUS_SIGN = "+";
    public static final String EDIT_SIGN = "✎";
    private static List<GreatCalendar> cachedCalendars;
    private static PersistenceManager persistenceManager;
    private static EventHandler<ActionEvent> cachedPersonHandler;
    private static ConflictResolutionCalculator conflictCalculator;
    private final List<String> preferredShift = Arrays.asList("nineToFive", "nineToSix", "eightToFour", "eightToFive");
    private static List<PersonalProfile> personForms;
    private static List<ConflictRule> ruleForms;
    private static EventHandler<ActionEvent> cachedConflictHandler;
    private static CalendarView calendarView;
    private EmployeeFormProvider formProvider;

    @Override
    public void start(Stage primaryStage) {
        calendarView = setupCalendarView();
        CalendarSource familyCalendarSource = new CalendarSource("Family");
        calendarView.getCalendarSources().setAll(familyCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());

        try {
            CSSFX.start();
        } catch (Throwable ignored) {
        }

        // Add cached calendars
        if( cachedCalendars != null && !cachedCalendars.isEmpty()){
            cachedCalendars.forEach(calendar -> {
                Calendar newCalendar = createCalendar( calendar.getName(), calendar.entries );
                familyCalendarSource.getCalendars().add(newCalendar);
            });
        }

        formProvider = new EmployeeFormProvider(preferredShift, WIDTH, HEIGHT, BUTTON_SPACING);

        EventHandler<ActionEvent> addPersonHandler = e -> {
            Form form = formProvider.createForm();
            formProvider.showFormWindow(primaryStage, form, () -> {
                PersonalProfileMapper profileMapper = new PersonalProfileMapper();
                PersonalProfile profile = profileMapper.fromForm(form);
                if (personForms == null) {
                    personForms = new ArrayList<>();
                }
                personForms.add(profile);
                Calendar calendar = createCalendar(profile.getName(), List.of());
                familyCalendarSource.getCalendars().add(calendar);
                GreatCalendar gc = persistenceManager.calendarSerializer.fromCalendar(calendar);
                cachedCalendars.add(gc);
                setupPrimaryStage(primaryStage, calendarView, null, null);
            });
        };

        ConflictRuleProvider conflictRuleProvider =
                new ConflictRuleProvider(preferredShift, WIDTH, HEIGHT, BUTTON_SPACING);

        if (ruleForms != null && !ruleForms.isEmpty()) {
            conflictRuleProvider.setRules(ruleForms);
        }

        EventHandler<ActionEvent> addConflictRuleHandler = e -> {
            Form form = conflictRuleProvider.createForm();
            refreshConflictsInView();
            if (conflictCalculator != null) {
                conflictCalculator.setCalendarView(calendarView);
            }
            conflictRuleProvider.showFormWindow(primaryStage, form, () -> {
                ruleForms = conflictRuleProvider.getRules();
                setupPrimaryStage(primaryStage, calendarView, null, null);
            });
        };

        conflictCalculator = new ConflictResolutionCalculator(ruleForms, personForms);
        conflictCalculator.setCalendarView(calendarView);
        conflictRuleProvider.setCalculator(conflictCalculator);

        setupPrimaryStage(primaryStage, calendarView, addPersonHandler, addConflictRuleHandler);
    }

    private void refreshConflictsInView() {
        if (calendarView == null || conflictCalculator == null) return;
        if (calendarView.getCalendars().isEmpty()) return;

        calendarView.getCalendars().forEach(calendar -> {
            final int totalHoursInCalendar = calendar.findEntries("").stream()
                    .mapToInt(e -> {
                        Entry<?> entry = (Entry<?>) e;
                        boolean hasConflict = conflictCalculator.hasConflict(entry);
                        final EntryViewBase<?> entryView = calendarView.findEntryView(entry);
                        if (entryView != null) {
                            if (hasConflict) {
                                entryView.setStyle(WARNING_STYLE);
                            } else {
                                entryView.setStyle("");
                            }
                        }
                        LocalDateTime start = LocalDateTime.of(entry.getStartDate(), entry.getStartTime());
                        LocalDateTime end = LocalDateTime.of(entry.getEndDate(), entry.getEndTime());
                        return (int) java.time.Duration.between(start, end).toHours();
                    }).sum();

            // Update working hours in GreatCalendar (cached version)
            if (cachedCalendars != null) {
                cachedCalendars.stream()
                        .filter(gc -> gc.getName().equals(calendar.getName()))
                        .findFirst()
                        .ifPresent(gc -> gc.setWorkingHours(totalHoursInCalendar));
            }
        });
    }

    private void openProfileSelectionWindow(EmployeeFormProvider formProvider) {
        Stage stage = new Stage();
        stage.setTitle("Select Profile to Edit");

        ListView<PersonalProfile> listView = new ListView<>();
        listView.setStyle(TEXT_STYLE);
        if (personForms != null) {
            listView.getItems().addAll(personForms);
        }

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(PersonalProfile item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
                setStyle(TEXT_STYLE);
            }
        });

        Button editButton = new Button("Edit");
        Button closeButton = new Button("Close");

        editButton.setStyle(StyleConstants.BUTTON_STYLE_LARGE);
        closeButton.setStyle(StyleConstants.BUTTON_STYLE_LARGE);

        editButton.setOnAction(e -> {
            PersonalProfile selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                formProvider.editEmployee(selected, () -> refreshConflictsInView());
            }
        });

        closeButton.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, editButton, closeButton);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10, listView, buttons);
        layout.setPadding(new Insets(10));
        stage.setScene(new Scene(layout, 300, 400));
        stage.show();
    }

    private void setupPrimaryStage(Stage primaryStage, final CalendarView calendarView, EventHandler<ActionEvent> personHandler, EventHandler<ActionEvent> conflictHandler) {
        refreshConflictsInView();

        // Regain handlers from cache if required
        personHandler = (personHandler == null) ? cachedPersonHandler : personHandler;
        conflictHandler = (conflictHandler == null) ? cachedConflictHandler : conflictHandler;
        EventHandler<ActionEvent> refreshHandler = e -> refreshConflictsInView();
        EventHandler<ActionEvent> editPersonHandler = e -> openProfileSelectionWindow(formProvider);

        Button addButton = createButton(personHandler, PLUS_SIGN, "Add a new personal calendar", ADD_BUTTON_STYLE, false);
        Button conflictsButton = createButton(conflictHandler, SHOCK_SIGN, "Add/Modify a calendar rule", CONFLICT_BUTTON_STYLE, false);
        Button refreshButton = createButton(refreshHandler, REFRESH_SIGN, "Refresh calendar conflicts", REFRESH_BUTTON_STYLE, true);
        Button editProfileButton = createButton(editPersonHandler, EDIT_SIGN, "Edit an existing profile", EDIT_BUTTON_STYLE, false);

        BorderPane root = new BorderPane();
        root.setCenter(calendarView);

        HBox appButtons = new HBox(BUTTON_SPACING);
        appButtons.getChildren().addAll(addButton, editProfileButton, conflictsButton, refreshButton);
        appButtons.setAlignment(Pos.TOP_CENTER);
        root.setTop(appButtons);

        primaryStage.setTitle(TITLE);
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();

        cachedPersonHandler = personHandler;
        cachedConflictHandler = conflictHandler;
    }

    private Button createButton(EventHandler<ActionEvent> handler, final String title, final String tooltip, final String style, final boolean hasAnimation) {
        if (title == null) {
            throw new IllegalArgumentException("Button title cannot be null");
        }
        Button addButton = new Button(title);
        addButton.setPrefSize(PREFERRED_BUTTON_SIZE, PREFERRED_BUTTON_SIZE);

        addButton.setOnAction(event -> {
            if (hasAnimation) {
                RotateTransition rt = new RotateTransition(Duration.millis(400), addButton);
                rt.setByAngle(360);
                rt.setInterpolator(Interpolator.EASE_OUT);
                rt.play();
            }
            if (handler != null) {
                handler.handle(event);
            }
        });

        addButton.setStyle(style);
        if (tooltip != null)
            addButton.setTooltip(new Tooltip(tooltip));

        return addButton;
    }

    private static CalendarView setupCalendarView() {
        CalendarView calendarView = new CalendarView(CalendarView.Page.DAY, CalendarView.Page.WEEK, CalendarView.Page.MONTH);
        calendarView.showWeekPage();
        calendarView.setEnableTimeZoneSupport(false);
        calendarView.setCreateEntryClickCount(ENTRY_CLICK_COUNT);
        calendarView.setShowAddCalendarButton(false);
        DetailedWeekView detailedWeekView = calendarView.getWeekPage().getDetailedWeekView();
        detailedWeekView.setShowToday(true);
        detailedWeekView.setEarlyLateHoursStrategy(EarlyLateHoursStrategy.HIDE);
        return calendarView;
    }

    public static void main(String[] args) {
        persistenceManager = new PersistenceManager();
        loadInformation();
        launch(args);
        // Save is done in stop().
    }

    private static void saveInformation() {
        cachedCalendars = getUpdatedCalendars();
        
        persistenceManager.saveInformation(cachedCalendars);
        persistenceManager.saveInformation(ruleForms);
        persistenceManager.saveInformation(personForms);
    }

    private static List<GreatCalendar> getUpdatedCalendars() {
        // Rebuild cachedCalendars from the current calendar view to capture all entry changes
        List<GreatCalendar> updatedCalendars = calendarView.getCalendars().stream()
                .map(persistenceManager.calendarSerializer::fromCalendar)
                .toList();
        
        // Copy over the working hours from the previous cachedCalendars to maintain calculated values
        if (cachedCalendars != null && !cachedCalendars.isEmpty()) {
            updatedCalendars.forEach(updatedCal -> {
                cachedCalendars.stream()
                        .filter(cached -> cached.getName().equals(updatedCal.getName()))
                        .findFirst()
                        .ifPresent(cached -> updatedCal.setWorkingHours(cached.getWorkingHours()));
            });
        }
        
        return updatedCalendars;
    }

    private static void loadInformation() {
        cachedCalendars = persistenceManager.loadInformation(GreatCalendar.class);
        ruleForms = persistenceManager.loadInformation(ConflictRule.class);
        personForms = persistenceManager.loadInformation(PersonalProfile.class);
    }

    @Override
    public void stop() throws Exception {
        try {
            saveInformation();
        } catch (Exception ex) {
            System.err.println("Failed to save application state: " + ex.getMessage());
        }
        super.stop();
    }

    private static Calendar createCalendar(String name, List<GreatCalendar.GreatEntry> entries ) {
        Calendar calendar = new Calendar(name);
        calendar.setShortName(name.substring(0,1));

        if( entries != null && !entries.isEmpty() ) {
            List<Entry> entriesToAdd = new ArrayList<>();
            entries.forEach(entry -> {
                Entry entryToAdd = persistenceManager.calendarSerializer.toEntry( entry );
                entriesToAdd.add( entryToAdd );
            });

            calendar.addEntries( entriesToAdd );
        }

        int idx = ThreadLocalRandom.current().nextInt(Calendar.Style.values().length - 1);
        Calendar.Style randomStyle = (Calendar.Style) Arrays.stream(Calendar.Style.values()).toArray()[idx];
        calendar.setStyle(randomStyle);

        return calendar;
    }
}