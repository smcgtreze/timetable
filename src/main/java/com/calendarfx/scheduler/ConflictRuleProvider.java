package com.calendarfx.scheduler;

import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class ConflictRuleProvider implements FormProvider<ConflictRule> {

    public static final int HGAP = 10;
    public static final int VGAP = 5;
    public static final String FIELD = "Field";
    public static final String OPERATOR = "Operator";
    public static final String VALUE = "Value";
    public static final String ACTIVE = "Active";
    public static final String SAVE_AND_EXIT = "Save and Exit";
    private final int width;
    private final int height;
    private final int buttonSpacing;

    private List<ConflictRule> rules = new ArrayList<>();
    private VBox ruleList;
    private ConflictResolutionCalculator calculator;

    public ConflictRuleProvider(List<String> preferredShift, int width, int height, int buttonSpacing) {
        this.width = width;
        this.height = height;
        this.buttonSpacing = buttonSpacing;
    }

    public void setCalculator(ConflictResolutionCalculator calculator) {

        this.calculator = calculator;
    }
    // ---------------------------------------------------------
    // 1. FORM CREATION
    // ---------------------------------------------------------
    @Override
    public Form createForm() {
        ruleList = new VBox(5);
        ruleList.setPadding(new Insets(this.buttonSpacing, 0, 0, 0));

        rules.forEach(rule ->
            createSupportButtons(ruleList, rule)
        );

        return Form.of(
        ).title("Rules");
    }

    @Override
    public void showFormWindow(Stage stage, Form form, Runnable onSave) {
        FormRenderer renderer = new FormRenderer(form);

        ComboBox<ConflictRule.FieldType> fieldBox = new ComboBox<>();
        fieldBox.getItems().addAll(ConflictRule.FieldType.values());

        ComboBox<ConflictRule.Operator> operatorBox = new ComboBox<>();
        operatorBox.getItems().addAll(ConflictRule.Operator.values());

        TextField valueField = new TextField();

        CheckBox activeBox = new CheckBox(ACTIVE);
        activeBox.setSelected(true);

        Button defineRuleButton = new Button("Define Rule");
        Button duplicateRuleButton = new Button("Duplicate Rule");
        Button resolveButton = new Button("Resolve Conflicts!");
        Button applyButton = new Button("Apply resolutions");

        GridPane ruleBuilder = new GridPane();
        ruleBuilder.setHgap(HGAP);
        ruleBuilder.setVgap(VGAP);

        // Add an element to the GridPane in format: Label, Column, Row
        ruleBuilder.add(new Label(FIELD), 0, 0);
        ruleBuilder.add(fieldBox, 0, 1);

        ruleBuilder.add(new Label(OPERATOR), 1, 0);
        ruleBuilder.add(operatorBox, 1, 1);

        ruleBuilder.add(new Label(VALUE), 2, 0);
        ruleBuilder.add(valueField, 2, 1);

        ruleBuilder.add(new Label(ACTIVE), 3, 0);
        ruleBuilder.add(activeBox, 3, 1);

        ruleBuilder.add(defineRuleButton, 4, 1);
        ruleBuilder.add(duplicateRuleButton, 5, 1);
        ruleBuilder.add(resolveButton, 6, 1);
        ruleBuilder.add(applyButton, 7, 1);

        Map<String, ConflictRule> currentConflicts = calculator.getCurrentConflicts();
        ConflictTable conflictTable = new ConflictTable(currentConflicts);
        calculator.getCurrentConflicts().addListener((MapChangeListener<String, ConflictRule>) change -> conflictTable.refreshData(calculator.getCurrentConflicts()));

        Button saveButton = new Button(SAVE_AND_EXIT);

        HBox buttons = new HBox(buttonSpacing, saveButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(this.buttonSpacing));

        Label status = new Label();

        CalendarView calendarView = calculator.getSnapshotCalendarView();
        VBox.setVgrow(calendarView, Priority.ALWAYS);
        layout.getChildren().addAll(renderer, ruleBuilder, ruleList, buttons, conflictTable, calendarView, status);

        Scene scene = new Scene(layout, width, height);
        makeCalendarViewReadOnly(calendarView);
        calendarView.showWeekPage();
        stage.setScene(scene);
        stage.setTitle("Rule Editor");
        stage.show();

        saveButton.setOnAction(e -> {
            stage.close();
            onSave.run();
        });

        defineRuleButton.setOnAction(e -> {
            defineRule(fieldBox, operatorBox, valueField, activeBox, ruleList, false);
            status.setText("Rule defined successfully");
        });

        duplicateRuleButton.setOnAction(e -> {
            defineRule(fieldBox, operatorBox, valueField, activeBox, ruleList, true);
            status.setText("Rule duplicated successfully");
        });

        resolveButton.setOnAction(e -> {
            resolveHandler();
            conflictTable.refreshData(calculator.getCurrentConflicts());
            status.setText("Resolved successfully");
        });

        applyButton.setOnAction(e -> {
            calculator.applySnapshot(
                    calculator.getOriginalCalendarView(),
                    calculator.getSnapshotCalendarView()
            );
            conflictTable.refreshData(calculator.getCurrentConflicts());
            status.setText("Snapshot applied");
        });
    }

    private void makeCalendarViewReadOnly(CalendarView calendarView) {
        calendarView.setShowSearchField(false);
        calendarView.setShowToolBar(false);
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowPrintButton(false);
        calendarView.setEntryDetailsPopOverContentCallback(param -> null);
        calendarView.addEventFilter(MouseEvent.DRAG_DETECTED, Event::consume);
        calendarView.addEventFilter(MouseEvent.MOUSE_DRAGGED, Event::consume);
        calendarView.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.isPrimaryButtonDown()) {
                event.consume();
            }
        });
    }

    private void resolveHandler() { calculator.calculate(); }

    private void defineRule(ComboBox<ConflictRule.FieldType> fieldBox,
                            ComboBox<ConflictRule.Operator> operatorBox,
                            TextField valueField,
                            CheckBox activeBox,
                            VBox ruleList,
                            boolean skipClear) {

        if (fieldBox.getValue() == null || operatorBox.getValue() == null || valueField.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Rule");
            alert.setHeaderText("Missing required fields");
            alert.setContentText("Please select a field, an operator, and enter a value.");
            alert.showAndWait();
            return;
        }

        ConflictRule rule = new ConflictRule(
                fieldBox.getValue(),
                operatorBox.getValue(),
                valueField.getText(),
                activeBox.isSelected()
        );
        rules.add(rule);

        // UI entry for support of current rule
        createSupportButtons(ruleList, rule);

        if (!skipClear) {
            fieldBox.setValue(null);
            operatorBox.setValue(null);
            valueField.clear();
            activeBox.setSelected(true);
        }
    }

    private void createSupportButtons(VBox ruleList, ConflictRule rule) {
        Label ruleLabel = new Label(formatRule(rule));

        CheckBox activeToggle = new CheckBox(ACTIVE);
        activeToggle.setSelected(rule.isActive());
        activeToggle.selectedProperty().addListener((obs, old, val) -> rule.setActive(val));

        Button editButton = new Button("Edit");
        editButton.setTooltip(new Tooltip("Edit related rule"));

        Button deleteButton = new Button("Delete");
        deleteButton.setTooltip(new Tooltip("Delete related rule"));

        HBox ruleEntry = new HBox(10);
        ruleEntry.setAlignment(Pos.CENTER_LEFT);

        // EDIT RULE
        editButton.setOnAction(e ->
            editRule(rule, ruleLabel)
        );

        // DELETE RULE
        deleteButton.setOnAction(e -> {
            deleteRule(rule); // remove from model
            ruleList.getChildren().remove(ruleEntry); // remove from UI
        });

        ruleEntry.getChildren().addAll(ruleLabel, activeToggle, editButton, deleteButton);
        ruleList.getChildren().add(ruleEntry);
    }

    private void editRule(ConflictRule rule, Label ruleLabel) {
        Form editForm = createForm(rule);
        Stage editStage = new Stage();
        editStage.setTitle("Edit Rule");

        FormRenderer renderer = new FormRenderer(editForm);
        Button save = new Button("Save");
        save.setOnAction(ev -> {
            ruleLabel.setText(formatRule(rule)); // refresh label
            editStage.close();
        });

        VBox box = new VBox(10, renderer, save);
        box.setPadding(new Insets(10));
        editStage.setScene(new Scene(box));
        editStage.show();
    }

    private String formatRule(ConflictRule rule) {
        return rule.getField() + " " +
                rule.getOperator() + " " +
                rule.getValue();
    }

    // ---------------------------------------------------------
    // 2. RULE MANAGEMENT
    // ---------------------------------------------------------
    @Override
    public Form createForm(ConflictRule rule) {
        return Form.of(
                Group.of(
                        fieldTypeField(rule),
                        operatorField(rule),
                        valueField(rule),
                        activeField(rule)
                )
        ).title("Conflict Rule");
    }

    private SingleSelectionField<ConflictRule.FieldType> fieldTypeField(ConflictRule rule) {
        var field = Field.ofSingleSelectionType(
                FXCollections.observableArrayList(ConflictRule.FieldType.values())
        ).label(FIELD);

        field.select(rule.getField().ordinal());
        field.selectionProperty().addListener((obs, old, val) -> rule.setField(val));

        return field;
    }

    private SingleSelectionField<ConflictRule.Operator> operatorField(ConflictRule rule) {
        var field = Field.ofSingleSelectionType(
                FXCollections.observableArrayList(ConflictRule.Operator.values())
        ).label(OPERATOR);

        field.select(rule.getOperator().ordinal());
        field.selectionProperty().addListener((obs, old, val) -> rule.setOperator(val));

        return field;
    }

    private StringField valueField(ConflictRule rule) {
        var field = Field.ofStringType(rule.getValue())
                .label(VALUE);

        field.valueProperty().addListener((obs, old, val) -> rule.setValue(val));

        return field;
    }

    private BooleanField activeField(ConflictRule rule) {
        var field = Field.ofBooleanType(rule.isActive())
                .label(ACTIVE);

        field.valueProperty().addListener((obs, old, val) -> rule.setActive(val));

        return field;
    }

    public List<ConflictRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public void setRules( final List<ConflictRule> conflictRules) {
        this.rules = conflictRules;
    }

    public void deleteRule(ConflictRule rule) {
        rules.remove(rule);
    }

    public ConflictRule duplicateRule(ConflictRule rule) {
        ConflictRule copy = new ConflictRule(
                rule.getField(),
                rule.getOperator(),
                rule.getValue(),
                rule.isActive());
        rules.add(copy);
        return copy;
    }

    public class ConflictTable extends TableView<ConflictTable.Row> {
        public void refreshData(Map<String, ConflictRule> conflicts) {
            getItems().clear();
            Map<String,Entry<?>> entryMap = calculator.getEntriesMap();
            conflicts.forEach((entry, rule) ->
                    getItems().add(new Row(getEntryDescription(entryMap.get(entry)), rule.getFullName()))
            );
        }

        public record Row(String entry, String rule) {
        }

        public ConflictTable(Map<String, ConflictRule> conflicts) {

            TableColumn<Row, String> entryCol = new TableColumn<>("Entry");
            entryCol.setCellValueFactory(c ->
                    new SimpleStringProperty(c.getValue().entry()));

            TableColumn<Row, String> ruleCol = new TableColumn<>("Rule");
            ruleCol.setCellValueFactory(c ->
                    new SimpleStringProperty(c.getValue().rule()));

            getColumns().addAll(List.of(entryCol, ruleCol));

            Map<String,Entry<?>> entryMap = calculator.getEntriesMap();
            conflicts.forEach((entry, rule) ->
                getItems().add(new Row(getEntryDescription( entryMap.get(entry) ), rule.getFullName()))
            );

            setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
            setPrefHeight(200);
        }

        private String getEntryDescription(Entry<?> entry) {
            return entry.getTitle() + " " + entry.getStartDate() + " " + entry.getStartTime() + " " + entry.getEndDate() + " " + entry.getEndTime();
        }
    }
}