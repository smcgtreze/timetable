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
    private static final int ENTRY_CLICK_COUNT = 2;
    private static final int WIDTH = 1300;
    private static final int HEIGHT = 1000;
    private static final String TITLE = "Calendar";
    private static final int BUTTON_SPACING = 10;
    private static List<GreatCalendar> cachedCalendars;
    private static PersistenceManager persistenceManager;
    private static EventHandler<ActionEvent> cachedHandler;
    private final List<String> preferredShift = Arrays.asList("nineToFive", "nineToSix", "eightToFour", "eightToFive");
    private static final  Map<String, Object> personForms = new HashMap<>();
    private static CalendarSource familyCalendarSource;

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
                setupPrimaryStage(primaryStage, calendarView, null);
            });
        };

        setupPrimaryStage(primaryStage, calendarView, addPersonHandler);
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

    private void setupPrimaryStage(Stage primaryStage, CalendarView calendarView, EventHandler<ActionEvent> handler) {
        Button addButton = new Button("+");
        if ( handler == null ) {
            handler = this.cachedHandler;
            // regain the handler from cache
        }
        addButton.setOnAction(handler);
        addButton.setStyle(GREAT_BUTTON_STYLE);
        addButton.setTooltip(new Tooltip("Add a new person"));

        BorderPane root = new BorderPane();
        root.setCenter(calendarView);

        HBox appButtons = new HBox(BUTTON_SPACING);
        appButtons.getChildren().add(addButton);
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
        this.cachedHandler = handler;
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

    public static void main(String[] args) {
        persistenceManager = new PersistenceManager();
        cachedCalendars = persistenceManager.loadInformation(GreatCalendar.class);

        launch(args);

        cachedCalendars = familyCalendarSource.getCalendars().stream()
                .map(persistenceManager.calendarSerializer::fromCalendar)
                .toList();

        persistenceManager.saveInformation(cachedCalendars);
    }
}
