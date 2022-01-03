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

package net.jazdw.rql.util;

import java.util.Comparator;

public interface PropertyAccessor<T, R> {

    /**
     * Get the value of an item's property/field
     *
     * @param item     any object
     * @param property name of the property/field, could be a JSON pointer for example.
     *                 May be null to represent the object itself.
     * @return value of the property/field
     */
    R getProperty(T item, String property);

    /**
     * Get a comparator for the given property name
     *
     * @param property name of the property/field, could be a JSON pointer for example
     *                 May be null to represent the object itself.
     * @return comparator for property
     */
    default Comparator<Object> getComparator(String property) {
        return DefaultComparator.INSTANCE;
    }

    /**
     * Get a comparator that can be used to sort a list of items by a certain property
     *
     * @param property name of the property/field, could be a JSON pointer for example
     *                 May be null to represent the object itself.
     * @return comparator for sorting
     */
    default Comparator<T> getSortComparator(String property) {
        Comparator<Object> propertyComparator = getComparator(property);
        return (a, b) -> {
            Object valueA = getProperty(a, property);
            Object valueB = getProperty(b, property);
            return propertyComparator.compare(valueA, valueB);
        };
    }
}
