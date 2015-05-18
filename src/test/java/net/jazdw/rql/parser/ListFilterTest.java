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

import java.util.ArrayList;
import java.util.List;

import net.jazdw.rql.parser.listfilter.ListFilter;
import net.jazdw.rql.parser.listfilter.Person;

import org.joda.time.LocalDate;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jared Wiltshire
 */
public class ListFilterTest {
    RQLParser parser;
    static List<Person> people = new ArrayList<>();
    static {
        people.add(new Person("Harry", "Smith", new LocalDate(1954, 3, 18), "Male", "English"));
        people.add(new Person("Jill", "Smith", new LocalDate(2001, 1, 16), "Female", "English"));
        people.add(new Person("Oliver", "Smith", new LocalDate(1930, 2, 12), "Male", "English"));
        people.add(new Person("Davo", "Jones", new LocalDate(1976, 11, 21), "Male", "Australian"));
        people.add(new Person("Dazza", "Williams", new LocalDate(1985, 11, 17), "Male", "Australian"));
        people.add(new Person("Shazza", "Taylor", new LocalDate(1987, 9, 29), "Female", "Australian"));
        people.add(new Person("Shazza", "Smith", new LocalDate(1917, 9, 20), "Female", "Australian"));
        people.add(new Person("Dũng", "Nguyễn", new LocalDate(1943, 8, 16), "Male", "Australian"));
        people.add(new Person("Manuel", "Muñoz", new LocalDate(2000, 12, 21), "Male", "Spanish"));
        people.add(new Person("José", "Rodríguez", new LocalDate(1960, 1, 2), "Male", "Spanish"));
        people.add(new Person("Dolores", "García", new LocalDate(1976, 10, 3), "Female", "Spanish"));
        people.add(new Person("María", "García", new LocalDate(2005, 4, 7), "Female", "Spanish"));
        people.add(new Person("Billy", "Brown", new LocalDate(1950, 9, 11), "Male", "American"));
        people.add(new Person("Betty", "Brown", new LocalDate(1985, 7, 10), "Female", "American"));
        people.add(new Person("Madison", "Miller", new LocalDate(1972, 3, 28), "Female", "American"));
        people.add(new Person("Jayden", "Davis", new LocalDate(2005, 12, 23), "Male", "American"));
    }

    @Before
    public void before() {
        parser = new RQLParser();
    }
    
    @Test
    public void testAndGt() {
        ListFilter<Person> filter = new ListFilter<Person>();
        ASTNode node = parser.parse("firstName=Shazza&age>50");
        List<Person> results = node.accept(filter, people);
        assertEquals(1, results.size());
        assertEquals("Taylor", results.get(0).getLastName());
    }

    @Test
    public void testSort() {
        ListFilter<Person> filter = new ListFilter<Person>();
        ASTNode node = parser.parse("sort(-firstName)");
        List<Person> results = node.accept(filter, people);
        assertEquals(people.size(), results.size());
        assertEquals("Shazza", results.get(0).getFirstName());
        
        node = parser.parse("sort(+lastName,-firstName)");
        results = node.accept(filter, people);
        assertEquals(people.size(), results.size());
        assertEquals("Billy", results.get(0).getFirstName());
    }
    
    @Test
    public void testLimit() {
        ListFilter<Person> filter = new ListFilter<Person>();
        ASTNode node = parser.parse("limit(10)");
        List<Person> results = node.accept(filter, people);
        assertEquals(10, results.size());
        
        Person last = results.get(9);
        
        node = parser.parse("limit(5,9)");
        results = node.accept(filter, people);
        assertEquals(5, results.size());
        assertEquals(last, results.get(0));
        
        // test for IndexOutOfBoundsException
        //node = parser.parse("limit(10,15)");
    }
}
