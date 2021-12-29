package net.jazdw.rql.util;

public interface PropertyAccessor<T, R> {
    R getProperty(T item, String propertyName);
}
