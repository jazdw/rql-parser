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

package net.jazdw.rql.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Test;

import net.jazdw.rql.RqlLexer;
import net.jazdw.rql.RqlParser;
import net.jazdw.rql.RqlParser.QueryContext;
import net.jazdw.rql.visitor.ValueVisitor;

/**
 * @author Jared Wiltshire
 */
public class RqlParserTest {

    ValueVisitor valueVisitor = new ValueVisitor();

    private RqlParser createParser(String rql) {
        CharStream inputStream = CharStreams.fromString(rql);
        RqlLexer lexer = new RqlLexer(inputStream);
        lexer.removeErrorListeners();
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        RqlParser parser = new RqlParser(tokenStream);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private Object parseValue(String rql) {
        return createParser(rql).value().accept(valueVisitor);
    }

    @Test(expected = ParseCancellationException.class)
    public void missingProperty() {
        createParser("=test").query();
    }

    @Test(expected = ParseCancellationException.class)
    public void missingProperty2() {
        createParser("age=30&=test").query();
    }

    @Test(expected = ParseCancellationException.class)
    public void missingProperty3() {
        createParser("=test&age=30").query();
    }

    @Test
    public void empty() {
        QueryContext result = createParser("").query();
        assertNull(result.expression());
    }

    @Test
    public void numbers() {
        // octal
        assertEquals(24, parseValue("number:030"));
        assertEquals(30, parseValue("number:30"));
        // hex
        assertEquals(48, parseValue("number:0x30"));
        assertEquals(0.1F, parseValue("number:0.1"));
    }

    @Test
    public void date() {
        LocalDate expected = LocalDate.of(2015, 1, 1);

        // auto converter
        assertEquals(expected, parseValue("2015-01-01"));

        // explicit converter
        assertEquals(expected, parseValue("date:2015-01-01"));
    }

    @Test
    public void dateTime() {
        LocalDateTime expected = LocalDateTime.of(2015, 1, 1, 0, 0);

        // auto converter
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00.000"));

        // explicit converter
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00.000"));
    }

    @Test
    public void offsetDateTime() {
        LocalDateTime local = LocalDateTime.of(2015, 1, 1, 0, 0, 0);
        ZonedDateTime expected = ZonedDateTime.of(local, ZoneOffset.ofHoursMinutes(10, 0));

        // auto converter
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00+10"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00+10%3A00"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00.000+10"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00.000+10%3A00"));

        // explicit converter
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00+10"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00+10%3A00"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00.000+10"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00.000+10%3A00"));
    }

    @Test
    public void utcDateTime() {
        LocalDateTime local = LocalDateTime.of(2015, 1, 1, 0, 0);
        ZonedDateTime expected = ZonedDateTime.of(local, ZoneOffset.UTC);

        // auto converter
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00Z"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00+00"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00+00%3A00"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00.000Z"));
        assertEquals(expected, parseValue("2015-01-01T00%3A00%3A00.000+00%3A00"));

        // explicit converter
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00Z"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00.000Z"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00+00"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00+00%3A00"));
        assertEquals(expected, parseValue("date:2015-01-01T00%3A00%3A00.000+00%3A00"));
    }

    @Test
    public void epoch() {
        Instant expected = Instant.ofEpochMilli(1420117993131L);
        assertEquals(expected, parseValue("epoch:1420117993131"));
    }

    @Test
    public void zonedDateTime() {
        LocalDateTime local = LocalDateTime.of(2011, 12, 3, 10, 15, 30);
        ZonedDateTime expected = ZonedDateTime.of(local, ZoneId.of("Europe/Paris"));
        assertEquals(expected, parseValue("date:2011-12-03T10%3A15%3A30%2B01%3A00%5BEurope%2FParis%5D"));
    }

    @Test
    public void specialValues() {
        assertEquals(true, parseValue("true"));
        assertEquals(false, parseValue("false"));
        assertNull(parseValue("null"));
        assertEquals(Double.POSITIVE_INFINITY, parseValue("Infinity"));
        assertEquals(Double.NEGATIVE_INFINITY, parseValue("-Infinity"));
    }

    @Test
    public void booleanValues() {
        assertEquals(true, parseValue("boolean:true"));
        assertEquals(true, parseValue("boolean:TRUE"));
        assertEquals(true, parseValue("boolean:tRue"));
        assertEquals(false, parseValue("boolean:false"));
        assertEquals(false, parseValue("boolean:FALSE"));
        assertEquals(false, parseValue("boolean:fAlse"));
        assertEquals(false, parseValue("boolean:0"));
        assertEquals(false, parseValue("boolean:1"));
        assertEquals(false, parseValue("boolean:yes"));
    }

    @Test
    public void regex() {
        Pattern expected = Pattern.compile("^.*abc$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Object parsed = parseValue("re:%5E.*abc%24");
        assertTrue(parsed instanceof Pattern);
        Pattern parsedPattern = (Pattern) parsed;
        assertEquals(expected.pattern(), parsedPattern.pattern());
        assertEquals(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE, parsedPattern.flags());
    }

    @Test
    public void regexCaseSensitive() {
        Pattern expected = Pattern.compile("^.*abc$");
        Object parsed = parseValue("RE:%5e.*abc%24");
        assertTrue(parsed instanceof Pattern);
        Pattern parsedPattern = (Pattern) parsed;
        assertEquals(expected.pattern(), parsedPattern.pattern());
        assertEquals(0, parsedPattern.flags());
    }
}
