package net.jazdw.rql.visitor;

import java.util.List;

import net.jazdw.rql.util.LimitOffset;
import net.jazdw.rql.util.TextDecoder;

public class LimitOffsetVisitor extends FunctionVisitor<LimitOffset> {

    public LimitOffsetVisitor(TextDecoder decoder, ValueVisitor valueVisitor) {
        super(decoder, valueVisitor);
    }

    @Override
    public LimitOffset applyFunction(String functionName, List<Object> arguments) {
        if ("limit".equals(functionName)) {
            Long offset = null;
            Long limit = ((Number) arguments.get(0)).longValue();
            if (arguments.size() > 1) {
                offset = ((Number) arguments.get(1)).longValue();
            }
            return new LimitOffset(limit, offset);
        }
        return null;
    }
}
