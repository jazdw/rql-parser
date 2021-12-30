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

package net.jazdw.rql.visitor;

import java.util.Comparator;
import java.util.List;

import net.jazdw.rql.util.PropertyAccessor;
import net.jazdw.rql.util.TextDecoder;

public class SortVisitor<T> extends FunctionVisitor<Comparator<T>> {

    private final PropertyAccessor<T, Object> accessor;

    public SortVisitor(TextDecoder decoder, ValueVisitor valueVisitor, PropertyAccessor<T, Object> accessor) {
        super(decoder, valueVisitor);
        this.accessor = accessor;
    }

    @Override
    public Comparator<T> applyFunction(String functionName, List<Object> arguments) {
        if ("sort".equals(functionName)) {
            Comparator<T> comparator = null;
            for (Object arg : arguments) {
                boolean descending = false;
                String propertyName;

                String argStr = (String) arg;
                if (argStr.startsWith("-")) {
                    descending = true;
                    propertyName = argStr.substring(1);
                } else if (argStr.startsWith("+")) {
                    propertyName = argStr.substring(1);
                } else {
                    propertyName = argStr;
                }

                Comparator<T> c = accessor.getSortComparator(propertyName);
                if (descending) {
                    c = c.reversed();
                }
                if (comparator == null) {
                    comparator = c;
                } else {
                    comparator = comparator.thenComparing(c);
                }
            }
            return comparator;
        }
        return null;
    }
}
