/*
 * Copyright (C) 2015 Jared Wiltshire (https://jazdw.net).
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jazdw.rql.converter.Converter;
import net.jazdw.rql.converter.ConverterException;

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
 */
public class RQLParser {
    // these patterns are straight from the Persevere Javascript parser
    // with the backslashes escaped and broken down into parts and the
    // +*$-:\w%._ character set replaced with an inverted reserved set
    
    // reserved characters that we parse against, these should be percent escaped
    // note that comma is also reserved except inside brackets
    private static final String RESERVED = "&|()=<>";
    
    private static final String PROPERTY_OR_VALUE =
            String.format("\\([^%s]+\\)|[^%1$s,]*|", RESERVED);
    private static final String COMPARISON_OPERATOR = "[<>!]?=(?:[\\w]*=)?|>|<";
    
    private static final String CLOSE_BRACKET = "\\)";
    private static final String DELIMITERS = "[&\\|,]";
    private static final String OPEN_BRACKET = "\\(";
    
    private static final Pattern SLASHED_PATTERN = Pattern.compile(
            "[\\+\\*\\$\\-:\\w%\\._]*\\/[\\+\\*\\$\\-:\\w%\\._\\/]*");
    
    private static final Pattern NORMALIZE_PATTERN = Pattern.compile(
            String.format("(%s)(%s)(%1$s)",
                    PROPERTY_OR_VALUE, COMPARISON_OPERATOR));
    
    private static final Pattern NODE_CREATE_PATTERN = Pattern.compile(
            String.format("(%s)|(%s)?([^%s,]*)(%s?)",
                    CLOSE_BRACKET, DELIMITERS, RESERVED, OPEN_BRACKET));
    
    private static final Map<String, String> operatorMap = new HashMap<String, String>();
    static {
        operatorMap.put("=", "eq");
        operatorMap.put("==", "eq");
        operatorMap.put(">", "gt");
        operatorMap.put(">=", "ge");
        operatorMap.put("<", "lt");
        operatorMap.put("<=", "le");
        operatorMap.put("!=", "ne");
    }

    private Converter converter;
    
    public RQLParser() {
        this(new Converter());
    }
    
    public RQLParser(Converter converter) {
        this.converter = converter;
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
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
        if (query.startsWith("?")) {
            throw new IllegalArgumentException("query must not start with ?");
        }

        Matcher matcher;
        
        // find slash delimited array patterns and convert to comma delimited with brackets
        // e.g. quick/brown/fox  ==>  (quick,brown,fox)
        if (query.contains("/")) {
            matcher = SLASHED_PATTERN.matcher(query);
            query = new RegexReplacer(matcher) {
                public String replaceWith() {
                    return "(" + matcher.group().replace("/", ",") + ")";
                }
            }.replace();
        }
        
        // convert simplified, "syntaxic sugar" comparison operators to normalized call syntax
        // e.g. name=john  ==>  eq(name,john)
        matcher = NORMALIZE_PATTERN.matcher(query);
        query = new RegexReplacer(matcher) {
            public String replaceWith() {
                String property = matcher.group(1);
                String operator = matcher.group(2);
                String value = matcher.group(3);
                
                if (property.isEmpty()) {
                    throw new RQLParserException("No property specified for operator '" + operator + "' at position " + matcher.start());
                }
                
                if (operator.length() < 3) {
                    String mapped = operatorMap.get(operator);
                    if (mapped == null) {
                        throw new RQLParserException("Illegal operator " + operator);
                    }
                    operator = mapped;
                }
                else {
                    operator = operator.substring(1, operator.length() - 1);
                }
                return operator + '(' + property + "," + value + ")";
            }
        }.replace();

        // create a root node with an empty name
        final ASTNode rootNode = new ASTNode("");
        
        // this replacer basically iteratively matches brackets and operators and replaces them
        // with an empty string, the result is then checked later for left over characters which
        // indicate a parsing error
        matcher = NODE_CREATE_PATTERN.matcher(query);
        String leftoverCharacters = new RegexReplacer(matcher) {
            ASTNode node = rootNode;
            
            public String replaceWith() {
                String closeBracket = matcher.group(1);
                String delimiter = matcher.group(2);
                String propertyOrValue = matcher.group(3);
                String openBracket = matcher.group(4);
                
                if (delimiter != null) {
                    if (delimiter.equals("&")) {
                        setConjunction(node, "and");
                    } else if (delimiter.equals("|")) {
                        setConjunction(node, "or");
                    }
                }
                if (openBracket != null && !openBracket.isEmpty()) {
                    node = node.createChildNode(propertyOrValue);
                    
                    /*
                    // cache the last seen sort(), select(), values() and limit()
                    if (contains(exports.lastSeen, term.name)) {
                        topTerm.cache[term.name] = term.args;
                    }
                    */
                }
                else if (closeBracket != null && !closeBracket.isEmpty()) {
                    if (node == null) {
                        throw new RQLParserException("Closing paranthesis without an opening paranthesis");
                    }

                    boolean isArray = !node.isNameValid();
                    node = node.getParent();

                    if (isArray) {
                        Object last = node.removeLastArgument();
                        if (last instanceof ASTNode) {
                            node.addArgument(((ASTNode) last).getArguments());
                        }
                        else {
                            throw new RQLParserException("Argument not ASTNode");
                        }
                    }
                }
                else if (propertyOrValue != null && !propertyOrValue.isEmpty() || ",".equals(delimiter)) {
                    try {
                        node.addArgument(converter.convert(propertyOrValue));
                    } catch (ConverterException e) {
                        throw new RQLParserException(e);
                    }

                    /*
                    // cache the last seen sort(), select(), values() and limit()
                    if (contains(exports.lastSeen, term.name)) {
                        topTerm.cache[term.name] = term.args;
                    }
                    // cache the last seen id equality
                    if (term.name.equals("eq") && term.args[0].equals(exports.primaryKeyName)) {
                        var id = term.args[1];
                        if (id && !(id instanceof RegExp)) id = id.toString();
                        topTerm.cache[exports.primaryKeyName] = id;
                    }
                     */
                }
                
                return "";
            }
        }.replace();
        
        if (!rootNode.isRootNode()) {
            throw new RQLParserException("Opening paranthesis without a closing paranthesis");
        }
        
        if (leftoverCharacters.length() > 0) {
            // any extra characters left over from the replace indicates invalid syntax
            throw new RQLParserException("Illegal character in query string encountered " + leftoverCharacters);
        }
        
        // remove parents from each node in the tree
        rootNode.removeParents();
        
        // no root level conjunction i.e. just a single conditional statement or function
        if (!rootNode.isNameValid() && rootNode.getArgumentsSize() == 1) {
            Object arg = rootNode.getArgument(0);
            if (arg instanceof ASTNode) {
                return (ASTNode) arg;
            }
        }
        
        return rootNode;
    }
    
    private void setConjunction(ASTNode node, String operator) {
        if (!node.isNameValid()) {
            node.setName(operator);
        }
        else if (!node.getName().equals(operator)) {
            throw new RQLParserException("Can not mix conjunctions within a group, use paranthesis around each set of same conjuctions (& and |)");
        }
    }
}
