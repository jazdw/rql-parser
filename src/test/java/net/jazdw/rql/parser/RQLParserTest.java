/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */
package net.jazdw.rql.parser;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jared Wiltshire
 */
public class RQLParserTest {
    RQLParser parser;
    
    @Before
    public void before() {
        parser = new RQLParser();
    }
    
    @Test
    public void equals() {
        ASTNode expected = new ASTNode("eq", "name", "jack");
        assertEquals(expected, parser.parse("name=jack"));
        assertEquals(expected, parser.parse("eq(name,jack)"));
        
        expected = new ASTNode("eq", "age", 30);
        assertEquals(expected, parser.parse("age=30"));
        assertEquals(expected, parser.parse("eq(age,30)"));
    }
    
    @Test
    public void limit() {
        assertEquals(new ASTNode("limit", 10, 30), parser.parse("limit(10,30)"));
        assertEquals(new ASTNode("limit", 10), parser.parse("limit(10)"));
    }
    
    @Test
    public void sort() {
        assertEquals(new ASTNode("sort", "+name"), parser.parse("sort(%2bname)"));
        assertEquals(new ASTNode("sort", "-date"), parser.parse("sort(-date)"));
        assertEquals(new ASTNode("sort", "+name", "-date"), parser.parse("sort(%2bname,-date)"));
    }
    
    @Test
    public void logical() {
        ASTNode expected = new ASTNode("and")
            .createChildNode("or")
            .createChildNode("eq", "name", "jack")
            .getParent()
            .createChildNode("eq", "name", "jill")
            .getParent()
            .getParent()
            .createChildNode("gt", "age", 30)
            .getParent()
            .removeParents();
        
        assertEquals(expected, parser.parse("(name=jack|name=jill)&age>30"));
        assertEquals(expected, parser.parse("or(name=jack,name=jill)&age>30"));
        assertEquals(expected, parser.parse("(eq(name,jack)|name=jill)&age>30"));
        assertEquals(expected, parser.parse("(name=jack|name=jill)&gt(age,30)"));
        assertEquals(expected, parser.parse("(name=jack|name=jill)&age>number:30"));
        assertEquals(expected, parser.parse("(name=string:jack|name=jill)&age>30"));
        assertEquals(expected, parser.parse("and((name=jack|name=jill),age>30)"));
    }
    
    @Test
    public void empty() {
        ASTNode expected = new ASTNode("");
        assertEquals(expected, parser.parse(""));
    }
    
    @Test
    public void oddRootNodes() {
        assertEquals(new ASTNode("", "test"), parser.parse("test"));
        assertEquals(new ASTNode("", "test", "test2"), parser.parse("test,test2"));
        assertEquals(new ASTNode("").addArgument(Arrays.asList(new String[] {"test", "test2"})),
                parser.parse("test/test2"));
        assertEquals(new ASTNode("", 10), parser.parse("10"));
    }
    
    @Test
    public void date() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2015, 0, 1);
        Date expected = cal.getTime();
        
        // auto converter
        assertEquals(expected, parser.parse("2015-01-01").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00.000").getArgument(0));
        
        // explicit converter
        assertEquals(expected, parser.parse("date:2015").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01T00:00:00").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01T00:00:00.000").getArgument(0));
    }
    
    @Test
    public void offsetDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+10:00"));
        cal.clear();
        cal.set(2015, 0, 1);
        Date expected = cal.getTime();
        
        // auto converter
        assertEquals(expected, parser.parse("2015-01-01T00:00:00%2b10").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00%2b10:00").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00.000%2b10").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00.000%2b10:00").getArgument(0));
        
        // explicit converter
        assertEquals(expected, parser.parse("date:2015-01-01T00:00:00%2b10").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01T00:00:00%2b10:00").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01T00:00:00.000%2b10").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01T00:00:00.000%2b10:00").getArgument(0));
    }
    
    @Test
    public void isoDate() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(2015, 0, 1);
        Date expected = cal.getTime();

        // auto converter
        assertEquals(expected, parser.parse("2015-01-01T00:00:00Z").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00%2b00:00").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00.000Z").getArgument(0));
        assertEquals(expected, parser.parse("2015-01-01T00:00:00.000%2b00:00").getArgument(0));
        
        // explicit converter
        assertEquals(expected, parser.parse("isodate:2015").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01-01").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01-01T00:00:00").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01-01T00:00:00.000").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01-01T00:00:00Z").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01-01T00:00:00.000Z").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01-01T00:00:00%2b00:00").getArgument(0));
        assertEquals(expected, parser.parse("isodate:2015-01-01T00:00:00.000%2b00:00").getArgument(0));
    }
    
    @Test
    public void epoch() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.clear();
        cal.set(2015, 0, 1, 13, 13, 13);
        cal.set(Calendar.MILLISECOND, 131);
        Date expected = cal.getTime();
        
        assertEquals(expected, parser.parse("epoch:1420117993131").getArgument(0));
    }
    
    @Test
    public void specialValues() {
        assertEquals(true, parser.parse("true").getArgument(0));
        assertEquals(false, parser.parse("false").getArgument(0));
        assertNull(parser.parse("null").getArgument(0));
        assertEquals(Double.POSITIVE_INFINITY, parser.parse("Infinity").getArgument(0));
        assertEquals(Double.NEGATIVE_INFINITY, parser.parse("-Infinity").getArgument(0));
    }
    
    @Test
    public void booleanValues() {
        assertEquals(true, parser.parse("boolean:true").getArgument(0));
        assertEquals(true, parser.parse("boolean:TRUE").getArgument(0));
        assertEquals(true, parser.parse("boolean:tRue").getArgument(0));
        assertEquals(false, parser.parse("boolean:false").getArgument(0));
        assertEquals(false, parser.parse("boolean:FALSE").getArgument(0));
        assertEquals(false, parser.parse("boolean:fAlse").getArgument(0));
        assertEquals(false, parser.parse("boolean:0").getArgument(0));
        assertEquals(false, parser.parse("boolean:1").getArgument(0));
        assertEquals(false, parser.parse("boolean:yes").getArgument(0));
    }
    
    @Test
    public void regex() {
        Pattern expected = Pattern.compile("^.*abc$", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Object parsed = parser.parse("re:%5e.*abc$").getArgument(0);
        assertTrue(parsed instanceof Pattern);
        Pattern parsedPattern = (Pattern) parsed;
        assertEquals(expected.pattern(), parsedPattern.pattern());
        assertEquals(Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE, parsedPattern.flags());
    }
    
    @Test
    public void regexCaseSensitive() {
        Pattern expected = Pattern.compile("^.*abc$");
        Object parsed = parser.parse("RE:%5e.*abc$").getArgument(0);
        assertTrue(parsed instanceof Pattern);
        Pattern parsedPattern = (Pattern) parsed;
        assertEquals(expected.pattern(), parsedPattern.pattern());
        assertEquals(0, parsedPattern.flags());
    }
}
