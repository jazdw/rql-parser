/*
 * Copyright (C) 2022 Jared Wiltshire (https://jazdw.net).
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

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Used to convert parts of the RQL query string into Java objects
 *
 * @author Jared Wiltshire
 * @see #convert(String)
 */
public class Converter {
    private final ValueConverter<?> defaultConverter;
    private final HashMap<String, ValueConverter<?>> converterMap;

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
    public Converter(ValueConverter<?> defaultConverter) {
        this(defaultConverter, CONVERTERS);
    }

    /**
     * Creates a new converter using the auto value converter as default and the given type conversion map
     *
     * @param converterMap auto conversion map, maps types e.g. 'date' to a value converter
     */
    public Converter(Map<String, ValueConverter<?>> converterMap) {
        this(new AutoValueConverter(), converterMap);
    }

    /**
     * Creates a new converter with the given default value converter and type conversion map
     *
     * @param defaultConverter the default value converter
     * @param converterMap     auto conversion map, maps types e.g. 'date' to a value converter
     */
    public Converter(ValueConverter<?> defaultConverter, Map<String, ValueConverter<?>> converterMap) {
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
     * @throws ConverterException if error encountered during conversion
     */
    public Object convert(String input) throws ConverterException {
        ValueConverter<?> converter = defaultConverter;

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

        // URLDecoder converts plus to space, percent encode the plus signs first
        input = URLDecoder.decode(input.replace("+", "%2B"), StandardCharsets.UTF_8);

        return converter.convert(input);
    }

    /**
     * Converter for numbers, can return integers, floats, {@link BigDecimal} etc.
     * Note: an integer starting with 0 is interpreted as octal
     */
    public static class NumberConverter implements ValueConverter<Number> {

        public static final NumberConverter INSTANCE = new NumberConverter();

        /**
         * @param input e.g. 30, 0x50, 0.02, 0.02D
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
    }

    /**
     * Converter for epoch millisecond timestamps
     */
    public static class EpochTimestampConverter implements ValueConverter<Instant> {

        public static final EpochTimestampConverter INSTANCE = new EpochTimestampConverter();

        /**
         * @param input milliseconds since 1970-01-01 00:00:00 UTC, e.g. 1431476669373
         */
        @Override
        public Instant convert(String input) throws ConverterException {
            try {
                return Instant.ofEpochMilli(Long.parseLong(input));
            } catch (NumberFormatException | DateTimeException e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * Converter for ISO 8601 formatted date-time with a zone/offset.
     */
    public static class ZonedDateTimeConverter implements ValueConverter<ZonedDateTime> {

        public static final ZonedDateTimeConverter INSTANCE = new ZonedDateTimeConverter();

        /**
         * @param input e.g. {@code 2011-12-03T10:15:30+01:00[Europe/Paris]} or {@code 2015-01-01T15:13:54+10:30}
         */
        @Override
        public ZonedDateTime convert(String input) throws ConverterException {
            try {
                return ZonedDateTime.parse(input, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * Converter for ISO 8601 formatted local date-time, no zone or offset is specified.
     */
    public static class LocalDateTimeConverter implements ValueConverter<LocalDateTime> {

        public static final LocalDateTimeConverter INSTANCE = new LocalDateTimeConverter();

        /**
         * @param input e.g. {@code 2015-01-01T15:13:54}
         */
        @Override
        public LocalDateTime convert(String input) throws ConverterException {
            try {
                return LocalDateTime.parse(input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * Converter for ISO 8601 formatted local date (with no time), no zone or offset is specified.
     */
    public static class LocalDateConverter implements ValueConverter<LocalDate> {

        public static final LocalDateConverter INSTANCE = new LocalDateConverter();

        /**
         * @param input e.g. {@code 2015-01-01}
         */
        @Override
        public LocalDate convert(String input) throws ConverterException {
            try {
                return LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * Tries to convert to ZonedDateTime, then LocalDateTime, then finally LocalDate.
     */
    public static class GenericDateTimeConverter implements ValueConverter<Temporal> {

        public static final GenericDateTimeConverter INSTANCE = new GenericDateTimeConverter();

        @Override
        public Temporal convert(String input) throws ConverterException {
            try {
                return ZonedDateTimeConverter.INSTANCE.convert(input);
            } catch (ConverterException e) {
                // ignore
            }
            try {
                return LocalDateTimeConverter.INSTANCE.convert(input);
            } catch (ConverterException e) {
                // ignore
            }
            return LocalDateConverter.INSTANCE.convert(input);
        }
    }

    /**
     * Converter for booleans, case-insensitive.
     */
    public static class BooleanConverter implements ValueConverter<Boolean> {

        public static final BooleanConverter INSTANCE = new BooleanConverter();

        /**
         * @param input e.g. {@code true} or {@code FALSE}
         */
        @Override
        public Boolean convert(String input) throws ConverterException {
            try {
                return Boolean.parseBoolean(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * String converter
     */
    public static class StringConverter implements ValueConverter<String> {

        public static final StringConverter INSTANCE = new StringConverter();

        @Override
        public String convert(String input) throws ConverterException {
            try {
                return Objects.requireNonNull(input);
            } catch (NullPointerException e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * Converts a string to a case-insensitive regex Pattern
     */
    public static class CaseInsensitiveRegexConverter implements ValueConverter<Pattern> {

        public static final CaseInsensitiveRegexConverter INSTANCE = new CaseInsensitiveRegexConverter();

        @Override
        public Pattern convert(String input) throws ConverterException {
            try {
                return Pattern.compile(input, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * Converts a string to a case-sensitive regex Pattern
     */
    public static class RegexConverter implements ValueConverter<Pattern> {

        public static final RegexConverter INSTANCE = new RegexConverter();

        @Override
        public Pattern convert(String input) throws ConverterException {
            try {
                return Pattern.compile(input);
            } catch (Exception e) {
                throw new ConverterException(e);
            }
        }
    }

    /**
     * The default type to value converter map
     */
    public static final Map<String, ValueConverter<?>> CONVERTERS = new HashMap<>();

    static {
        CONVERTERS.put("number", NumberConverter.INSTANCE);
        CONVERTERS.put("epoch", EpochTimestampConverter.INSTANCE);
        CONVERTERS.put("date", GenericDateTimeConverter.INSTANCE);
        CONVERTERS.put("boolean", BooleanConverter.INSTANCE);
        CONVERTERS.put("string", StringConverter.INSTANCE);
        CONVERTERS.put("re", CaseInsensitiveRegexConverter.INSTANCE);
        CONVERTERS.put("RE", RegexConverter.INSTANCE);
    }
}
