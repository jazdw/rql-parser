/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */
package net.jazdw.rql.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jared Wiltshire
 */
public class ASTNode implements Iterable<Object> {
    private ASTNode parent;
    private String name;
    private final List<Object> arguments;
    
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
    
    public void removeParent() {
        parent = null;
        for (Object arg : arguments) {
            if (arg instanceof ASTNode) {
                ((ASTNode) arg).removeParent();
            }
        }
    }
    
    public boolean isRootNode() {
        return parent == null;
    }
    
    public void addArgument(Object argument) {
        arguments.add(argument);
    }
    
    public Object removeLastArgument() {
        return arguments.remove(arguments.size() - 1);
    }

    public ASTNode getParent() {
        return parent;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
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

    public List<Object> getArguments() {
        return arguments;
    }
    
    public int getArgumentsSize() {
        return arguments.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ASTNode other = (ASTNode) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parent == null) {
            if (other.parent != null)
                return false;
        } else if (!parent.equals(other.parent))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ASTNode [parent=" + parent + ", name=" + name + ", arguments="
                + arguments + "]";
    }

    @Override
    public Iterator<Object> iterator() {
        return arguments.iterator();
    }
    
    public String format(String format) {
        return String.format(format, arguments.toArray());
    }
}
