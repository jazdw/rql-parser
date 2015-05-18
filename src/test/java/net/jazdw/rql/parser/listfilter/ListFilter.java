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

package net.jazdw.rql.parser.listfilter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.comparators.ComparatorChain;

import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.parser.ASTVisitor;

/**
 * @author Jared Wiltshire
 */
public class ListFilter<T> implements ASTVisitor<List<T>, List<T>> {
    /* (non-Javadoc)
     * @see net.jazdw.rql.parser.ASTVisitor#visit(net.jazdw.rql.parser.ASTNode, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<T> visit(ASTNode node, List<T> input) {
        List<T> result = new ArrayList<>();
        switch (node.getName()) {
        case "and":
            result.addAll(input);
            for (Object obj : node) {
                result = ((ASTNode) obj).accept(this, result);
            }
            return result;
        case "or":
            for (Object obj : node) {
                result.addAll(((ASTNode) obj).accept(this, input));
            }
            return result;
        case "eq":
        case "gt":
        case "ge":
        case "lt":
        case "le":
        case "ne":
            String propName = (String) node.getArgument(0);
            Comparable<Object> test = (Comparable<Object>) node.getArgument(1);
            for (T item : input) {
                try {
                    Object property = PropertyUtils.getProperty(item, propName);
                    if (checkComparisonValue(node.getName(), test.compareTo(property))) {
                        result.add(item);
                    }
                } catch (IllegalAccessException | InvocationTargetException| NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            // another option?
            //BeanPropertyValueEqualsPredicate predicate =
            //        new BeanPropertyValueEqualsPredicate(propName, compareTo);
            //CollectionUtils.filter(input, predicate);
            return result;
        case "limit":
            result.addAll(input);
            int limit = (int) node.getArgument(0);
            int offset = node.getArgumentsSize() > 1 ? (int) node.getArgument(1) : 0;
            return result.subList(offset, offset + limit);
        case "sort":
            result.addAll(input);
            ComparatorChain cc = new ComparatorChain();
            for (Object obj : node) {
                String sortOption = (String) obj;
                boolean desc = sortOption.startsWith("-");
                cc.addComparator(new BeanComparator<T>(sortOption.substring(1)), desc);
            }
            Collections.sort(result, cc);
            return result;
        default:
        }
        return result;
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
}
