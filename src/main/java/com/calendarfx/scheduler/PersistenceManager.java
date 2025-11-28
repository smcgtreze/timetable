package com.calendarfx.scheduler;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.nio.file.Path;
import java.util.List;

public class PersistenceManager {
    private final ObjectMapper mapper;

    public PersistenceManager() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // handle LocalDateTime
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.ANY);
    }

    public <T> void saveToFile(T object, Path filePath) {
        try {
            mapper.writeValue(filePath.toFile(), object);
            System.out.println("Saved " + object.getClass().getSimpleName() + " to " + filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save object", e);
        }
    }

    public <T> List<T> loadFromFile(Path filePath, Class<T> clazz) {
        try {
            return mapper.readValue(filePath.toFile(), mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load object", e);
        }
    }
}
