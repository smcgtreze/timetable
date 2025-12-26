package com.calendarfx.scheduler;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.CalendarView.Page;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import com.calendarfx.view.DetailedWeekView;
import com.dlsc.formsfx.model.structure.*;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CalendarApp extends Application {

    public static final String GREAT_BUTTON_STYLE = "-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";
    public static final String GREAT_BUTTON_STYLE_2 = "-fx-background-color: orange; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";
    public static final String GREAT_BUTTON_STYLE_3 = "-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";
    public static final int PREFERED_BUTTON_SIZE = 50;
    private static final int ENTRY_CLICK_COUNT = 2;
    private static final int WIDTH = 1300;
    private static final int HEIGHT = 1000;
    private static final String TITLE = "Calendar";
    private static final int BUTTON_SPACING = 10;
    public static final String WARNING_SIGN = "⚠";
    public static final String REFRESH_SIGN ="🗘";
    public static final String SHOCK_SIGN = "⚡";
    public static final String PLUS_SIGN = "+";
    private static List<GreatCalendar> cachedCalendars;
    private static PersistenceManager persistenceManager;
    private static EventHandler<ActionEvent> cachedPersonHandler;
    private final List<String> preferredShift = Arrays.asList("nineToFive", "nineToSix", "eightToFour", "eightToFive");
    private static List<PersonalProfile> personForms;
    private static List<ConflictRule> ruleForms;
    private static CalendarSource familyCalendarSource;
    private static EventHandler<ActionEvent> cachedConflictHandler;

    @Override
    public void start(Stage primaryStage) {
        CalendarView calendarView = setupCalendarView();

        familyCalendarSource = new CalendarSource("Family");

        calendarView.getCalendarSources().setAll(familyCalendarSource);
        calendarView.setRequestedTime(LocalTime.now());

        // Add cached calendars
        if( cachedCalendars != null && !cachedCalendars.isEmpty()){
            cachedCalendars.forEach(calendar -> {
                Calendar newCalendar = createCalendar( calendar.getName(), calendar.entries );
                familyCalendarSource.getCalendars().add(newCalendar);
            });
        }

        EmployeeFormProvider formProvider =
                new EmployeeFormProvider(preferredShift, WIDTH, HEIGHT, BUTTON_SPACING);

        // PERSON CALENDAR HANDLING
        EventHandler<ActionEvent> addPersonHandler = e -> {
            Form form = formProvider.createForm();
            formProvider.showFormWindow(primaryStage, form, () -> {

                String name = cachePersonForm(form);
                Calendar calendar = createCalendar(name, List.of());
                familyCalendarSource.getCalendars().add(calendar);

                GreatCalendar gc = persistenceManager.calendarSerializer.fromCalendar(calendar);
                cachedCalendars.add(gc);
                setupPrimaryStage(primaryStage, calendarView, null, null);
            });
        };

        // CONFLICT RULE HANDLING
        ConflictRuleProvider conflictRuleProvider =
                new ConflictRuleProvider(preferredShift, WIDTH, HEIGHT, BUTTON_SPACING);

        // Load cached ruleForms
        if(ruleForms != null && !ruleForms.isEmpty()){
            conflictRuleProvider.setRules( ruleForms );
        }

        EventHandler<ActionEvent> addConflictRuleHandler = e -> {
            Form form = conflictRuleProvider.createForm();
            conflictRuleProvider.showFormWindow(primaryStage, form, () -> {
                ruleForms = conflictRuleProvider.getRules();
                setupPrimaryStage(primaryStage, calendarView, null, null);
            });
        };

        setupPrimaryStage(primaryStage, calendarView, addPersonHandler, addConflictRuleHandler);
    }

    private String cachePersonForm(Form form) {
        List<? extends DataField<?, ?, ?>> dataFields = getDataFields(form);

        // Extract the data fields from the form
        String name = getField( dataFields,"Name");
        String workingHours = getField( dataFields,"Working Hours");
        String email = getField( dataFields,"Email");
        String job = getField( dataFields,"Job");
        String age = getField( dataFields,"Age");
        String preferredShift = getField( dataFields,"Preferred Shift");

        PersonalProfile profile = new PersonalProfile(Integer.parseInt(workingHours), email, job, Integer.parseInt(age), name, preferredShift );

        // Store under that calendar name
        personForms.add(profile);
        return name;
    }

    private String getField(List<? extends DataField<?, ?, ?>> dataFields, String fieldName) {
        return dataFields.stream()
                .filter(df -> fieldName.equals(df.getLabel()))
                .map(DataField::getValue)
                .map(Object::toString)
                .findFirst()
                .orElse("Unknown");
    }

    private void refreshConflictsInView() {
        // Optimize this very well !!
        if(familyCalendarSource.getCalendars().isEmpty())
            return;

        familyCalendarSource.getCalendars().stream()
                .flatMap(c -> c.findEntries("").stream())
                .forEach(entry -> ((Entry<?>) entry).setLocation(
                        isConflict(((Entry<?>) entry)) ? WARNING_SIGN + " Conflict with one or more rules" : ""
                ));
    }

    private static List<? extends DataField<?, ?, ?>> getDataFields(Form form) {
        List<? extends DataField<?, ?, ?>> dataFields = form.getFields().stream()
                .map(f -> switch (f) {
                    case DataField<?, ?, ?> dataField -> dataField;
                    default -> null;
                })
                .filter(Objects::nonNull)
                .toList();
        return dataFields;
    }

    private void setupPrimaryStage(Stage primaryStage, final CalendarView calendarView, EventHandler<ActionEvent> personHandler, EventHandler<ActionEvent> conflictHandler) {
        refreshConflictsInView();

        // regain handlers from cache if required
        personHandler = ( personHandler == null ) ? cachedPersonHandler : personHandler;
        conflictHandler = ( conflictHandler == null ) ? cachedConflictHandler : conflictHandler;
        EventHandler<ActionEvent> refreshHandler = e -> { refreshConflictsInView();};

        Button addButton = createButton(personHandler, PLUS_SIGN,"Add a new personal calendar", GREAT_BUTTON_STYLE, false);
        Button conflictsButton = createButton(conflictHandler, SHOCK_SIGN,"Add/Modify a calendar conflict", GREAT_BUTTON_STYLE_2, false);
        Button refreshButton = createButton(refreshHandler, REFRESH_SIGN,"Refresh calendar conflicts", GREAT_BUTTON_STYLE_3, true);

        BorderPane root = new BorderPane();
        root.setCenter(calendarView);

        HBox appButtons = new HBox(BUTTON_SPACING);
        appButtons.getChildren().addAll(addButton,conflictsButton, refreshButton);
        appButtons.setAlignment(Pos.TOP_CENTER);
        root.setTop(appButtons);

        Scene scene = new Scene(root);
        CSSFX.start(scene);

        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
        primaryStage.centerOnScreen();
        primaryStage.show();

        //Store handler in cache
        cachedPersonHandler = personHandler;
        cachedConflictHandler = conflictHandler;
    }

    private Button createButton(EventHandler<ActionEvent> handler, final String title , final String tooltip, final String style, final boolean hasAnimation) {
        if( title == null )
            return null;
        Button addButton = new Button(title);
        addButton.setPrefSize(PREFERED_BUTTON_SIZE, PREFERED_BUTTON_SIZE);

        // Wrap the handler to add animation
        addButton.setOnAction(event -> {
            if (hasAnimation) {
                RotateTransition rt = new RotateTransition(Duration.millis(400), addButton);
                rt.setByAngle(360);
                rt.setInterpolator(Interpolator.EASE_OUT);
                rt.play();
            }

            // Call the original handler
            if (handler != null) {
                handler.handle(event);
            }
        });

        addButton.setStyle(style);
        if( tooltip != null )
            addButton.setTooltip(new Tooltip(tooltip));

        return addButton;
    }

    private static CalendarView setupCalendarView() {
        CalendarView calendarView = new CalendarView(Page.DAY, Page.WEEK, Page.MONTH );
        calendarView.showWeekPage();
        calendarView.setEnableTimeZoneSupport(false);
        calendarView.setCreateEntryClickCount(ENTRY_CLICK_COUNT);
        calendarView.setShowAddCalendarButton(false);

        DetailedWeekView detailedWeekView = calendarView.getWeekPage().getDetailedWeekView();
        detailedWeekView.setShowToday(true);
        detailedWeekView.setEarlyLateHoursStrategy(EarlyLateHoursStrategy.HIDE);
        return calendarView;
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

        int idx = ThreadLocalRandom.current().nextInt(Style.values().length - 1);
        Style randomStyle = (Style) Arrays.stream(Style.values()).toArray()[idx];
        calendar.setStyle(randomStyle);

        return calendar;
    }

    private boolean isConflict(Entry<?> entry) {
        if (ruleForms == null || ruleForms.isEmpty()) return false;

        boolean existsConflict = false;
        final String calendarName = entry.getCalendar().getName();
        PersonalProfile personalProfile = personForms.stream()
                .filter(p -> p.getName().equals(calendarName))
                .findFirst()
                .orElse(null);

        for (ConflictRule rule : ruleForms) {
            if (!rule.isActive()) continue;

            switch (rule.getField()) {
                case NAME -> {
                    existsConflict |= (calendarName.equals(rule.getValue()));
                }
                case WORKING_HOURS -> {
                    if (personalProfile != null) {
                        int workingHours = personalProfile.getWorkingHours();
                        existsConflict |= (workingHours == Integer.parseInt(rule.getValue()));
                    }
                }
                case PREFERRED_SHIFT -> {
                    if (personalProfile != null) {
                        Object preferredShift = personalProfile.getPreferredShift();
                        existsConflict |= (preferredShift != null && preferredShift.toString().equals(rule.getValue()));
                    }
                }
                case JOB -> {
                    if (personalProfile != null) {
                        Object job = personalProfile.getJob();
                        existsConflict |= (job != null && job.toString().equals(rule.getValue()));
                    }
                }
                case EMAIL -> {
                    if (personalProfile != null) {
                        Object email = personalProfile.getEmail();
                        existsConflict |= (email != null && email.toString().equals(rule.getValue()));
                    }
                }
            }
        }
        return existsConflict;
    }

    public static void main(String[] args) {
        persistenceManager = new PersistenceManager();
        cachedCalendars = persistenceManager.loadInformation(GreatCalendar.class);
        ruleForms = persistenceManager.loadInformation(ConflictRule.class);
        personForms = persistenceManager.loadInformation(PersonalProfile.class);

        launch(args);

        cachedCalendars = familyCalendarSource.getCalendars().stream()
                .map(persistenceManager.calendarSerializer::fromCalendar)
                .toList();

        persistenceManager.saveInformation(cachedCalendars);
        persistenceManager.saveInformation(ruleForms);
        persistenceManager.saveInformation(personForms);
    }
}
