package net.jazdw.rql.visitor;

import java.util.List;
import java.util.stream.Collectors;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.FunctionContext;
import net.jazdw.rql.util.TextDecoder;

public abstract class FunctionVisitor<T> extends RqlBaseVisitor<T> {

    private final ValueVisitor valueVisitor;
    private final TextDecoder decoder;

    public FunctionVisitor(TextDecoder decoder, ValueVisitor valueVisitor) {
        this.valueVisitor = valueVisitor;
        this.decoder = decoder;
    }

    @Override
    public T visitFunction(FunctionContext ctx) {
        String functionName = decoder.apply(ctx.functionName().name.getText());
        List<Object> arguments = ctx.value().stream()
                .map(valueVisitor::visitValue)
                .collect(Collectors.toList());

        return applyFunction(functionName, arguments);
    }

    public abstract T applyFunction(String functionName, List<Object> arguments);
}