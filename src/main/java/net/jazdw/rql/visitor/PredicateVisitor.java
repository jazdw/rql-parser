package net.jazdw.rql.visitor;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
        return (item) -> accessor.getComparator(propertyName).compare(accessor.getProperty(item, propertyName), value) == 0;
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
                    int result = accessor.getComparator(propertyName).compare(propertyValue, firstArg);
                    return checkComparatorResult(operator, result);
                };
            case RqlParser.MATCH: {
                Pattern pattern;
                if (firstArg instanceof Pattern) {
                    pattern = (Pattern) firstArg;
                } else if (firstArg instanceof String) {
                    String regex = (String) firstArg;
                    pattern = Pattern.compile(regex.replace("*", ".*"),
                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                } else {
                    throw new IllegalStateException("Must be pattern or regex");
                }

                return (item) -> {
                    Object propertyValue = accessor.getProperty(item, propertyName);
                    if (propertyValue instanceof String) {
                        String valueString = (String) propertyValue;
                        return pattern.matcher(valueString).matches();
                    }
                    return false;
                };
            }
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
