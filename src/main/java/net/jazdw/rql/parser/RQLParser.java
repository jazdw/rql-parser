/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */

package net.jazdw.rql.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jazdw.rql.converter.Converter;
import net.jazdw.rql.converter.ConverterException;

/**
 * @author Jared Wiltshire
 */
public final class RQLParser {
    private static final Pattern SLASHED_PATTERN = Pattern.compile("[\\+\\*\\$\\-:\\w%\\._]*\\/[\\+\\*\\$\\-:\\w%\\._\\/]*");
    private static final Pattern NORMALIZE_PATTERN = Pattern.compile("(\\([\\+\\*\\$\\-:\\w%\\._,]+\\)|[\\+\\*\\$\\-:\\w%\\._]*|)([<>!]?=(?:[\\w]*=)?|>|<)(\\([\\+\\*\\$\\-:\\w%\\._,]+\\)|[\\+\\*\\$\\-:\\w%\\._]*|)");
    private static final Pattern NODE_CREATE_PATTERN = Pattern.compile("(\\))|([&\\|,])?([\\+\\*\\$\\-:\\w%\\._]*)(\\(?)");
    
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
        
        if (query.contains("/")) {
            matcher = SLASHED_PATTERN.matcher(query);
            query = new RegexReplacer(matcher) {
                public String replaceWith() {
                    return "(" + matcher.group().replace("/", ",") + ")";
                }
            }.replace();
        }
        
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

        final ASTNode rootNode = new ASTNode("");
        
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
