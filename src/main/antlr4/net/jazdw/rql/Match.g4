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

lexer grammar Match;

MATCH_SINGLE        : '?' ;
MATCH_MANY          : '*' ;
QUESTION_MARK       : '\\?' ;
ASTERISK            : '\\*' ;
SLASH               : '\\\\' ;

OTHER               : ~[?*\\]+ ;
