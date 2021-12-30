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

package net.jazdw.rql.util;

import java.util.Comparator;

public class DefaultComparator implements Comparator<Object> {
    public static final DefaultComparator INSTANCE = new DefaultComparator();

    @Override
    public int compare(Object a, Object b) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else if (a instanceof Comparable) {
            try {
                //noinspection unchecked,rawtypes
                return ((Comparable) a).compareTo(b);
            } catch (ClassCastException e) {
                // ignore
            }
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }
}
