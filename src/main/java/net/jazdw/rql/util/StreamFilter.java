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
    private final Long offset;
    private final Long limit;

    public StreamFilter(Predicate<T> predicate, Comparator<T> sort, Long offset, Long limit) {
        this.predicate = predicate;
        this.sort = sort;
        this.offset = offset;
        this.limit = limit;
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
