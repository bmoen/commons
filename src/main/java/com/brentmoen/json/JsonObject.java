package com.brentmoen.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JsonObject {
    private final Map<String, Object> values = new HashMap<>();

    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public Set<String> getKeys() {
        return values.keySet();
    }

    public JsonObject put(String key, int value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, long value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, float value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, double value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, BigDecimal value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, String value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, boolean value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, JsonObject value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, JsonArray value) {
        values.put(key, value);
        return this;
    }

    public JsonObject put(String key, LocalDate value) {
        values.put(key, "" + value);
        return this;
    }

    JsonObject putObject(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public JsonObject putRaw(String key, String code) {
        values.put(key, new RawJS(code));
        return this;
    }

    public JsonObject putNull(String key) {
        values.put(key, new RawJS("null"));
        return this;
    }

    public JsonObject putUndefined(String key) {
        values.put(key, new RawJS("undefined"));
        return this;
    }

    public JsonObject obj(String key) {
        Object value = values.get(key);

        if (value == null) {
            return new JsonObject();
        }

        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }

        return new JsonObject();
    }

    public JsonObject getObject(String key) {
        Object value = values.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }

        throw new ClassCastException();
    }

    public JsonArray getArray(String key) {
        Object value = values.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof JsonArray) {
            return (JsonArray) value;
        }

        throw new ClassCastException();
    }

    public String getString(String key) {
        Object value = values.get(key);

        if (value == null) {
            return null;
        }

        return value.toString();
    }

    public boolean getBoolean(String key) {
        Object value = values.get(key);

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        throw new ClassCastException();
    }

    public int getInt(String key) {
        return getNumber(key).intValue();
    }

    public long getLong(String key) {
        return getNumber(key).longValue();
    }

    public float getFloat(String key) {
        return getNumber(key).floatValue();
    }

    public double getDouble(String key) {
        return getNumber(key).doubleValue();
    }

    private Number getNumber(String key) {
        Object value = values.get(key);

        if (value instanceof Number) {
            return ((Number) value);
        }

        throw new ClassCastException();
    }

    public BigDecimal getBigDecimal(String key) {
        Object value = values.get(key);

        if (value instanceof BigDecimal) {
            return ((BigDecimal) value);
        } else if (value instanceof Number) {
            return new BigDecimal(((Number) value).doubleValue()).setScale(8, RoundingMode.HALF_EVEN);
        }

        throw new ClassCastException();
    }

    public LocalDate getLocalDate(String key) {
        return LocalDate.parse(getString(key));
    }

    public String encode() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (first) {
                first = false;
            } else {
                builder.append(",");
            }

            builder.append("\"");
            builder.append(entry.getKey().replace("\"", "\\\""));
            builder.append("\":");
            builder.append(encodeValue(entry.getValue()));
        }

        builder.append("}");
        return builder.toString();
    }

    static String encodeValue(Object value) {
        if (value instanceof Number) {
            String str = value.toString();
            int length = str.length();

            if (length > 2 && str.substring(length - 2, length).equalsIgnoreCase(".0")) {
                return str.substring(0, length - 2);
            } else {
                return str;
            }
        }

        if (value instanceof String) {
            return "\"" + ((String) value).replace("\"", "\\\"") + "\"";
        }

        if (value instanceof Boolean) {
            return ((Boolean) value) ? "true" : "false";
        }

        if (value instanceof JsonObject) {
            return ((JsonObject) value).encode();
        }

        if (value instanceof JsonArray) {
            return ((JsonArray) value).encode();
        }

        if (value instanceof RawJS) {
            return ((RawJS) value).code;
        }

        throw new RuntimeException("Unexpected type: " + value.getClass().getName());
    }

    public static JsonObject decode(String json) {
        try {
            return decode(new ByteArrayInputStream(json.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonObject decode(InputStream inputStream) throws IOException {
        return decode(new JsonInputStreamReader(inputStream));
    }

    static JsonObject decode(JsonInputStreamReader reader) throws IOException {
        reader.expect('{');
        JsonObject object = new JsonObject();
        int b;

        do {
            String key = readQuotedString(reader);
            reader.expect(':');
            object.putObject(key, readValue(reader));
            b = reader.readAndSkipWhitespace();
        } while (b == ',');

        if (b != '}') {
            throw new RuntimeException("Failed to parse JSON, expected '}', but got: " + byteToString(b));
        }

        return object;
    }

    static Object readValue(JsonInputStreamReader reader) throws IOException {
        int b = reader.readAndSkipWhitespace();
        reader.rewind();

        if (b == '"') {
            return readQuotedString(reader);
        }

        if (b == '[') {
            return JsonArray.decode(reader);
        }

        if (b == '{') {
            return decode(reader);
        }

        String rawValue = readRawValue(reader);

        if (rawValue.equalsIgnoreCase("true") || rawValue.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(rawValue);
        }

        if (rawValue.equalsIgnoreCase("null")) {
            return new RawJS("null");
        }

        if (rawValue.equalsIgnoreCase("undefined")) {
            return new RawJS("undefined");
        }

        try {
            return new BigDecimal(rawValue);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Failed to parse JSON, unrecognized value: " + rawValue);
        }
    }

    static String readQuotedString(JsonInputStreamReader reader) throws IOException {
        reader.expect('"');
        int b = reader.readAndSkipWhitespace();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (b != '"') {
            if (b == '\\') {
                b = reader.read();

                if (b != '"') {
                    baos.write('\\');
                }
            }

            baos.write(b);
            b = reader.read();
        }

        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    static String readRawValue(JsonInputStreamReader reader) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;

        do {
            b = reader.read();
        } while (isWhitespace(b));

        while (b != ',' && b != '}' && !isWhitespace(b)) {
            baos.write(b);
            b = reader.read();
        }

        reader.rewind();
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    static class RawJS {
        String code;

        RawJS(String code) {
            this.code = code;
        }
    }

    static class JsonInputStreamReader {
        private final InputStream inputStream;
        private int lastByte;
        private boolean rewind = false;

        JsonInputStreamReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        int read() throws IOException {
            if (rewind) {
                rewind = false;
                return lastByte;
            }

            lastByte = inputStream.read();
            return lastByte;
        }

        int readAndSkipWhitespace() throws IOException {
            if (rewind) {
                rewind = false;

                if (!isWhitespace(lastByte)) {
                    return lastByte;
                }
            }

            int b;

            do {
                b = inputStream.read();
            } while (isWhitespace(b));

            lastByte = b;
            return b;
        }

        void expect(int expectedChar) throws IOException {
            int b = readAndSkipWhitespace();

            if (b != expectedChar) {
                throw new RuntimeException("Unexpected character: " + byteToString(b));
            }
        }

        void rewind() {
            if (rewind) {
                throw new RuntimeException("Cannot rewind() more than one time before reading again from the stream");
            }

            rewind = true;
        }
    }

    private static boolean isWhitespace(int b) {
        return b == ' ' || b == '\n' || b == '\r';
    }

    private static String byteToString(int b) {
        return new String(new byte[] {(byte) b}, StandardCharsets.UTF_8);
    }

//    public static void main(String[] args) {
//        JsonObject test = JsonObject.decode("  {\"key\":    { \"key2\" : \"some value\", \"key  3\": [ 1, 3, 4,   5 ] } }  ");
//        System.out.println(test.encode());
//    }
}
