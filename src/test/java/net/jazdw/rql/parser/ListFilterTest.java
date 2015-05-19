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

package net.jazdw.rql.parser;

import java.util.Arrays;
import java.util.List;

import net.jazdw.rql.parser.listfilter.ListFilter;
import net.jazdw.rql.parser.listfilter.Person;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jared Wiltshire
 */
public class ListFilterTest {
    RQLParser parser;
    ListFilter<Person> filter;
    
    private static DateTime birthday(int year, int monthOfYear, int dayOfMonth) {
        DateTime dt = new DateTime(DateTimeZone.UTC);
        return dt.withDate(year, monthOfYear, dayOfMonth).withTimeAtStartOfDay();
    }
    
    static final Person HARRY_SMITH = new Person("Harry", "Smith", birthday(1954, 3, 18), "Male", "English");
    static final Person JILL_SMITH = new Person("Jill", "Smith", birthday(2001, 1, 16), "Female", "English");
    static final Person OLIVER_SMITH = new Person("Oliver", "Smith", birthday(1930, 2, 12), "Male", "English");
    static final Person DAVO_JONES = new Person("Davo", "Jones", birthday(1976, 11, 21), "Male", "Australian");
    static final Person DAZZA_WILLIAMS = new Person("Dazza", "Williams", birthday(1985, 11, 17), "Male", "Australian");
    static final Person SHAZZA_TAYLOR = new Person("Shazza", "Taylor", birthday(1987, 9, 29), "Female", "Australian");
    static final Person SHAZZA_SMITH = new Person("Shazza", "Smith", birthday(1917, 9, 20), "Female", "Australian");
    static final Person DUNG_NGUYEN = new Person("Dũng", "Nguyễn", birthday(1943, 8, 16), "Male", "Australian");
    static final Person MANUEL_MUNOZ = new Person("Manuel", "Muñoz", birthday(2000, 12, 21), "Male", "Spanish");
    static final Person JOSE_RODRIGUEZ = new Person("José", "Rodríguez", birthday(1960, 1, 2), "Male", "Spanish");
    static final Person DOLORES_GARCIA = new Person("Dolores", "García", birthday(1976, 10, 3), "Female", "Spanish");
    static final Person MARIA_GARCIA = new Person("María", "García", birthday(2005, 4, 7), "Female", "Spanish");
    static final Person BILLY_BROWN = new Person("Billy", "Brown", birthday(1950, 9, 11), "Male", "American");
    static final Person BETTY_BROWN = new Person("Betty", "Brown", birthday(1985, 7, 10), "Female", "American");
    static final Person MADISON_MILLER = new Person("Madison", "Miller", birthday(1972, 3, 28), "Female", "American");
    static final Person JAYDEN_DAVIS = new Person("Jayden", "Davis", birthday(2005, 12, 23), "Male", "American");
    
    static List<Person> people = Arrays.asList(new Person[] {
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
            JAYDEN_DAVIS
    });

    @Before
    public void before() {
        parser = new RQLParser();
        filter = new ListFilter<Person>();
    }
    
    @Test
    public void testAnd() {
        ASTNode node = parser.parse("firstName=Shazza&age>50");
        List<Person> results = node.accept(filter, people);
        assertEquals(1, results.size());
        assertEquals(SHAZZA_SMITH, results.get(0));
    }
    
    @Test
    public void testOr() {
        ASTNode node = parser.parse("nationality=Spanish|dateOfBirth>=isodate:2000");
        List<Person> results = node.accept(filter, people);
        assertEquals(6, results.size());
        for (Person p : results) {
            assertTrue(p.getNationality().equals("Spanish") ||
                    p.getDateOfBirth().compareTo(birthday(2000, 1, 1)) >= 0);
        }
    }
    
    @Test
    public void testAndOr() {
        ASTNode node = parser.parse("(nationality=English|lastName=Smith)&age>20");
        List<Person> results = node.accept(filter, people);
        assertEquals(3, results.size());
        for (Person p : results) {
            assertTrue((p.getNationality().equals("English") ||
                    p.getLastName().equals("Smith")) && p.getAge() > 20);
        }
    }
    
    @Test
    public void testMatch() {
        ASTNode node = parser.parse("firstName=like=*azza");
        List<Person> results = node.accept(filter, people);
        assertEquals(3, results.size());
        for (Person p : results) {
            assertTrue(p.getNationality().equals("Australian"));
        }
        
        node = parser.parse("match(firstName,m*)");
        results = node.accept(filter, people);
        assertEquals(3, results.size());
        for (Person p : results) {
            assertTrue(p.getFirstName().startsWith("M"));
        }
        
        node = parser.parse("lastName=match=*Ñ*");
        results = node.accept(filter, people);
        assertEquals(1, results.size());
        assertEquals(MANUEL_MUNOZ, results.get(0));
    }
    
    @Test
    public void testUTFEncoding() {
        ASTNode node = parser.parse("firstName=Jos%C3%A9|lastName=Rodr%C3%ADguez");
        List<Person> results = node.accept(filter, people);
        assertEquals(1, results.size());
        assertEquals(JOSE_RODRIGUEZ, results.get(0));
    }

    @Test
    public void testSort() {
        ASTNode node = parser.parse("sort(-firstName)");
        List<Person> results = node.accept(filter, people);
        assertEquals(people.size(), results.size());
        assertEquals("Shazza", results.get(0).getFirstName());
        
        node = parser.parse("sort(+lastName,-firstName)");
        results = node.accept(filter, people);
        assertEquals(people.size(), results.size());
        assertEquals(BILLY_BROWN, results.get(0));
        assertEquals(DAZZA_WILLIAMS, results.get(15));
    }
    
    @Test
    public void testLimit() {
        ASTNode node = parser.parse("limit(10)");
        List<Person> results = node.accept(filter, people);
        assertEquals(10, results.size());
        
        node = parser.parse("limit(5,9)");
        results = node.accept(filter, people);
        assertEquals(5, results.size());
        assertEquals(JOSE_RODRIGUEZ, results.get(0));
        
        // test for IndexOutOfBoundsException
        node = parser.parse("limit(10,15)");
        results = node.accept(filter, people);
        assertEquals(1, results.size());
        assertEquals(JAYDEN_DAVIS, results.get(0));
    }
}
