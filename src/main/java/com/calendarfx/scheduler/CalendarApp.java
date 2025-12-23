package com.calendarfx.scheduler;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.calendarfx.view.CalendarView.Page;
import com.calendarfx.view.DayView;
import com.calendarfx.view.DayViewBase.EarlyLateHoursStrategy;
import com.calendarfx.view.DetailedWeekView;
import com.calendarfx.view.WeekView;
import com.dlsc.formsfx.model.structure.*;
import fr.brouillard.oss.cssfx.CSSFX;
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

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CalendarApp extends Application {

    public static final String UNKNOWN = "Unknown";
    public static final String NAME_LABEL = "Name";
    public static final String GREAT_BUTTON_STYLE = "-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";
    public static final String GREAT_BUTTON_STYLE_2 = "-fx-background-color: orange; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px;";
    public static final int PREFERED_BUTTON_SIZE = 50;
    private static final int ENTRY_CLICK_COUNT = 2;
    private static final int WIDTH = 1300;
    private static final int HEIGHT = 1000;
    private static final String TITLE = "Calendar";
    private static final int BUTTON_SPACING = 10;
    public static final String WARNING_SIGN = "⚠";
    public static final String SHOCK_SIGN = "⚡";
    public static final String PLUS_SIGN = "+";
    private static List<GreatCalendar> cachedCalendars;
    private static PersistenceManager persistenceManager;
    private static EventHandler<ActionEvent> cachedPersonHandler;
    private final List<String> preferredShift = Arrays.asList("nineToFive", "nineToSix", "eightToFour", "eightToFive");
    private static final  Map<String, Object> personForms = new HashMap<>();
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

                List<? extends DataField<?, ?, ?>> dataFields = getDataFields(form);
                dataFields.forEach(df -> personForms.put(df.getLabel(), df.getValue()));

                String name = dataFields.stream()
                        .filter(df -> NAME_LABEL.equals(df.getLabel()))
                        .map(DataField::getValue)
                        .map(Object::toString)
                        .findFirst()
                        .orElse(UNKNOWN);

                Calendar calendar = createCalendar(name, List.of() );
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

        Button addButton = createButton(personHandler, PLUS_SIGN,"Add a new personal calendar", GREAT_BUTTON_STYLE);
        Button conflictsButton = createButton(conflictHandler, SHOCK_SIGN,"Add/Modify a calendar conflict", GREAT_BUTTON_STYLE_2);

        BorderPane root = new BorderPane();
        root.setCenter(calendarView);

        HBox appButtons = new HBox(BUTTON_SPACING);
        appButtons.getChildren().addAll(addButton,conflictsButton);
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

    private Button createButton(EventHandler<ActionEvent> handler, final String title , final String tooltip, final String style) {
        if( title == null )
            return null;
        Button addButton = new Button(title);
        addButton.setPrefSize(PREFERED_BUTTON_SIZE, PREFERED_BUTTON_SIZE);
        addButton.setOnAction(handler);
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
        DayView dayView = calendarView.getDayPage().getDetailedDayView().getDayView();

        DetailedWeekView detailedWeekView = calendarView.getWeekPage().getDetailedWeekView();
        detailedWeekView.setShowToday(true);
        detailedWeekView.setEarlyLateHoursStrategy(EarlyLateHoursStrategy.HIDE);
        WeekView weekView = detailedWeekView.getWeekView();
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
        for (ConflictRule rule : ruleForms) {
            if (!rule.isActive()) continue;

            final String calendarName = entry.getCalendar().getName();
            switch (rule.getField()) {
                case NAME -> {
                    if (calendarName.equals(rule.getValue())) {
                        return true;
                    }
                }
                case WORKING_HOURS -> {}
                case PREFERRED_SHIFT -> {}
                case JOB -> {}
            }
        }
        return false;
    }

    public static void main(String[] args) {
        persistenceManager = new PersistenceManager();
        cachedCalendars = persistenceManager.loadInformation(GreatCalendar.class);
        ruleForms = persistenceManager.loadInformation(ConflictRule.class);

        launch(args);

        cachedCalendars = familyCalendarSource.getCalendars().stream()
                .map(persistenceManager.calendarSerializer::fromCalendar)
                .toList();

        persistenceManager.saveInformation(cachedCalendars);
        persistenceManager.saveInformation(ruleForms);
    }
}
