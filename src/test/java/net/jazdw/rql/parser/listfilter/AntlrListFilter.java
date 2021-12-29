/*
 * Copyright (C) 2015 Jared Wiltshire (https://jazdw.net).
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.beanutils.PropertyUtils;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.AndContext;
import net.jazdw.rql.RqlParser.ArrayValueContext;
import net.jazdw.rql.RqlParser.EqualsContext;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.FunctionContext;
import net.jazdw.rql.RqlParser.FunctionNameContext;
import net.jazdw.rql.RqlParser.GroupContext;
import net.jazdw.rql.RqlParser.IdentifierContext;
import net.jazdw.rql.RqlParser.LogicalContext;
import net.jazdw.rql.RqlParser.OrContext;
import net.jazdw.rql.RqlParser.PredicateContext;
import net.jazdw.rql.RqlParser.QueryContext;
import net.jazdw.rql.RqlParser.TypedValueContext;
import net.jazdw.rql.RqlParser.ValueContext;
import net.jazdw.rql.parser.ASTNode;

/**
 * @author Jared Wiltshire
 */
public class AntlrListFilter<T> extends RqlBaseVisitor<AntlrResult<T>> {

    @Override
    public AntlrResult<T> visitQuery(QueryContext ctx) {
        ExpressionContext expression = ctx.expression();
        if (expression == null) {
            return new AntlrResult<>();
        }
        return expression.accept(this);
    }

    @Override
    public AntlrResult<T> visitAnd(AndContext ctx) {
        List<Predicate<T>> childPredicates = childPredicates(ctx.children);
        Predicate<T> predicate = item -> childPredicates.stream().allMatch(p -> p.test(item));
        return new AntlrResult<>(predicate);
    }

    @Override
    public AntlrResult<T> visitEquals(EqualsContext ctx) {
        TerminalNode property = ctx.identifier().TEXT();
        TerminalNode value = ctx.value().TEXT();
        Comparator<String> comparator = Comparator.naturalOrder();
        return new AntlrResult<>((item) -> comparator.compare("" + getProperty(item, property.getText()), value.getText()) == 0);
    }

    private List<Predicate<T>> childPredicates(List<ParseTree> arguments) {
        return arguments.stream()
                .map(c -> c.accept(this))
                .filter(Objects::nonNull) // Terminal token & symbol
                .map(c -> c.predicate)
                .collect(Collectors.toList());
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
