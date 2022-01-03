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

package net.jazdw.rql.visitor;

import java.util.Comparator;
import java.util.function.Predicate;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.QueryContext;
import net.jazdw.rql.converter.DefaultValueConverter;
import net.jazdw.rql.util.DefaultTextDecoder;
import net.jazdw.rql.util.LimitOffset;
import net.jazdw.rql.util.PropertyAccessor;
import net.jazdw.rql.util.StreamFilter;
import net.jazdw.rql.util.TextDecoder;

public class QueryVisitor<T> extends RqlBaseVisitor<StreamFilter<T>> {

    private final PredicateVisitor<T> predicateVisitor;
    private final SortVisitor<T> sortVisitor;
    private final LimitOffsetVisitor limitOffsetVisitor;


    public QueryVisitor(PropertyAccessor<T, Object> accessor) {
        this(accessor, new DefaultValueConverter());
    }

    public QueryVisitor(PropertyAccessor<T, Object> accessor, DefaultValueConverter converter) {
        TextDecoder decoder = new DefaultTextDecoder();
        ValueVisitor valueVisitor = new ValueVisitor(decoder, converter);

        this.predicateVisitor = new PredicateVisitor<>(decoder, valueVisitor, accessor);
        this.sortVisitor = new SortVisitor<>(decoder, valueVisitor, accessor);
        this.limitOffsetVisitor = new LimitOffsetVisitor(decoder, valueVisitor);
    }

    @Override
    public StreamFilter<T> visitQuery(QueryContext ctx) {
        Predicate<T> predicate = null;
        Comparator<T> sort = null;
        Long limit = null;
        Long offset = null;

        ExpressionContext expression = ctx.expression();
        if (expression != null) {
            predicate = expression.accept(predicateVisitor);
            sort = expression.accept(sortVisitor);
            LimitOffset limitOffset = expression.accept(limitOffsetVisitor);
            if (limitOffset != null) {
                limit = limitOffset.getLimit();
                offset = limitOffset.getOffset();
            }
        }

        return new StreamFilter<>(predicate, sort, limit, offset);
    }

}
