package com.brentmoen.commons.logging;

public interface LoggerFactory {
    Logger create(Class<?> category);
}
