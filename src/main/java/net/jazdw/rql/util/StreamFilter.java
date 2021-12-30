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

package net.jazdw.rql.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamFilter<T> implements UnaryOperator<Stream<T>> {

    private final Predicate<T> predicate;
    private final Comparator<T> sort;
    private final Long limit;
    private final Long offset;

    public StreamFilter(Predicate<T> predicate, Comparator<T> sort, Long limit, Long offset) {
        this.predicate = predicate;
        this.sort = sort;
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public Stream<T> apply(Stream<T> stream) {
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        if (sort != null) {
            stream = stream.sorted(sort);
        }
        if (offset != null) {
            stream = stream.skip(offset);
        }
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return stream;
    }

    public List<T> applyList(List<T> list) {
        return apply(list.stream()).collect(Collectors.toList());
    }
}
