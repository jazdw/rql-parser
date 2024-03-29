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

package net.jazdw.rql.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Jared Wiltshire
 */
public class ASTNode implements Iterable<Object> {
    private final List<Object> arguments;
    private ASTNode parent;
    private String name;

    public ASTNode(String name, Object... arguments) {
        this(null, name, Arrays.asList(arguments));
    }

    public ASTNode(String name, List<Object> arguments) {
        this(null, name, arguments);
    }

    public ASTNode(ASTNode parent, String name, Object... arguments) {
        this(parent, name, Arrays.asList(arguments));
    }

    public ASTNode(ASTNode parent, String name, List<Object> arguments) {
        this.parent = parent;
        this.name = name;
        this.arguments = new ArrayList<>(arguments);
    }

    public <R, A> R accept(SimpleASTVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public <R, A> R accept(ASTVisitor<R, A> visitor, A param) {
        return visitor.visit(this, param);
    }

    public ASTNode removeParents() {
        parent = null;
        for (Object arg : arguments) {
            if (arg instanceof ASTNode) {
                ((ASTNode) arg).removeParents();
            }
        }
        return this;
    }

    public boolean isRootNode() {
        return parent == null;
    }

    public ASTNode createChildNode(String name, Object... arguments) {
        return createChildNode(name, Arrays.asList(arguments));
    }

    public ASTNode createChildNode(String name, List<Object> arguments) {
        ASTNode child = new ASTNode(this, name, arguments);
        child.parent = this;
        this.arguments.add(child);
        return child;
    }

    public ASTNode addArgument(Object argument) {
        if (argument instanceof ASTNode) {
            ((ASTNode) argument).parent = this;
        }
        arguments.add(argument);
        return this;
    }

    public Object removeLastArgument() {
        return arguments.remove(arguments.size() - 1);
    }

    public ASTNode getParent() {
        return parent;
    }

    public boolean isNameValid() {
        return name != null && !name.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getArgument(int i) {
        return arguments.get(i);
    }

    public List<Object> getArguments() {
        return arguments;
    }

    public int getArgumentsSize() {
        return arguments.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ASTNode objects = (ASTNode) o;
        return Objects.equals(arguments, objects.arguments) && Objects.equals(name, objects.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arguments, name);
    }

    @Override
    public String toString() {
        return name + "(" + StringUtils.join(arguments, ",") + ")";
    }

    @Override
    public Iterator<Object> iterator() {
        return arguments.iterator();
    }

    public String format(String format) {
        return String.format(format, arguments.toArray());
    }
}
