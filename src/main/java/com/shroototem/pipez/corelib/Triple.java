package com.shroototem.pipez.corelib;

public record Triple<A, B, C>(A value1, B value2, C value3) {
    public A getValue1() {
        return value1;
    }

    public B getValue2() {
        return value2;
    }

    public C getValue3() {
        return value3;
    }
}
