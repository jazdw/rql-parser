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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Before;
import org.junit.Test;

import net.jazdw.rql.RqlLexer;
import net.jazdw.rql.RqlParser;
import net.jazdw.rql.util.ThrowWithDetailsErrorListener;
import net.jazdw.rql.visitor.QueryVisitor;

/**
 * @author Jared Wiltshire
 */
public class ListFilterTest {

    static final Person HARRY_SMITH = new Person("Harry", "Smith", LocalDate.of(1954, 3, 18), "Male", "English");
    static final Person JILL_SMITH = new Person("Jill", "Smith", LocalDate.of(2001, 1, 16), "Female", "English");
    static final Person OLIVER_SMITH = new Person("Oliver", "Smith", LocalDate.of(1930, 2, 12), "Male", "English");
    static final Person DAVO_JONES = new Person("Davo", "Jones", LocalDate.of(1976, 11, 21), "Male", "Australian");
    static final Person DAZZA_WILLIAMS = new Person("Dazza", "Williams", LocalDate.of(1985, 11, 17), "Male", "Australian");
    static final Person SHAZZA_TAYLOR = new Person("Shazza", "Taylor", LocalDate.of(1987, 9, 29), "Female", "Australian");
    static final Person SHAZZA_SMITH = new Person("Shazza", "Smith", LocalDate.of(1917, 9, 20), "Female", "Australian");
    static final Person DUNG_NGUYEN = new Person("Dũng", "Nguyễn", LocalDate.of(1943, 8, 16), "Male", "Australian");
    static final Person MANUEL_MUNOZ = new Person("Manuel", "Muñoz", LocalDate.of(2000, 12, 21), "Male", "Spanish");
    static final Person JOSE_RODRIGUEZ = new Person("José", "Rodríguez", LocalDate.of(1960, 1, 2), "Male", "Spanish");
    static final Person DOLORES_GARCIA = new Person("Dolores", "García", LocalDate.of(1976, 10, 3), "Female", "Spanish");
    static final Person MARIA_GARCIA = new Person("María", "García", LocalDate.of(2005, 4, 7), "Female", "Spanish");
    static final Person BILLY_BROWN = new Person("Billy", "Brown", LocalDate.of(1950, 9, 11), "Male", "American");
    static final Person BETTY_BROWN = new Person("Betty", "Brown", LocalDate.of(1985, 7, 10), "Female", "American");
    static final Person MADISON_MILLER = new Person("Madison", "Miller", LocalDate.of(1972, 3, 28), "Female", "American");
    static final Person JAYDEN_DAVIS = new Person("Jayden", "Davis", LocalDate.of(2005, 12, 23), "Male", "American");
    static final List<Person> PEOPLE = List.of(
            HARRY_SMITH,
            JILL_SMITH,
            OLIVER_SMITH,
            DAVO_JONES,
            DAZZA_WILLIAMS,
            SHAZZA_TAYLOR,
            SHAZZA_SMITH,
            DUNG_NGUYEN,
            MANUEL_MUNOZ,
            JOSE_RODRIGUEZ,
            DOLORES_GARCIA,
            MARIA_GARCIA,
            BILLY_BROWN,
            BETTY_BROWN,
            MADISON_MILLER,
            JAYDEN_DAVIS);
    QueryVisitor<Person> filter;

    @Before
    public void before() {
        filter = new QueryVisitor<>(this::getProperty);
    }

    private RqlParser createParser(String rql) {
        CharStream inputStream = CharStreams.fromString(rql);
        RqlLexer lexer = new RqlLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ThrowWithDetailsErrorListener());
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        RqlParser parser = new RqlParser(tokenStream);
        parser.removeErrorListeners();
        parser.addErrorListener(new ThrowWithDetailsErrorListener());
        return parser;
    }

    /**
     * Basic reflection based property access, do not use in production.
     */
    private Object getProperty(Person item, String propertyName) {
        String upperFirstChar = propertyName.substring(0, 1).toUpperCase(Locale.ROOT) + propertyName.substring(1);
        try {
            Method method = item.getClass().getDeclaredMethod("get" + upperFirstChar);
            return method.invoke(item);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(String.format("Error accessing property named '%s'", propertyName), e);
        }
    }

    @Test
    public void testBasicAndSymbol() {
        RqlParser parser = createParser("firstName=Shazza&lastName=Smith");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertEquals(SHAZZA_SMITH, results.get(0));
    }

    @Test
    public void testBasicOrSymbol() {
        RqlParser parser = createParser("firstName=Shazza|lastName=Smith");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(5, results.size());
        assertTrue(results.contains(HARRY_SMITH));
        assertTrue(results.contains(JILL_SMITH));
        assertTrue(results.contains(OLIVER_SMITH));
        assertTrue(results.contains(SHAZZA_TAYLOR));
        assertTrue(results.contains(SHAZZA_SMITH));
    }

    @Test
    public void testBasicAnd() {
        RqlParser parser = createParser("and(firstName=Shazza,lastName=Smith)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertEquals(SHAZZA_SMITH, results.get(0));
    }

    @Test
    public void testBasicOr() {
        RqlParser parser = createParser("or(firstName=Shazza,lastName=Smith)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(5, results.size());
        assertTrue(results.contains(HARRY_SMITH));
        assertTrue(results.contains(JILL_SMITH));
        assertTrue(results.contains(OLIVER_SMITH));
        assertTrue(results.contains(SHAZZA_TAYLOR));
        assertTrue(results.contains(SHAZZA_SMITH));
    }

    @Test
    public void testNot() {
        RqlParser parser = createParser("not(and(firstName=Shazza,lastName=Smith))");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(PEOPLE.size() - 1, results.size());
        assertFalse(results.contains(SHAZZA_SMITH));
    }

    @Test
    public void testAnd() {
        RqlParser parser = createParser("firstName=Shazza&dateOfBirth=lt=date:1980-01-01");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertEquals(SHAZZA_SMITH, results.get(0));
    }

    @Test
    public void testOr() {
        RqlParser parser = createParser("nationality=Spanish|dateOfBirth=ge=date:2000-01-01");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(6, results.size());
        for (Person p : results) {
            assertTrue(p.getNationality().equals("Spanish") || p.getDateOfBirth().compareTo(LocalDate.of(2000, 1, 1)) >= 0);
        }
    }

    @Test
    public void testAndOr() {
        RqlParser parser = createParser("(nationality=English|lastName=Smith)&dateOfBirth=lt=date:2001-01-01");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(3, results.size());
        for (Person p : results) {
            assertTrue((p.getNationality().equals("English") || p.getLastName().equals("Smith")) &&
                    p.getDateOfBirth().isBefore(LocalDate.of(2001, 1, 1)));
        }
    }

    @Test
    public void testMatch() {
        RqlParser parser = createParser("firstName=match=*azza");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(3, results.size());
        for (Person p : results) {
            assertEquals("Australian", p.getNationality());
        }

        parser = createParser("match(firstName,m*)");
        results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(3, results.size());
        for (Person p : results) {
            assertTrue(p.getFirstName().startsWith("M"));
        }
    }

    @Test
    public void testMatchSingleChar() {
        RqlParser parser = createParser("match(firstName,%3Fazza)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertEquals(DAZZA_WILLIAMS, results.get(0));
    }

    @Test
    public void testMatchUnicode() {
        RqlParser parser = createParser("lastName=match=*%C3%91*");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertEquals(MANUEL_MUNOZ, results.get(0));
    }

    @Test
    public void testMatchRegex() {
        RqlParser parser = createParser("match(firstName,re:m.*l)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertTrue(results.contains(MANUEL_MUNOZ));
    }

    @Test
    public void testMatchRegexCaseSensitive() {
        RqlParser parser = createParser("match(firstName,RE:m.*)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(0, results.size());

        parser = createParser("match(firstName,RE:M.*)");
        results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(3, results.size());
    }

    @Test
    public void testUTFEncoding() {
        RqlParser parser = createParser("firstName=Jos%C3%A9|lastName=Rodr%C3%ADguez");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertEquals(JOSE_RODRIGUEZ, results.get(0));
    }

    @Test
    public void testSort() {
        RqlParser parser = createParser("sort(-firstName)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(PEOPLE.size(), results.size());
        assertEquals("Shazza", results.get(0).getFirstName());

        parser = createParser("sort(+lastName,-firstName)");
        results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(PEOPLE.size(), results.size());
        assertEquals(BILLY_BROWN, results.get(0));
        assertEquals(DAZZA_WILLIAMS, results.get(15));
    }

    @Test
    public void testLimit() {
        RqlParser parser = createParser("limit(10)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(10, results.size());

        parser = createParser("limit(5,9)");
        results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(5, results.size());
        assertEquals(JOSE_RODRIGUEZ, results.get(0));

        // test for IndexOutOfBoundsException
        parser = createParser("limit(10,15)");
        results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(1, results.size());
        assertEquals(JAYDEN_DAVIS, results.get(0));
    }

    @Test
    public void testIn() {
        RqlParser parser = createParser("firstName=in=(Shazza,Dazza)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(3, results.size());
        assertTrue(results.contains(DAZZA_WILLIAMS));
        assertTrue(results.contains(SHAZZA_TAYLOR));
        assertTrue(results.contains(SHAZZA_SMITH));
    }

    @Test
    public void testInForm2() {
        RqlParser parser = createParser("in(firstName,(Shazza,Dazza))");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(3, results.size());
        assertTrue(results.contains(DAZZA_WILLIAMS));
        assertTrue(results.contains(SHAZZA_TAYLOR));
        assertTrue(results.contains(SHAZZA_SMITH));
    }

    @Test
    public void testInForm3() {
        RqlParser parser = createParser("in(firstName,Shazza,Dazza)");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(3, results.size());
        assertTrue(results.contains(DAZZA_WILLIAMS));
        assertTrue(results.contains(SHAZZA_TAYLOR));
        assertTrue(results.contains(SHAZZA_SMITH));
    }

    @Test
    public void testContains() {
        RqlParser parser = createParser("names=contains=Shazza");
        List<Person> results = filter.visit(parser.query()).applyList(PEOPLE);
        assertEquals(2, results.size());
        assertTrue(results.contains(SHAZZA_TAYLOR));
        assertTrue(results.contains(SHAZZA_SMITH));
    }

    @Test
    public void filterIntegers() {
        QueryVisitor<Integer> filter = new QueryVisitor<>((item, prop) -> item);

        RqlParser parser = createParser("lt(2)");
        List<Integer> results = filter.visit(parser.query()).applyList(List.of(1, 2));
        assertEquals(1, results.size());
        assertTrue(results.contains(1));
    }

    @Test
    public void sortStrings() {
        QueryVisitor<String> filter = new QueryVisitor<>((item, prop) -> item);

        RqlParser parser = createParser("sort()");
        List<String> results = filter.visit(parser.query()).applyList(List.of("b", "a"));
        assertEquals(2, results.size());
        assertEquals("a", results.get(0));
        assertEquals("b", results.get(1));
    }

    @Test
    public void sortStrings2() {
        QueryVisitor<String> filter = new QueryVisitor<>((item, prop) -> item);

        RqlParser parser = createParser("sort(+)");
        List<String> results = filter.visit(parser.query()).applyList(List.of("b", "a"));
        assertEquals(2, results.size());
        assertEquals("a", results.get(0));
        assertEquals("b", results.get(1));
    }

    @Test
    public void sortStringsReverse() {
        QueryVisitor<String> filter = new QueryVisitor<>((item, prop) -> item);

        RqlParser parser = createParser("sort(-)");
        List<String> results = filter.visit(parser.query()).applyList(List.of("a", "b"));
        assertEquals(2, results.size());
        assertEquals("b", results.get(0));
        assertEquals("a", results.get(1));
    }
}
