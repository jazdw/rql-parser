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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Used to convert parts of the RQL query string into Java objects
 * @see #convert(String)
 * 
 * @author Jared Wiltshire
 */
public class Converter {
    private ValueConverter defaultConverter;
    private HashMap<String, ValueConverter> converterMap;

    /**
     * Creates a new converter using the auto value converter as default
     */
    public Converter() {
        this(new AutoValueConverter(), CONVERTERS);
    }
    
    /**
     * Creates a new converter using the given value converter as default
     * 
     * @param defaultConverter the default value converter
     */
    public Converter(ValueConverter defaultConverter) {
        this(defaultConverter, CONVERTERS);
    }
    
    /**
     * Creates a new converter using the auto value converter as default and the given type conversion map
     * 
     * @param converterMap auto conversion map, maps types e.g. 'date' to a value converter
     */
    public Converter(Map<String, ValueConverter> converterMap) {
        this(new AutoValueConverter(), converterMap);
    }
    
    /**
     * Creates a new converter with the given default value converter and type conversion map
     * 
     * @param defaultConverter the default value converter
     * @param converterMap auto conversion map, maps types e.g. 'date' to a value converter
     */
    public Converter(ValueConverter defaultConverter, Map<String, ValueConverter> converterMap) {
        this.defaultConverter = defaultConverter;
        this.converterMap = new HashMap<>(converterMap);
    }
    
    /**
     * <p>Converts the string input into a Java object. The part before the colon denotes the type and
     * is used to find a suitable value converter. If no type is specified the default automatic converter
     * will try to automatically convert the string to a suitable Java object.</p>
     * 
     * <p>As the colon delimits the type from the value, colons in a value should be percent encoded</p>
     * 
     * @param input eg 'john', 'date:2015-01-01' or 'number:30'
     * @return Java object
     * @throws ConverterException
     */
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
    
    /**
     * Converter for numbers
     */
    public static final ValueConverter NUMBER = new ValueConverter() {
        /**
         * Converts an input string to a Number. Can return integers, floats, BigDecimal etc.
         * Note that an integer starting with 0 is interpreted as octal
         * @param input e.g. 30, 0x50, 0.02, 0.02D
         * @return Number
         */
        public Number convert(String input) throws ConverterException {
            try {
                // parser interprets leading zeros as octal, strip leading zeros?
                // input = input.replaceAll("^0*", "");
                return NumberUtils.createNumber(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    /**
     * Converter for epoch millisecond timestamps
     */
    public static final ValueConverter EPOCH = new ValueConverter() {
        /**
         * Converts an epoch string to a Date
         * @param input milliseconds since 1970-01-01 00:00:00 UTC e.g. 1431476669373
         * @return Date
         */
        public Date convert(String input) throws ConverterException {
            try {
                return new Date(Long.parseLong(input));
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    /**
     * Converter for ISO 8601 formatted dates/times. If no timezone offset is specified, UTC
     * is assumed
     */
    public static final ValueConverter ISODATE = new ValueConverter() {
        /**
         * Converts an ISO 8601 formatted dates/times to a DateTime, assuming UTC timezone if
         * none specified
         * 
         * @param input e.g. 2015-01-01T15:13:54 or 2015-01-01T15:13:54+10:30
         * @return DateTime with UTC timezone
         */
        public DateTime convert(String input) throws ConverterException {
            try {
                DateTimeFormatter parser = ISODateTimeFormat.dateOptionalTimeParser()
                        .withZoneUTC();
                return parser.parseDateTime(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    /**
     * Converter for ISO 8601 formatted dates/times. If no timezone offset is specified, local time
     * is assumed.
     */
    public static final ValueConverter DATE = new ValueConverter() {
        /**
         * Converts an ISO 8601 formatted dates/times to a DateTime, assuming local timezone if
         * none specified
         * 
         * @param input e.g. 2015-01-01T15:13:54 or 2015-01-01T15:13:54+10:30
         * @return DateTime with timezone as specified in string or local timezone
         * if not given
         */
        public DateTime convert(String input) throws ConverterException {
            try {
                DateTimeFormatter parser = ISODateTimeFormat.dateOptionalTimeParser()
                        .withOffsetParsed();
                return parser.parseDateTime(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    /**
     * Converter for booleans
     */
    public static final ValueConverter BOOLEAN = new ValueConverter() {
        /**
         * Converts a string to a boolean value
         * 
         * @param input e.g. true or FALSE
         * @return Boolean
         */
        public Boolean convert(String input) throws ConverterException {
            try {
                return Boolean.parseBoolean(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    /**
     * String converter
     */
    public static final ValueConverter STRING = new ValueConverter() {
        public String convert(String input) throws ConverterException {
            try {
                return input;
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    /**
     * Converts a string to a case insensitive regex Pattern
     */
    public static final ValueConverter REGEX_I = new ValueConverter() {
        public Pattern convert(String input) throws ConverterException {
            try {
                return Pattern.compile(input, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    };
    
    /**
     * Converts a string to a case sensitive regex Pattern
     */
    public static final ValueConverter REGEX = new ValueConverter() {
        public Pattern convert(String input) throws ConverterException {
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
    
    /**
     * The default type to value converter map
     */
    public static final Map<String, ValueConverter> CONVERTERS = new HashMap<>();
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
