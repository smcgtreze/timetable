package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.Form;
import javafx.stage.Stage;

public interface FormProvider<T> {

    Form createForm();
    Form createForm(T object);
    void showFormWindow(Stage stage, Form form, Runnable onSave);
}
