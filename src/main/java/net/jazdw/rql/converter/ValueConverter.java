/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */

package net.jazdw.rql.converter;

/**
 * @author Jared Wiltshire
 */
public interface ValueConverter {
    public Object convert(String input) throws ConverterException;
}