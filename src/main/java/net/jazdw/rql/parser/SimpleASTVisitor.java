/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */
package net.jazdw.rql.parser;

/**
 * @author Jared Wiltshire
 */
public interface SimpleASTVisitor<R> {
    public R visit(ASTNode node);
}
