/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */

package net.jazdw.rql.converter;

/**
 * @author Jared Wiltshire
 */
public class ConverterException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(Throwable cause) {
        super(cause);
    }
}
