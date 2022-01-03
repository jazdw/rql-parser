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

package net.jazdw.rql.parser.listfilter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.PropertyUtils;

import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.ASTVisitor;

/**
 * @author Jared Wiltshire
 */
public class ListFilter<T> implements ASTVisitor<List<T>, List<T>> {

    @SuppressWarnings("unchecked")
    @Override
    public List<T> visit(ASTNode node, List<T> list) {
        switch (node.getName()) {
            case "and":
                for (Object obj : node) {
                    if (obj instanceof ASTNode) {
                        list = ((ASTNode) obj).accept(this, list);
                    } else {
                        throw new UnsupportedOperationException("Encountered a non-ASTNode argument in AND statement");
                    }
                }
                return list;
            case "or":
                Set<T> set = new LinkedHashSet<>();
                for (Object obj : node) {
                    if (obj instanceof ASTNode) {
                        set.addAll(((ASTNode) obj).accept(this, list));
                    } else {
                        throw new UnsupportedOperationException("Encountered a non-ASTNode argument in OR statement");
                    }
                }
                return new ArrayList<>(set);
            case "eq":
            case "gt":
            case "ge":
            case "lt":
            case "le":
            case "ne":
                String propName = (String) node.getArgument(0);
                Object test = node.getArgumentsSize() > 1 ? node.getArgument(1) : null;

                List<T> result = new ArrayList<>();

                for (T item : list) {
                    Object property = getProperty(item, propName);

                    Comparable<Object> comparableProperty;
                    if (property instanceof Comparable) {
                        comparableProperty = (Comparable<Object>) property;
                    } else {
                        throw new UnsupportedOperationException(String.format("Property '%s' is not comparable", propName));
                    }

                    int comparisonValue;
                    try {
                        comparisonValue = comparableProperty.compareTo(test);
                    } catch (ClassCastException e) {
                        throw new UnsupportedOperationException(String.format("Couldn't compare '%s' to '%s'",
                                property, test));
                    }

                    if (checkComparisonValue(node.getName(), comparisonValue)) {
                        result.add(item);
                    }
                }
                return result;
            case "like":
            case "match":
                propName = (String) node.getArgument(0);
                String matchString = (String) node.getArgument(1);
                Pattern matchPattern = Pattern.compile(matchString.replace("*", ".*"),
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

                result = new ArrayList<>();

                for (T item : list) {
                    Object property = getProperty(item, propName);

                    String stringProperty;
                    if (property instanceof String) {
                        stringProperty = (String) property;
                    } else {
                        throw new UnsupportedOperationException(String.format("Property '%s' is not a string", propName));
                    }

                    if (matchPattern.matcher(stringProperty).matches()) {
                        result.add(item);
                    }
                }
                return result;
            case "limit":
                int limit = (int) node.getArgument(0);
                int offset = node.getArgumentsSize() > 1 ? (int) node.getArgument(1) : 0;

                if (offset > list.size() - 1) {
                    return Collections.emptyList();
                }

                int toIndex = offset + limit;
                if (toIndex > list.size()) {
                    toIndex = list.size();
                }

                return list.subList(offset, toIndex);
            case "sort":
                Comparator<T> comparator = null;
                for (Object obj : node) {
                    String sortOption = (String) obj;
                    boolean desc = sortOption.startsWith("-");

                    Comparator<T> beanComparator = new BeanComparator<T>(sortOption.substring(1));
                    if (desc) {
                        beanComparator = beanComparator.reversed();
                    }

                    comparator = comparator == null ? beanComparator : comparator.thenComparing(beanComparator);
                }
                if (comparator != null) {
                    // copy the list as we are modifying it
                    list = new ArrayList<>(list);
                    list.sort(comparator);
                }
                return list;
            default:
                throw new UnsupportedOperationException(String.format("Encountered unknown operator '%s'", node.getName()));
        }
    }

    private boolean checkComparisonValue(String name, int value) {
        switch (name) {
            case "eq":
                return value == 0;
            case "gt":
                return value > 0;
            case "ge":
                return value >= 0;
            case "lt":
                return value < 0;
            case "le":
                return value <= 0;
            case "ne":
                return value != 0;
        }
        return false;
    }

    private Object getProperty(Object item, String propName) {
        Object property;
        try {
            property = PropertyUtils.getProperty(item, propName);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new UnsupportedOperationException(String.format("Could not retrieve property '%s' from list object", propName));
        }
        return property;
    }
}
