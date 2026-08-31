package org.mapnaom.resttemplate.service;

public class ExcelImportException extends RuntimeException {
    public ExcelImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelImportException(String message) {
        super(message);
    }
}
