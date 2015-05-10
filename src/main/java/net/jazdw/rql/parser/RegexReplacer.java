/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */

package net.jazdw.rql.parser;

import java.util.regex.Matcher;

/**
 * @author Jared Wiltshire
 */
public abstract class RegexReplacer {
    protected final Matcher matcher;
    
    public RegexReplacer(Matcher matcher) {
        this.matcher = matcher;
    }
    
    public String replace() {
        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, replaceWith());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    public abstract String replaceWith();
}