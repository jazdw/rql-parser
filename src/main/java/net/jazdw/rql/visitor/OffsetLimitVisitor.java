package net.jazdw.rql.visitor;

import java.util.List;

import net.jazdw.rql.util.OffsetLimit;
import net.jazdw.rql.util.TextDecoder;

public class OffsetLimitVisitor extends FunctionVisitor<OffsetLimit> {

    public OffsetLimitVisitor(TextDecoder decoder, ValueVisitor valueVisitor) {
        super(decoder, valueVisitor);
    }

    @Override
    public OffsetLimit applyFunction(String functionName, List<Object> arguments) {
        if ("limit".equals(functionName)) {
            Long limit = null;
            Long offset = ((Number) arguments.get(0)).longValue();
            if (arguments.size() > 1) {
                limit = ((Number) arguments.get(1)).longValue();
            }
            return new OffsetLimit(offset, limit);
        }
        return null;
    }
}
