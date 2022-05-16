package com.brentmoen.commons.logging;

class NoOpLogger implements Logger {
    @Override
    public void debug(String message) {
    }

    @Override
    public void debug(String message, Throwable cause) {
    }

    @Override
    public void info(String message) {
    }

    @Override
    public void info(String message, Throwable cause) {
    }

    @Override
    public void warn(String message) {
    }

    @Override
    public void warn(String message, Throwable cause) {
    }

    @Override
    public void error(String message) {
    }

    @Override
    public void error(String message, Throwable cause) {
    }
}
