package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.dlsc.formsfx.view.util.ColSpan;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class EmployeeFormProvider implements FormProvider<PersonalProfile> {

    private final List<String> preferredShift;
    private final int width;
    private final int height;
    private final int buttonSpacing;
    private final PersonalProfileMapper mapper;

    // Centralized error messages
    private static final String REQUIRED_MSG = "This field is required";
    private static final String NAME_ERROR = "Name must have at least 2 characters";
    private static final String AGE_ERROR = "Age must be at least 1";
    private static final String JOB_ERROR = "Job must have at least 2 characters";
    private static final String EMAIL_ERROR = "Email must have at least 2 characters";

    public EmployeeFormProvider(List<String> preferredShift, int width, int height, int buttonSpacing) {
        this.preferredShift = preferredShift;
        this.width = width;
        this.height = height;
        this.buttonSpacing = buttonSpacing;
        mapper = new PersonalProfileMapper();
    }

    // --- Field builders ---------------------------------------------------------
    private StringField nameField(String value) {
        return Field.ofStringType(value)
                .label("Name")
                .placeholder("Insert person name")
                .required(REQUIRED_MSG)
                .validate(StringLengthValidator.atLeast(2, NAME_ERROR));
    }

    private IntegerField ageField(Integer value) {
        return Field.ofIntegerType(value)
                .label("Age")
                .format("Invalid number format")
                .placeholder("Insert person age")
                .required(REQUIRED_MSG)
                .span(ColSpan.HALF)
                .validate(IntegerRangeValidator.atLeast(1, AGE_ERROR));
    }

    private StringField jobField(String value) {
        return Field.ofStringType(value)
                .label("Job")
                .placeholder("Insert person job")
                .required(REQUIRED_MSG)
                .validate(StringLengthValidator.atLeast(2, JOB_ERROR));
    }

    private StringField emailField(String value) {
        return Field.ofStringType(value)
                .label("Email")
                .placeholder("Insert person email")
                .required(REQUIRED_MSG)
                .validate(StringLengthValidator.atLeast(2, EMAIL_ERROR));
    }

    private Field<?> preferredShiftField() {
        return Field.ofSingleSelectionType(preferredShift)
                .label("Preferred shift")
                .span(ColSpan.HALF);
    }

    // --- Form builders ----------------------------------------------------------
    @Override
    public Form createForm() {
        return buildForm(
                nameField(""),
                ageField(30),
                jobField(""),
                emailField("")
        );
    }

    @Override
    public Form createForm(PersonalProfile profile) {
        return buildForm(
                nameField(profile.getName()),
                ageField(profile.getAge()),
                jobField(profile.getJob()),
                emailField(profile.getEmail())
        );
    }

    private Form buildForm(Field<?> name,
                           Field<?> age,
                           Field<?> job,
                           Field<?> email) {
        return Form.of(
                Group.of(name, age, job, email),
                Section.of(preferredShiftField())
                        .title("Work hours Configuration")
        ).title("Form");
    }

    @Override
    public void showFormWindow(Stage stage, Form form, Runnable onSave) {
        FormRenderer renderer = new FormRenderer(form);
        Button saveButton = new Button("Save and Exit");

        BorderPane root = new BorderPane();
        HBox buttons = new HBox(buttonSpacing, saveButton);
        buttons.setAlignment(Pos.TOP_CENTER);

        root.setRight(buttons);
        root.setCenter(renderer);

        Scene scene = new Scene(root, width, height);
        stage.setScene(scene);
        stage.setTitle("Employee Form");
        stage.show();

        saveButton.setOnAction(e -> {
            stage.close();
            onSave.run();
        });
    }

    protected void editEmployee(PersonalProfile employee, Runnable onSave) {
        Form editForm = createForm(employee);

        Stage editStage = new Stage();
        editStage.setTitle("Edit PersonalProfile");

        FormRenderer renderer = new FormRenderer(editForm);

        Button save = new Button("Save");
        save.setOnAction(ev -> {
            mapper.fromForm(editForm, employee);
            onSave.run();   // refresh UI, re-save JSON, etc.
            editStage.close();
        });

        VBox box = new VBox(10, renderer, save);
        box.setPadding(new Insets(10));

        editStage.setScene(new Scene(box));
        editStage.show();

    }
}