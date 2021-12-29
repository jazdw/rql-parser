package net.jazdw.rql.visitor;

import java.util.function.Predicate;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.QueryContext;
import net.jazdw.rql.util.PropertyAccessor;
import net.jazdw.rql.util.StreamFilter;

public class QueryVisitor<T> extends RqlBaseVisitor<StreamFilter<T>> {

    private final PredicateVisitor<T> predicateVisitor;

    public QueryVisitor(PropertyAccessor<T, Object> accessor) {
        this.predicateVisitor = new PredicateVisitor<>(accessor);
    }

    @Override
    public StreamFilter<T> visitQuery(QueryContext ctx) {
        Predicate<T> predicate = null;

        ExpressionContext expression = ctx.expression();
        if (expression != null) {
            predicate = expression.accept(predicateVisitor);
        }

        return new StreamFilter<>(predicate);
    }

}
