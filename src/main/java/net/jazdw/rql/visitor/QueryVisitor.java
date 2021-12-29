package net.jazdw.rql.visitor;

import java.util.Comparator;
import java.util.function.Predicate;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.QueryContext;
import net.jazdw.rql.converter.DefaultValueConverter;
import net.jazdw.rql.converter.ValueConverter;
import net.jazdw.rql.util.DefaultTextDecoder;
import net.jazdw.rql.util.OffsetLimit;
import net.jazdw.rql.util.PropertyAccessor;
import net.jazdw.rql.util.StreamFilter;
import net.jazdw.rql.util.TextDecoder;

public class QueryVisitor<T> extends RqlBaseVisitor<StreamFilter<T>> {

    private final PredicateVisitor<T> predicateVisitor;
    private final OffsetLimitVisitor offsetLimitVisitor;

    public QueryVisitor(PropertyAccessor<T, Object> accessor) {
        ValueConverter<Object> converter = new DefaultValueConverter();
        TextDecoder decoder = new DefaultTextDecoder();
        ValueVisitor valueVisitor = new ValueVisitor(decoder, converter);

        this.predicateVisitor = new PredicateVisitor<>(decoder, valueVisitor, accessor);
        this.offsetLimitVisitor = new OffsetLimitVisitor(decoder, valueVisitor);
    }

    @Override
    public StreamFilter<T> visitQuery(QueryContext ctx) {
        Predicate<T> predicate = null;
        Comparator<T> sort = null;
        Long offset = null;
        Long limit = null;

        ExpressionContext expression = ctx.expression();
        if (expression != null) {
            predicate = expression.accept(predicateVisitor);
            OffsetLimit offsetLimit = expression.accept(offsetLimitVisitor);
            if (offsetLimit != null) {
                offset = offsetLimit.getOffset();
                limit = offsetLimit.getLimit();
            }
        }

        return new StreamFilter<>(predicate, sort, offset, limit);
    }

}
