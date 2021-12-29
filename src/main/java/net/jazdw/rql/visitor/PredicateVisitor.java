package net.jazdw.rql.visitor;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser;
import net.jazdw.rql.RqlParser.AndContext;
import net.jazdw.rql.RqlParser.EqualsContext;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.LogicalContext;
import net.jazdw.rql.RqlParser.OrContext;
import net.jazdw.rql.util.PropertyAccessor;
import net.jazdw.rql.util.TextDecoder;

public class PredicateVisitor<T> extends RqlBaseVisitor<Predicate<T>> {

    private final ValueVisitor valueVisitor;
    private final PropertyAccessor<T, Object> accessor;
    private final TextDecoder decoder;

    public PredicateVisitor(TextDecoder decoder, ValueVisitor valueVisitor, PropertyAccessor<T, Object> accessor) {
        this.valueVisitor = valueVisitor;
        this.accessor = accessor;
        this.decoder = decoder;
    }

    @Override
    public Predicate<T> visitAnd(AndContext ctx) {
        List<Predicate<T>> childPredicates = childPredicates(ctx.expression());
        return item -> childPredicates.stream().allMatch(p -> p.test(item));
    }

    @Override
    public Predicate<T> visitOr(OrContext ctx) {
        List<Predicate<T>> childPredicates = childPredicates(ctx.expression());
        return item -> childPredicates.stream().anyMatch(p -> p.test(item));
    }

    @Override
    public Predicate<T> visitLogical(LogicalContext ctx) {
        List<Predicate<T>> childPredicates = childPredicates(ctx.expression());
        switch (ctx.op.getType()) {
            case RqlParser.AND:
                return item -> childPredicates.stream().allMatch(p -> p.test(item));
            case RqlParser.OR:
                return item -> childPredicates.stream().anyMatch(p -> p.test(item));
            case RqlParser.NOT:
                if (childPredicates.size() != 1) {
                    throw new IllegalStateException("NOT logical operator must have one child");
                }
                return childPredicates.get(0).negate();
            default:
                throw new IllegalStateException("Unknown logical operation type");
        }
    }

    @Override
    public Predicate<T> visitEquals(EqualsContext ctx) {
        String propertyName = decoder.apply(ctx.identifier().id.getText());
        Object value = valueVisitor.visitValue(ctx.value());

        Comparator<String> comparator = Comparator.naturalOrder();
        // TODO comparator
        return (item) -> comparator.compare("" + accessor.getProperty(item, propertyName), "" + value) == 0;
    }

    private List<Predicate<T>> childPredicates(List<ExpressionContext> arguments) {
        return arguments.stream()
                .map(ctx -> ctx.accept(this))
                .filter(Objects::nonNull) // filter out any non-predicate expressions / tokens
                .collect(Collectors.toList());
    }

}
