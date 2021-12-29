package net.jazdw.rql.util;

import java.util.Comparator;

public class DefaultComparator implements Comparator<Object> {
    public static final DefaultComparator INSTANCE = new DefaultComparator();

    @Override
    public int compare(Object a, Object b) {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else if (a instanceof Comparable) {
            try {
                //noinspection unchecked,rawtypes
                return ((Comparable) a).compareTo(b);
            } catch (ClassCastException e) {
                // ignore
            }
        }
        return String.valueOf(a).compareTo(String.valueOf(b));
    }
}
