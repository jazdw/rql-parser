package net.jazdw.rql.util;

public class OffsetLimit {
    private final Long offset;
    private final Long limit;

    public OffsetLimit(Long offset, Long limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public Long getOffset() {
        return offset;
    }

    public Long getLimit() {
        return limit;
    }
}
