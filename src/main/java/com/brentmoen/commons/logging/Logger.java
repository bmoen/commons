package com.brentmoen.commons.logging;

public interface Logger {
    void debug(String message);

    void debug(String message, Throwable cause);

    void info(String message);

    void info(String message, Throwable cause);

    void warn(String message);

    void warn(String message, Throwable cause);

    void error(String message);

    void error(String message, Throwable cause);
}
