package net.jazdw.rql.util;

import java.util.Comparator;

public interface PropertyAccessor<T, R> {
    R getProperty(T item, String propertyName);

    default Comparator<Object> getComparator(String property) {
        return DefaultComparator.INSTANCE;
    }

    default Comparator<T> getSortComparator(String property) {
        Comparator<Object> propertyComparator = getComparator(property);
        return (a, b) -> {
            Object valueA = getProperty(a, property);
            Object valueB = getProperty(b, property);
            return propertyComparator.compare(valueA, valueB);
        };
    }
}
