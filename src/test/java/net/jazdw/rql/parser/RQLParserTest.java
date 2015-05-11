/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */
package net.jazdw.rql.parser;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
    }
    
    @Test
    public void limit() {
        assertEquals(new ASTNode("limit", 10, 30), parser.parse("limit(10,30)"));
        assertEquals(new ASTNode("limit", 10), parser.parse("limit(10)"));
    }
    
    @Test
    public void sort() {
        assertEquals(new ASTNode("sort", "+name"), parser.parse("sort(+name)"));
        assertEquals(new ASTNode("sort", "-date"), parser.parse("sort(-date)"));
        assertEquals(new ASTNode("sort", "+name", "-date"), parser.parse("sort(+name,-date)"));
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
        assertEquals(new ASTNode("").addArgument(Arrays.asList(new String[] {"test", "test2"})), parser.parse("test/test2"));
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
        
        // explicit converter
        assertEquals(expected, parser.parse("date:2015").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01").getArgument(0));
        assertEquals(expected, parser.parse("date:2015-01-01T00:00:00").getArgument(0));
    }
}
