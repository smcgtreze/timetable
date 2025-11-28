package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.Form;
import javafx.stage.Stage;

public interface FormProvider {

    Form createForm();
    void showFormWindow(Stage stage, Form form, Runnable onSave);
}
