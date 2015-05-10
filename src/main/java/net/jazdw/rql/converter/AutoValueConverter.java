/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */

package net.jazdw.rql.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author Jared Wiltshire
 */
class AutoValueConverter implements ValueConverter {
    private static final Map<String, Object> DEFAULT_CONVERSIONS = new HashMap<>();
    static {
        DEFAULT_CONVERSIONS.put("true", Boolean.TRUE);
        DEFAULT_CONVERSIONS.put("false", Boolean.FALSE);
        DEFAULT_CONVERSIONS.put("null", null);
        DEFAULT_CONVERSIONS.put("Infinity", Double.POSITIVE_INFINITY);
        DEFAULT_CONVERSIONS.put("-Infinity", Double.NEGATIVE_INFINITY);
    }
    
    // detects ISO 8601 dates with a minimum of year, month and day specified
    private static final Pattern DATE_PATTERN = Pattern.compile("^[0-9]{4}-(1[0-2]|0[1-9])-(3[01]|0[1-9]|[12][0-9])(T(2[0-3]|[01][0-9])(:[0-5][0-9])?(:[0-5][0-9])?(Z|[+-](?:2[0-3]|[01][0-9])(?::?(?:[0-5][0-9]))?)?)?$");
    
    Map<String, Object> conversions;
    
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
                if (NumberUtils.isNumber(input)) {
                    return Converter.NUMBER.convert(input);
                }
            } catch (ConverterException e) {}
            
            
            try {
                if (DATE_PATTERN.matcher(input).matches()) {
                    return Converter.DATE.convert(input);
                }
            } catch (ConverterException e) {}
            
            return Converter.STRING.convert(input);
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }
}
