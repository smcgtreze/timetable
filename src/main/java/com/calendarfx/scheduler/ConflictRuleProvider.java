package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.structure.SingleSelectionField;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConflictRuleProvider implements FormProvider {

    public static final int HGAP = 10;
    public static final int VGAP = 5;
    public static final String FIELD = "Field";
    public static final String OPERATOR = "Operator";
    public static final String VALUE = "Value";
    public static final String ACTIVE = "Active";
    public static final String SAVE_AND_EXIT = "Save and Exit";
    public static final String GREAT_BUTTON_STYLE_ORANGE = "-fx-background-color: orange; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;";
    public static final String GREAT_BUTTON_STYLE_RED = "-fx-background-color: red; -fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 12px;";
    private final List<String> preferredShift;
    private final int width;
    private final int height;
    private final int buttonSpacing;

    private final List<ConflictRule> rules = new ArrayList<>();

    public ConflictRuleProvider(List<String> preferredShift, int width, int height, int buttonSpacing) {
        this.preferredShift = preferredShift;
        this.width = width;
        this.height = height;
        this.buttonSpacing = buttonSpacing;
    }

    // ---------------------------------------------------------
    // 1. FORM CREATION
    // ---------------------------------------------------------

    @Override
    public Form createForm() {
        return Form.of(
        ).title("Conflict Rules");
    }

    @Override
    public void showFormWindow(Stage stage, Form form, Runnable onSave) {
        FormRenderer renderer = new FormRenderer(form);

        VBox ruleList = new VBox(5);
        ruleList.setPadding(new Insets(this.buttonSpacing, 0, 0, 0));

        ComboBox<ConflictRule.FieldType> fieldBox = new ComboBox<>();
        fieldBox.getItems().addAll(ConflictRule.FieldType.values());

        ComboBox<ConflictRule.Operator> operatorBox = new ComboBox<>();
        operatorBox.getItems().addAll(ConflictRule.Operator.values());

        TextField valueField = new TextField();

        CheckBox activeBox = new CheckBox("Active");
        activeBox.setSelected(true);

        Button defineRuleButton = new Button("Define Conflict Rule");
        Button duplicateRuleButton = new Button("Duplicate Conflict Rule");

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

        Button saveButton = new Button(SAVE_AND_EXIT);

        HBox buttons = new HBox(buttonSpacing, saveButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(this.buttonSpacing));
        layout.getChildren().addAll(renderer, ruleBuilder, ruleList, buttons);

        Scene scene = new Scene(layout, width, height);
        stage.setScene(scene);
        stage.setTitle("Conflict Rule Editor");
        stage.show();

        saveButton.setOnAction(e -> {
            stage.close();
            onSave.run();
        });

        defineRuleButton.setOnAction(e -> {
            defineRule(fieldBox, operatorBox, valueField, activeBox, ruleList, false);
        });

        duplicateRuleButton.setOnAction(e -> {
            defineRule(fieldBox, operatorBox, valueField, activeBox, ruleList, true);
        });
    }

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

        // UI entry for current rule
        HBox ruleEntry = new HBox(10);
        ruleEntry.setAlignment(Pos.CENTER_LEFT);

        Label ruleLabel = new Label(formatRule(rule));

        CheckBox activeToggle = new CheckBox("Active");
        activeToggle.setSelected(rule.isActive());
        activeToggle.selectedProperty().addListener((obs, old, val) -> rule.setActive(val));

        Button editButton = new Button("Edit");
        editButton.setTooltip(new Tooltip("Edit related rule"));

        Button deleteButton = new Button("Delete");
        deleteButton.setTooltip(new Tooltip("Delete related rule"));

        // EDIT RULE
        editButton.setOnAction(e -> {
            editRule(rule, ruleLabel);
        });

        // DELETE RULE
        deleteButton.setOnAction(e -> {
            deleteRule(rule); // remove from model
            ruleList.getChildren().remove(ruleEntry); // remove from UI
        });

        ruleEntry.getChildren().addAll(ruleLabel, activeToggle, editButton, deleteButton);
        ruleList.getChildren().add(ruleEntry);

        if (!skipClear) {
            fieldBox.setValue(null);
            operatorBox.setValue(null);
            valueField.clear();
            activeBox.setSelected(true);
        }
    }

    private void editRule(ConflictRule rule, Label ruleLabel) {
        Form editForm = createRuleForm(rule);
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

    private Form createRuleForm(ConflictRule rule) {
        SingleSelectionField<ConflictRule.FieldType> fieldField =
                Field.ofSingleSelectionType(
                                FXCollections.observableArrayList(ConflictRule.FieldType.values())
                        )
                        .label(FIELD);

        fieldField.select(rule.getField().ordinal());
        fieldField.selectionProperty().addListener((obs, old, val) -> rule.setField(val));

        SingleSelectionField<ConflictRule.Operator> operatorField =
                Field.ofSingleSelectionType(
                                FXCollections.observableArrayList(ConflictRule.Operator.values())
                        )
                        .label(OPERATOR);

        operatorField.select(rule.getOperator().ordinal());
        operatorField.selectionProperty().addListener((obs, old, val) -> rule.setOperator(val));

        var valueField =
                Field.ofStringType(rule.getValue())
                        .label(VALUE);
        valueField.valueProperty().addListener((obs, old, val) -> rule.setValue(val));

        var activeField =
                Field.ofBooleanType(rule.isActive())
                        .label(ACTIVE);
        activeField.valueProperty().addListener((obs, old, val) -> rule.setActive(val));

        return Form.of(
                Group.of(fieldField, operatorField, valueField, activeField)
        ).title("Conflict Rule");
    }

    public List<ConflictRule> getRules() {
        return Collections.unmodifiableList(rules);
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

}