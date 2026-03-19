package com.shroototem.pipez.corelib;

public record Pair<A, B>(A key, B value) {
    public A getKey() {
        return key;
    }

    public B getValue() {
        return value;
    }
}
