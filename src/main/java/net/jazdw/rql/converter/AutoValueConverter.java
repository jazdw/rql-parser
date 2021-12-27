/*
 * Copyright (C) 2015 Jared Wiltshire (http://jazdw.net).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 3 which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/lgpl.txt
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */

package net.jazdw.rql.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import net.jazdw.rql.converter.Converter.GenericDateTimeConverter;
import net.jazdw.rql.converter.Converter.NumberConverter;

/**
 * The default value converter which tries to guess the value type and convert it to
 * the correct Java object type
 *
 * @author Jared Wiltshire
 */
class AutoValueConverter implements ValueConverter<Object> {
    /**
     * The default automatic conversion map
     */
    public static final Map<String, Object> DEFAULT_CONVERSIONS = new HashMap<>();

    static {
        DEFAULT_CONVERSIONS.put("true", Boolean.TRUE);
        DEFAULT_CONVERSIONS.put("false", Boolean.FALSE);
        DEFAULT_CONVERSIONS.put("null", null);
        DEFAULT_CONVERSIONS.put("Infinity", Double.POSITIVE_INFINITY);
        DEFAULT_CONVERSIONS.put("-Infinity", Double.NEGATIVE_INFINITY);
    }

    // detects ISO 8601 dates with a minimum of year, month and day specified
    private static final Pattern DATE_PATTERN = Pattern.compile("^[0-9]{4}-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])(T(2[0-3]|[01][0-9])(:[0-5][0-9])?(:[0-5][0-9])?(\\.[0-9][0-9]?[0-9]?)?(Z|[+-](?:2[0-3]|[01][0-9])(?::?(?:[0-5][0-9]))?)?)?$");

    private final Map<String, Object> conversions;

    public AutoValueConverter() {
        this(DEFAULT_CONVERSIONS);
    }

    public AutoValueConverter(Map<String, Object> autoConversionMap) {
        this.conversions = new HashMap<>(autoConversionMap);
    }

    public Object convert(String input) throws ConverterException {
        try {
            if (conversions.containsKey(input)) {
                return conversions.get(input);
            }

            try {
                if (NumberUtils.isCreatable(input)) {
                    return NumberConverter.INSTANCE.convert(input);
                }
            } catch (ConverterException e) {
                // ignore
            }

            try {
                if (DATE_PATTERN.matcher(input).matches()) {
                    return GenericDateTimeConverter.INSTANCE.convert(input);
                }
            } catch (ConverterException e) {
                // ignore
            }

            return input;
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }
}
