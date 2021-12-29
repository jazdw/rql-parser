package net.jazdw.rql.parser.listfilter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AntlrResult<T> implements UnaryOperator<Stream<T>> {

    public AntlrResult() {
    }

    public AntlrResult(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    Predicate<T> predicate;
    Long limit;
    Long offset;
    Comparator<T> sort;

    @Override
    public Stream<T> apply(Stream<T> stream) {
        if (this.predicate != null) {
            stream = stream.filter(predicate);
        }
        if (this.sort != null) {
            stream = stream.sorted(this.sort);
        }
        if (this.offset != null) {
            stream = stream.skip(this.offset);
        }
        if (this.limit != null) {
            stream = stream.limit(this.limit);
        }
        return stream;
    }

    public List<T> applyList(List<T> list) {
        return apply(list.stream()).collect(Collectors.toList());
    }
}
