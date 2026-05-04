package com.cvinsight.cv.parser;

public class CVParseException extends Exception {

    public CVParseException(String message) {
        super(message);
    }

    public CVParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
