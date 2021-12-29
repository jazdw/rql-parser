package net.jazdw.rql.visitor;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser;
import net.jazdw.rql.RqlParser.AndContext;
import net.jazdw.rql.RqlParser.EqualsContext;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.GroupContext;
import net.jazdw.rql.RqlParser.LogicalContext;
import net.jazdw.rql.RqlParser.OrContext;
import net.jazdw.rql.RqlParser.PredicateContext;
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
    public Predicate<T> visitGroup(GroupContext ctx) {
        return ctx.expression().accept(this);
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
        Token operator = ctx.logicalOperator().op;
        switch (operator.getType()) {
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
                throw new UnsupportedOperationException("Unsupported logical operation type: " + operator.getText());
        }
    }

    @Override
    public Predicate<T> visitEquals(EqualsContext ctx) {
        String propertyName = decoder.apply(ctx.identifier().id.getText());
        Object value = valueVisitor.visitValue(ctx.value());
        return (item) -> getComparator(propertyName).compare(accessor.getProperty(item, propertyName), value) == 0;
    }

    @Override
    public Predicate<T> visitPredicate(PredicateContext ctx) {
        String propertyName = decoder.apply(ctx.identifier().id.getText());
        Object firstArg = valueVisitor.visitValue(ctx.value(0));

        Token operator = ctx.predicateOperator().op;
        switch (operator.getType()) {
            case RqlParser.EQUALS:
            case RqlParser.NOT_EQUALS:
            case RqlParser.LESS_THAN:
            case RqlParser.LESS_THAN_OR_EQUAL:
            case RqlParser.GREATER_THAN:
            case RqlParser.GREATER_THAN_OR_EQUAL:
                return (item) -> {
                    Object propertyValue = accessor.getProperty(item, propertyName);
                    int result = getComparator(propertyName).compare(propertyValue, firstArg);
                    return checkComparatorResult(operator, result);
                };
            case RqlParser.CONTAINS:
                return (item) -> {
                    Object propertyValue = accessor.getProperty(item, propertyName);
                    if (propertyValue instanceof Collection) {
                        Collection<?> collectionValue = (Collection<?>) propertyValue;
                        return collectionValue.contains(firstArg);
                    }
                    return false;
                };
            case RqlParser.IN: {
                List<?> values;
                if (firstArg instanceof List) {
                    values = (List<?>) firstArg;
                } else {
                    values = ctx.value().stream().map(valueVisitor::visitValue).collect(Collectors.toList());
                }
                return (item) -> {
                    Object propertyValue = accessor.getProperty(item, propertyName);
                    return values.contains(propertyValue);
                };
            }
            default:
                throw new UnsupportedOperationException("Unsupported predicate type: " + operator.getText());
        }
    }

    protected Comparator<Object> getComparator(String propertyName) {
        return DefaultComparator.INSTANCE;
    }

    protected Comparator<T> getSortComparator(String property) {
        Comparator<Object> propertyComparator = getComparator(property);
        return (a, b) -> {
            Object valueA = accessor.getProperty(a, property);
            Object valueB = accessor.getProperty(b, property);
            return propertyComparator.compare(valueA, valueB);
        };
    }

    private static class DefaultComparator implements Comparator<Object> {
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

    private List<Predicate<T>> childPredicates(List<ExpressionContext> arguments) {
        return arguments.stream()
                .map(ctx -> ctx.accept(this))
                .filter(Objects::nonNull) // filter out any non-predicate expressions / tokens
                .collect(Collectors.toList());
    }

    private boolean checkComparatorResult(Token operator, int value) {
        switch (operator.getType()) {
            case RqlParser.EQUALS:
                return value == 0;
            case RqlParser.NOT_EQUALS:
                return value != 0;
            case RqlParser.LESS_THAN:
                return value < 0;
            case RqlParser.LESS_THAN_OR_EQUAL:
                return value <= 0;
            case RqlParser.GREATER_THAN:
                return value > 0;
            case RqlParser.GREATER_THAN_OR_EQUAL:
                return value >= 0;
            default:
                throw new UnsupportedOperationException("Unsupported predicate type: " + operator.getText());
        }
    }

}
