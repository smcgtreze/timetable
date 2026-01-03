package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.Form;

public interface FormMapper<T> {
    T fromForm(Form form);
}
