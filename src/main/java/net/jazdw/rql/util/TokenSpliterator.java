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

package net.jazdw.rql.util;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;

/**
 * @author Jared Wiltshire
 */
public class TokenSpliterator implements Spliterator<Token> {

    private final TokenSource tokenSource;

    public TokenSpliterator(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Token> action) {
        Token next = tokenSource.nextToken();
        boolean hasToken = next.getType() != Token.EOF;
        if (hasToken) {
            action.accept(next);
        }
        return hasToken;
    }

    @Override
    public Spliterator<Token> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | IMMUTABLE;
    }

    public static Stream<Token> stream(TokenSource tokenSource) {
        return StreamSupport.stream(new TokenSpliterator(tokenSource), false);
    }

}
