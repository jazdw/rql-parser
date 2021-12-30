/*
 * Copyright (C) 2021 Jared Wiltshire (https://jazdw.net).
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

import java.util.stream.Collectors;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.ArrayValueContext;
import net.jazdw.rql.RqlParser.TypedValueContext;
import net.jazdw.rql.RqlParser.ValueContext;
import net.jazdw.rql.converter.DefaultValueConverter;
import net.jazdw.rql.converter.ValueConverter;
import net.jazdw.rql.util.DefaultTextDecoder;
import net.jazdw.rql.util.TextDecoder;

public class ValueVisitor extends RqlBaseVisitor<Object> {

    private final TextDecoder decoder;
    private final ValueConverter<Object> converter;

    public ValueVisitor() {
        this(new DefaultTextDecoder(), new DefaultValueConverter());
    }

    public ValueVisitor(TextDecoder decoder, ValueConverter<Object> converter) {
        this.decoder = decoder;
        this.converter = converter;
    }

    @Override
    public Object visitValue(ValueContext ctx) {
        if (ctx.typedValue() != null) {
            return visitTypedValue(ctx.typedValue());
        } else if (ctx.arrayValue() != null) {
            return visitArrayValue(ctx.arrayValue());
        }
        String textValue = decoder.apply(ctx.textValue.getText());
        // unknown type, use automatic conversion
        return converter.convert(textValue);
    }

    @Override
    public Object visitTypedValue(TypedValueContext ctx) {
        String type = decoder.apply(ctx.type.getText());
        String value = decoder.apply(ctx.textValue.getText());
        return converter.convert(type, value);
    }

    @Override
    public Object visitArrayValue(ArrayValueContext ctx) {
        return ctx.value().stream().map(this::visitValue).collect(Collectors.toList());
    }

}
