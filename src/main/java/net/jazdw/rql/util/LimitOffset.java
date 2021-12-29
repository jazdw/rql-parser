package net.jazdw.rql.util;

public class LimitOffset {
    private final Long limit;
    private final Long offset;

    public LimitOffset(Long limit, Long offset) {
        this.offset = offset;
        this.limit = limit;
    }

    public Long getLimit() {
        return limit;
    }

    public Long getOffset() {
        return offset;
    }
}
