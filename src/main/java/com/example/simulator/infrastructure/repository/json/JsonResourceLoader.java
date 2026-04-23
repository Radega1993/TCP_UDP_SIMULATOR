package com.example.simulator.infrastructure.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

final class JsonResourceLoader {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonResourceLoader() {
    }

    static <T> T readResource(String path, Class<T> type) {
        try (InputStream inputStream = JsonResourceLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("No se encontró el recurso " + path);
            }
            return OBJECT_MAPPER.readValue(inputStream, type);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer el recurso " + path, exception);
        }
    }
}
