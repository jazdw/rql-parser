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

import java.util.Map;
import java.util.Objects;

import net.jazdw.rql.RqlBaseVisitor;
import net.jazdw.rql.RqlParser.AndContext;
import net.jazdw.rql.RqlParser.ExpressionContext;
import net.jazdw.rql.RqlParser.FunctionContext;
import net.jazdw.rql.RqlParser.GroupContext;
import net.jazdw.rql.RqlParser.IdentifierContext;
import net.jazdw.rql.RqlParser.LogicalContext;
import net.jazdw.rql.RqlParser.OrContext;
import net.jazdw.rql.RqlParser.PredicateContext;
import net.jazdw.rql.RqlParser.QueryContext;
import net.jazdw.rql.RqlParser.ShortPredicateContext;
import net.jazdw.rql.converter.DefaultValueConverter;
import net.jazdw.rql.converter.ValueConverter;
import net.jazdw.rql.parser.ASTNode;
import net.jazdw.rql.util.DefaultTextDecoder;
import net.jazdw.rql.util.TextDecoder;

/**
 * @author Jared Wiltshire
 */
public class ASTGenerator extends RqlBaseVisitor<ASTNode> {

    public static final Map<String, String> SHORT_OPERATOR_MAP = Map.of(
            "=", "eq",
            "==", "eq",
            ">", "gt",
            ">=", "ge",
            "<", "lt",
            "<=", "le",
            "!=", "ne"
    );

    private final TextDecoder decoder;
    private final ValueVisitor valueVisitor;

    public ASTGenerator() {
        this(new DefaultValueConverter());
    }

    public ASTGenerator(ValueConverter<Object> converter) {
        this(new DefaultTextDecoder(), converter);
    }

    public ASTGenerator(TextDecoder decoder, ValueConverter<Object> converter) {
        this.decoder = decoder;
        this.valueVisitor = new ValueVisitor(decoder, converter);
    }

    @Override
    public ASTNode visitQuery(QueryContext ctx) {
        ExpressionContext expression = ctx.expression();
        if (expression != null) {
            return expression.accept(this);
        }
        return new ASTNode("and");
    }

    @Override
    public ASTNode visitGroup(GroupContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public ASTNode visitLogical(LogicalContext ctx) {
        String operatorName = decoder.apply(ctx.logicalOperator().getText());
        ASTNode node = new ASTNode(operatorName);
        ctx.expression().stream()
                .map(this::visit)
                .filter(Objects::nonNull)
                .forEach(node::addArgument);
        return node;
    }

    @Override
    public ASTNode visitAnd(AndContext ctx) {
        ASTNode node = new ASTNode("and");
        ctx.expression().stream()
                .map(this::visit)
                .filter(Objects::nonNull)
                .forEach(node::addArgument);
        return node;
    }

    @Override
    public ASTNode visitOr(OrContext ctx) {
        ASTNode node = new ASTNode("or");
        ctx.expression().stream()
                .map(this::visit)
                .filter(Objects::nonNull)
                .forEach(node::addArgument);
        return node;
    }

    @Override
    public ASTNode visitPredicate(PredicateContext ctx) {
        String operatorName = decoder.apply(ctx.predicateOperator().getText());
        IdentifierContext id = ctx.identifier();
        String identifier = id == null ? null : decoder.apply(id.getText());
        ASTNode node = new ASTNode(operatorName);
        node.addArgument(identifier);
        ctx.value().stream()
                .map(valueVisitor::visitValue)
                .forEach(node::addArgument);
        return node;
    }

    @Override
    public ASTNode visitShortPredicate(ShortPredicateContext ctx) {
        String identifier = decoder.apply(ctx.identifier().getText());
        String shortOperator = ctx.shortPredicateOperator().getText();
        String operator = SHORT_OPERATOR_MAP.get(shortOperator);
        ASTNode node = new ASTNode(operator);
        node.addArgument(identifier);
        node.addArgument(valueVisitor.visitValue(ctx.value()));
        return node;
    }

    @Override
    public ASTNode visitFunction(FunctionContext ctx) {
        String functionName = decoder.apply(ctx.functionName().getText());
        ASTNode node = new ASTNode(functionName);
        ctx.value().stream()
                .map(valueVisitor::visitValue)
                .forEach(node::addArgument);
        return node;
    }

}
