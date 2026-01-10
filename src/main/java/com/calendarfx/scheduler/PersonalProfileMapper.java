package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.DataField;
import com.dlsc.formsfx.model.structure.Form;

import java.util.List;
import java.util.Objects;

public class PersonalProfileMapper implements FormMapper<PersonalProfile> {

    public static final String NAME = "Name";
    public static final String EMAIL = "Email";
    public static final String JOB = "Job";
    public static final String AGE = "Age";
    public static final String PREFERRED_SHIFT = "Preferred Shift";

    @Override
    public PersonalProfile fromForm(Form form) {
        List<? extends DataField<?, ?, ?>> dataFields = getDataFields(form);

        int workingHours = 0;
        String name = getField(dataFields, NAME);
        String email = getField(dataFields, EMAIL);
        String job = getField(dataFields, JOB);
        String age = getField(dataFields, AGE);
        String preferredShift = getField(dataFields, PREFERRED_SHIFT);


        return new PersonalProfile(
                workingHours,
                email,
                job,
                Integer.parseInt(age),
                name,
                preferredShift
        );
    }

    public void fromForm(Form form, PersonalProfile personalProfile) {
        List<? extends DataField<?, ?, ?>> dataFields = getDataFields(form);

        int workingHours = 0;
        String email = getField(dataFields, EMAIL);
        String job = getField(dataFields, JOB);
        String age = getField(dataFields, AGE);
        String preferredShift = getField(dataFields, PREFERRED_SHIFT);

        personalProfile.setEmail(email);
        personalProfile.setJob(job);
        personalProfile.setAge(Integer.parseInt(age));
        personalProfile.setPreferredShift(preferredShift);
        personalProfile.setWorkingHours(workingHours);
    }

    public void toForm(Form form, PersonalProfile profile) {
            getDataFields(form).forEach(field -> {
            switch (field.getLabel()) {
                case NAME -> field.valueProperty().setValue(profile.getName());
                case AGE -> field.valueProperty().setValue(profile.getAge());
                case JOB -> field.valueProperty().setValue(profile.getJob());
                case EMAIL -> field.valueProperty().setValue(profile.getEmail());
                case PREFERRED_SHIFT -> field.valueProperty().setValue(profile.getPreferredShift());
            }
        });
    }

    private List<? extends DataField<?, ?, ?>> getDataFields(Form form) {
        return form.getFields().stream()
                .map(f -> switch (f) {
                    case DataField<?, ?, ?> dataField -> dataField;
                    default -> null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private String getField(List<? extends DataField<?, ?, ?>> dataFields, String fieldName) {
        return dataFields.stream()
                .filter(df -> fieldName.equals(df.getLabel()))
                .map(DataField::getValue)
                .map(Object::toString)
                .findFirst()
                .orElse("Unknown");
    }
}
