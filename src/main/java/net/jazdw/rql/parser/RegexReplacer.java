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
        while (matcher.find()) {
            matcher.appendReplacement(sb, replaceWith());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public abstract String replaceWith();
}