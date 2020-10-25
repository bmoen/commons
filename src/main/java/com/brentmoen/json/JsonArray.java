package com.brentmoen.json;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

public class JsonArray {
    private final List<Object> values = new ArrayList<>();

    public int size() {
        return values.size();
    }

    public JsonObject obj(int index) {
        Object value = values.get(index);

        if (value == null) {
            return new JsonObject();
        }

        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }

        return new JsonObject();
    }

    public JsonObject getObject(int index) {
        Object value = values.get(index);

        if (value == null) {
            return null;
        }

        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }

        throw new ClassCastException();
    }

    public String getString(int index) {
        Object value = values.get(index);

        if (value == null) {
            return null;
        }

        return value.toString();
    }
    public boolean getBoolean(int index) {
        Object value = values.get(index);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        throw new ClassCastException();
    }

    public int getInt(int index) {
        return getNumber(index).intValue();
    }

    public long getLong(int index) {
        return getNumber(index).longValue();
    }

    public float getFloat(int index) {
        return getNumber(index).floatValue();
    }

    public double getDouble(int index) {
        return getNumber(index).doubleValue();
    }

    private Number getNumber(int index) {
        Object value = values.get(index);

        if (value instanceof Number) {
            return ((Number) value);
        }

        throw new ClassCastException();
    }

    public JsonArray add(int value) {
        values.add(value);
        return this;
    }

    public JsonArray add(long value) {
        values.add(value);
        return this;
    }

    public JsonArray add(float value) {
        values.add(value);
        return this;
    }

    public JsonArray add(double value) {
        values.add(value);
        return this;
    }

    public JsonArray add(Number value) {
        values.add(Objects.requireNonNull(value));
        return this;
    }

    public JsonArray add(boolean value) {
        values.add(value);
        return this;
    }

    public JsonArray add(String value) {
        values.add(Objects.requireNonNull(value));
        return this;
    }

    public JsonArray add(JsonObject value) {
        values.add(Objects.requireNonNull(value));
        return this;
    }

    public JsonArray add(JsonArray value) {
        values.add(Objects.requireNonNull(value));
        return this;
    }

    public JsonArray addRaw(String code) {
        values.add(new JsonObject.RawJS(Objects.requireNonNull(code)));
        return this;
    }

    public JsonArray addAll(Collection<?> values) {
        for (Object value : values) {
            Objects.requireNonNull(value);

            if (value instanceof Integer || value instanceof Long || value instanceof String || value instanceof Boolean || value instanceof Double || value instanceof Float || value instanceof BigDecimal || value instanceof JsonArray || value instanceof JsonObject) {
                this.values.add(value);
            } else {
                throw new UnsupportedOperationException("Unexpected value type: " + value.getClass().getName());
            }
        }

        return this;
    }

    JsonArray addObject(Object value) {
        values.add(Objects.requireNonNull(value));
        return this;
    }

    public List<JsonObject> toObjectList() {
        List<JsonObject> objects = new ArrayList<>();

        for (Object value : values) {
            if (value instanceof JsonObject) {
                objects.add((JsonObject) value);
            }
        }

        return Collections.unmodifiableList(objects);
    }

    public List<String> toStringList() {
        List<String> objects = new ArrayList<>();

        for (Object value : values) {
            if (value instanceof String) {
                objects.add((String) value);
            }
        }

        return Collections.unmodifiableList(objects);
    }

    public String encode() {
        if (values.isEmpty()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(JsonObject.encodeValue(values.get(0)));

        for (int i = 1, size = values.size(); i < size; i++) {
            builder.append(",");
            builder.append(JsonObject.encodeValue(values.get(i)));
        }

        builder.append("]");
        return builder.toString();
    }

    public static JsonArray decode(String jsonArray) {
        try {
            return decode(new ByteArrayInputStream(jsonArray.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonArray decode(InputStream inputStream) throws IOException {
        return decode(new JsonObject.JsonInputStreamReader(inputStream));
    }

    static JsonArray decode(JsonObject.JsonInputStreamReader reader) throws IOException {
        reader.expect('[');
        JsonArray array = new JsonArray();
        int b;

        do {
            array.addObject(JsonObject.readValue(reader));
            b = reader.readAndSkipWhitespace();
        } while (b == ',');

        if (b != ']') {
            throw new RuntimeException("Failed to parse JSON, expected ']'");
        }

        return array;
    }
}
