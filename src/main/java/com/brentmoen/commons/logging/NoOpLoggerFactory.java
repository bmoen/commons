package com.brentmoen.commons.logging;

public class NoOpLoggerFactory implements LoggerFactory {
    @Override
    public Logger create(Class<?> category) {
        return new NoOpLogger();
    }
}
