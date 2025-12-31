package com.calendarfx.scheduler;

import com.dlsc.formsfx.model.structure.DataField;
import com.dlsc.formsfx.model.structure.Form;

import java.util.List;
import java.util.Objects;

public class PersonalProfileMapper implements FormMapper<PersonalProfile> {

    @Override
    public PersonalProfile fromForm(Form form) {
        List<? extends DataField<?, ?, ?>> dataFields = getDataFields(form);

        int workingHours = 0;
        String name = getField(dataFields, "Name");
        String email = getField(dataFields, "Email");
        String job = getField(dataFields, "Job");
        String age = getField(dataFields, "Age");
        String preferredShift = getField(dataFields, "Preferred Shift");


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
        String email = getField(dataFields, "Email");
        String job = getField(dataFields, "Job");
        String age = getField(dataFields, "Age");
        String preferredShift = getField(dataFields, "Preferred Shift");

        personalProfile.setEmail(email);
        personalProfile.setJob(job);
        personalProfile.setAge(Integer.parseInt(age));
        personalProfile.setPreferredShift(preferredShift);
        personalProfile.setWorkingHours(workingHours);
    }

    public void toForm(Form form, PersonalProfile profile) {
            getDataFields(form).forEach(field -> {
            switch (field.getLabel()) {
                case "Name" -> field.valueProperty().setValue(profile.getName());
                case "Age" -> field.valueProperty().setValue(profile.getAge());
                case "Job" -> field.valueProperty().setValue(profile.getJob());
                case "Email" -> field.valueProperty().setValue(profile.getEmail());
                case "Preferred shift" -> field.valueProperty().setValue(profile.getPreferredShift());
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
