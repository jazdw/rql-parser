/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */
package net.jazdw.rql.parser;

/**
 * @author Jared Wiltshire
 */
public interface ASTVisitor<R, A> {
    R visit(ASTNode node, A param);
}
