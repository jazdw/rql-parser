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

grammar Rql;

/* Parser rules */

/*
 * TODO Arbitrary predicates / functions
 * TODO Sort with direction
 * TODO Limit
 * TODO more predicates
 * TODO short form predicates?
 * TODO unreserved chars * and + should be encoded in query parts..?
 * TODO + should be decoded as a space
 * TODO define wildcard character? do we encode star symbol?
 * TODO define list of characters that should be encoded and where
 * TODO define list of characters that should be decoded and where
 * TODO null values? true/false/null/number/string tokens?
*/

query
    : expression? EOF
    ;

expression
    : OPEN_PARENTHESIS expression? CLOSE_PARENTHESIS #group
    | LOGICAL OPEN_PARENTHESIS expression? (COMMA expression)* CLOSE_PARENTHESIS #logical
    | expression AMPERSAND expression #and
    | expression VERTICAL_BAR expression #or
    | PREDICATE OPEN_PARENTHESIS identifier COMMA value (COMMA value)* CLOSE_PARENTHESIS #predicate
    | function_name OPEN_PARENTHESIS value? (COMMA value)* CLOSE_PARENTHESIS #function
    | identifier EQUALS_SIGN PREDICATE EQUALS_SIGN value #predicate
    | identifier EQUALS_SIGN value #equals
    ;

function_name
    : TEXT
    ;

identifier
    : TEXT
    ;

value
    : TEXT
    | typed_value
    | array_value
    ;

typed_value
    : TEXT COLON TEXT
    ;

array_value
    : OPEN_PARENTHESIS value? (COMMA value)* CLOSE_PARENTHESIS
    ;

/* Lexer rules */

fragment ALPHA      : [a-zA-Z] ;
fragment DIGIT      : [0-9] ;
fragment HEXDIG     : [a-fA-F0-9] ;

AMPERSAND           : '&' ;
VERTICAL_BAR        : '|' ;
EQUALS_SIGN         : '=' ;
OPEN_PARENTHESIS    : '(' ;
CLOSE_PARENTHESIS   : ')' ;
COMMA               : ',' ;
COLON               : ':' ;

LOGICAL
    : AND
    | OR
    | NOT
    ;

AND                 : 'and' ;
OR                  : 'or' ;
NOT                 : 'not' ;

PREDICATE
    : EQUALS
    | NOT_EQUALS
    | LESS_THAN
    | LESS_THAN_OR_EQUAL
    | GREATER_THAN
    | GREATER_THAN_OR_EQUAL
    | IN
    | CONTAINS
    ;

EQUALS                  : 'eq' ;
NOT_EQUALS              : 'ne' ;
LESS_THAN               : 'lt' ;
LESS_THAN_OR_EQUAL      : 'le' ;
GREATER_THAN            : 'gt' ;
GREATER_THAN_OR_EQUAL   : 'ge' ;
IN                      : 'in' ;
CONTAINS                : 'contains' ;

TEXT
    : (UNRESERVED | PERCENT_ENCODED | '*' | '+')+
    ;

PERCENT_ENCODED
    : '%' HEXDIG HEXDIG
    ;

// RFC3986 unreserved characters
// https://datatracker.ietf.org/doc/html/rfc3986#section-2.3
UNRESERVED
    : ALPHA
    | DIGIT
    | '-'
    | '.'
    | '_'
    | '~'
    ;