package com.brauer.android.yofyz.service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Status {
    RUNNING(true),
    STOPPED(false);


    private static Map<Boolean, Status> REVERSE_LOOKUP_MAP = Stream.of(Status.values())
            .collect(Collectors.toMap(
                    opt -> opt.value,
                    opt -> opt
            ));

    private boolean value;

    Status(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    public String getValueAsString() {
        if (value) {
            return "RUNNING";
        }
        return "STOPPED";
    }

    public static Status fromString(String str) {
        return Optional.ofNullable(REVERSE_LOOKUP_MAP.get(Boolean.parseBoolean(str)))
                .orElse(STOPPED);
    }
}
