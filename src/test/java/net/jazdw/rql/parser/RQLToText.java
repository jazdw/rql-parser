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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Jared Wiltshire
 */
public class RQLToText implements SimpleASTVisitor<String> {

    @Override
    public String visit(ASTNode node) {
        switch (node.getName()) {
            case "and":
            case "or":
                return visitAndOr(node);
            case "eq":
                return node.format("%s=%s");
            case "gt":
                return node.format("%s>%s");
            case "ge":
                return node.format("%s>=%s");
            case "lt":
                return node.format("%s<%s");
            case "le":
                return node.format("%s<=%s");
            case "ne":
                return node.format("%s!=%s");
            case "match":
            case "like":
                return node.format("%s like (%s)");
            case "sort":
                return visitSort(node);
            case "limit":
                return visitLimit(node);
            case "in":
                return node.format("%s in (%s)");
            default:
                return node.toString();
        }
    }

    private String visitLimit(ASTNode node) {
        if (node.getArgumentsSize() == 2) {
            return node.format("limit %s offset %s");
        } else {
            return node.format("limit %s");
        }
    }

    public String visitSort(ASTNode node) {
        List<String> sortProps = new ArrayList<>();
        for (Object obj : node) {
            boolean descending = false;
            String prop = (String) obj;
            if (prop.startsWith("-")) {
                descending = true;
                prop = prop.substring(1);
            } else if (prop.startsWith("+")) {
                prop = prop.substring(1);
            }
            String ascDesc = descending ? " desc" : " asc";
            sortProps.add(prop + ascDesc);
        }
        return "sort by " + StringUtils.join(sortProps, ", ");
    }

    public String visitAndOr(ASTNode node) {
        List<String> components = new ArrayList<>();
        for (Object obj : node) {
            if (obj instanceof ASTNode) {
                components.add(((ASTNode) obj).accept(this));
            } else {
                throw new RuntimeException("AND/OR terms should only have ASTNode arguments");
            }
        }
        return "(" + StringUtils.join(components, ") " + node.getName().toUpperCase() + " (") + ")";
    }
}
