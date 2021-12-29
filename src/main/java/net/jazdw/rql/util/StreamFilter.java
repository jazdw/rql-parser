package net.jazdw.rql.util;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamFilter<T> implements UnaryOperator<Stream<T>> {

    private final Predicate<T> predicate;

    public StreamFilter(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Stream<T> apply(Stream<T> stream) {
        if (predicate != null) {
            stream = stream.filter(predicate);
        }
        return stream;
    }

    public List<T> applyList(List<T> list) {
        return apply(list.stream()).collect(Collectors.toList());
    }
}
