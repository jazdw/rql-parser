/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */
package net.jazdw.rql.parser;

/**
 * @author Jared Wiltshire
 */
public class RQLParserException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RQLParserException(Throwable cause) {
        super(cause);
    }
    
    public RQLParserException(String string) {
        this(new RuntimeException(string));
    }
}
