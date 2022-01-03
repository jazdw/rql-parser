/*
 * Copyright (C) 2022 Jared Wiltshire (https://jazdw.net).
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
