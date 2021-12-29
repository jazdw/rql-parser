package net.jazdw.rql.visitor;

import java.util.Comparator;
import java.util.function.Predicate;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.QueryContext;
import net.jazdw.rql.converter.DefaultValueConverter;
import net.jazdw.rql.converter.ValueConverter;
import net.jazdw.rql.util.DefaultTextDecoder;
import net.jazdw.rql.util.LimitOffset;
import net.jazdw.rql.util.PropertyAccessor;
import net.jazdw.rql.util.StreamFilter;
import net.jazdw.rql.util.TextDecoder;

public class QueryVisitor<T> extends RqlBaseVisitor<StreamFilter<T>> {

    private final PredicateVisitor<T> predicateVisitor;
    private final LimitOffsetVisitor limitOffsetVisitor;

    public QueryVisitor(PropertyAccessor<T, Object> accessor) {
        ValueConverter<Object> converter = new DefaultValueConverter();
        TextDecoder decoder = new DefaultTextDecoder();
        ValueVisitor valueVisitor = new ValueVisitor(decoder, converter);

        this.predicateVisitor = new PredicateVisitor<>(decoder, valueVisitor, accessor);
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
            LimitOffset limitOffset = expression.accept(limitOffsetVisitor);
            if (limitOffset != null) {
                limit = limitOffset.getLimit();
                offset = limitOffset.getOffset();
            }
        }

        return new StreamFilter<>(predicate, sort, limit, offset);
    }

}
