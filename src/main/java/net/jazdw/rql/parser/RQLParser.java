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

package net.jazdw.rql.parser;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import net.jazdw.rql.RqlLexer;
import net.jazdw.rql.RqlParser;
import net.jazdw.rql.converter.DefaultValueConverter;
import net.jazdw.rql.converter.ValueConverter;
import net.jazdw.rql.visitor.ASTGenerator;

/**
 * Resource Query Language (RQL) Parser
 *
 * <p>Parses RQL encoded query strings and returns a tree of Abstract Syntax Tree
 * (AST) nodes. These nodes can then be visited using a visitor pattern to produce
 * a SQL query for example.</p>
 *
 * <p>
 * The RQL language is defined by Dojo Foundation's Persevere project -
 * <a href="https://github.com/persvr/rql">https://github.com/persvr/rql</a>
 * </p>
 *
 * @author Jared Wiltshire
 * @deprecated Use {@link RqlParser} directly instead
 */
@Deprecated
public class RQLParser {

    private final ASTGenerator astGenerator;

    public RQLParser() {
        this(new DefaultValueConverter());
    }

    public RQLParser(ValueConverter<Object> converter) {
        this.astGenerator = new ASTGenerator(converter);
    }

    public <R> R parse(String query, SimpleASTVisitor<R> visitor) throws RQLParserException {
        ASTNode node = parse(query);
        return node.accept(visitor);
    }

    public <R, A> R parse(String query, ASTVisitor<R, A> visitor, A param) throws RQLParserException {
        ASTNode node = parse(query);
        return node.accept(visitor, param);
    }

    public ASTNode parse(String query) throws RQLParserException {
        CharStream inputStream = CharStreams.fromString(query);
        RqlLexer lexer = new RqlLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        RqlParser parser = new RqlParser(tokenStream);
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            return parser.query().accept(astGenerator);
        } catch (ParseCancellationException e) {
            throw new RQLParserException(e);
        }
    }

}
