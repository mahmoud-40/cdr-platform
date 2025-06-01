package com.cdr.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class ArrayToLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        int[] arr = p.readValueAs(int[].class);
        // year, month, day, hour, minute
        if (arr.length == 5) {
            return LocalDateTime.of(arr[0], arr[1], arr[2], arr[3], arr[4]);
        }
        throw new IllegalArgumentException("Invalid date array: " + java.util.Arrays.toString(arr));
    }
}