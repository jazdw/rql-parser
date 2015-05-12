/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */

package net.jazdw.rql.converter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Jared Wiltshire
 */
public class Converter {
    private ValueConverter defaultConverter;
    private HashMap<String, ValueConverter> converterMap;

    public Converter() {
        this(new AutoValueConverter(), CONVERTERS);
    }
    
    public Converter(ValueConverter defaultConverter) {
        this(defaultConverter, CONVERTERS);
    }
    
    public Converter(ValueConverter defaultConverter, Map<String, ValueConverter> converterMap) {
        this.defaultConverter = defaultConverter;
        this.converterMap = new HashMap<>(converterMap);
    }
    
    public Object convert(String input) throws ConverterException {
        ValueConverter converter = defaultConverter;
        
        int pos = input.indexOf(":");
        if (pos >= 0) {
            String type = input.substring(0, pos);
            String value = input.length() > pos + 1 ? input.substring(pos + 1) : "";
            
            if (converterMap.containsKey(type)) {
                converter = converterMap.get(type);
                input = value;
            }
            // could throw exception if not found as other colons should probably be percent encoded
        }
        
        try {
            // URLDecoder converts plus to space, percent encode the plus signs first
            input = URLDecoder.decode(input.replace("+", "%2B"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ConverterException(e);
        }
        
        return converter.convert(input);
    }
    
    public static final ValueConverter NUMBER = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                // parser interprets leading zeros as octal, strip leading zeros?
                // input = input.replaceAll("^0*", "");
                return NumberUtils.createNumber(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter EPOCH = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                return new Date(Long.parseLong(input));
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter ISODATE = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                DateTimeFormatter parser = ISODateTimeFormat.dateOptionalTimeParser()
                        .withZoneUTC();
                return parser.parseDateTime(input).toDate();
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter DATE = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                DateTimeFormatter parser = ISODateTimeFormat.dateOptionalTimeParser();
                return parser.parseDateTime(input).toDate();
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter BOOLEAN = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                return Boolean.parseBoolean(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter STRING = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                return input;
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter REGEX_I = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                return Pattern.compile(input, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter REGEX = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                return Pattern.compile(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    public static final ValueConverter GLOB = new ValueConverter() {
        public Object convert(String input) throws ConverterException {
            try {
                throw new UnsupportedOperationException("Not yet implemented");
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    private static final Map<String, ValueConverter> CONVERTERS = new HashMap<>();
    static {
        CONVERTERS.put("number", NUMBER);
        CONVERTERS.put("epoch", EPOCH);
        CONVERTERS.put("isodate", ISODATE);
        CONVERTERS.put("date", DATE);
        CONVERTERS.put("boolean", BOOLEAN);
        CONVERTERS.put("string", STRING);
        CONVERTERS.put("re", REGEX_I);
        CONVERTERS.put("RE", REGEX);
        //CONVERTERS.put("glob", GLOB);
    }
}
