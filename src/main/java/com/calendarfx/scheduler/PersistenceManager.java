package com.calendarfx.scheduler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PersistenceManager {
    private final ObjectMapper mapper = new ObjectMapper();
    protected final GreatCalendarSerializer calendarSerializer = new GreatCalendarSerializer();
    private static final String JSON = ".json";
    private static final Path DATA_DIR = Path.of(System.getProperty("user.dir"), "data");

    public PersistenceManager() {
        mapper.registerModule(new JavaTimeModule()); // handle LocalDateTime
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
    }

    protected <T> void saveInformation(List<T> objects) {
        if( objects == null || objects.isEmpty() )
            return;
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            System.out.println("Unable to create directory");
            throw new RuntimeException(e);
        }
        this.saveToFile(objects,DATA_DIR.resolve( objects.getFirst().getClass().getSimpleName() + JSON) );
    }

    protected <T> List<T> loadInformation(Class<T> clazz) {
        try {
            Path pathToFile = DATA_DIR.resolve( clazz.getSimpleName() + JSON );
            return this.loadFromFile(pathToFile, clazz);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return new ArrayList<>();
        }
    }

    private <T> void saveToFile(T object, Path filePath) {
        try {
            mapper.writeValue(filePath.toFile(), object);
            System.out.println("Saved " + object.getClass().getSimpleName() + " to " + filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save object", e);
        }
    }

    private <T> List<T> loadFromFile(Path filePath, Class<T> clazz) {
        try {
            return mapper.readValue(filePath.toFile(), mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load object", e);
        }
    }
}
