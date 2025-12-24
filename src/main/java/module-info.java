module com.calendarfx.scheduler {
    requires transitive javafx.graphics;
    requires fr.brouillard.oss.cssfx;
    requires javafx.controls;
    requires com.calendarfx.view;
    requires com.dlsc.formsfx;
    requires com.fasterxml.jackson.databind;
    opens com.calendarfx.scheduler to com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires javafx.base;
    requires com.fasterxml.jackson.annotation;

    exports com.calendarfx.scheduler;
}