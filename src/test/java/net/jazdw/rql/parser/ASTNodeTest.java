/*
 * Copyright (C) 2021 Jared Wiltshire (https://jazdw.net).
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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Jared Wiltshire
 */
public class ASTNodeTest {
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
        assertEquals(expected, parser.parse("name==jack"));

        expected = new ASTNode("eq", "age", 30);
        assertEquals(expected, parser.parse("age=30"));
        assertEquals(expected, parser.parse("eq(age,30)"));
        assertEquals(expected, parser.parse("age==30"));
    }

    @Test(expected = RQLParserException.class)
    public void missingProperty() {
        parser.parse("=test");
    }

    @Test(expected = RQLParserException.class)
    public void missingProperty2() {
        parser.parse("age=30&=test");
    }

    @Test(expected = RQLParserException.class)
    public void missingProperty3() {
        parser.parse("=test&age=30");
    }

    @Test
    public void unicode() {
        assertEquals(new ASTNode("eq", "ab", "\u03b1\u03b2"), parser.parse("eq(ab,%CE%B1%CE%B2)"));
        assertEquals(new ASTNode("eq", "ab", "\u03b1\u03b2"), parser.parse("ab=%CE%B1%CE%B2"));
    }

    @Test
    public void percentEncoding() {
        assertEquals(new ASTNode("eq", "equation", "(a+b)*c"), parser.parse("eq(equation,%28a+b%29*c)"));
        assertEquals(new ASTNode("eq", "equation", "(a+b)*c"), parser.parse("equation=%28a+b%29*c"));
        assertEquals(new ASTNode("eq", "equation", "(a+b)*c"), parser.parse("equation=%28a%2Bb%29%2Ac"));
    }

    // TODO decide if we should support these
    @Ignore
    @Test
    public void operatorPropertyNames() {
        assertEquals(new ASTNode("eq", "and", "yes"), parser.parse("and=yes"));

        ASTNode expected = new ASTNode("and")
                .createChildNode("eq", "and", "no").getParent()
                .createChildNode("eq", "or", "yes").getParent();

        assertEquals(expected, parser.parse("and(and=no,or=yes)"));
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
                .createChildNode("eq", "name", "jack").getParent()
                .createChildNode("eq", "name", "jill").getParent()
                .getParent()
                .createChildNode("gt", "age", 30).getParent();

        assertEquals(expected, parser.parse("(name=jack|name=jill)&age>30"));
        assertEquals(expected, parser.parse("or(name=jack,name=jill)&age>30"));
        assertEquals(expected, parser.parse("(eq(name,jack)|name=jill)&age>30"));
        assertEquals(expected, parser.parse("(name=jack|name=jill)&gt(age,30)"));
        assertEquals(expected, parser.parse("(name=jack|name=jill)&age>number:30"));
        assertEquals(expected, parser.parse("(name=string:jack|name=jill)&age>30"));
        assertEquals(expected, parser.parse("and((name=jack|name=jill),age>30)"));
        assertEquals(expected, parser.parse("and(or(name=jack,name=jill),age>30)"));
    }

    @Test
    public void empty() {
        ASTNode expected = new ASTNode("");
        assertEquals(expected, parser.parse(""));
    }

    // TODO decide if we should support these
    @Ignore
    @Test
    public void oddRootNodes() {
        assertEquals(new ASTNode("", "test"), parser.parse("test"));
        assertEquals(new ASTNode("", "test", "test2"), parser.parse("test,test2"));
        assertEquals(new ASTNode("").addArgument(Arrays.asList("test", "test2")),
                parser.parse("test/test2"));
        assertEquals(new ASTNode("", 10), parser.parse("10"));
    }

}
