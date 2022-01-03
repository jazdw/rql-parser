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
    R getProperty(T item, String propertyName);

    default Comparator<Object> getComparator(String property) {
        return DefaultComparator.INSTANCE;
    }

    default Comparator<T> getSortComparator(String property) {
        Comparator<Object> propertyComparator = getComparator(property);
        return (a, b) -> {
            Object valueA = getProperty(a, property);
            Object valueB = getProperty(b, property);
            return propertyComparator.compare(valueA, valueB);
        };
    }
}
