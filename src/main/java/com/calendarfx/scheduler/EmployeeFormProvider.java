package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.structure.Section;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import com.dlsc.formsfx.view.util.ColSpan;

public class EmployeeFormProvider implements FormProvider {

    private final List<String> preferredShift;
    private final int width;
    private final int height;
    private final int buttonSpacing;

    public EmployeeFormProvider(List<String> preferredShift, int width, int height, int buttonSpacing) {
        this.preferredShift = preferredShift;
        this.width = width;
        this.height = height;
        this.buttonSpacing = buttonSpacing;
    }

    @Override
    public Form createForm() {
        Form form = Form.of(
                Group.of(
                        Field.ofStringType("name")
                                .label("Name")
                                .placeholder("Insert person name")
                                .required("required_error_message")
                                .validate(StringLengthValidator.atLeast(2, "name_error_message")),
                        Field.ofIntegerType(30)
                                .label("Age")
                                .format("format_error_message")
                                .placeholder("Insert person age")
                                .required("required_error_message")
                                .span(ColSpan.HALF)
                                .validate(IntegerRangeValidator.atLeast(1, "age_error_message")),
                        Field.ofStringType("Job")
                                .label("Job")
                                .placeholder("Insert person job")
                                .required("required_error_message")
                                .validate(StringLengthValidator.atLeast(2, "job_error_message")),
                        Field.ofStringType("email@")
                                .label("Email")
                                .placeholder("Insert person email")
                                .required("required_error_message")
                                .validate(StringLengthValidator.atLeast(2, "email_error_message"))
                ),
                Section.of(
                        Field.ofIntegerType(40)
                                .label("Working Hours")
                                .format("format_error_message")
                                .placeholder("population_placeholder")
                                .required("required_error_message")
                                .span(ColSpan.HALF)
                                .validate(IntegerRangeValidator.atLeast(1, "population_error_message")),
                        Field.ofSingleSelectionType(preferredShift)
                                .label("Preferred shift")
                                .span(ColSpan.HALF)
                ).title("Work hours Configuration")
        ).title("Form");
        return form;
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
}